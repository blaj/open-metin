package com.blaj.openmetin.game.application.features.entergame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.character.dto.CharacterAdditionalDataPacket;
import com.blaj.openmetin.game.application.common.character.dto.SpawnCharacterPacket;
import com.blaj.openmetin.game.application.common.config.ChannelPropertiesConfig;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntergameCommandHandlerServiceTest {

  private EntergameCommandHandlerService entergameCommandHandlerService;

  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SessionService sessionService;
  @Mock private ChannelPropertiesConfig channelPropertiesConfig;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    entergameCommandHandlerService =
        new EntergameCommandHandlerService(
            sessionManagerService, sessionService, channelPropertiesConfig);

    DateTimeUtils.initialize();
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var entergameCommand = new EntergameCommand(sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> entergameCommandHandlerService.handle(entergameCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Session not exists");
  }

  @Test
  public void givenNonExistingAccountId_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var entergameCommand = new EntergameCommand(sessionId);
    var gameSession = new GameSession(sessionId, channel);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    entergameCommandHandlerService.handle(entergameCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenValid_whenHandle_thenPacketsSend() {
    // given
    var sessionId = 123L;
    var entergameCommand = new EntergameCommand(sessionId);
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .vid(141L)
            .positionX(12451)
            .positionY(5474)
            .movementSpeed((short) 3154)
            .attackSpeed((short) 141)
            .empire(Empire.CHUNJO)
            .characterDto(
                CharacterDto.builder()
                    .name("name")
                    .level(100)
                    .classType(ClassType.SURA_FEMALE)
                    .build())
            .build();
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setGameCharacterEntity(gameCharacterEntity);
    gameSession.setAccountId(12341L);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(channelPropertiesConfig.channelIndex()).willReturn((short) 3);

    // when
    entergameCommandHandlerService.handle(entergameCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new PhasePacket().setPhase(Phase.IN_GAME));
    then(sessionService).should().sendPacketAsync(eq(sessionId), any(GameTimePacket.class));
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new ChannelPacket().setChannelNo((short) 3));
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
}
