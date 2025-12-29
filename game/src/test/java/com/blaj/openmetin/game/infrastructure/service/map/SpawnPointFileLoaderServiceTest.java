package com.blaj.openmetin.game.infrastructure.service.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpawnPointFileLoaderServiceTest {

  private SpawnPointFileLoaderService spawnPointFileLoaderService;

  @Mock private DataPathProperties dataPathProperties;

  @TempDir private Path tempDir;

  @BeforeEach
  public void beforeEach() {
    given(dataPathProperties.mapsDirectory()).willReturn(tempDir);

    spawnPointFileLoaderService = new SpawnPointFileLoaderService(dataPathProperties);
  }

  @Test
  public void givenNoSpawnFiles_whenLoad_thenReturnEmptyList() {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenInvalidLine_whenLoad_thenSkipInvalidLine() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var regenContent =
        """
        m 100 200 10 20 1 10 60 0 0 mob1
        invalid_line_with_too_few_fields
        s 300 400 15 25 2 20 120 0 0 mob2
        """;
    Files.writeString(mapDir.resolve("regen.txt"), regenContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getX()).isEqualTo(100);
    assertThat(result.get(1).getX()).isEqualTo(300);
  }

  @Test
  public void givenFilesWithCommentsAndEmptyLines_whenLoad_thenSkipThemAndReturnSpawnPoints()
      throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var regenContent =
        """
        # This is a comment
        m 100 200 10 20 1 10 60 0 0 mob1

        // Another comment style
        s 300 400 15 25 2 20 120 0 0 mob2
        """;
    Files.writeString(mapDir.resolve("regen.txt"), regenContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  public void givenRegenFile_whenLoad_thenReturnSpawnPoints() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var regenContent = "m 100 200 10 20 1 10 60 0 0 mob1";
    Files.writeString(mapDir.resolve("regen.txt"), regenContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(SpawnPointType.MONSTER);
    assertThat(result.get(0).getX()).isEqualTo(100);
    assertThat(result.get(0).getY()).isEqualTo(200);
    assertThat(result.get(0).getRangeX()).isEqualTo(10);
    assertThat(result.get(0).getRangeY()).isEqualTo(20);
    assertThat(result.get(0).isAggressive()).isFalse();
  }

  @Test
  public void givenNpcFile_whenLoad_thenReturnSpawnPoints() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var npcContent = "g 300 400 15 25 2 20 120 0 0 npc1";
    Files.writeString(mapDir.resolve("npc.txt"), npcContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(SpawnPointType.GROUP);
    assertThat(result.get(0).getX()).isEqualTo(300);
    assertThat(result.get(0).getY()).isEqualTo(400);
  }

  @Test
  public void givenStoneFile_whenLoad_thenReturnSpawnPoints() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var stoneContent = "e 500 600 20 30 3 30 180 0 0 stone1";
    Files.writeString(mapDir.resolve("stone.txt"), stoneContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(SpawnPointType.EXCEPTION);
    assertThat(result.get(0).getX()).isEqualTo(500);
    assertThat(result.get(0).getY()).isEqualTo(600);
  }

  @Test
  public void givenBossFile_whenLoad_thenReturnSpawnPoints() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var bossContent = "r 700 800 25 35 4 40 240 0 0 boss1";
    Files.writeString(mapDir.resolve("boss.txt"), bossContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(SpawnPointType.GROUP_COLLECTION);
    assertThat(result.get(0).getX()).isEqualTo(700);
    assertThat(result.get(0).getY()).isEqualTo(800);
  }

  @Test
  public void givenAggressiveSpawnPoint_whenLoad_thenSetAggressiveTrue() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var regenContent = "sa 100 200 10 20 1 10 60 0 0 aggressive_mob";
    Files.writeString(mapDir.resolve("regen.txt"), regenContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getType()).isEqualTo(SpawnPointType.SPECIAL);
    assertThat(result.get(0).isAggressive()).isTrue();
  }

  @Test
  public void givenAggressiveSpawnPointWithUppercaseA_whenLoad_thenSetAggressiveTrue()
      throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    var regenContent = "sA 100 200 10 20 1 10 60 0 0 aggressive_mob";
    Files.writeString(mapDir.resolve("regen.txt"), regenContent);

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).isAggressive()).isTrue();
  }

  @Test
  public void givenMultipleFiles_whenLoad_thenReturnAllSpawnPoints() throws IOException {
    // given
    var mapName = "map1";
    var mapDir = tempDir.resolve(mapName);
    Files.createDirectories(mapDir);

    Files.writeString(mapDir.resolve("regen.txt"), "m 100 200 10 20 1 10 60 0 0 mob1");
    Files.writeString(mapDir.resolve("npc.txt"), "s 300 400 15 25 2 20 120 0 0 npc1");
    Files.writeString(mapDir.resolve("stone.txt"), "e 500 600 20 30 3 30 180 0 0 stone1");
    Files.writeString(mapDir.resolve("boss.txt"), "m 700 800 25 35 4 40 240 0 0 boss1");

    // when
    var result = spawnPointFileLoaderService.load(mapName);

    // then
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getX()).isEqualTo(100);
    assertThat(result.get(1).getX()).isEqualTo(300);
    assertThat(result.get(2).getX()).isEqualTo(500);
    assertThat(result.get(3).getX()).isEqualTo(700);
  }
}
