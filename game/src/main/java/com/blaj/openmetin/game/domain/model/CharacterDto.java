package com.blaj.openmetin.game.domain.model;

import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joou.UByte;
import org.joou.UInteger;
import org.joou.UShort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CharacterDto {

  private Long id;

  private String name;

  private UByte slot;

  private UShort bodyPart;

  private UShort hairPart;

  private Empire empire;

  private ClassType classType;

  private UByte skillGroup;

  private UByte level;

  private Integer experience;

  private Long health;

  private Long mana;

  private Long stamina;

  private Long maxHealth;

  private Long maxMana;

  private UByte st;

  private UByte ht;

  private UByte dx;

  private UByte iq;

  private Integer minWeaponDamage;

  private Integer maxWeaponDamage;

  private Integer minAttackDamage;

  private Integer maxAttackDamage;

  private Integer givenStatusPoints;

  private Integer availableStatusPoints;

  private Integer availableSkillPoints;

  private UInteger gold;

  private Integer positionX;

  private Integer positionY;

  private UInteger playTime;

  private Long accountId;
}
