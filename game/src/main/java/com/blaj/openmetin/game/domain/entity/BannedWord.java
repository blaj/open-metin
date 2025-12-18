package com.blaj.openmetin.game.domain.entity;

import com.blaj.openmetin.shared.domain.entity.ArchiveEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(schema = "dictionary", name = "banned_word")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BannedWord extends ArchiveEntity {

  @Column(nullable = false, length = 50)
  private String word;
}
