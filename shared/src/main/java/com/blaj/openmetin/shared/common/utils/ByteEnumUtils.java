package com.blaj.openmetin.shared.common.utils;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ByteEnumUtils {

  public static <E extends Enum<E> & ByteEnum> E fromValue(Class<E> enumClass, byte value) {
    return Optional.ofNullable(enumClass).map(Class::getEnumConstants).stream()
        .flatMap(Arrays::stream)
        .filter(enumConstant -> enumConstant.getValue() == value)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Unknown "
                        + (enumClass != null ? enumClass.getSimpleName() : "null")
                        + " value: "
                        + value));
  }
}
