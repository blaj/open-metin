package com.blaj.openmetin.shared.infrastructure.network.session;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.shared.common.model.Packet;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionServiceImplTest {

  private SessionServiceImpl sessionService;

  @Mock private SessionManagerServiceImpl sessionManagerService;
  @Mock private Session session;
  @Mock private Channel channel;
  @Mock private ChannelFuture channelFuture;

  @BeforeEach
  public void beforeEach() {
    sessionService = new SessionServiceImpl(sessionManagerService);
  }

  @Test
  public void givenExistingSession_whenSendPacketAsync_thenSendsPacket() {
    // given
    var sessionId = 1L;
    var packet = new TestPacket();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(session));
    given(session.getChannel()).willReturn(channel);

    // when
    sessionService.sendPacketAsync(sessionId, packet);

    // then
    then(channel).should().writeAndFlush(packet);
  }

  @Test
  public void givenNonExistingSession_whenSendPacketAsync_thenDoesNothing() {
    // given
    var sessionId = 999L;
    var packet = new TestPacket();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    sessionService.sendPacketAsync(sessionId, packet);

    // then
    then(channel).should(never()).writeAndFlush(packet);
  }

  @Test
  public void givenSuccessfulSend_whenSendPacketSync_thenSendsPacketSuccessfully()
      throws InterruptedException {
    // given
    var sessionId = 1L;
    var packet = new TestPacket();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(session));
    given(session.getChannel()).willReturn(channel);
    given(channel.writeAndFlush(packet)).willReturn(channelFuture);
    given(channelFuture.await(5, TimeUnit.SECONDS)).willReturn(true);
    given(channelFuture.isSuccess()).willReturn(true);

    // when
    sessionService.sendPacketSync(sessionId, packet);

    // then
    then(channel).should().writeAndFlush(packet);
    then(channelFuture).should().await(5, TimeUnit.SECONDS);
  }

  @Test
  public void givenFailedSend_whenSendPacketSync_thenLogsWarning() throws InterruptedException {
    // given
    var sessionId = 1L;
    var packet = new TestPacket();
    var cause = new RuntimeException("Send failed");

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(session));
    given(session.getChannel()).willReturn(channel);
    given(channel.writeAndFlush(packet)).willReturn(channelFuture);
    given(channelFuture.await(5, TimeUnit.SECONDS)).willReturn(true);
    given(channelFuture.isSuccess()).willReturn(false);
    given(channelFuture.cause()).willReturn(cause);

    // when
    sessionService.sendPacketSync(sessionId, packet);

    // then
    then(channel).should().writeAndFlush(packet);
    then(channelFuture).should().cause();
  }

  @Test
  public void givenTimeout_whenSendPacketSync_thenLogsWarning() throws InterruptedException {
    // given
    var sessionId = 1L;
    var packet = new TestPacket();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(session));
    given(session.getChannel()).willReturn(channel);
    given(channel.writeAndFlush(packet)).willReturn(channelFuture);
    given(channelFuture.await(5, TimeUnit.SECONDS)).willReturn(false);

    // when
    sessionService.sendPacketSync(sessionId, packet);

    // then
    then(channel).should().writeAndFlush(packet);
    then(channelFuture).should().await(5, TimeUnit.SECONDS);
  }

  @Test
  public void givenInterruption_whenSendPacketSync_thenHandlesInterruptedException()
      throws InterruptedException {
    // given
    var sessionId = 1L;
    var packet = new TestPacket();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(session));
    given(session.getChannel()).willReturn(channel);
    given(channel.writeAndFlush(packet)).willReturn(channelFuture);
    given(channelFuture.await(5, TimeUnit.SECONDS)).willThrow(new InterruptedException());

    // when
    sessionService.sendPacketSync(sessionId, packet);

    // then
    then(channel).should().writeAndFlush(packet);
  }

  @Test
  public void givenNonExistingSession_whenSendPacketSync_thenDoesNothing() {
    // given
    var sessionId = 999L;
    var packet = new TestPacket();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    sessionService.sendPacketSync(sessionId, packet);

    // then
    then(channel).should(never()).writeAndFlush(packet);
  }

  static class TestPacket implements Packet {}
}
