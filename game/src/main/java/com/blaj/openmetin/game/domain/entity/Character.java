package com.blaj.openmetin.game.domain.entity;

import com.blaj.openmetin.game.domain.enums.character.ClassType;
import com.blaj.openmetin.game.domain.enums.character.Empire;
import com.blaj.openmetin.shared.domain.entity.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
  @Column(nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
  private Integer skillGroup = 0;

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
}
