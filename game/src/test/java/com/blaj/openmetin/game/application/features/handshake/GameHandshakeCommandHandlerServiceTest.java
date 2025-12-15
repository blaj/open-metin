package com.blaj.openmetin.game.application.features.handshake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.eventsystem.EventSystemService;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameHandshakeCommandHandlerServiceTest {

  private GameHandshakeCommandHandlerService gameHandshakeCommandHandlerService;

  @Mock private SessionService sessionService;
  @Mock private SessionManagerService sessionManagerService;
  @Mock private EventSystemService eventSystemService;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<PhasePacket> phasePacketArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    gameHandshakeCommandHandlerService =
        new GameHandshakeCommandHandlerService(
            sessionService, sessionManagerService, eventSystemService);
  }

  @Test
  public void givenValid_whenOnSuccessHandshake_thenSetPhaseToLoginAndSendPacket() {
    // given
    var sessionId = 123L;
    var session = new Session(sessionId, channel);
    session.setPhase(Phase.LOADING);

    // when
    gameHandshakeCommandHandlerService.onSuccessHandshake(session);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(eq(sessionId), phasePacketArgumentCaptor.capture());
    then(eventSystemService).should().scheduleEvent(any(Supplier.class), eq(Duration.ofSeconds(5)));

    assertThat(phasePacketArgumentCaptor.getValue().getPhase()).isEqualTo(Phase.LOGIN);
  }
}
