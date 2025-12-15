package com.blaj.openmetin.shared.domain.repository;

import com.blaj.openmetin.shared.domain.entity.AuditingEntity;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

@NoRepositoryBean
public interface AuditingEntityRepository<T extends AuditingEntity>
    extends CrudRepository<T, Long> {

  @Override
  @Transactional
  default void delete(@NonNull T entity) {
    this.softDelete(entity);
  }

  @Override
  @Transactional
  default void deleteById(@NonNull Long id) {
    findById(id).ifPresent(this::softDelete);
  }

  @Override
  @Transactional
  default void deleteAll(@NonNull Iterable<? extends T> entities) {
    entities.forEach(this::softDelete);
  }

  @Override
  @Transactional
  default void deleteAll() {
    findAll().forEach(this::softDelete);
  }

  private void softDelete(@NonNull T entity) {
    entity.setDeleted(Boolean.TRUE);
    entity.setDeletedAt(LocalDateTime.now());
  }
}
