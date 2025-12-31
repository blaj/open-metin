package com.blaj.openmetin.shared.domain.repository;

import com.blaj.openmetin.shared.domain.entity.ArchiveEntity;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

@NoRepositoryBean
public interface ArchiveEntityRepository<T extends ArchiveEntity> extends JpaRepository<T, Long> {

  @Override
  @Transactional
  default void delete(@NonNull T entity) {
    this.archive(entity);
  }

  @Override
  @Transactional
  default void deleteById(@NonNull Long id) {
    findById(id).ifPresent(this::archive);
  }

  @Override
  @Transactional
  default void deleteAll(@NonNull Iterable<? extends T> entities) {
    entities.forEach(this::archive);
  }

  @Override
  @Transactional
  default void deleteAll() {
    findAll().forEach(this::archive);
  }

  private void archive(@NonNull T entity) {
    entity.setArchived(Boolean.TRUE);
    entity.setArchivedAt(LocalDateTime.now());
  }
}
