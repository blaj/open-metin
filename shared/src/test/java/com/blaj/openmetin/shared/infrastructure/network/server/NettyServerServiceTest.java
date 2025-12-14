package com.blaj.openmetin.shared.infrastructure.network.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import com.blaj.openmetin.shared.infrastructure.network.properties.TcpProperties;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NettyServerServiceTest {

  private static final String testHost = "localhost";
  private static final int testPort = 0; // Random available port

  private NettyServerService nettyServerService;
  private boolean manualStop = false;

  @Mock private GameChannelInitializerService gameChannelInitializerService;

  @AfterEach
  public void afterEach() {
    if (manualStop) {
      return;
    }

    Optional.ofNullable(nettyServerService).ifPresent(NettyServerService::stop);
  }

  @Test
  void givenValidConfiguration_whenStart_thenServerBindsToPort() {
    // given
    var tcpProperties = new TcpProperties(testHost, testPort, 60);
    nettyServerService = new NettyServerService(tcpProperties, gameChannelInitializerService);

    // when
    nettyServerService.start();

    // then
    await()
        .atMost(Duration.ofSeconds(1))
        .untilAsserted(
            () -> {
              try (Socket socket = new Socket()) {
                socket.connect(
                    new InetSocketAddress(testHost, nettyServerService.getBoundPort()), 1000);
                assertThat(socket.isConnected()).isTrue();
              }
            });
  }

  @Test
  void givenStartedServer_whenStop_thenServerStopsGracefully() {
    // given
    var tcpProperties = new TcpProperties(testHost, testPort, 60);
    nettyServerService = new NettyServerService(tcpProperties, gameChannelInitializerService);
    nettyServerService.start();

    // when
    nettyServerService.stop();
    manualStop = true;

    // then
    await()
        .pollDelay(Duration.ofMillis(100))
        .atMost(Duration.ofSeconds(1))
        .untilAsserted(
            () -> {
              try (Socket socket = new Socket()) {
                socket.connect(
                    new InetSocketAddress(testHost, nettyServerService.getBoundPort()), 1000);
                assertThat(socket.isConnected()).isFalse();
              } catch (IOException e) {
                assertThat(e).isNotNull();
              }
            });
  }

  @Test
  void givenNotStartedServer_whenStop_thenNoExceptionThrown() {
    // given
    var tcpProperties = new TcpProperties(testHost, testPort, 60);
    nettyServerService = new NettyServerService(tcpProperties, gameChannelInitializerService);

    // when & then
    assertThatCode(() -> nettyServerService.stop()).doesNotThrowAnyException();
  }
}
