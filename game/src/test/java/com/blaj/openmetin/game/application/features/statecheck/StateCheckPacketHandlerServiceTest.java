package com.blaj.openmetin.game.application.features.statecheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StateCheckPacketHandlerServiceTest {

  private StateCheckPacketHandlerService stateCheckPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<StateCheckCommand> stateCheckCommandArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    stateCheckPacketHandlerService = new StateCheckPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSend() {
    // given
    var stateCheckPacket = new StateCheckPacket();
    var session = new Session(33L, channel);

    // when
    stateCheckPacketHandlerService.handle(stateCheckPacket, session);

    // then
    then(mediator).should().send(stateCheckCommandArgumentCaptor.capture());

    assertThat(stateCheckCommandArgumentCaptor.getValue().sessionId()).isEqualTo(session.getId());
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnPacketType() {
    // given

    // when
    var packetType = stateCheckPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(StateCheckPacket.class);
  }
}
