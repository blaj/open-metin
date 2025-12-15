package com.blaj.openmetin.shared.common.utils;

import java.time.Instant;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DateTimeUtils {

  private static Instant bootTime = null;

  public static void initialize() {
    if (bootTime == null) {
      bootTime = Instant.now();
    }
  }

  public static long getUnixTime() {
    var currentTime = Instant.now();
    var elapsedSeconds = currentTime.getEpochSecond() - getBootTime();

    return elapsedSeconds * 1000 + currentTime.toEpochMilli() % 1000;
  }

  public static long getBootTime() {
    if (bootTime == null) {
      throw new IllegalStateException("DateTimeUtils not initialized. Call initialize() first.");
    }

    return bootTime.getEpochSecond();
  }

  public static long getGlobalTime() {
    return Instant.now().getEpochSecond();
  }
}
