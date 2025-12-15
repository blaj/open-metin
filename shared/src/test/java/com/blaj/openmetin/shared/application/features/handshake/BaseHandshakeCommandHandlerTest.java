package com.blaj.openmetin.shared.application.features.handshake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import io.netty.channel.Channel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BaseHandshakeCommandHandlerTest {

  private TestHandshakeCommandHandler testHandshakeCommandHandler;
  private boolean onSuccessHandshakeCalled;

  @Mock private SessionService sessionService;
  @Mock private SessionManagerService sessionManagerService;
  @Mock private Session session;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<HandshakePacket> handshakePacketCaptor;

  @BeforeEach
  public void beforeEach() {
    testHandshakeCommandHandler =
        new TestHandshakeCommandHandler(sessionService, sessionManagerService);
    onSuccessHandshakeCalled = false;
  }

  @Test
  public void givenSessionNotFound_whenHandle_thenDoesNothing() {
    // given
    var command = new HandshakeCommand(12345L, 100L, 0, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.empty());

    // when
    testHandshakeCommandHandler.handle(command);

    // then
    then(channel).should(never()).close();
  }

  @Test
  public void givenSessionNotHandshaking_whenHandle_thenClosesChannel() {
    // given
    var command = new HandshakeCommand(12345L, 100L, 0, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.of(session));
    given(session.isHandshaking()).willReturn(false);
    given(session.getChannel()).willReturn(channel);

    // when
    testHandshakeCommandHandler.handle(command);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenWrongHandshake_whenHandle_thenClosesChannel() {
    // given
    var command = new HandshakeCommand(12345L, 100L, 0, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.of(session));
    given(session.isHandshaking()).willReturn(true);
    given(session.getHandshake()).willReturn(99999L);
    given(session.getChannel()).willReturn(channel);

    // when
    testHandshakeCommandHandler.handle(command);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenValidHandshakeWithinTimeRange_whenHandle_thenCompletesHandshake() {
    // given
    var command = new HandshakeCommand(12345L, 100L, 10, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.of(session));
    given(session.isHandshaking()).willReturn(true);
    given(session.getHandshake()).willReturn(12345L);

    try (var dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(130L);

      // when
      testHandshakeCommandHandler.handle(command);

      // then
      then(session).should().setHandshaking(false);
      assertThat(onSuccessHandshakeCalled).isTrue();
    }
  }

  @Test
  public void givenValidHandshakeOutsideTimeRange_whenHandle_thenSendsNewHandshakePacket() {
    // given
    var command = new HandshakeCommand(12345L, 100L, 10, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.of(session));
    given(session.isHandshaking()).willReturn(true);
    given(session.getHandshake()).willReturn(12345L);

    try (var dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(200L);

      // when
      testHandshakeCommandHandler.handle(command);

      // then
      then(session).should().setLastHandshakeTime(200L);
      then(sessionService).should().sendPacketAsync(eq(1L), handshakePacketCaptor.capture());

      var packet = handshakePacketCaptor.getValue();
      assertThat(packet.getHandshake()).isEqualTo(12345L);
      assertThat(packet.getTime()).isEqualTo(200L);
      assertThat(packet.getDelta()).isEqualTo(50);
    }
  }

  @Test
  public void givenNegativeDelta_whenHandle_thenUsesLastHandshakeTime() {
    // given
    var command = new HandshakeCommand(12345L, 300L, 10, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.of(session));
    given(session.isHandshaking()).willReturn(true);
    given(session.getHandshake()).willReturn(12345L);
    given(session.getLastHandshakeTime()).willReturn(100L);

    try (var dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(200L);

      // when
      testHandshakeCommandHandler.handle(command);

      // then
      then(sessionService).should().sendPacketAsync(eq(1L), handshakePacketCaptor.capture());

      var packet = handshakePacketCaptor.getValue();
      assertThat(packet.getDelta()).isEqualTo(50);
    }
  }

  @Test
  public void givenNegativeDeltaWithNullLastHandshakeTime_whenHandle_thenUsesZero() {
    // given
    var command = new HandshakeCommand(12345L, 300L, 10, 1L);

    given(sessionManagerService.getSession(1L)).willReturn(Optional.of(session));
    given(session.isHandshaking()).willReturn(true);
    given(session.getHandshake()).willReturn(12345L);
    given(session.getLastHandshakeTime()).willReturn(null);

    try (var dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(200L);

      // when
      testHandshakeCommandHandler.handle(command);

      // then
      then(sessionService).should().sendPacketAsync(eq(1L), handshakePacketCaptor.capture());

      var packet = handshakePacketCaptor.getValue();
      assertThat(packet.getDelta()).isEqualTo(100);
    }
  }

  class TestHandshakeCommandHandler extends BaseHandshakeCommandHandler {
    public TestHandshakeCommandHandler(
        SessionService sessionService, SessionManagerService sessionManagerService) {
      super(sessionService, sessionManagerService);
    }

    @Override
    protected void onSuccessHandshake(Session session) {
      onSuccessHandshakeCalled = true;
    }
  }
}
