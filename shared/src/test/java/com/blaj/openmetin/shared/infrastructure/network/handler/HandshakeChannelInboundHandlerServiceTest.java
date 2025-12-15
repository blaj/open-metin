package com.blaj.openmetin.shared.infrastructure.network.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

import com.blaj.openmetin.shared.application.features.handshake.HandshakePacket;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.infrastructure.encryption.HandshakeUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HandshakeChannelInboundHandlerServiceTest {

  private HandshakeChannelInboundHandlerService handshakeChannelInboundHandlerService;

  @Mock private ChannelHandlerContext channelHandlerContext;
  @Mock private Channel channel;
  @Mock private Attribute<Session> sessionAttribute;
  @Mock private Session session;

  @Captor private ArgumentCaptor<PhasePacket> phasePacketArgumentCaptor;
  @Captor private ArgumentCaptor<HandshakePacket> handshakePacketArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    handshakeChannelInboundHandlerService = new HandshakeChannelInboundHandlerService();

    given(channelHandlerContext.channel()).willReturn(channel);
    given(channel.attr(SessionManagerService.sessionKey)).willReturn(sessionAttribute);
    given(sessionAttribute.get()).willReturn(session);
    given(session.getPhase()).willReturn(Phase.HANDSHAKE);
  }

  @Test
  public void whenChannelActive_thenInitializesHandshakeAndSendsPackets() throws Exception {
    // given
    try (var handshakeUtilsMock = mockStatic(HandshakeUtils.class);
        var dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      handshakeUtilsMock.when(HandshakeUtils::generateUInt32).thenReturn(12345L);
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(67890L);

      // when
      handshakeChannelInboundHandlerService.channelActive(channelHandlerContext);

      // then
      then(channelHandlerContext).should().writeAndFlush(phasePacketArgumentCaptor.capture());

      var phasePacket = phasePacketArgumentCaptor.getValue();
      assertThat(phasePacket.getPhase()).isEqualTo(Phase.HANDSHAKE);

      then(session).should().setHandshaking(true);
      then(session).should().setHandshake(12345L);

      then(channelHandlerContext).should().writeAndFlush(handshakePacketArgumentCaptor.capture());

      var handshakePacket = handshakePacketArgumentCaptor.getValue();
      assertThat(handshakePacket.getHandshake()).isEqualTo(12345L);
      assertThat(handshakePacket.getTime()).isEqualTo(67890L);
      assertThat(handshakePacket.getDelta()).isEqualTo(0L);

      then(channelHandlerContext).should().fireChannelActive();
    }
  }
}
