package com.blaj.openmetin.shared.infrastructure.network.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.model.Packet;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChannelInboundHandlerServiceTest {

  private ChannelInboundHandlerService channelInboundHandlerService;

  @Mock private PacketHandlerFactoryService packetHandlerFactoryService;
  @Mock private SessionManagerService<?> sessionManagerService;
  @Mock private ChannelHandlerContext channelHandlerContext;
  @Mock private Channel channel;
  @Mock private Attribute<Session> sessionAttribute;
  @Mock private Session session;
  @Mock private PacketHandlerService<TestPacket> packetHandlerService;

  @BeforeEach
  public void beforeEach() {
    channelInboundHandlerService =
        new ChannelInboundHandlerService(packetHandlerFactoryService, sessionManagerService);

    given(channelHandlerContext.channel()).willReturn(channel);
    given(channel.attr(SessionManagerService.sessionKey)).willReturn(sessionAttribute);
    given(sessionAttribute.get()).willReturn(session);
  }

  @Test
  public void givenNonExistingSession_whenChannelInactive_thenDoNothing() throws Exception {
    // given
    given(sessionAttribute.get()).willReturn(null);

    // when
    channelInboundHandlerService.channelInactive(channelHandlerContext);

    // then
    then(sessionManagerService).should(never()).removeSession(anyLong());
  }

  @Test
  public void givenValid_whenChannelInactive_thenRemoveSession() throws Exception {
    // given
    var sessionId = 123L;

    given(session.getId()).willReturn(sessionId);

    // when
    channelInboundHandlerService.channelInactive(channelHandlerContext);

    // then
    then(sessionManagerService).should().removeSession(sessionId);
  }

  @Test
  public void givenHandlerNotFound_whenChannelRead0_thenThrowsException() {
    // given
    var packet = new TestPacket();

    given(packetHandlerFactoryService.getPacketHandlerService(TestPacket.class))
        .willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            NoSuchElementException.class,
            () -> channelInboundHandlerService.channelRead0(channelHandlerContext, packet));

    // then
    assertThat(thrownException).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void givenHandlerFound_whenChannelRead0_thenCallsHandler() {
    // given
    var packet = new TestPacket();

    given(packetHandlerFactoryService.getPacketHandlerService(TestPacket.class))
        .willReturn(Optional.of(packetHandlerService));

    // when
    channelInboundHandlerService.channelRead0(channelHandlerContext, packet);

    // then
    then(packetHandlerService).should().handle(packet, session);
  }

  public static class TestPacket implements Packet {}
}
