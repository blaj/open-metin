package com.blaj.openmetin.shared.infrastructure.network.server;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.infrastructure.network.codec.MainByteToMessageDecoderService;
import com.blaj.openmetin.shared.infrastructure.network.codec.MainMessageToByteEncoderService;
import com.blaj.openmetin.shared.infrastructure.network.codec.PacketCodecFactoryService;
import com.blaj.openmetin.shared.infrastructure.network.handler.ChannelInboundHandlerService;
import com.blaj.openmetin.shared.infrastructure.network.handler.HandshakeChannelInboundHandlerService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameChannelInitializerService extends ChannelInitializer<SocketChannel> {

  private final HandshakeChannelInboundHandlerService handshakeChannelInboundHandlerService;
  private final ChannelInboundHandlerService channelInboundHandlerService;
  private final SessionManagerService sessionManagerService;
  private final PacketCodecFactoryService packetCodecFactoryService;

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    socketChannel
        .pipeline()
        .addLast("encoder", new MainMessageToByteEncoderService(packetCodecFactoryService))
        .addLast("decoder", new MainByteToMessageDecoderService(packetCodecFactoryService))
        .addLast("handshakeHandler", handshakeChannelInboundHandlerService)
        .addLast("handler", channelInboundHandlerService);

    var session = sessionManagerService.createSession(socketChannel);
    socketChannel.attr(SessionManagerService.sessionKey).set(session);
  }
}
