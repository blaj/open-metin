package com.blaj.openmetin.game.application.common.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.application.common.character.dto.CharacterAdditionalDataPacket;
import com.blaj.openmetin.game.application.common.character.dto.RemoveCharacterPacket;
import com.blaj.openmetin.game.application.common.character.dto.SpawnCharacterPacket;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntityVisibilityServiceTest {

  private EntityVisibilityService entityVisibilityService;

  @Mock private SessionService sessionService;

  @BeforeEach
  public void beforeEach() {
    entityVisibilityService = new EntityVisibilityService(sessionService);
  }

  @Test
  public void givenNonPlayerEntityType_whenShowEntityToPlayer_thenDoNothing() {
    // given
    var sessionId = 123L;
    var entity = mock(BaseGameEntity.class);

    given(entity.getType()).willReturn(EntityType.DOOR);

    // when
    entityVisibilityService.showEntityToPlayer(entity, sessionId);

    // then
    then(sessionService).should(never()).sendPacketAsync(anyLong(), any());
  }

  @Test
  public void givenValid_whenShowEntityToPlayer_thenSendPackets() {
    // given
    var sessionId = 123L;
    var characterDto =
        CharacterDto.builder().name("name").level(51).classType(ClassType.NINJA_MALE).build();
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .vid(111L)
            .positionX(23423)
            .positionY(1241)
            .movementSpeed((short) 14)
            .attackSpeed((short) 25)
            .empire(Empire.CHUNJO)
            .characterDto(characterDto)
            .build();

    // when
    entityVisibilityService.showEntityToPlayer(gameCharacterEntity, sessionId);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(
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

    then(sessionService)
        .should()
        .sendPacketAsync(
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

  @Test
  public void givenNonPlayerEntityType_whenHideEntityFromPlayer_thenDoNothing() {
    // given
    var sessionId = 123L;
    var entity = mock(BaseGameEntity.class);

    given(entity.getType()).willReturn(EntityType.DOOR);

    // when
    entityVisibilityService.hideEntityFromPlayer(entity, sessionId);

    // then
    then(sessionService).should(never()).sendPacketAsync(anyLong(), any());
  }

  @Test
  public void givenValid_whenHideEntityFromPlayer_thenSendPacket() {
    // given
    var sessionId = 123L;
    var gameCharacterEntity = GameCharacterEntity.builder().vid(111L).build();

    // when
    entityVisibilityService.hideEntityFromPlayer(gameCharacterEntity, sessionId);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(
            sessionId, new RemoveCharacterPacket().setVid(gameCharacterEntity.getVid()));
  }
}
