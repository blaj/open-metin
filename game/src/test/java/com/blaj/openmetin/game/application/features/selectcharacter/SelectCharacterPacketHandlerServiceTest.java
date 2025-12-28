package com.blaj.openmetin.game.application.features.selectcharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SelectCharacterPacketHandlerServiceTest {

  private SelectCharacterPacketHandlerService selectCharacterPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    selectCharacterPacketHandlerService = new SelectCharacterPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendAsync() {
    // given
    var session = new GameSession(123L, channel);
    var selectCharacterPacket = new SelectCharacterPacket().setSlot((short) 2);

    // when
    selectCharacterPacketHandlerService.handle(selectCharacterPacket, session);

    // then
    then(mediator)
        .should()
        .sendAsync(new SelectCharacterCommand(selectCharacterPacket.getSlot(), session.getId()));
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnSelectCharacterPacketClass() {
    // given

    // when
    var packetType = selectCharacterPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(SelectCharacterPacket.class);
  }
}
