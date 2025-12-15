package com.blaj.openmetin.game.application.features.serverstatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.entity.ServerStatus;
import com.blaj.openmetin.game.domain.entity.ServerStatus.Status;
import com.blaj.openmetin.game.domain.repository.ServerStatusRepository;
import com.blaj.openmetin.shared.infrastructure.network.properties.TcpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServerStatusCommandHandlerServiceTest {

  private ServerStatusCommandHandlerService serverStatusCommandHandlerService;

  @Mock private ServerStatusRepository serverStatusRepository;
  @Mock private TcpProperties tcpProperties;

  @Captor private ArgumentCaptor<ServerStatus> serverStatusArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    serverStatusCommandHandlerService =
        new ServerStatusCommandHandlerService(serverStatusRepository, tcpProperties);
  }

  @Test
  public void givenValid_whenHandle_thenSaveServerStatus() {
    // given
    var serverStatusCommand = new ServerStatusCommand(22);
    var port = 244;

    given(tcpProperties.port()).willReturn(port);

    // when
    serverStatusCommandHandlerService.handle(serverStatusCommand);

    // then
    then(serverStatusRepository).should().saveServerStatus(serverStatusArgumentCaptor.capture());

    var serverStatus = serverStatusArgumentCaptor.getValue();
    assertThat(serverStatus.getChannelIndex()).isEqualTo(serverStatusCommand.channelIndex());
    assertThat(serverStatus.getPort()).isEqualTo(port);
    assertThat(serverStatus.getStatus()).isEqualTo(Status.NORMAL);
  }
}
