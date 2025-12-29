package com.blaj.openmetin.game.infrastructure.service.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.infrastructure.service.compression.LzoDecompressorService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapAttributeFileLoaderServiceTest {

  private MapAttributeFileLoaderService mapAttributeFileLoaderService;

  @Mock private LzoDecompressorService lzoDecompressorService;

  @TempDir private Path tempDir;

  @BeforeEach
  public void beforeEach() {
    mapAttributeFileLoaderService = new MapAttributeFileLoaderService(lzoDecompressorService);
  }

  @Test
  public void givenFileNotExists_whenLoad_thenReturnEmpty() {
    // given
    var serverAttrPath = tempDir.resolve("server_attr");
    var baseCoordinates = new Coordinates(0, 0);
    var mapWidth = 4L;
    var mapHeight = 5L;

    // when
    var result =
        mapAttributeFileLoaderService.load(serverAttrPath, baseCoordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenInvalidFileFormat_whenLoad_thenReturnEmpty() throws IOException {
    // given
    var serverAttrPath = tempDir.resolve("server_attr");
    var baseCoordinates = new Coordinates(0, 0);
    var mapWidth = 4L;
    var mapHeight = 5L;

    var sectreesWidth = calculateExpectedSectrees(mapWidth);
    var sectreesHeight = calculateExpectedSectrees(mapHeight);

    var buffer = ByteBuffer.allocate(12);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(sectreesWidth);
    buffer.putInt(sectreesHeight);
    buffer.putInt(100);
    Files.write(serverAttrPath, buffer.array());

    // when
    var result =
        mapAttributeFileLoaderService.load(serverAttrPath, baseCoordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenDecompressionFails_whenLoad_thenReturnMapAttributeSetWithEmptySectrees()
      throws IOException {
    // given
    var serverAttrPath = tempDir.resolve("server_attr");
    var baseCoordinates = new Coordinates(0, 0);
    var mapWidth = 1L;
    var mapHeight = 1L;

    var sectreesWidth = calculateExpectedSectrees(mapWidth);
    var sectreesHeight = calculateExpectedSectrees(mapHeight);

    var fileContent = createServerAttrFile(sectreesWidth, sectreesHeight, new byte[] {1, 2, 3, 4});
    Files.write(serverAttrPath, fileContent);

    given(lzoDecompressorService.decompress(anyInt(), any(byte[].class)))
        .willThrow(new IOException("Decompression failed"));

    // when
    var result =
        mapAttributeFileLoaderService.load(serverAttrPath, baseCoordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().sectreesWidth()).isEqualTo(sectreesWidth);
    assertThat(result.get().sectreesHeight()).isEqualTo(sectreesHeight);
  }

  @Test
  public void givenValidFile_whenLoad_thenReturnMapAttributeSet() throws IOException {
    // given
    var serverAttrPath = tempDir.resolve("server_attr");
    var baseCoordinates = new Coordinates(1000, 2000);
    var mapWidth = 1L;
    var mapHeight = 1L;

    var sectreesWidth = calculateExpectedSectrees(mapWidth);
    var sectreesHeight = calculateExpectedSectrees(mapHeight);

    var compressedData = new byte[] {1, 2, 3, 4};
    var fileContent = createServerAttrFile(sectreesWidth, sectreesHeight, compressedData);
    Files.write(serverAttrPath, fileContent);

    var decompressedData = createDecompressedSectreeData();

    given(
            lzoDecompressorService.decompress(
                eq(MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES), eq(compressedData)))
        .willReturn(decompressedData);

    // when
    var result =
        mapAttributeFileLoaderService.load(serverAttrPath, baseCoordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().sectreesWidth()).isEqualTo(sectreesWidth);
    assertThat(result.get().sectreesHeight()).isEqualTo(sectreesHeight);
    assertThat(result.get().baseCoordinates()).isEqualTo(baseCoordinates);
  }

  @Test
  public void givenValidFileWithMultipleSectrees_whenLoad_thenReturnMapAttributeSet()
      throws IOException {
    // given
    var serverAttrPath = tempDir.resolve("server_attr");
    var baseCoordinates = new Coordinates(1000, 2000);
    var mapWidth = 2L;
    var mapHeight = 2L;

    var sectreesWidth = calculateExpectedSectrees(mapWidth);
    var sectreesHeight = calculateExpectedSectrees(mapHeight);

    var compressedData = new byte[] {1, 2, 3, 4};
    var fileContent = createServerAttrFile(sectreesWidth, sectreesHeight, compressedData);
    Files.write(serverAttrPath, fileContent);

    var decompressedData = createDecompressedSectreeData();
    given(
            lzoDecompressorService.decompress(
                eq(MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES), any(byte[].class)))
        .willReturn(decompressedData);

    // when
    var result =
        mapAttributeFileLoaderService.load(serverAttrPath, baseCoordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().sectreesWidth()).isEqualTo(sectreesWidth);
    assertThat(result.get().sectreesHeight()).isEqualTo(sectreesHeight);
    assertThat(result.get().baseCoordinates()).isEqualTo(baseCoordinates);
  }

  private int calculateExpectedSectrees(long mapSize) {
    return (int) (mapSize * Map.MAP_UNIT / MapAttributeSet.SECTREE_SIZE);
  }

  private byte[] createServerAttrFile(
      int sectreesWidth, int sectreesHeight, byte[] compressedData) {
    var totalSectrees = sectreesWidth * sectreesHeight;
    var buffer = ByteBuffer.allocate(8 + totalSectrees * (4 + compressedData.length));
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(sectreesWidth);
    buffer.putInt(sectreesHeight);

    for (int i = 0; i < totalSectrees; i++) {
      buffer.putInt(compressedData.length);
      buffer.put(compressedData);
    }

    return buffer.array();
  }

  private byte[] createDecompressedSectreeData() {
    var buffer = ByteBuffer.allocate(MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for (var i = 0; i < MapAttributeSet.CELLS_PER_SECTREE; i++) {
      buffer.putInt(0);
    }

    return buffer.array();
  }
}
