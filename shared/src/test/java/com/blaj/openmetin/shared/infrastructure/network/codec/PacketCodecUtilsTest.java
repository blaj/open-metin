package com.blaj.openmetin.shared.infrastructure.network.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PacketCodecUtilsTest {

  private ByteBuf byteBuf;

  @BeforeEach
  public void beforeEach() {
    byteBuf = Unpooled.buffer();
  }

  @AfterEach
  public void afterEach() {
    byteBuf.release();
  }

  @Test
  public void givenNullString_whenWriteString_thenWritesZeroLength() {
    // given

    // when
    PacketCodecUtils.writeString(byteBuf, null);

    // then
    assertThat(byteBuf.readShort()).isZero();
  }

  @Test
  public void givenEmptyString_whenWriteString_thenWritesZeroLength() {
    // given

    // when
    PacketCodecUtils.writeString(byteBuf, "");

    // then
    assertThat(byteBuf.readShort()).isZero();
  }

  @Test
  public void givenNormalString_whenWriteString_thenWritesLengthAndBytes() {
    // given
    var value = "Hello";

    // when
    PacketCodecUtils.writeString(byteBuf, value);

    // then
    assertThat(byteBuf.readShortLE()).isEqualTo((short) 5);
    var bytes = new byte[5];
    byteBuf.readBytes(bytes);
    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo(value);
  }

  @Test
  public void givenZeroLength_whenReadString_thenReturnsEmptyString() {
    // given
    byteBuf.writeShort(0);

    // when
    var result = PacketCodecUtils.readString(byteBuf);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenNonZeroLength_whenReadString_thenReadsString() {
    // given
    var value = "Hello";
    byteBuf.writeShortLE(5);
    byteBuf.writeBytes(value.getBytes(StandardCharsets.UTF_8));

    // when
    var result = PacketCodecUtils.readString(byteBuf);

    // then
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void givenNullString_whenWriteFixedString_thenWritesZeros() {
    // given

    // when
    PacketCodecUtils.writeFixedString(byteBuf, null, 10);

    // then
    assertThat(byteBuf.readableBytes()).isEqualTo(10);
    IntStream.range(0, 10).forEach(_ -> assertThat(byteBuf.readByte()).isZero());
  }

  @Test
  public void givenStringTooLong_whenWriteFixedString_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(
            IllegalArgumentException.class,
            () -> PacketCodecUtils.writeFixedString(byteBuf, "TooLongString", 5));

    // then
    assertThat(thrownException).hasMessageContaining("String too long");
  }

  @Test
  public void givenNormalString_whenWriteFixedString_thenWritesStringWithPadding() {
    // given
    var value = "Hi";

    // when
    PacketCodecUtils.writeFixedString(byteBuf, value, 5);

    // then
    var bytes = new byte[5];
    byteBuf.readBytes(bytes);
    assertThat(bytes).containsExactly('H', 'i', 0, 0, 0);
  }

  @Test
  public void givenBufferWithPadding_whenReadFixedString_thenReadsStringWithoutPadding() {
    // given
    byteBuf.writeBytes("Hi".getBytes(StandardCharsets.UTF_8));
    byteBuf.writeZero(3);

    // when
    var result = PacketCodecUtils.readFixedString(byteBuf, 5);

    // then
    assertThat(result).isEqualTo("Hi");
  }

  @Test
  public void givenBufferWithAllZeros_whenReadFixedString_thenReturnsEmptyString() {
    // given
    byteBuf.writeZero(10);

    // when
    var result = PacketCodecUtils.readFixedString(byteBuf, 10);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenNullArray_whenWriteFixedByteArray_thenWritesZeros() {
    // given

    // when
    PacketCodecUtils.writeFixedByteArray(byteBuf, null, 10);

    // then
    assertThat(byteBuf.readableBytes()).isEqualTo(10);
    IntStream.range(0, 10).forEach(_ -> assertThat(byteBuf.readByte()).isZero());
  }

  @Test
  public void givenArrayTooLong_whenWriteFixedByteArray_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(
            IllegalArgumentException.class,
            () -> PacketCodecUtils.writeFixedByteArray(byteBuf, new byte[] {1, 2, 3, 4, 5}, 3));

    // then
    assertThat(thrownException).hasMessageContaining("Array too long");
  }

  @Test
  public void givenNormalArray_whenWriteFixedByteArray_thenWritesArrayWithPadding() {
    // given
    var value = new byte[] {1, 2, 3};

    // when
    PacketCodecUtils.writeFixedByteArray(byteBuf, value, 5);

    // then
    var bytes = new byte[5];
    byteBuf.readBytes(bytes);
    assertThat(bytes).containsExactly(1, 2, 3, 0, 0);
  }

  @Test
  public void givenBuffer_whenReadFixedByteArray_thenReadsArray() {
    // given
    byteBuf.writeBytes(new byte[] {1, 2, 3, 4, 5});

    // when
    var result = PacketCodecUtils.readFixedByteArray(byteBuf, 5);

    // then
    assertThat(result).containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  public void givenBuffer_whenReadFixedLongArray_thenReadsLongs() {
    // given
    byteBuf.writeLongLE(100L);
    byteBuf.writeLongLE(200L);
    byteBuf.writeLongLE(300L);

    // when
    var result = PacketCodecUtils.readFixedLongArray(byteBuf, 3);

    // then
    assertThat(result).containsExactly(100L, 200L, 300L);
  }

  @Test
  public void givenArrayShorterThanLength_whenWriteFixedLongArray_thenWritesArrayWithZeros() {
    // given
    var array = new long[] {100L, 200L};

    // when
    PacketCodecUtils.writeFixedLongArray(byteBuf, array, 5);

    // then
    assertThat(byteBuf.readLongLE()).isEqualTo(100L);
    assertThat(byteBuf.readLongLE()).isEqualTo(200L);
    assertThat(byteBuf.readLongLE()).isZero();
    assertThat(byteBuf.readLongLE()).isZero();
    assertThat(byteBuf.readLongLE()).isZero();
  }

  @Test
  public void givenArrayLongerThanLength_whenWriteFixedLongArray_thenWritesOnlySpecifiedLength() {
    // given
    var array = new long[] {100L, 200L, 300L, 400L, 500L};

    // when
    PacketCodecUtils.writeFixedLongArray(byteBuf, array, 3);

    // then
    assertThat(byteBuf.readLongLE()).isEqualTo(100L);
    assertThat(byteBuf.readLongLE()).isEqualTo(200L);
    assertThat(byteBuf.readLongLE()).isEqualTo(300L);
    assertThat(byteBuf.readableBytes()).isZero();
  }

  @Test
  public void givenBuffer_whenReadFixedIntArray_thenReadsInts() {
    // given
    byteBuf.writeIntLE(10);
    byteBuf.writeIntLE(20);
    byteBuf.writeIntLE(30);

    // when
    var result = PacketCodecUtils.readFixedIntArray(byteBuf, 3);

    // then
    assertThat(result).containsExactly(10, 20, 30);
  }

  @Test
  public void givenArrayShorterThanLength_whenWriteFixedIntArray_thenWritesArrayWithZeros() {
    // given
    var array = new int[] {10, 20};

    // when
    PacketCodecUtils.writeFixedIntArray(byteBuf, array, 5);

    // then
    assertThat(byteBuf.readIntLE()).isEqualTo(10);
    assertThat(byteBuf.readIntLE()).isEqualTo(20);
    assertThat(byteBuf.readIntLE()).isZero();
    assertThat(byteBuf.readIntLE()).isZero();
    assertThat(byteBuf.readIntLE()).isZero();
  }

  @Test
  public void givenBuffer_whenReadFixedShortArray_thenReadsShorts() {
    // given
    byteBuf.writeShortLE(1);
    byteBuf.writeShortLE(2);
    byteBuf.writeShortLE(3);

    // when
    var result = PacketCodecUtils.readFixedShortArray(byteBuf, 3);

    // then
    assertThat(result).containsExactly((short) 1, (short) 2, (short) 3);
  }

  @Test
  public void givenArrayShorterThanLength_whenWriteFixedShortArray_thenWritesArrayWithZeros() {
    // given
    var array = new short[] {1, 2};

    // when
    PacketCodecUtils.writeFixedShortArray(byteBuf, array, 5);

    // then
    assertThat(byteBuf.readShortLE()).isEqualTo((short) 1);
    assertThat(byteBuf.readShortLE()).isEqualTo((short) 2);
    assertThat(byteBuf.readShortLE()).isZero();
    assertThat(byteBuf.readShortLE()).isZero();
    assertThat(byteBuf.readShortLE()).isZero();
  }

  @Test
  public void givenBuffer_whenReadFixedUnsignedIntArray_thenReadsUnsignedInts() {
    // given
    byteBuf.writeIntLE(-1); // 0xFFFFFFFF as unsigned = 4294967295L
    byteBuf.writeIntLE(100);

    // when
    var result = PacketCodecUtils.readFixedUnsignedIntArray(byteBuf, 2);

    // then
    assertThat(result).containsExactly(4294967295L, 100L);
  }

  @Test
  public void
      givenArrayShorterThanLength_whenWriteFixedUnsignedIntArray_thenWritesArrayWithZeros() {
    // given
    var array = new long[] {4294967295L, 100L}; // First value is max unsigned int

    // when
    PacketCodecUtils.writeFixedUnsignedIntArray(byteBuf, array, 4);

    // then
    assertThat(byteBuf.readIntLE()).isEqualTo(-1); // 4294967295L as int
    assertThat(byteBuf.readIntLE()).isEqualTo(100);
    assertThat(byteBuf.readIntLE()).isZero();
    assertThat(byteBuf.readIntLE()).isZero();
  }

  @Test
  public void givenBuffer_whenReadFixedStringArray_thenReadsStrings() {
    // given
    byteBuf.writeBytes("Hi".getBytes(StandardCharsets.UTF_8));
    byteBuf.writeZero(3);
    byteBuf.writeBytes("Bye".getBytes(StandardCharsets.UTF_8));
    byteBuf.writeZero(2);

    // when
    var result = PacketCodecUtils.readFixedStringArray(byteBuf, 2, 5);

    // then
    assertThat(result).containsExactly("Hi", "Bye");
  }

  @Test
  public void givenArrayShorterThanLength_whenWriteFixedStringArray_thenWritesWithEmptyStrings() {
    // given
    var array = new String[] {"Hi", "Bye"};

    // when
    PacketCodecUtils.writeFixedStringArray(byteBuf, array, 4, 5);

    // then
    var bytes1 = new byte[5];
    byteBuf.readBytes(bytes1);
    assertThat(new String(bytes1, 0, 2, StandardCharsets.UTF_8)).isEqualTo("Hi");

    var bytes2 = new byte[5];
    byteBuf.readBytes(bytes2);
    assertThat(new String(bytes2, 0, 3, StandardCharsets.UTF_8)).isEqualTo("Bye");

    var bytes3 = new byte[5];
    byteBuf.readBytes(bytes3);
    assertThat(bytes3).containsOnly((byte) 0);

    var bytes4 = new byte[5];
    byteBuf.readBytes(bytes4);
    assertThat(bytes4).containsOnly((byte) 0);
  }

  @Test
  public void givenUtf8String_whenWriteAndReadString_thenPreservesEncoding() {
    // given
    var value = "Witaj świecie! 你好世界";
    PacketCodecUtils.writeString(byteBuf, value);

    // when
    var result = PacketCodecUtils.readString(byteBuf);

    // then
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void givenUtf8String_whenWriteAndReadFixedString_thenPreservesEncoding() {
    // given
    var value = "Cześć!";
    PacketCodecUtils.writeFixedString(byteBuf, value, 20);

    // when
    var result = PacketCodecUtils.readFixedString(byteBuf, 20);

    // then
    assertThat(result).isEqualTo(value);
  }
}
