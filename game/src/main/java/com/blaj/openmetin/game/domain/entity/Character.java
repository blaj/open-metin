package com.blaj.openmetin.game.domain.entity;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import com.blaj.openmetin.game.domain.enums.JobType;
import com.blaj.openmetin.shared.domain.entity.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(schema = "character", name = "character")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Character extends AuditingEntity {

  @Column(nullable = false, length = 24)
  private String name;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer slot = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer basePart = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer bodyPart = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer hairPart = 0;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Empire empire = Empire.NEUTRAL;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClassType classType;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 1")
  private Integer level = 1;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer experience = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  private Long health = 0L;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  private Long mana = 0L;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  private Long stamina = 0L;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer st = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer ht = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer dx = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer iq = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer givenStatusPoints = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer availableStatusPoints = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer availableSkillPoints = 0;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
  private Integer gold = 0;

  @Column(name = "position_x", nullable = false, columnDefinition = "INTEGER")
  private Integer positionX;

  @Column(name = "position_y", nullable = false, columnDefinition = "INTEGER")
  private Integer positionY;

  @Builder.Default
  @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  private Long playTime = 0L;

  @Column(nullable = false, columnDefinition = "BIGINT")
  private Long accountId;

  public enum Empire implements ByteEnum {
    NEUTRAL((byte) 0),
    SHINSOO((byte) 1),
    CHUNJO((byte) 2),
    JINNO((byte) 3);

    private final byte value;

    Empire(byte value) {
      this.value = value;
    }

    @Override
    public byte getValue() {
      return value;
    }
  }

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
}
