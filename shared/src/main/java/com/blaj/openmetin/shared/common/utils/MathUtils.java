package com.blaj.openmetin.shared.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtils {

  public static double distance(int x1, int y1, int x2, int y2) {
    var a = x1 - x2;
    var b = y1 - y2;

    return Math.sqrt(a * a + b * b);
  }

  public static double rotation(double x, double y) {
    var vectorLength = Math.sqrt(x * x + y * y);

    if (vectorLength == 0) {
      return 0;
    }

    var normalizedX = x / vectorLength;
    var normalizedY = y / vectorLength;
    var upVectorX = 0;
    var upVectorY = 1;

    var rotationRadians =
        -(Math.atan2(normalizedY, normalizedX) - Math.atan2(upVectorY, upVectorX));
    var rotationDegrees = Math.toDegrees(rotationRadians);

    if (rotationDegrees < 0) {
      rotationDegrees += 360;
    }

    return rotationDegrees;
  }
}
