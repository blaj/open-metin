package com.blaj.openmetin.game.application.features.clientversion;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class ClientVersionPacketHandlerServiceTest {

  private ClientVersionPacketHandlerService clientVersionPacketHandlerService;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    clientVersionPacketHandlerService = new ClientVersionPacketHandlerService();

    logger = (Logger) LoggerFactory.getLogger(ClientVersionPacketHandlerService.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @AfterEach
  public void afterEach() {
    logger.detachAppender(listAppender);
  }

  @Test
  public void givenValid_whenHandle_thenLogInfo() {
    // given
    var clientVersionPacket =
        new ClientVersionPacket().setExecutableName("executableName").setTimestamp("timestamp");
    var session = new Session(33L, channel);

    // when
    clientVersionPacketHandlerService.handle(clientVersionPacket, session);

    // then
    var logsList = listAppender.list;
    assertThat(logsList).hasSize(1);

    var logEvent = logsList.get(0);
    assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(logEvent.getFormattedMessage())
        .isEqualTo("Received client version: executableName, timestamp");
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnPacketType() {
    // given

    // when
    var packetType = clientVersionPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(ClientVersionPacket.class);
  }
}
