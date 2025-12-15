package com.blaj.openmetin.shared.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

@MappedSuperclass
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  private Long id;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }

    var entityId = (IdEntity) o;

    return Objects.equals(id, entityId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
