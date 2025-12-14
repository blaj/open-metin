package com.blaj.openmetin.authentication.application.features.handshake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthHandshakeCommandHandlerServiceTest {

  private AuthHandshakeCommandHandlerService authHandshakeCommandHandlerService;

  @Mock private SessionService sessionService;
  @Mock private SessionManagerService sessionManagerService;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<PhasePacket> phasePacketArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    authHandshakeCommandHandlerService =
        new AuthHandshakeCommandHandlerService(sessionService, sessionManagerService);
  }

  @Test
  public void givenValid_whenOnSuccessHandshake_thenSetPhaseToAuthAndSendPacket() {
    // given
    var sessionId = 123L;
    var session = new Session(sessionId, channel);
    session.setPhase(Phase.LOADING);

    // when
    authHandshakeCommandHandlerService.onSuccessHandshake(session);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(eq(sessionId), phasePacketArgumentCaptor.capture());

    assertThat(phasePacketArgumentCaptor.getValue().getPhase()).isEqualTo(Phase.AUTH);
  }
}
