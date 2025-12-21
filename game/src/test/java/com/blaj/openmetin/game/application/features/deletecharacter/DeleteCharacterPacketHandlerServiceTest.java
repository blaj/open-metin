package com.blaj.openmetin.game.application.features.deletecharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteCharacterPacketHandlerServiceTest {

  private DeleteCharacterPacketHandlerService deleteCharacterPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    deleteCharacterPacketHandlerService = new DeleteCharacterPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendAsync() {
    // given
    var sessionId = 123L;
    var deleteCharacterPacket =
        new DeleteCharacterPacket().setSlot((short) 3).setDeleteCode("1234567");
    var session = new GameSession(sessionId, channel);

    // when
    deleteCharacterPacketHandlerService.handle(deleteCharacterPacket, session);

    // then
    then(mediator)
        .should()
        .sendAsync(
            new DeleteCharacterCommand(
                deleteCharacterPacket.getSlot(), deleteCharacterPacket.getDeleteCode(), sessionId));
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnDeleteCharacterPacketClass() {
    // given

    // when
    var packetType = deleteCharacterPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(DeleteCharacterPacket.class);
  }
}
