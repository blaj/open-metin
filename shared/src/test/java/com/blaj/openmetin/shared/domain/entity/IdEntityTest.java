package com.blaj.openmetin.shared.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class IdEntityTest {

  @Test
  public void givenSameId_whenEquals_thenReturnsTrue() {
    // given
    var entity1 = IdEntity.builder().id(1L).build();
    var entity2 = IdEntity.builder().id(1L).build();

    // when
    var result = entity1.equals(entity2);

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void givenDifferentId_whenEquals_thenReturnsFalse() {
    // given
    var entity1 = IdEntity.builder().id(1L).build();
    var entity2 = IdEntity.builder().id(2L).build();

    // when
    var result = entity1.equals(entity2);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenSameInstance_whenEquals_thenReturnsTrue() {
    // given
    var entity = IdEntity.builder().id(1L).build();

    // when
    var result = entity.equals(entity);

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void givenNull_whenEquals_thenReturnsFalse() {
    // given
    var entity = IdEntity.builder().id(1L).build();

    // when
    var result = entity.equals(null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenSameId_whenHashCode_thenReturnsSameHashCode() {
    // given
    var entity1 = IdEntity.builder().id(1L).build();
    var entity2 = IdEntity.builder().id(1L).build();

    // when & then
    assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
  }
}
