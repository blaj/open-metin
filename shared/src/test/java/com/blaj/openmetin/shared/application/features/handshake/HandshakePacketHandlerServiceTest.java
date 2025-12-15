package com.blaj.openmetin.shared.application.features.handshake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HandshakePacketHandlerServiceTest {

  private HandshakePacketHandlerService handshakePacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Session session;

  @Captor private ArgumentCaptor<HandshakeCommand> commandCaptor;

  @BeforeEach
  public void beforeEach() {
    handshakePacketHandlerService = new HandshakePacketHandlerService(mediator);
  }

  @Test
  public void givenPacket_whenHandle_thenSendsCommandToMediator() {
    // given
    var packet = new HandshakePacket().setHandshake(12345L).setTime(67890L).setDelta(100);
    given(session.getId()).willReturn(999L);

    // when
    handshakePacketHandlerService.handle(packet, session);

    // then
    then(mediator).should().send(commandCaptor.capture());
    var command = commandCaptor.getValue();
    assertThat(command.handshake()).isEqualTo(12345L);
    assertThat(command.time()).isEqualTo(67890L);
    assertThat(command.delta()).isEqualTo(100L);
    assertThat(command.sessionId()).isEqualTo(999L);
  }

  @Test
  public void whenGetPacketType_thenReturnsHandshakePacketClass() {
    // given

    // when
    var result = handshakePacketHandlerService.getPacketType();

    // then
    assertThat(result).isEqualTo(HandshakePacket.class);
  }
}
