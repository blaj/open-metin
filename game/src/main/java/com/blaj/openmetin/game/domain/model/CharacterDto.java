package com.blaj.openmetin.game.domain.model;

import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CharacterDto {

  private Long id;

  private String name;

  private Integer slot;

  private Integer bodyPart;

  private Integer hairPart;

  private Empire empire;

  private ClassType classType;

  private Integer skillGroup;

  private Integer level;

  private Integer experience;

  private Long health;

  private Long mana;

  private Long stamina;

  private Long maxHealth;

  private Long maxMana;

  private Integer st;

  private Integer ht;

  private Integer dx;

  private Integer iq;

  private Integer minWeaponDamage;

  private Integer maxWeaponDamage;

  private Integer minAttackDamage;

  private Integer maxAttackDamage;

  private Integer givenStatusPoints;

  private Integer availableStatusPoints;

  private Integer availableSkillPoints;

  private Integer gold;

  private Integer positionX;

  private Integer positionY;

  private Long playTime;

  private Long accountId;
}
