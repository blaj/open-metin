package com.blaj.openmetin.game.application.features.movecharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.game.application.common.character.dto.MoveCharacterBroadcastPacket;
import com.blaj.openmetin.game.application.common.entity.GameEntityMovementService;
import com.blaj.openmetin.game.domain.enums.character.CharacterMovementType;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MoveCharacterCommandHandlerServiceTest {

  private MoveCharacterCommandHandlerService moveCharacterCommandHandlerService;

  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SessionService sessionService;
  @Mock private GameEntityMovementService gameEntityMovementService;

  @Mock private Channel channel;
  @Mock private Channel nearbyChannel1;
  @Mock private Channel nearbyChannel2;

  @Captor
  private ArgumentCaptor<MoveCharacterBroadcastPacket> moveCharacterBroadcastPacketArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    moveCharacterCommandHandlerService =
        new MoveCharacterCommandHandlerService(
            sessionManagerService, sessionService, gameEntityMovementService);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var moveCharacterCommand =
        new MoveCharacterCommand(
            CharacterMovementType.MOVE, (short) 123, (short) 321, 333, 444, 555L, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> moveCharacterCommandHandlerService.handle(moveCharacterCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Session not exists");
  }

  @Test
  public void givenNonExistingGameCharacterEntity_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var gameSession = new GameSession(sessionId, channel);
    var moveCharacterCommand =
        new MoveCharacterCommand(
            CharacterMovementType.MOVE, (short) 123, (short) 321, 333, 444, 555L, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    moveCharacterCommandHandlerService.handle(moveCharacterCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNullMovementType_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var gameCharacterEntity = GameCharacterEntity.builder().build();

    var gameSession = new GameSession(sessionId, channel);
    gameSession.setGameCharacterEntity(gameCharacterEntity);

    var moveCharacterCommand =
        new MoveCharacterCommand(null, (short) 123, (short) 321, 333, 444, 555L, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    moveCharacterCommandHandlerService.handle(moveCharacterCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenMovementTypeMove_whenHandle_thenSetRotationAndGoToAndSendPackets() {
    // given
    DateTimeUtils.initialize();

    var sessionId = 123L;
    var nearbyPlayer1 = GameCharacterEntity.builder().vid(4324L).build();
    var nearbyPlayer2 = GameCharacterEntity.builder().vid(6456L).build();
    var nearbySession1 = new GameSession(456L, channel);
    var nearbySession2 = new GameSession(789L, channel);
    nearbyPlayer1.setSession(nearbySession1);
    nearbyPlayer2.setSession(nearbySession2);

    var gameCharacterEntity =
        GameCharacterEntity.builder().vid(333L).movementDuration(444L).build();
    gameCharacterEntity.addNearbyEntity(nearbyPlayer1);
    gameCharacterEntity.addNearbyEntity(nearbyPlayer2);

    var gameSession = new GameSession(sessionId, channel);
    gameSession.setGameCharacterEntity(gameCharacterEntity);

    var moveCharacterCommand =
        new MoveCharacterCommand(
            CharacterMovementType.MOVE, (short) 10, (short) 20, 100, 200, 555L, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    moveCharacterCommandHandlerService.handle(moveCharacterCommand);

    // then
    assertThat(gameCharacterEntity.getRotation()).isEqualTo(moveCharacterCommand.rotation() * 5);

    then(gameEntityMovementService)
        .should()
        .goTo(
            eq(gameCharacterEntity),
            eq(moveCharacterCommand.positionX()),
            eq(moveCharacterCommand.positionY()),
            anyLong());
    then(sessionService)
        .should(times(2))
        .sendPacketAsync(anyLong(), moveCharacterBroadcastPacketArgumentCaptor.capture());

    var packets = moveCharacterBroadcastPacketArgumentCaptor.getAllValues();
    assertThat(packets).hasSize(2);

    packets.forEach(
        packet -> {
          assertThat(packet.getMovementType()).isEqualTo(CharacterMovementType.MOVE);
          assertThat(packet.getArgument()).isEqualTo(moveCharacterCommand.argument());
          assertThat(packet.getRotation()).isEqualTo(moveCharacterCommand.rotation());
          assertThat(packet.getVid()).isEqualTo(gameCharacterEntity.getVid());
          assertThat(packet.getPositionX()).isEqualTo(moveCharacterCommand.positionX());
          assertThat(packet.getPositionY()).isEqualTo(moveCharacterCommand.positionY());
          assertThat(packet.getTime()).isEqualTo(moveCharacterCommand.time());
          assertThat(packet.getDuration()).isEqualTo(gameCharacterEntity.getMovementDuration());
        });
  }

  @Test
  public void givenMovementTypeWait_whenHandle_thenWaitAndSendPackets() {
    // given
    var sessionId = 123L;
    var nearbyPlayer = GameCharacterEntity.builder().vid(5353L).build();
    var nearbySession = new GameSession(456L, nearbyChannel1);
    nearbyPlayer.setSession(nearbySession);

    var gameCharacterEntity = GameCharacterEntity.builder().vid(333L).build();
    gameCharacterEntity.addNearbyEntity(nearbyPlayer);

    var gameSession = new GameSession(sessionId, channel);
    gameSession.setGameCharacterEntity(gameCharacterEntity);

    var moveCharacterCommand =
        new MoveCharacterCommand(
            CharacterMovementType.WAIT, (short) 10, (short) 20, 100, 200, 555L, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    moveCharacterCommandHandlerService.handle(moveCharacterCommand);

    // then
    then(gameEntityMovementService)
        .should()
        .wait(
            gameCharacterEntity,
            moveCharacterCommand.positionX(),
            moveCharacterCommand.positionY());
    then(sessionService)
        .should()
        .sendPacketAsync(
            eq(nearbySession.getId()), moveCharacterBroadcastPacketArgumentCaptor.capture());

    var packet = moveCharacterBroadcastPacketArgumentCaptor.getValue();
    assertThat(packet.getMovementType()).isEqualTo(CharacterMovementType.WAIT);
    assertThat(packet.getArgument()).isEqualTo(moveCharacterCommand.argument());
    assertThat(packet.getRotation()).isEqualTo(moveCharacterCommand.rotation());
    assertThat(packet.getVid()).isEqualTo(gameCharacterEntity.getVid());
    assertThat(packet.getPositionX()).isEqualTo(moveCharacterCommand.positionX());
    assertThat(packet.getPositionY()).isEqualTo(moveCharacterCommand.positionY());
    assertThat(packet.getTime()).isEqualTo(moveCharacterCommand.time());
    assertThat(packet.getDuration()).isEqualTo(0L);
  }
}
