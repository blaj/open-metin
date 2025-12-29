package com.blaj.openmetin.game.application.features.movecharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.enums.character.CharacterMovementType;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MoveCharacterPacketHandlerServiceTest {

  private MoveCharacterPacketHandlerService moveCharacterPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    moveCharacterPacketHandlerService = new MoveCharacterPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendAsync() {
    // given
    var moveCharacterPacket =
        new MoveCharacterPacket()
            .setMovementType(CharacterMovementType.MOVE)
            .setArgument((short) 123)
            .setRotation((short) 141)
            .setPositionX(151)
            .setPositionY(542)
            .setTime(353L);
    var session = new Session(123L, channel);

    // when
    moveCharacterPacketHandlerService.handle(moveCharacterPacket, session);

    // then
    then(mediator)
        .should()
        .sendAsync(
            new MoveCharacterCommand(
                moveCharacterPacket.getMovementType(),
                moveCharacterPacket.getArgument(),
                moveCharacterPacket.getRotation(),
                moveCharacterPacket.getPositionX(),
                moveCharacterPacket.getPositionY(),
                moveCharacterPacket.getTime(),
                session.getId()));
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnMoveCharacterPacketClass() {
    // given

    // when
    var packetType = moveCharacterPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(MoveCharacterPacket.class);
  }
}
