package com.blaj.openmetin.shared.infrastructure.encryption;

import java.security.SecureRandom;
import lombok.experimental.UtilityClass;
import org.joou.UInteger;

@UtilityClass
public class HandshakeUtils {

  private static final SecureRandom secureRandom = new SecureRandom();

  public static UInteger generateUInt32() {
    var r1 = secureRandom.nextInt(1 << 30);
    var r2 = secureRandom.nextInt(1 << 2);
    var value = (((long) r1 << 2) | r2) & 0xFFFFFFFFL;

    return UInteger.valueOf(value);
  }

  public static boolean percentageCheck(long percentage) {
    return generateInt32(1, 101) <= percentage;
  }

  public static int generateInt32(int fromInclusive, int toExclusive) {
    if (fromInclusive >= toExclusive) {
      throw new IllegalArgumentException("fromInclusive must be smaller than toExclusive");
    }

    return secureRandom.nextInt(fromInclusive, toExclusive);
  }

  public static UInteger generateUInt32(UInteger fromInclusive, UInteger toExclusive) {
    if (fromInclusive.compareTo(toExclusive) >= 0) {
      throw new IllegalArgumentException("fromInclusive must be smaller than toExclusive");
    }

    var from = fromInclusive.longValue();
    var to = toExclusive.longValue();

    var range = to - from - 1;
    if (range == 0) {
      return fromInclusive;
    }

    var mask = range;
    mask |= mask >>> 1;
    mask |= mask >>> 2;
    mask |= mask >>> 4;
    mask |= mask >>> 8;
    mask |= mask >>> 16;

    long result;
    do {
      result = mask & (secureRandom.nextInt() & 0xFFFFFFFFL);
    } while (result > range);

    return UInteger.valueOf(result + from);
  }
}
