package com.blaj.openmetin.shared.infrastructure.network.codec;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class PacketCodecUtils {

  public static void writeString(ByteBuf out, String value) {
    Optional.ofNullable(value)
        .ifPresentOrElse(
            v -> {
              var bytes = v.getBytes(StandardCharsets.UTF_8);
              out.writeShortLE(bytes.length);
              out.writeBytes(bytes);
            },
            () -> out.writeShortLE(0));
  }

  public static String readString(ByteBuf in) {
    return Optional.of(in.readShortLE())
        .filter(length -> length > 0)
        .map(
            length -> {
              var bytes = new byte[length];
              in.readBytes(bytes);
              return new String(bytes, StandardCharsets.UTF_8);
            })
        .orElse("");
  }

  public static void writeFixedString(ByteBuf out, String value, int length) {
    Optional.ofNullable(value)
        .map(v -> v.getBytes(StandardCharsets.UTF_8))
        .ifPresentOrElse(
            bytes -> {
              if (bytes.length > length) {
                throw new IllegalArgumentException(
                    "String too long: " + bytes.length + " > " + length);
              }

              out.writeBytes(bytes);
              out.writeZero(length - bytes.length);
            },
            () -> out.writeZero(length));
  }

  public static String readFixedString(ByteBuf in, int length) {
    var bytes = new byte[length];
    in.readBytes(bytes);

    var actualLength =
        IntStream.range(0, length).filter(i -> bytes[i] == 0).findFirst().orElse(length);

    return actualLength > 0 ? new String(bytes, 0, actualLength, StandardCharsets.UTF_8) : "";
  }

  public static void writeFixedByteArray(ByteBuf out, byte[] value, int length) {
    Optional.ofNullable(value)
        .ifPresentOrElse(
            bytes -> {
              if (bytes.length > length) {
                throw new IllegalArgumentException(
                    "Array too long: " + bytes.length + " > " + length);
              }

              out.writeBytes(bytes);
              out.writeZero(length - bytes.length);
            },
            () -> out.writeZero(length));
  }

  public static byte[] readFixedByteArray(ByteBuf in, int length) {
    var bytes = new byte[length];
    in.readBytes(bytes);
    return bytes;
  }

  public static long[] readFixedLongArray(ByteBuf in, int length) {
    return IntStream.range(0, length).mapToLong(i -> in.readLongLE()).toArray();
  }

  public static void writeFixedLongArray(ByteBuf out, long[] array, int length) {
    IntStream.range(0, length)
        .mapToLong(i -> i < array.length ? array[i] : 0)
        .forEach(out::writeLongLE);
  }

  public static int[] readFixedIntArray(ByteBuf in, int length) {
    return IntStream.range(0, length).map(i -> in.readIntLE()).toArray();
  }

  public static void writeFixedIntArray(ByteBuf out, int[] array, int length) {
    IntStream.range(0, length).map(i -> i < array.length ? array[i] : 0).forEach(out::writeIntLE);
  }

  public static short[] readFixedShortArray(ByteBuf in, int length) {
    var array = new short[length];
    IntStream.range(0, length).forEach(i -> array[i] = in.readShortLE());
    return array;
  }

  public static void writeFixedShortArray(ByteBuf out, short[] array, int length) {
    IntStream.range(0, length).map(i -> i < array.length ? array[i] : 0).forEach(out::writeShortLE);
  }

  public static long[] readFixedUnsignedIntArray(ByteBuf in, int length) {
    return IntStream.range(0, length)
        .mapToLong(i -> Integer.toUnsignedLong(in.readIntLE()))
        .toArray();
  }

  public static void writeFixedUnsignedIntArray(ByteBuf out, long[] array, int length) {
    IntStream.range(0, length)
        .map(i -> (int) (i < array.length ? array[i] : 0))
        .forEach(out::writeIntLE);
  }

  public static String[] readFixedStringArray(ByteBuf in, int arrayLength, int stringLength) {
    return IntStream.range(0, arrayLength)
        .mapToObj(i -> readFixedString(in, stringLength))
        .toArray(String[]::new);
  }

  public static void writeFixedStringArray(
      ByteBuf out, String[] array, int arrayLength, int stringLength) {
    IntStream.range(0, arrayLength)
        .forEach(i -> writeFixedString(out, i < array.length ? array[i] : "", stringLength));
  }
}
