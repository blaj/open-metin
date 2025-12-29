package com.blaj.openmetin.game.application.common.entity;

import com.blaj.openmetin.game.application.common.character.utils.CharacterPointsUtils;
import com.blaj.openmetin.game.domain.config.JobConfig;
import com.blaj.openmetin.game.domain.enums.character.PointType;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import org.springframework.stereotype.Service;

@Service
public class GameCharacterEntityCalculationService {

  public void calculate(GameCharacterEntity gameCharacterEntity) {
    var jobConfig =
        gameCharacterEntity.getCharacterDto().getClassType().getJobType().getJobConfig();

    gameCharacterEntity
        .getCharacterDto()
        .setMaxHealth(
            getMaxHp(
                jobConfig,
                gameCharacterEntity.getCharacterDto().getLevel(),
                gameCharacterEntity.getCharacterDto().getHt()));

    gameCharacterEntity
        .getCharacterDto()
        .setMaxMana(
            getMaxSp(
                jobConfig,
                gameCharacterEntity.getCharacterDto().getLevel(),
                gameCharacterEntity.getCharacterDto().getIq()));

    gameCharacterEntity.setDefence(
        getDefence(
            CharacterPointsUtils.getPointValue(gameCharacterEntity, PointType.LEVEL),
            CharacterPointsUtils.getPointValue(gameCharacterEntity, PointType.HT)));
    gameCharacterEntity.setMovementSpeed(getMovementSpeed());
    gameCharacterEntity.setAttackSpeed(getAttackSpeed());
  }

  private long getMaxHp(JobConfig jobConfig, int level, int htPoints) {
    return jobConfig.startHp()
        + (long) jobConfig.hpPerHt() * htPoints
        + (long) jobConfig.hpPerLevel() * level;
  }

  private long getMaxSp(JobConfig jobConfig, int level, int iqPoints) {
    return jobConfig.startSp()
        + (long) jobConfig.spPerIq() * iqPoints
        + (long) jobConfig.spPerLevel() * level;
  }

  private long getDefence(long level, long htPoints) {
    return (long) (level + Math.floor(0.8 * htPoints));
  }

  private short getMovementSpeed() {
    var modifier = 0.0;
    var calculatedSpeed = CharacterConstants.DEFAULT_MOVEMENT_SPEED * (1 + modifier / 100);

    return (short) Math.min(calculatedSpeed, 255);
  }

  private short getAttackSpeed() {
    var modifier = 0.0;
    var calculatedSpeed = CharacterConstants.DEFAULT_ATTACK_SPEED * (1 + modifier / 100);

    return (short) Math.min(calculatedSpeed, 255);
  }
}
