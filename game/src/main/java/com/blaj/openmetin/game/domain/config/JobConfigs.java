package com.blaj.openmetin.game.domain.config;

import com.blaj.openmetin.game.domain.enums.character.JobType;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JobConfigs {

  private static final Map<JobType, JobConfig> CONFIGS =
      Map.of(
          JobType.WARRIOR,
          new JobConfig(4, 6, 3, 3, 600, 200, 40, 20, 36, 44),
          JobType.NINJA,
          new JobConfig(3, 4, 6, 3, 650, 200, 40, 20, 36, 44),
          JobType.SURA,
          new JobConfig(3, 5, 3, 5, 650, 200, 40, 20, 36, 44),
          JobType.SHAMAN,
          new JobConfig(4, 3, 3, 6, 700, 200, 40, 20, 36, 44));

  public static JobConfig get(JobType jobType) {
    return CONFIGS.get(jobType);
  }
}
