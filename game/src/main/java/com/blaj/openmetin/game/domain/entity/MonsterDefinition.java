package com.blaj.openmetin.game.domain.entity;

import com.blaj.openmetin.game.domain.enums.character.Empire;
import com.blaj.openmetin.game.domain.enums.common.ClickType;
import com.blaj.openmetin.game.domain.enums.common.ImmuneType;
import com.blaj.openmetin.game.domain.enums.entity.AiFlag;
import com.blaj.openmetin.game.domain.enums.entity.BattleType;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterEnchantType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterRaceType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterRankType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterResistType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterSize;
import com.blaj.openmetin.game.infrastructure.converter.AiFlagEnumSetAttributeConverter;
import com.blaj.openmetin.game.infrastructure.converter.ImmuneTypeEnumSetAttributeConverter;
import com.blaj.openmetin.game.infrastructure.converter.MonsterEnchantTypeEnumSetAttributeConverter;
import com.blaj.openmetin.game.infrastructure.converter.MonsterRaceTypeEnumSetAttributeConverter;
import com.blaj.openmetin.game.infrastructure.converter.MonsterResistTypeEnumSetAttributeConverter;
import com.blaj.openmetin.shared.domain.entity.ArchiveEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.EnumSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.NonNull;

@Entity
@Table(schema = "dictionary", name = "monster_definition")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterDefinition extends ArchiveEntity {

  @NonNull
  @NotBlank
  @Size(max = 25)
  @Column(nullable = false, length = 25)
  private String name;

  @NonNull
  @NotBlank
  @Size(max = 25)
  @Column(nullable = false, length = 25)
  private String translatedName;

  @NonNull
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private EntityType type;

  @NonNull
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private MonsterRankType rankType;

  @NonNull
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private BattleType battleType;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("1")
  private short level = 1;

  @NonNull
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private MonsterSize size;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long minGold = 0L;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long maxGold = 0L;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long experience = 0L;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("1")
  private long health = 1L;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short regenDelay = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short regenPercentage = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private int defence = 0;

  @NonNull
  @NotNull
  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  @ColumnDefault("'[]'::jsonb")
  @Convert(converter = AiFlagEnumSetAttributeConverter.class)
  private EnumSet<AiFlag> aiFlags = EnumSet.noneOf(AiFlag.class);

  @NonNull
  @NotNull
  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  @ColumnDefault("'[]'::jsonb")
  @Convert(converter = MonsterRaceTypeEnumSetAttributeConverter.class)
  private EnumSet<MonsterRaceType> raceTypes = EnumSet.noneOf(MonsterRaceType.class);

  @NonNull
  @NotNull
  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  @ColumnDefault("'[]'::jsonb")
  @Convert(converter = ImmuneTypeEnumSetAttributeConverter.class)
  private EnumSet<ImmuneType> immuneTypes = EnumSet.noneOf(ImmuneType.class);

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short st = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short ht = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short dx = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short iq = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long minDamage = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long maxDamage = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private int attackSpeed = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private int movementSpeed = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short aggressivePoint = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long aggressiveSight = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long attackRange = 0;

  @NonNull
  @NotNull
  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  @ColumnDefault("'[]'::jsonb")
  @Convert(converter = MonsterEnchantTypeEnumSetAttributeConverter.class)
  private EnumSet<MonsterEnchantType> enchantTypes = EnumSet.noneOf(MonsterEnchantType.class);

  @NonNull
  @NotNull
  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  @ColumnDefault("'[]'::jsonb")
  @Convert(converter = MonsterResistTypeEnumSetAttributeConverter.class)
  private EnumSet<MonsterResistType> resistTypes = EnumSet.noneOf(MonsterResistType.class);

  @Column private Long resurrectionId;

  @Column private Long dropItemId;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short mountCapacity = 0;

  @NonNull
  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  @ColumnDefault("'NONE'")
  private ClickType onClickType = ClickType.NONE;

  @NonNull
  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  @ColumnDefault("'NEUTRAL'")
  private Empire empire = Empire.NEUTRAL;

  @NonNull
  @NotBlank
  @Size(max = 65)
  @Column(nullable = false, length = 65)
  private String folder;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("1.0")
  private double damageMultiply = 1.0;

  @Column private Long summonId;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long drainSp = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private long monsterColor = 0;

  @Column private Long polymorphItemId;

  @NonNull
  @NotNull
  @Singular
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private List<SkillData> skillDatas;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short berserkPoint = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short stoneSkinPoint = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short godSpeedPoint = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short deathBlowPoint = 0;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private short revivePoint = 0;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SkillData {

    private long id;

    private short level;
  }
}
