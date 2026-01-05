package com.blaj.openmetin.game.infrastructure.service.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.game.application.common.entity.MonsterGameEntityFactoryService;
import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.entity.MonsterDefinition.SkillData;
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
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity;
import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.TownCoordinates;
import com.blaj.openmetin.game.domain.model.spawn.SpawnGroup;
import com.blaj.openmetin.game.domain.model.spawn.SpawnGroupCollection;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProcessMapSpawnPointsServiceTest {

  private ProcessMapSpawnPointsService processMapSpawnPointsService;

  @Mock private MonsterGameEntityFactoryService monsterGameEntityFactoryService;

  @Mock private GameWorldService gameWorldService;

  @Captor private ArgumentCaptor<MonsterGameEntity> monsterGameEntityArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    processMapSpawnPointsService =
        new ProcessMapSpawnPointsService(monsterGameEntityFactoryService);
  }

  @Test
  public void givenNoSpawnPoints_whenProcess_thenDoNothing() {
    // given
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(monsterGameEntityFactoryService).should(never()).createForSpawn(anyLong(), any(), any());
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenMonsterSpawnPoint_whenProcess_thenSpawnMonster() {
    // given
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.MONSTER).monsterId(101L).x(100).y(200).build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var monsterDefinition = monsterDefinition();
    var monsterGameEntity =
        MonsterGameEntity.builder().vid(1L).monsterDefinition(monsterDefinition).build();

    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(monsterGameEntity);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should().spawnEntity(monsterGameEntity);
  }

  @Test
  public void givenMonsterSpawnPointWithNullEntity_whenProcess_thenDoNotSpawn() {
    // given
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.MONSTER).monsterId(101L).x(100).y(200).build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);

    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(null);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(monsterGameEntityFactoryService)
        .should()
        .createForSpawn(eq(101L), eq(spawnPoint), eq(map));
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupSpawnPoint_whenProcess_thenSpawnGroup() {
    // given
    var spawnGroup = new SpawnGroup(1001L, "TestGroup", 101L, List.of(201L, 202L));
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.GROUP).monsterId(1001L).x(100).y(200).build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var monsterDefinition = monsterDefinition();
    var leaderEntity =
        MonsterGameEntity.builder().vid(1L).monsterDefinition(monsterDefinition).build();
    var member1Entity =
        MonsterGameEntity.builder().vid(2L).monsterDefinition(monsterDefinition).build();
    var member2Entity =
        MonsterGameEntity.builder().vid(3L).monsterDefinition(monsterDefinition).build();

    given(gameWorldService.getGroup(1001L)).willReturn(Optional.of(spawnGroup));
    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(leaderEntity);
    given(monsterGameEntityFactoryService.createForSpawn(eq(201L), eq(spawnPoint), eq(map)))
        .willReturn(member1Entity);
    given(monsterGameEntityFactoryService.createForSpawn(eq(202L), eq(spawnPoint), eq(map)))
        .willReturn(member2Entity);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should().spawnEntity(leaderEntity);
    then(gameWorldService).should().spawnEntity(member1Entity);
    then(gameWorldService).should().spawnEntity(member2Entity);
    then(gameWorldService).should(times(3)).spawnEntity(any());
  }

  @Test
  public void givenGroupSpawnPointWithGroupNotFound_whenProcess_thenDoNotSpawn() {
    // given
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.GROUP).monsterId(1001L).x(100).y(200).build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    given(gameWorldService.getGroup(1001L)).willReturn(Optional.empty());

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupSpawnPointWithNullLeader_whenProcess_thenDoNotSpawnGroup() {
    // given
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.GROUP).monsterId(1001L).x(100).y(200).build();
    var spawnGroup = new SpawnGroup(1001L, "TestGroup", 101L, List.of(201L));
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);

    given(gameWorldService.getGroup(1001L)).willReturn(Optional.of(spawnGroup));
    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(null);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupSpawnPointWithNullMember_whenProcess_thenContinueWithOtherMembers() {
    // given
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.GROUP).monsterId(1001L).x(100).y(200).build();
    var spawnGroup = new SpawnGroup(1001L, "TestGroup", 101L, List.of(201L, 202L));
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var monsterDefinition = monsterDefinition();
    var leaderEntity =
        MonsterGameEntity.builder().vid(1L).monsterDefinition(monsterDefinition).build();
    var memberEntity =
        MonsterGameEntity.builder().vid(2L).monsterDefinition(monsterDefinition).build();

    given(gameWorldService.getGroup(1001L)).willReturn(Optional.of(spawnGroup));
    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(leaderEntity);
    given(monsterGameEntityFactoryService.createForSpawn(eq(201L), eq(spawnPoint), eq(map)))
        .willReturn(null); // First member fails
    given(monsterGameEntityFactoryService.createForSpawn(eq(202L), eq(spawnPoint), eq(map)))
        .willReturn(memberEntity);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should().spawnEntity(leaderEntity);
    then(gameWorldService).should().spawnEntity(memberEntity);
    then(gameWorldService).should(times(2)).spawnEntity(any());
  }

  @Test
  public void givenGroupCollectionSpawnPoint_whenProcess_thenSpawnGroupFromCollection() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.GROUP_COLLECTION)
            .monsterId(2001L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var spawnGroupCollection = new SpawnGroupCollection.Entry(1001L, 1.0f);
    var groupCollection =
        new SpawnGroupCollection(2001L, "TestCollection", List.of(spawnGroupCollection));
    var spawnGroup = new SpawnGroup(1001L, "TestGroup", 101L, List.of(201L));
    var monsterDefinition = monsterDefinition();
    var leaderEntity =
        MonsterGameEntity.builder().vid(1L).monsterDefinition(monsterDefinition).build();
    var memberEntity =
        MonsterGameEntity.builder().vid(2L).monsterDefinition(monsterDefinition).build();

    given(gameWorldService.getGroupCollection(2001L)).willReturn(Optional.of(groupCollection));
    given(gameWorldService.getGroup(1001L)).willReturn(Optional.of(spawnGroup));
    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(leaderEntity);
    given(monsterGameEntityFactoryService.createForSpawn(eq(201L), eq(spawnPoint), eq(map)))
        .willReturn(memberEntity);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should().spawnEntity(leaderEntity);
    then(gameWorldService).should().spawnEntity(memberEntity);
  }

  @Test
  public void givenGroupCollectionSpawnPointWithCollectionNotFound_whenProcess_thenDoNotSpawn() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.GROUP_COLLECTION)
            .monsterId(2001L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    given(gameWorldService.getGroupCollection(2001L)).willReturn(Optional.empty());

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupCollectionSpawnPointWithEmptyEntries_whenProcess_thenDoNotSpawn() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.GROUP_COLLECTION)
            .monsterId(2001L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var groupCollection = new SpawnGroupCollection(2001L, "TestCollection", List.of());

    given(gameWorldService.getGroupCollection(2001L)).willReturn(Optional.of(groupCollection));

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupCollectionSpawnPointWithGroupNotFound_whenProcess_thenDoNotSpawn() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.GROUP_COLLECTION)
            .monsterId(2001L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var spawnGroupCollection = new SpawnGroupCollection.Entry(1001L, 1.0f);
    var groupCollection =
        new SpawnGroupCollection(2001L, "TestCollection", List.of(spawnGroupCollection));

    given(gameWorldService.getGroupCollection(2001L)).willReturn(Optional.of(groupCollection));
    given(gameWorldService.getGroup(1001L)).willReturn(Optional.empty());

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupCollectionWithZeroProbability_whenProcess_thenDoNotSpawn() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.GROUP_COLLECTION)
            .monsterId(2001L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var spawnGroupCollection = new SpawnGroupCollection.Entry(1001L, 0.0f); // 0% probability
    var groupCollection =
        new SpawnGroupCollection(2001L, "TestCollection", List.of(spawnGroupCollection));
    var spawnGroup = new SpawnGroup(1001L, "TestGroup", 101L, List.of());

    given(gameWorldService.getGroupCollection(2001L)).willReturn(Optional.of(groupCollection));
    given(gameWorldService.getGroup(1001L)).willReturn(Optional.of(spawnGroup));

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenUnknownSpawnPointType_whenProcess_thenDoNothing() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.SPECIAL) // Unknown type not in strategy map
            .monsterId(101L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(monsterGameEntityFactoryService).should(never()).createForSpawn(anyLong(), any(), any());
    then(gameWorldService).should(never()).spawnEntity(any());
  }

  @Test
  public void givenGroupWithNoMembers_whenProcess_thenSpawnOnlyLeader() {
    // given
    var spawnPoint =
        SpawnPoint.builder().type(SpawnPointType.GROUP).monsterId(1001L).x(100).y(200).build();
    var spawnGroup = new SpawnGroup(1001L, "TestGroup", 101L, List.of()); // No members
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);

    var monsterDefinition = monsterDefinition();
    var leaderEntity =
        MonsterGameEntity.builder().vid(1L).monsterDefinition(monsterDefinition).build();

    given(gameWorldService.getGroup(1001L)).willReturn(Optional.of(spawnGroup));
    given(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .willReturn(leaderEntity);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should().spawnEntity(leaderEntity);
    then(gameWorldService).should(times(1)).spawnEntity(any());
  }

  @Test
  public void givenGroupCollectionWithMultipleEntries_whenProcess_thenSelectOneRandomly() {
    // given
    var spawnPoint =
        SpawnPoint.builder()
            .type(SpawnPointType.GROUP_COLLECTION)
            .monsterId(2001L)
            .x(100)
            .y(200)
            .build();
    var map =
        new Map(
            "name",
            new Coordinates(10, 20),
            10,
            20,
            TownCoordinates.allOf(new Coordinates(15, 25)));
    map.getSpawnPoints().add(spawnPoint);
    var entry1 = new SpawnGroupCollection.Entry(1001L, 1.0f);
    var entry2 = new SpawnGroupCollection.Entry(1002L, 1.0f);
    var groupCollection =
        new SpawnGroupCollection(2001L, "TestCollection", List.of(entry1, entry2));
    var spawnGroup1 = new SpawnGroup(1001L, "Group1", 101L, List.of());
    var spawnGroup2 = new SpawnGroup(1002L, "Group2", 102L, List.of());
    var monsterDefinition = monsterDefinition();
    var leader1Entity =
        MonsterGameEntity.builder().vid(1L).monsterDefinition(monsterDefinition).build();
    var leader2Entity =
        MonsterGameEntity.builder().vid(2L).monsterDefinition(monsterDefinition).build();

    given(gameWorldService.getGroupCollection(2001L)).willReturn(Optional.of(groupCollection));

    lenient().when(gameWorldService.getGroup(1001L)).thenReturn(Optional.of(spawnGroup1));
    lenient().when(gameWorldService.getGroup(1002L)).thenReturn(Optional.of(spawnGroup2));
    lenient()
        .when(monsterGameEntityFactoryService.createForSpawn(eq(101L), eq(spawnPoint), eq(map)))
        .thenReturn(leader1Entity);
    lenient()
        .when(monsterGameEntityFactoryService.createForSpawn(eq(102L), eq(spawnPoint), eq(map)))
        .thenReturn(leader2Entity);

    // when
    processMapSpawnPointsService.process(map, gameWorldService);

    // then
    then(gameWorldService).should(times(1)).spawnEntity(monsterGameEntityArgumentCaptor.capture());

    var spawnedEntity = monsterGameEntityArgumentCaptor.getValue();
    assertThat(spawnedEntity).isIn(leader1Entity, leader2Entity);
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
}
