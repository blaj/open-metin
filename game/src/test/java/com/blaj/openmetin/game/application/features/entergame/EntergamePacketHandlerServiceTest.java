package com.blaj.openmetin.game.application.features.entergame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntergamePacketHandlerServiceTest {

  private EntergamePacketHandlerService entergamePacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    entergamePacketHandlerService = new EntergamePacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendAsync() {
    // given
    var entergamePacket = new EntergamePacket();
    var session = new Session(123L, channel);

    // when
    entergamePacketHandlerService.handle(entergamePacket, session);

    // then
    then(mediator).should().sendAsync(new EntergameCommand(session.getId()));
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnEntergamePacketClass() {
    // given

    // when
    var packetType = entergamePacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(EntergamePacket.class);
  }
}
