package com.blaj.openmetin.game.domain.enums;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import com.blaj.openmetin.game.domain.config.JobConfig;
import com.blaj.openmetin.game.domain.config.JobConfigs;
import lombok.Getter;

@Getter
public enum JobType implements ByteEnum {
  WARRIOR((byte) 0),
  NINJA((byte) 1),
  SURA((byte) 2),
  SHAMAN((byte) 3);

  private final byte value;

  JobType(byte value) {
    this.value = value;
  }

  public JobConfig getJobConfig() {
    return JobConfigs.get(this);
  }
}
