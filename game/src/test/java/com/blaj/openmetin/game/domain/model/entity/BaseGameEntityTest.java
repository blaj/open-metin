package com.blaj.openmetin.game.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BaseGameEntityTest {
  private TestGameEntity entity;

  @BeforeEach
  public void setUp() {
    entity =
        TestGameEntity.builder()
            .vid(1L)
            .positionX(1000)
            .positionY(2000)
            .positionChanged(false)
            .build();
  }

  @Test
  public void givenSamePositionX_whenSetPositionX_thenPositionChangedRemainsFalse() {
    // given
    var currentPositionX = entity.getPositionX();

    // when
    entity.setPositionX(currentPositionX);

    // then
    assertThat(entity.getPositionX()).isEqualTo(currentPositionX);
    assertThat(entity.isPositionChanged()).isFalse();
  }

  @Test
  public void givenDifferentPositionX_whenSetPositionX_thenPositionChangedBecomesTrue() {
    // given
    var newPositionX = 5000;

    // when
    entity.setPositionX(newPositionX);

    // then
    assertThat(entity.getPositionX()).isEqualTo(newPositionX);
    assertThat(entity.isPositionChanged()).isTrue();
  }

  @Test
  public void givenPositionChangedAlreadyTrue_whenSetPositionX_thenPositionChangedRemainsTrue() {
    // given
    entity.setPositionX(5000);
    var anotherPositionX = 6000;

    // when
    entity.setPositionX(anotherPositionX);

    // then
    assertThat(entity.getPositionX()).isEqualTo(anotherPositionX);
    assertThat(entity.isPositionChanged()).isTrue();
  }

  @Test
  public void givenSamePositionY_whenSetPositionY_thenPositionChangedRemainsFalse() {
    // given
    var currentPositionY = entity.getPositionY();

    // when
    entity.setPositionY(currentPositionY);

    // then
    assertThat(entity.getPositionY()).isEqualTo(currentPositionY);
    assertThat(entity.isPositionChanged()).isFalse();
  }

  @Test
  public void givenDifferentPositionY_whenSetPositionY_thenPositionChangedBecomesTrue() {
    // given
    var newPositionY = 5000;

    // when
    entity.setPositionY(newPositionY);

    // then
    assertThat(entity.getPositionY()).isEqualTo(newPositionY);
    assertThat(entity.isPositionChanged()).isTrue();
  }

  @Test
  public void givenPositionChangedAlreadyTrue_whenSetPositionY_thenPositionChangedRemainsTrue() {
    // given
    entity.setPositionY(5000);
    var anotherPositionY = 6000;

    // when
    entity.setPositionY(anotherPositionY);

    // then
    assertThat(entity.getPositionY()).isEqualTo(anotherPositionY);
    assertThat(entity.isPositionChanged()).isTrue();
  }

  @Test
  public void givenNewEntity_whenAddNearbyEntity_thenEntityIsAdded() {
    // given
    var nearbyEntity = TestGameEntity.builder().vid(2L).build();

    // when
    entity.addNearbyEntity(nearbyEntity);

    // then
    assertThat(entity.getNearbyEntities()).containsExactly(nearbyEntity);
  }

  @Test
  public void givenMultipleEntities_whenAddNearbyEntity_thenAllEntitiesAreAdded() {
    // given
    var entity1 = TestGameEntity.builder().vid(2L).build();
    var entity2 = TestGameEntity.builder().vid(3L).build();

    // when
    entity.addNearbyEntity(entity1);
    entity.addNearbyEntity(entity2);

    // then
    assertThat(entity.getNearbyEntities()).containsExactlyInAnyOrder(entity1, entity2);
  }

  @Test
  public void givenExistingEntity_whenRemoveNearbyEntity_thenEntityIsRemoved() {
    // given
    var nearbyEntity = TestGameEntity.builder().vid(2L).build();
    entity.addNearbyEntity(nearbyEntity);

    // when
    entity.removeNearbyEntity(nearbyEntity);

    // then
    assertThat(entity.getNearbyEntities()).isEmpty();
  }

  @Test
  public void givenNonExistingEntity_whenRemoveNearbyEntity_thenNothingHappens() {
    // given
    var entity1 = TestGameEntity.builder().vid(2L).build();
    var entity2 = TestGameEntity.builder().vid(3L).build();
    entity.addNearbyEntity(entity1);

    // when
    entity.removeNearbyEntity(entity2);

    // then
    assertThat(entity.getNearbyEntities()).containsExactly(entity1);
  }

  @Test
  public void givenEntityPosition_whenGetCoordinates_thenReturnsCorrectCoordinates() {
    // given
    var expectedX = 1000;
    var expectedY = 2000;

    // when
    var coordinates = entity.getCoordinates();

    // then
    assertThat(coordinates.x()).isEqualTo(expectedX);
    assertThat(coordinates.y()).isEqualTo(expectedY);
  }

  @Test
  public void givenChangedPosition_whenGetCoordinates_thenReturnsUpdatedCoordinates() {
    // given
    entity.setPositionX(3000);
    entity.setPositionY(4000);

    // when
    var coordinates = entity.getCoordinates();

    // then
    assertThat(coordinates.x()).isEqualTo(3000);
    assertThat(coordinates.y()).isEqualTo(4000);
  }

  @Getter
  @Setter
  @SuperBuilder
  private static class TestGameEntity extends BaseGameEntity {

    @Override
    public EntityType getType() {
      return EntityType.PLAYER;
    }
  }
}
