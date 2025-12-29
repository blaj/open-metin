package com.blaj.openmetin.game.domain.model.spatial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuadTreeTest {

  @Mock private BaseGameEntity entity1;
  @Mock private BaseGameEntity entity2;
  @Mock private BaseGameEntity entity3;

  @Test
  public void givenEntityOutsideArea_whenInsert_thenReturnFalse() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(150);
    given(entity1.getPositionY()).willReturn(150);

    // when
    var result = quadTree.insert(entity1);

    // then
    assertThat(result).isFalse();
    assertThat(quadTree.getEntities()).isEmpty();
  }

  @Test
  public void givenEntityInsideAreaWithCapacity_whenInsert_thenAddToEntities() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);

    // when
    var result = quadTree.insert(entity1);

    // then
    assertThat(result).isTrue();
    assertThat(quadTree.getEntities()).hasSize(1).contains(entity1);

    then(entity1).should().setLastPositionX(50);
    then(entity1).should().setLastPositionY(50);
    then(entity1).should().setLastQuadTree(quadTree);
  }

  @Test
  public void givenEntityInsideAreaNoCapacitySmallSize_whenInsert_thenIncreaseCapacity() {
    // given
    var quadTree = new QuadTree(0, 0, 10, 10, 1);
    given(entity1.getPositionX()).willReturn(5);
    given(entity1.getPositionY()).willReturn(5);
    given(entity2.getPositionX()).willReturn(6);
    given(entity2.getPositionY()).willReturn(6);

    // when
    quadTree.insert(entity1);
    var result = quadTree.insert(entity2);

    // then
    assertThat(result).isTrue();
    assertThat(quadTree.getCapacity()).isEqualTo(2);
    assertThat(quadTree.getEntities()).hasSize(2);
    assertThat(quadTree.isSubdivided()).isFalse();
  }

  @Test
  public void givenEntityInsideAreaNoCapacityLargeSize_whenInsert_thenSubdivide() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 2);
    given(entity1.getPositionX()).willReturn(25);
    given(entity1.getPositionY()).willReturn(25);
    given(entity2.getPositionX()).willReturn(30);
    given(entity2.getPositionY()).willReturn(30);
    given(entity3.getPositionX()).willReturn(35);
    given(entity3.getPositionY()).willReturn(35);

    // when
    quadTree.insert(entity1);
    quadTree.insert(entity2);
    var result = quadTree.insert(entity3);

    // then
    assertThat(result).isTrue();
    assertThat(quadTree.isSubdivided()).isTrue();
    assertThat(quadTree.getEntities()).isEmpty();
    assertThat(quadTree.getNorthWestQuadTree()).isNotNull();
  }

  @Test
  public void givenEntityNotInTree_whenRemove_thenReturnFalse() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);

    // when
    var result = quadTree.remove(entity1);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenEntityInTree_whenRemove_thenRemoveAndReturnTrue() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    quadTree.insert(entity1);

    // when
    var result = quadTree.remove(entity1);

    // then
    assertThat(result).isTrue();
    assertThat(quadTree.getEntities()).isEmpty();

    then(entity1).should().setLastQuadTree(null);
  }

  @Test
  public void givenEntityInSubdividedTree_whenRemove_thenRemoveFromSubtree() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 2);
    given(entity1.getPositionX()).willReturn(25);
    given(entity1.getPositionY()).willReturn(25);
    given(entity2.getPositionX()).willReturn(30);
    given(entity2.getPositionY()).willReturn(30);
    given(entity3.getPositionX()).willReturn(35);
    given(entity3.getPositionY()).willReturn(35);

    quadTree.insert(entity1);
    quadTree.insert(entity2);
    quadTree.insert(entity3);

    // when
    var result = quadTree.remove(entity1);

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void givenCircleNotIntersecting_whenQueryAround_thenReturnEmpty() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    quadTree.insert(entity1);

    var results = new ArrayList<BaseGameEntity>();

    // when
    quadTree.queryAround(results, 200, 200, 10, null);

    // then
    assertThat(results).isEmpty();
  }

  @Test
  public void givenEntitiesWithinRadius_whenQueryAround_thenReturnEntities() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    given(entity2.getPositionX()).willReturn(55);
    given(entity2.getPositionY()).willReturn(55);

    quadTree.insert(entity1);
    quadTree.insert(entity2);

    var results = new ArrayList<BaseGameEntity>();

    // when
    quadTree.queryAround(results, 50, 50, 10, null);

    // then
    assertThat(results).hasSize(2).contains(entity1, entity2);
  }

  @Test
  public void givenEntitiesOutsideRadius_whenQueryAround_thenReturnEmpty() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    quadTree.insert(entity1);

    var results = new ArrayList<BaseGameEntity>();

    // when
    quadTree.queryAround(results, 80, 80, 10, null);

    // then
    assertThat(results).isEmpty();
  }

  @Test
  public void givenFilterType_whenQueryAround_thenReturnOnlyMatchingType() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    given(entity1.getType()).willReturn(EntityType.PLAYER);
    given(entity2.getPositionX()).willReturn(55);
    given(entity2.getPositionY()).willReturn(55);
    given(entity2.getType()).willReturn(EntityType.NPC);

    quadTree.insert(entity1);
    quadTree.insert(entity2);

    var results = new ArrayList<BaseGameEntity>();

    // when
    quadTree.queryAround(results, 50, 50, 20, EntityType.PLAYER);

    // then
    assertThat(results).hasSize(1).contains(entity1);
  }

  @Test
  public void givenSubdividedTree_whenQueryAround_thenSearchSubtrees() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 2);
    given(entity1.getPositionX()).willReturn(25);
    given(entity1.getPositionY()).willReturn(25);
    given(entity2.getPositionX()).willReturn(30);
    given(entity2.getPositionY()).willReturn(30);
    given(entity3.getPositionX()).willReturn(35);
    given(entity3.getPositionY()).willReturn(35);

    quadTree.insert(entity1);
    quadTree.insert(entity2);
    quadTree.insert(entity3);

    var results = new ArrayList<BaseGameEntity>();

    // when
    quadTree.queryAround(results, 30, 30, 20, null);

    // then
    assertThat(results).contains(entity1, entity2, entity3);
  }

  @Test
  public void givenEntityWithoutLastQuadTree_whenUpdatePosition_thenInsert() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    given(entity1.getLastQuadTree()).willReturn(null);

    // when
    quadTree.updatePosition(entity1);

    // then
    assertThat(quadTree.getEntities()).hasSize(1).contains(entity1);
  }

  @Test
  public void givenEntityStillInSameArea_whenUpdatePosition_thenDoNothing() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    quadTree.insert(entity1);

    given(entity1.getLastQuadTree()).willReturn(quadTree);
    given(entity1.getPositionX()).willReturn(55);
    given(entity1.getPositionY()).willReturn(55);

    // when
    quadTree.updatePosition(entity1);

    // then
    assertThat(quadTree.getEntities()).hasSize(1).contains(entity1);
  }

  @Test
  public void givenEntityMovedOutsideArea_whenUpdatePosition_thenRemoveAndReinsert() {
    // given
    var quadTree = new QuadTree(0, 0, 100, 100, 10);
    given(entity1.getPositionX()).willReturn(50);
    given(entity1.getPositionY()).willReturn(50);
    quadTree.insert(entity1);

    given(entity1.getLastQuadTree()).willReturn(quadTree);
    given(entity1.getPositionX()).willReturn(150);
    given(entity1.getPositionY()).willReturn(150);

    // when
    quadTree.updatePosition(entity1);

    // then
    assertThat(quadTree.getEntities()).isEmpty();
  }
}
