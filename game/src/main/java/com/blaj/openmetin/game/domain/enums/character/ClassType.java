package com.blaj.openmetin.game.domain.enums.character;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum ClassType implements ByteEnum {
  WARRIOR_MALE((byte) 0, JobType.WARRIOR),
  NINJA_FEMALE((byte) 1, JobType.NINJA),
  SURA_MALE((byte) 2, JobType.SURA),
  SHAMAN_FEMALE((byte) 3, JobType.SHAMAN),
  WARRIOR_FEMALE((byte) 4, JobType.WARRIOR),
  NINJA_MALE((byte) 5, JobType.NINJA),
  SURA_FEMALE((byte) 6, JobType.SURA),
  SHAMAN_MALE((byte) 7, JobType.SHAMAN);

  private final byte value;
  private final JobType jobType;

  ClassType(byte value, JobType jobType) {
    this.value = value;
    this.jobType = jobType;
  }

  public static ClassType fromValue(int value) {
    return Arrays.stream(values())
        .filter(empire -> empire.value == value)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid empire value: " + value));
  }
}
