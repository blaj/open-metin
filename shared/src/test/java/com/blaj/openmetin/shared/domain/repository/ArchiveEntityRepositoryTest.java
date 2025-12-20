package com.blaj.openmetin.shared.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.shared.domain.entity.ArchiveEntity;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = ArchiveEntityRepositoryTest.TestConfig.class)
public class ArchiveEntityRepositoryTest {

  @Autowired private TestEntityRepository testEntityRepository;

  @Repository
  public interface TestEntityRepository extends ArchiveEntityRepository<TestEntity> {}

  @Test
  public void givenEntity_whenDelete_thenArchive() {
    // given
    var entity = testEntityRepository.save(
        TestEntity.builder().name("name").build());

    // when
    testEntityRepository.delete(entity);

    // then
    var result = testEntityRepository.findById(entity.getId());
    assertThat(result).isPresent();
    assertThat(result.get().getArchived()).isTrue();
    assertThat(result.get().getArchivedAt()).isNotNull();
  }

  @Test
  public void givenEntityId_whenDeleteById_thenArchive() {
    // given
    var entity = testEntityRepository.save(
        TestEntity.builder().name("name").build());
    var entityId = entity.getId();

    // when
    testEntityRepository.deleteById(entityId);

    // then
    var result = testEntityRepository.findById(entityId);
    assertThat(result).isPresent();
    assertThat(result.get().getArchived()).isTrue();
    assertThat(result.get().getArchivedAt()).isNotNull();
  }

  @Test
  public void givenMultipleEntities_whenDeleteAll_thenArchiveAll() {
    // given
    var entity1 = testEntityRepository.save(
        TestEntity.builder().name("name").build());
    var entity2 = testEntityRepository.save(
        TestEntity.builder().name("name").build());

    // when
    testEntityRepository.deleteAll(List.of(entity1, entity2));

    // then
    var result1 = testEntityRepository.findById(entity1.getId());
    var result2 = testEntityRepository.findById(entity2.getId());
    assertThat(result1).isPresent();
    assertThat(result1.get().getArchived()).isTrue();
    assertThat(result2).isPresent();
    assertThat(result2.get().getArchived()).isTrue();
  }

  @Test
  public void givenEntities_whenDeleteAll_thenArchiveAll() {
    // given
    var entity1 = testEntityRepository.save(
        TestEntity.builder().name("name").build());
    var entity2 = testEntityRepository.save(
        TestEntity.builder().name("name").build());

    // when
    testEntityRepository.deleteAll();

    // then
    var allEntities = testEntityRepository.findAll();
    assertThat(allEntities).isNotEmpty();
    assertThat(allEntities).extracting(ArchiveEntity::getArchived).contains(true, true);
  }

  @Entity
  @Getter
  @Setter
  @ToString(onlyExplicitlyIncluded = true)
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestEntity extends ArchiveEntity {
    private String name;
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableJpaRepositories(considerNestedRepositories = true)
  public static class TestConfig {}
}
