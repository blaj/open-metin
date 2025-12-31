package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.model.spawn.SpawnGroupCollection;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
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
public class GroupCollectionFileLoaderService {

  private static final Pattern GROUP_PATTERN = Pattern.compile("^Group\\s+(.+)$");
  private static final Pattern VNUM_PATTERN = Pattern.compile("^Vnum\\s+(\\d+)$");
  private static final Pattern ENTRY_PATTERN = Pattern.compile("^(\\d+)\\s+(\\d+)\\s+(\\d+)$");

  private final DataPathProperties dataPathProperties;

  public Map<Long, SpawnGroupCollection> load() {
    if (!Files.exists(dataPathProperties.groupCollectionFile())) {
      log.warn("No group_group.txt file found at {}", dataPathProperties.groupCollectionFile());

      return new HashMap<>();
    }

    try (var lines =
        Files.lines(dataPathProperties.groupCollectionFile(), StandardCharsets.ISO_8859_1)) {
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

      var groupCollections =
          splitIntoGroupBlocks(allLines).stream()
              .map(this::parseGroupBlock)
              .flatMap(Optional::stream)
              .collect(
                  Collectors.toMap(
                      SpawnGroupCollection::id,
                      groupCollection -> groupCollection,
                      (existing, duplicate) -> {
                        log.warn(
                            "Duplicate group collection vnum: {} (keeping first occurrence)",
                            existing.id());
                        return existing;
                      }));

      log.info("Successfully loaded {} group collections from file", groupCollections.size());

      return groupCollections;
    } catch (IOException e) {
      log.error("Failed to load group_group.txt file", e);
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
      log.warn("Unclosed group collection block starting at line {}", blockStartLineNo);
      groupBlocks.add(new GroupBlock(blockStartLineNo, currentGroupBlock));
    }

    return groupBlocks;
  }

  private Optional<SpawnGroupCollection> parseGroupBlock(GroupBlock groupBlock) {
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
              .orElseThrow(
                  () -> new ParseException(startLine, "Group collection name not found in block"));

      var vnum =
          lineWrappers.stream()
              .map(LineWrapper::line)
              .map(VNUM_PATTERN::matcher)
              .filter(Matcher::matches)
              .findFirst()
              .map(matcher -> matcher.group(1))
              .map(Long::parseLong)
              .orElseThrow(
                  () ->
                      new ParseException(
                          startLine, "Vnum not found for group collection: " + groupName));

      var entries =
          lineWrappers.stream()
              .map(LineWrapper::line)
              .map(ENTRY_PATTERN::matcher)
              .filter(Matcher::matches)
              .map(
                  matcher ->
                      new EntryWrapper(
                          Integer.parseInt(matcher.group(1)), // index
                          Long.parseLong(matcher.group(2)), // group vnum
                          Integer.parseInt(matcher.group(3)))) // probability/weight
              .sorted(Comparator.comparingInt(EntryWrapper::index))
              .map(entry -> new SpawnGroupCollection.Entry(entry.groupId(), entry.probability()))
              .toList();

      var groupCollection = new SpawnGroupCollection(vnum, groupName, entries);

      log.debug(
          "Line {}: Successfully parsed group collection '{}' (vnum={}, {} entries)",
          startLine,
          groupName,
          vnum,
          entries.size());

      return Optional.of(groupCollection);

    } catch (ParseException e) {
      log.error("Parse error at line {}: {}", e.getLineNumber(), e.getMessage());
    } catch (NumberFormatException e) {
      log.error(
          "Invalid number format in group collection block starting at line {}: {}",
          startLine,
          e.getMessage());
    } catch (Exception e) {
      log.error(
          "Unexpected error parsing group collection block starting at line {}", startLine, e);
    }

    return Optional.empty();
  }

  private record EntryWrapper(int index, long groupId, int probability) {}

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
