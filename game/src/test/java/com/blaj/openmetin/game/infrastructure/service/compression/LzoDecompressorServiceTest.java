package com.blaj.openmetin.game.infrastructure.service.compression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.anarres.lzo.LzoAlgorithm;
import org.anarres.lzo.LzoCompressor1x_1;
import org.anarres.lzo.LzoLibrary;
import org.anarres.lzo.lzo_uintp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LzoDecompressorServiceTest {

  private LzoDecompressorService lzoDecompressorService;
  private LzoCompressor1x_1 compressor;

  @BeforeEach
  public void beforeEach() {
    lzoDecompressorService = new LzoDecompressorService();
    compressor =
        (LzoCompressor1x_1) LzoLibrary.getInstance().newCompressor(LzoAlgorithm.LZO1X, null);
  }

  @Test
  public void givenNullCompressedData_whenDecompress_thenThrowException() {
    // given
    byte[] compressed = null;
    var expectedSize = 100;

    // when
    var thrownException =
        assertThrows(
            IOException.class, () -> lzoDecompressorService.decompress(expectedSize, compressed));

    // then
    assertThat(thrownException).hasMessageContaining("Compressed data is empty");
  }

  @Test
  public void givenEmptyCompressedData_whenDecompress_thenThrowException() {
    // given
    var compressed = new byte[0];
    var expectedSize = 100;

    // when
    var thrownException =
        assertThrows(
            IOException.class, () -> lzoDecompressorService.decompress(expectedSize, compressed));

    // then
    assertThat(thrownException).hasMessageContaining("Compressed data is empty");
  }

  @Test
  public void givenWrongExpectedSize_whenDecompress_thenThrowException() throws IOException {
    // given
    var originalData = "Test data for compression".getBytes();
    var compressed = compress(originalData);
    var wrongExpectedSize = originalData.length + 10;

    // when
    var thrownException =
        assertThrows(
            IOException.class,
            () -> lzoDecompressorService.decompress(wrongExpectedSize, compressed));

    // then
    assertThat(thrownException).hasMessageContaining("LZO decompression failed");
  }

  @Test
  public void givenInvalidCompressedData_whenDecompress_thenThrowException() {
    // given
    var invalidCompressed = new byte[] {1, 2, 3, 4, 5};
    var expectedSize = 100;

    // when
    var thrownException =
        assertThrows(
            IOException.class,
            () -> lzoDecompressorService.decompress(expectedSize, invalidCompressed));

    // then
    assertThat(thrownException).hasMessageContaining("LZO decompression failed");
  }

  @Test
  public void givenValidCompressedData_whenDecompress_thenReturnDecompressedData()
      throws IOException {
    // given
    var originalData = "Test data for compression and decompression".getBytes();
    var compressed = compress(originalData);

    // when
    var decompressed = lzoDecompressorService.decompress(originalData.length, compressed);

    // then
    assertThat(decompressed).isEqualTo(originalData);
  }

  @Test
  public void givenLargeCompressedData_whenDecompress_thenReturnDecompressedData()
      throws IOException {
    // given
    var originalData = new byte[10000];
    for (var i = 0; i < originalData.length; i++) {
      originalData[i] = (byte) (i % 256);
    }
    var compressed = compress(originalData);

    // when
    var decompressed = lzoDecompressorService.decompress(originalData.length, compressed);

    // then
    assertThat(decompressed).isEqualTo(originalData);
  }

  private byte[] compress(byte[] data) {
    var compressed = new byte[data.length + data.length / 16 + 64 + 3];
    var compressedLengthWrapper = new lzo_uintp(compressed.length);

    compressor.compress(data, 0, data.length, compressed, 0, compressedLengthWrapper);

    var actualCompressed = new byte[compressedLengthWrapper.value];
    System.arraycopy(compressed, 0, actualCompressed, 0, compressedLengthWrapper.value);

    return actualCompressed;
  }
}
