package com.blaj.openmetin.shared.infrastructure.network.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.network.codec.MainByteToMessageDecoderService;
import com.blaj.openmetin.shared.infrastructure.network.codec.MainMessageToByteEncoderService;
import com.blaj.openmetin.shared.infrastructure.network.codec.PacketCodecFactoryService;
import com.blaj.openmetin.shared.infrastructure.network.handler.ChannelInboundHandlerService;
import com.blaj.openmetin.shared.infrastructure.network.handler.HandshakeChannelInboundHandlerService;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameChannelInitializerServiceTest {

  private GameChannelInitializerService gameChannelInitializerService;

  @Mock private HandshakeChannelInboundHandlerService handshakeChannelInboundHandlerService;
  @Mock private ChannelInboundHandlerService channelInboundHandlerService;
  @Mock private SessionManagerService sessionManagerService;
  @Mock private PacketCodecFactoryService packetCodecFactoryService;
  @Mock private SocketChannel socketChannel;
  @Mock private ChannelPipeline pipeline;
  @Mock private Attribute<Session> sessionAttribute;
  @Mock private Session session;

  @BeforeEach
  public void beforeEach() {
    gameChannelInitializerService =
        new GameChannelInitializerService(
            handshakeChannelInboundHandlerService,
            channelInboundHandlerService,
            sessionManagerService,
            packetCodecFactoryService);

    given(socketChannel.pipeline()).willReturn(pipeline);
    given(pipeline.addLast(any(String.class), any())).willReturn(pipeline);
    given(socketChannel.attr(SessionManagerService.sessionKey)).willReturn(sessionAttribute);
    given(sessionManagerService.createSession(socketChannel)).willReturn(session);
  }

  @Test
  public void whenInitChannel_thenAddsHandlersInCorrectOrder() throws Exception {
    // given

    // when
    gameChannelInitializerService.initChannel(socketChannel);

    // then
    var inOrder = inOrder(pipeline);
    inOrder.verify(pipeline).addLast(eq("encoder"), any(MainMessageToByteEncoderService.class));
    inOrder.verify(pipeline).addLast(eq("decoder"), any(MainByteToMessageDecoderService.class));
    inOrder
        .verify(pipeline)
        .addLast(eq("handshakeHandler"), eq(handshakeChannelInboundHandlerService));
    inOrder.verify(pipeline).addLast(eq("handler"), eq(channelInboundHandlerService));
  }

  @Test
  public void whenInitChannel_thenCreatesSessionAndSetsAttribute() throws Exception {
    // given

    // when
    gameChannelInitializerService.initChannel(socketChannel);

    // then
    then(sessionManagerService).should().createSession(socketChannel);
    then(sessionAttribute).should().set(session);
  }
}
