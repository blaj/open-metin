package com.blaj.openmetin.shared.infrastructure.network.handler;

import com.blaj.openmetin.shared.application.features.handshake.HandshakePacket;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.infrastructure.encryption.HandshakeUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Service;

@Service
@Sharable
public class HandshakeChannelInboundHandlerService extends ChannelInboundHandlerAdapter {

  @Override
  public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
    var session = channelHandlerContext.channel().attr(SessionManagerService.sessionKey).get();

    var phasePacket = new PhasePacket().setPhase(session.getPhase());
    channelHandlerContext.writeAndFlush(phasePacket);

    var handshake = HandshakeUtils.generateUInt32();

    session.setHandshaking(true);
    session.setHandshake(handshake);

    var handshakePacket =
        new HandshakePacket()
            .setHandshake(handshake)
            .setTime(DateTimeUtils.getUnixTime())
            .setDelta(0);

    channelHandlerContext.writeAndFlush(handshakePacket);

    super.channelActive(channelHandlerContext);
  }
}
