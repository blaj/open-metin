package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.model.spawn.SpawnGroup;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupFileLoaderService {

  private static final Pattern GROUP_PATTERN = Pattern.compile("^Group\\s+(.+)$");
  private static final Pattern LEADER_PATTERN =
      Pattern.compile("^Leader\\s+(?:\"([^\"]+)\"|(\\S+))\\s+(\\d+)$");
  private static final Pattern VNUM_PATTERN = Pattern.compile("^Vnum\\s+(\\d+)$");
  private static final Pattern MEMBER_PATTERN =
      Pattern.compile("^(\\d+)\\s+(?:\"([^\"]+)\"|(\\S+))\\s+(\\d+)$");

  private final DataPathProperties dataPathProperties;

  public Map<Long, SpawnGroup> load() {
    if (!Files.exists(dataPathProperties.groupFile())) {
      log.warn("No group.txt file found at {}", dataPathProperties.groupFile());

      return new HashMap<>();
    }

    try (var lines = Files.lines(dataPathProperties.groupFile(), StandardCharsets.ISO_8859_1)) {
      var lineCounter = new AtomicInteger(0);

      var allLines =
          lines
              .map(String::trim)
              .map(line -> new LineWrapper(lineCounter.incrementAndGet(), line))
              .filter(lineWrapper -> !lineWrapper.line().isEmpty())
              .filter(
                  lineWrapper ->
                      !lineWrapper.line().startsWith("//") && !lineWrapper.line().startsWith("#"))
              .toList();

      log.debug(
          "Read {} valid lines from file (total {} lines)", allLines.size(), lineCounter.get());

      var spawnGroups =
          splitIntoGroupBlocks(allLines).stream()
              .map(this::parseGroupBlock)
              .flatMap(Optional::stream)
              .collect(
                  Collectors.toMap(
                      SpawnGroup::id,
                      spawnGroup -> spawnGroup,
                      (existingSpawnGroup, duplicateSpawnGroup) -> {
                        log.warn(
                            "Duplicate group vnum: {} (keeping first occurrence)",
                            existingSpawnGroup.id());

                        return existingSpawnGroup;
                      }));

      log.info("Successfully loaded {} groups from file", spawnGroups.size());

      return spawnGroups;
    } catch (IOException | UncheckedIOException e) {
      log.error("Failed to load group.txt file", e);
    }

    return new HashMap<>();
  }

  private List<GroupBlock> splitIntoGroupBlocks(List<LineWrapper> lineWrappers) {
    var groupBlocks = new ArrayList<GroupBlock>();
    var currentGroupBlock = new ArrayList<LineWrapper>();
    var blockStartLineNo = 0;

    for (var lineWrapper : lineWrappers) {
      if (GROUP_PATTERN.matcher(lineWrapper.line()).matches()) {
        if (!currentGroupBlock.isEmpty()) {
          groupBlocks.add(new GroupBlock(blockStartLineNo, new ArrayList<>(currentGroupBlock)));
          currentGroupBlock.clear();
        }

        blockStartLineNo = lineWrapper.lineNo();
      }

      currentGroupBlock.add(lineWrapper);

      if (lineWrapper.line().equals("}")) {
        groupBlocks.add(new GroupBlock(blockStartLineNo, new ArrayList<>(currentGroupBlock)));
        currentGroupBlock.clear();
      }
    }

    if (!currentGroupBlock.isEmpty()) {
      log.warn("Unclosed group block starting at line {}", blockStartLineNo);
      groupBlocks.add(new GroupBlock(blockStartLineNo, currentGroupBlock));
    }

    return groupBlocks;
  }

  private Optional<SpawnGroup> parseGroupBlock(GroupBlock groupBlock) {
    var startLine = groupBlock.startLineNo();
    var lineWrappers = groupBlock.lineWrappers();

    try {
      var groupName =
          lineWrappers.stream()
              .map(LineWrapper::line)
              .map(GROUP_PATTERN::matcher)
              .filter(Matcher::matches)
              .findFirst()
              .map(matcher -> matcher.group(1))
              .orElseThrow(() -> new ParseException(startLine, "Group name not found in block"));

      var vnum =
          lineWrappers.stream()
              .map(LineWrapper::line)
              .map(VNUM_PATTERN::matcher)
              .filter(Matcher::matches)
              .findFirst()
              .map(matcher -> matcher.group(1))
              .map(Long::parseLong)
              .orElseThrow(
                  () -> new ParseException(startLine, "Vnum not found for group: " + groupName));

      var leader =
          lineWrappers.stream()
              .map(LineWrapper::line)
              .map(LEADER_PATTERN::matcher)
              .filter(Matcher::matches)
              .findFirst()
              .map(matcher -> matcher.group(3))
              .map(Long::parseLong)
              .orElseThrow(
                  () -> new ParseException(startLine, "Leader not found for group: " + groupName));

      var membersIds =
          lineWrappers.stream()
              .map(LineWrapper::line)
              .map(MEMBER_PATTERN::matcher)
              .filter(Matcher::matches)
              .map(
                  matcher ->
                      new MemberWrapper(
                          Integer.parseInt(matcher.group(1)), Long.parseLong(matcher.group(4))))
              .sorted(Comparator.comparingInt(memberWrapper -> memberWrapper.index))
              .map(MemberWrapper::id)
              .toList();

      return Optional.of(new SpawnGroup(vnum, groupName, leader, membersIds));
    } catch (ParseException e) {
      log.error("Parse error at line {}: {}", e.getLineNumber(), e.getMessage());
    } catch (NumberFormatException e) {
      log.error(
          "Invalid number format in group block starting at line {}: {}",
          startLine,
          e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error parsing group block starting at line {}", startLine, e);
    }

    return Optional.empty();
  }

  private record MemberWrapper(int index, long id) {}

  private record GroupBlock(int startLineNo, List<LineWrapper> lineWrappers) {}

  private record LineWrapper(int lineNo, String line) {}

  @Getter
  private static class ParseException extends Exception {
    private final int lineNumber;

    public ParseException(int lineNumber, String message) {
      super(message);
      this.lineNumber = lineNumber;
    }
  }
}
