package com.blaj.openmetin.shared.infrastructure.network.handler;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.model.Packet;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Sharable
@Service
@RequiredArgsConstructor
public class ChannelInboundHandlerService extends SimpleChannelInboundHandler<Packet> {

  private final PacketHandlerFactoryService packetHandlerFactoryService;
  private final SessionManagerService sessionManagerService;

  @Override
  public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
    super.channelActive(channelHandlerContext);
  }

  @Override
  public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
    Optional.ofNullable(channelHandlerContext)
        .map(ChannelHandlerContext::channel)
        .map(channel -> channel.attr(SessionManagerService.sessionKey))
        .map(Attribute::get)
        .map(Session::getId)
        .ifPresent(sessionManagerService::removeSession);

    super.channelInactive(channelHandlerContext);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) {
    var session =
        Optional.ofNullable(channelHandlerContext)
            .map(ChannelHandlerContext::channel)
            .map(channel -> channel.attr(SessionManagerService.sessionKey))
            .map(Attribute::get)
            .orElseThrow();

    var packetHandlerService =
        packetHandlerFactoryService
            .getPacketHandlerService(packet.getClass())
            .map(phs -> (PacketHandlerService<Packet>) phs)
            .orElseThrow();

    packetHandlerService.handle(packet, session);
  }
}
