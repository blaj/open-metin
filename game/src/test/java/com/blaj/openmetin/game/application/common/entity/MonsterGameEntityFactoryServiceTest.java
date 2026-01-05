package com.blaj.openmetin.game.application.common.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.game.application.common.game.GameEntityVidAllocator;
import com.blaj.openmetin.game.application.common.monster.MonsterDefinitionService;
import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.entity.MonsterDefinition.SkillData;
import com.blaj.openmetin.game.domain.enums.character.Empire;
import com.blaj.openmetin.game.domain.enums.common.ClickType;
import com.blaj.openmetin.game.domain.enums.common.ImmuneType;
import com.blaj.openmetin.game.domain.enums.entity.AiFlag;
import com.blaj.openmetin.game.domain.enums.entity.BattleType;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.game.domain.enums.monster.MonsterEnchantType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterRaceType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterRankType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterResistType;
import com.blaj.openmetin.game.domain.enums.monster.MonsterSize;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointDirection;
import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSectree;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.domain.model.map.TownCoordinates;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MonsterGameEntityFactoryServiceTest {

  private MonsterGameEntityFactoryService monsterGameEntityFactoryService;

  @Mock private GameEntityVidAllocator gameEntityVidAllocator;
  @Mock private MonsterDefinitionService monsterDefinitionService;

  @BeforeEach
  public void beforeEach() {
    monsterGameEntityFactoryService =
        new MonsterGameEntityFactoryService(gameEntityVidAllocator, monsterDefinitionService);
  }

  @Test
  public void givenValidMonsterId_whenCreate_thenReturnMonsterEntity() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.create(monsterId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getVid()).isEqualTo(1L);
    assertThat(result.getEntityClass()).isEqualTo(monsterId);
    assertThat(result.getMonsterDefinition()).isEqualTo(monsterDefinition);
    assertThat(result.getHealth()).isEqualTo(monsterDefinition.getHealth());
    assertThat(result.getMovementSpeed()).isEqualTo((short) monsterDefinition.getMovementSpeed());
  }

  @Test
  public void givenInvalidMonsterId_whenCreate_thenReturnNull() {
    // given
    var monsterId = 999L;
    given(monsterDefinitionService.getMonsterDefinition(monsterId)).willReturn(Optional.empty());

    // when
    var result = monsterGameEntityFactoryService.create(monsterId);

    // then
    assertThat(result).isNull();
  }

  @Test
  public void givenValidMonsterAndSpawnPoint_whenCreateForSpawn_thenReturnMonsterWithPosition() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition();
    var spawnPoint =
        SpawnPoint.builder()
            .x(100)
            .y(200)
            .rangeX(0)
            .rangeY(0)
            .spawnPointDirection(SpawnPointDirection.NORTH)
            .build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getPositionX()).isNotZero();
    assertThat(result.getPositionY()).isNotZero();
    assertThat(result.getRotation()).isBetween(0f, 360f);
  }

  @Test
  public void givenInvalidMonsterId_whenCreateForSpawn_thenReturnNull() {
    // given
    var monsterId = 999L;
    var spawnPoint = SpawnPoint.builder().x(100).y(200).rangeX(0).rangeY(0).build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId)).willReturn(Optional.empty());

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNull();
  }

  @Test
  public void givenNoValidPositionAfterMaxAttempts_whenCreateForSpawn_thenReturnNull() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition();
    monsterDefinition.setAiFlags(EnumSet.of(AiFlag.NO_MOVE));
    var spawnPoint =
        SpawnPoint.builder()
            .x(0)
            .y(0)
            .rangeX(0)
            .rangeY(0)
            .spawnPointDirection(SpawnPointDirection.NORTH)
            .build();

    var map = map();
    var mapAttributeSet = createFullyBlockedMapAttributeSet();
    map.setMapAttributeSet(mapAttributeSet);

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNull();
  }

  @Test
  public void givenMonsterWithNoMoveFlag_whenCreateForSpawn_thenSetRotationFromDirection() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition(EnumSet.of(AiFlag.NO_MOVE));
    var spawnPoint =
        SpawnPoint.builder()
            .x(100)
            .y(200)
            .rangeX(0)
            .rangeY(0)
            .spawnPointDirection(SpawnPointDirection.NORTH)
            .build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getRotation())
        .isEqualTo(Map.SPAWN_ROTATION_SLICE_DEGREES * SpawnPointDirection.NORTH.ordinal());
  }

  @Test
  public void givenNpcType_whenCreateForSpawn_thenIgnoreMapAttributeCheck() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition(EntityType.NPC);
    var spawnPoint = SpawnPoint.builder().x(100).y(200).rangeX(0).rangeY(0).build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
  }

  @Test
  public void givenWarpType_whenCreateForSpawn_thenIgnoreMapAttributeCheck() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition(EntityType.WARP);
    var spawnPoint = SpawnPoint.builder().x(100).y(200).rangeX(0).rangeY(0).build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
  }

  @Test
  public void givenGotoType_whenCreateForSpawn_thenIgnoreMapAttributeCheck() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition(EntityType.GOTO);
    var spawnPoint = SpawnPoint.builder().x(100).y(200).rangeX(0).rangeY(0).build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
  }

  @Test
  public void givenSpawnPointWithRange_whenCreateForSpawn_thenRandomizePositionWithinRange() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition();
    var spawnPoint = SpawnPoint.builder().x(100).y(200).rangeX(10).rangeY(10).build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getPositionX()).isNotZero();
    assertThat(result.getPositionY()).isNotZero();
  }

  @Test
  public void givenMonsterWithNoMoveAndRandomDirection_whenCreateForSpawn_thenSetRandomRotation() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition(EnumSet.of(AiFlag.NO_MOVE));
    var spawnPoint =
        SpawnPoint.builder()
            .x(100)
            .y(200)
            .rangeX(0)
            .rangeY(0)
            .spawnPointDirection(SpawnPointDirection.RANDOM)
            .build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getRotation()).isBetween(0f, 360f);
  }

  @Test
  public void givenMultipleAttempts_whenCreateForSpawn_thenFindValidPosition() {
    // given
    var monsterId = 101L;
    var monsterDefinition = monsterDefinition();
    var spawnPoint = SpawnPoint.builder().x(100).y(200).rangeX(5).rangeY(5).build();
    var map = map();

    given(monsterDefinitionService.getMonsterDefinition(monsterId))
        .willReturn(Optional.of(monsterDefinition));
    given(gameEntityVidAllocator.allocate()).willReturn(1);

    // when
    var result = monsterGameEntityFactoryService.createForSpawn(monsterId, spawnPoint, map);

    // then
    assertThat(result).isNotNull();
  }

  private MonsterDefinition monsterDefinition() {
    return MonsterDefinition.builder()
        .id(123L)
        .name("name")
        .translatedName("translatedName")
        .type(EntityType.MONSTER)
        .rankType(MonsterRankType.KNIGHT)
        .battleType(BattleType.MELEE)
        .level((short) 20)
        .size(MonsterSize.MEDIUM)
        .minGold(100L)
        .maxGold(200L)
        .experience(300L)
        .health(400L)
        .regenDelay((short) 20)
        .regenPercentage((short) 30)
        .defence(40)
        .aiFlags(EnumSet.of(AiFlag.AGGRESSIVE))
        .raceTypes(EnumSet.of(MonsterRaceType.ATT_ICE))
        .immuneTypes(EnumSet.of(ImmuneType.POISON))
        .st((short) 1)
        .ht((short) 2)
        .dx((short) 3)
        .iq((short) 4)
        .minDamage(100L)
        .maxDamage(200L)
        .attackSpeed(50)
        .movementSpeed(60)
        .aggressivePoint((short) 1)
        .aggressiveSight(20L)
        .attackRange(50L)
        .enchantTypes(EnumSet.of(MonsterEnchantType.POISON))
        .resistTypes(EnumSet.of(MonsterResistType.BELL))
        .resurrectionId(111L)
        .dropItemId(222L)
        .onClickType(ClickType.TALK)
        .empire(Empire.SHINSOO)
        .folder("/folder")
        .damageMultiply(1.5)
        .summonId(333L)
        .drainSp(666L)
        .monsterColor(444L)
        .polymorphItemId(555L)
        .skillDatas(List.of(SkillData.builder().id(777L).level((short) 55).build()))
        .berserkPoint((short) 10)
        .stoneSkinPoint((short) 11)
        .godSpeedPoint((short) 12)
        .deathBlowPoint((short) 13)
        .revivePoint((short) 14)
        .build();
  }

  private MonsterDefinition monsterDefinition(EntityType entityType) {
    var monsterDefinition = monsterDefinition();
    monsterDefinition.setType(entityType);
    return monsterDefinition;
  }

  private MonsterDefinition monsterDefinition(EnumSet<AiFlag> aiFlags) {
    var monsterDefinition = monsterDefinition();
    monsterDefinition.setAiFlags(aiFlags);
    return monsterDefinition;
  }

  private Map map() {
    return new Map(
        "testmap",
        new Coordinates(1000, 2000),
        100,
        100,
        TownCoordinates.allOf(new Coordinates(50, 50)));
  }

  private MapAttributeSet createFullyBlockedMapAttributeSet() {
    var cellAttributes = new int[MapAttributeSectree.CELLS_PER_SECTREE];
    Arrays.fill(cellAttributes, MapAttribute.BLOCK.getValue());

    var sectree = new MapAttributeSectree(cellAttributes);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;

    return new MapAttributeSet(1, 1, new Coordinates(0, 0), sectrees);
  }
}
