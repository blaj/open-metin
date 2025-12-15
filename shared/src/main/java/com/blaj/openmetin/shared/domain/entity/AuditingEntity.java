package com.blaj.openmetin.shared.domain.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@MappedSuperclass
@SQLRestriction("deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditingEntity extends IdEntity {
  @CreatedBy private UUID createdBy;

  @PastOrPresent @NotNull @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();

  @LastModifiedBy private UUID updatedBy;

  @PastOrPresent private LocalDateTime updatedAt;

  private UUID deletedBy;

  private LocalDateTime deletedAt;

  @Builder.Default private Boolean deleted = Boolean.FALSE;
}
