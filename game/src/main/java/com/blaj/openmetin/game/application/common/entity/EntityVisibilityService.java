package com.blaj.openmetin.game.application.common.entity;

import com.blaj.openmetin.game.application.common.character.dto.CharacterAdditionalDataPacket;
import com.blaj.openmetin.game.application.common.character.dto.RemoveCharacterPacket;
import com.blaj.openmetin.game.application.common.character.dto.SpawnCharacterPacket;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityVisibilityService {

  private final SessionService sessionService;

  public void showEntityToPlayer(BaseGameEntity entityToShow, long sessionId) {
    if (entityToShow.getType() != EntityType.PLAYER) {
      return;
    }

    var gameCharacterEntity = (GameCharacterEntity) entityToShow;

    sessionService.sendPacketAsync(
        sessionId,
        new SpawnCharacterPacket()
            .setVid(gameCharacterEntity.getVid())
            .setAngle(0)
            .setPositionX(gameCharacterEntity.getPositionX())
            .setPositionY(gameCharacterEntity.getPositionY())
            .setPositionZ(0)
            .setCharacterType((short) gameCharacterEntity.getType().ordinal())
            .setClassType(gameCharacterEntity.getCharacterDto().getClassType().getValue())
            .setMoveSpeed(gameCharacterEntity.getMovementSpeed())
            .setAttackSpeed(gameCharacterEntity.getAttackSpeed())
            .setState((short) 0)
            .setAffects(new long[2]));

    sessionService.sendPacketAsync(
        sessionId,
        new CharacterAdditionalDataPacket()
            .setVid(gameCharacterEntity.getVid())
            .setName(gameCharacterEntity.getCharacterDto().getName())
            .setParts(new int[] {0, 0, 1001, 1001})
            .setEmpire(gameCharacterEntity.getEmpire())
            .setGuildId(0)
            .setLevel(gameCharacterEntity.getCharacterDto().getLevel())
            .setRankPoints((short) 0)
            .setPkMode((short) 0)
            .setMountVnum(0));
  }

  public void hideEntityFromPlayer(BaseGameEntity entityToHide, long sessionId) {
    if (entityToHide.getType() != EntityType.PLAYER) {
      return;
    }

    sessionService.sendPacketAsync(
        sessionId, new RemoveCharacterPacket().setVid(entityToHide.getVid()));
  }
}
