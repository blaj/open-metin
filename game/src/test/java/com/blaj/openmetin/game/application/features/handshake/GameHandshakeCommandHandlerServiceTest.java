package com.blaj.openmetin.game.application.features.handshake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.game.application.common.eventsystem.EventSystemService;
import com.blaj.openmetin.game.application.common.ping.PingPacket;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Packet;
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
  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private EventSystemService eventSystemService;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<Packet> packetArgumentCaptor;
  @Captor private ArgumentCaptor<Supplier<Duration>> supplierArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    gameHandshakeCommandHandlerService =
        new GameHandshakeCommandHandlerService(
            sessionService, sessionManagerService, eventSystemService);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void givenValid_whenOnSuccessHandshake_thenSetPhaseToLoginAndSendPacket() {
    // given
    var sessionId = 123L;
    var session = new Session(sessionId, channel);
    session.setPhase(Phase.LOADING);

    given(eventSystemService.scheduleEvent(any(Supplier.class), any(Duration.class)))
        .willAnswer(
            invocation -> {
              var supplier = (Supplier<Duration>) invocation.getArgument(0);
              supplier.get();
              return null;
            });

    // when
    gameHandshakeCommandHandlerService.onSuccessHandshake(session);

    // then
    then(sessionService)
        .should(times(2))
        .sendPacketAsync(eq(sessionId), packetArgumentCaptor.capture());

    then(eventSystemService).should().scheduleEvent(any(Supplier.class), eq(Duration.ofSeconds(5)));

    var capturedPackets = packetArgumentCaptor.getAllValues();
    assertThat(capturedPackets).hasSize(2);
    assertThat(capturedPackets.get(0)).isInstanceOf(PhasePacket.class);
    assertThat(((PhasePacket) capturedPackets.get(0)).getPhase()).isEqualTo(Phase.LOGIN);
    assertThat(capturedPackets.get(1)).isInstanceOf(PingPacket.class);
  }
}
