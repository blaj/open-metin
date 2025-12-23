package com.blaj.openmetin.shared.infrastructure.network.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Sharable
public class GlobalExceptionHandlerService extends ChannelInboundHandlerAdapter {

  @Override
  public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause)
      throws Exception {
    log.error("Unhandled exception in pipeline", cause);
    cause.printStackTrace();
    channelHandlerContext.close();
  }
}
