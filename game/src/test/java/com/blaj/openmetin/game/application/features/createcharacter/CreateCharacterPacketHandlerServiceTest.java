package com.blaj.openmetin.game.application.features.createcharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.enums.character.ClassType;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateCharacterPacketHandlerServiceTest {

  private CreateCharacterPacketHandlerService createCharacterPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    createCharacterPacketHandlerService = new CreateCharacterPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendAsync() {
    // given
    var createCharacterPacket =
        new CreateCharacterPacket()
            .setSlot((short) 3)
            .setName("name")
            .setClassType(3)
            .setShape((short) 2);
    var sessionId = 123L;
    var session = new GameSession(sessionId, channel);

    // when
    createCharacterPacketHandlerService.handle(createCharacterPacket, session);

    // then
    then(mediator)
        .should()
        .sendAsync(
            new CreateCharacterCommand(
                createCharacterPacket.getSlot(),
                createCharacterPacket.getName(),
                ClassType.SHAMAN_FEMALE,
                createCharacterPacket.getShape(),
                sessionId));
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnCreateCharacterPacketClass() {
    // given

    // when
    var packetType = createCharacterPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(CreateCharacterPacket.class);
  }
}
