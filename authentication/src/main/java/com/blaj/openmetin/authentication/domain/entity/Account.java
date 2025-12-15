package com.blaj.openmetin.authentication.domain.entity;

import com.blaj.openmetin.shared.domain.entity.AuditingEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(schema = "account", name = "account")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends AuditingEntity {

  @NotNull
  @Length(max = 30)
  private String username;

  @NotNull
  @Length(max = 100)
  private String password;

  @NotNull
  @Length(min = 3, max = 200)
  private String email;

  private LocalDateTime lastLoginAt;

  @NotNull
  @Length(max = 7)
  private String deleteCode;
}
