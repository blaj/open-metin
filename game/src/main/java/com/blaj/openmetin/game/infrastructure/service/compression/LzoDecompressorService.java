package com.blaj.openmetin.game.infrastructure.service.compression;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.anarres.lzo.LzoAlgorithm;
import org.anarres.lzo.LzoDecompressor1x;
import org.anarres.lzo.LzoLibrary;
import org.anarres.lzo.lzo_uintp;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LzoDecompressorService {

  private final LzoDecompressor1x decompressor;

  public LzoDecompressorService() {
    this.decompressor =
        (LzoDecompressor1x) LzoLibrary.getInstance().newDecompressor(LzoAlgorithm.LZO1X, null);
  }

  public byte[] decompress(int expectedSize, byte[] compressed) throws IOException {
    if (compressed == null || compressed.length == 0) {
      throw new IOException("Compressed data is empty");
    }

    log.debug(
        "Decompressing LZO data: compressed size={}, expected decompressed size={}",
        compressed.length,
        expectedSize);

    var decompressed = new byte[expectedSize];
    var decompressedLengthWrapper = new lzo_uintp(expectedSize);

    try {
      int result =
          decompressor.decompress(
              compressed, 0, compressed.length, decompressed, 0, decompressedLengthWrapper);

      if (result != 0) {
        throw new IOException("LZO decompression failed with error code: " + result);
      }

      int actualDecompressedLength = decompressedLengthWrapper.value;

      log.debug("Decompressed successfully: actual size={}", actualDecompressedLength);

      if (actualDecompressedLength != expectedSize) {
        throw new IOException(
            "Decompressed size mismatch: expected %d, got %d"
                .formatted(expectedSize, actualDecompressedLength));
      }

      return decompressed;
    } catch (Exception e) {
      log.error(
          "Failed to decompress LZO data (compressed size: {}, expected size: {})",
          compressed.length,
          expectedSize,
          e);
      throw new IOException("LZO decompression failed", e);
    }
  }
}
