package com.blaj.openmetin.shared.network.service;

import com.blaj.openmetin.shared.network.properties.TcpProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NettyServerService {

  private final TcpProperties tcpProperties;

  private EventLoopGroup bossEventLoopGroup;
  private EventLoopGroup workerEventLoopGroup;
  private Channel serverChannel;

  @Getter private volatile int boundPort = -1;

  public void start() {
    bossEventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    workerEventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    var serverBootstrap =
        new ServerBootstrap()
            .group(bossEventLoopGroup, workerEventLoopGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_BACKLOG, 512)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.SO_RCVBUF, 8192)
            .childOption(ChannelOption.SO_SNDBUF, 8192)
            .childHandler(
                new ChannelInitializer<SocketChannel>() {
                  @Override
                  protected void initChannel(SocketChannel socketChannel) throws Exception {}
                });

    serverChannel =
        serverBootstrap
            .bind(tcpProperties.host(), tcpProperties.port())
            .syncUninterruptibly()
            .channel();

    boundPort = ((java.net.InetSocketAddress) serverChannel.localAddress()).getPort();
  }

  public void stop() {
    Optional.ofNullable(serverChannel).ifPresent(sc -> sc.close().syncUninterruptibly());

    Optional.ofNullable(workerEventLoopGroup)
        .ifPresent(welg -> welg.shutdownGracefully().syncUninterruptibly());

    Optional.ofNullable(bossEventLoopGroup)
        .ifPresent(belg -> belg.shutdownGracefully().syncUninterruptibly());
  }
}
