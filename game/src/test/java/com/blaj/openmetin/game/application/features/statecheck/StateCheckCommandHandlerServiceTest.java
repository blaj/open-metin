package com.blaj.openmetin.game.application.features.statecheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.entity.ServerStatus;
import com.blaj.openmetin.game.domain.entity.ServerStatus.Status;
import com.blaj.openmetin.game.domain.repository.ServerStatusRepository;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StateCheckCommandHandlerServiceTest {

  private StateCheckCommandHandlerService stateCheckCommandHandlerService;

  @Mock private SessionService sessionService;
  @Mock private ServerStatusRepository serverStatusRepository;

  @Captor private ArgumentCaptor<ServerStatusPacket> serverStatusPacketArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    stateCheckCommandHandlerService =
        new StateCheckCommandHandlerService(sessionService, serverStatusRepository);
  }

  @Test
  public void givenValid_whenHandle_thenSendServerStatusPacket() {
    // given
    var stateCheckCommand = new StateCheckCommand(123L);
    var serverStatus1 =
        ServerStatus.builder().channelIndex(1).port(111).status(Status.UNKNOWN).build();
    var serverStatus2 =
        ServerStatus.builder().channelIndex(2).port(222).status(Status.BUSY).build();

    given(serverStatusRepository.getServerStatuses())
        .willReturn(List.of(serverStatus1, serverStatus2));

    // when
    stateCheckCommandHandlerService.handle(stateCheckCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(
            eq(stateCheckCommand.sessionId()), serverStatusPacketArgumentCaptor.capture());

    var serverStatusPacket = serverStatusPacketArgumentCaptor.getValue();
    assertThat(serverStatusPacket.getSize()).isEqualTo(2);
    assertThat(serverStatusPacket.getStatuses()).isNotEmpty();
    assertThat(serverStatusPacket.getStatuses())
        .contains(
            new ServerStatusPacket.ServerStatus()
                .setStatus(serverStatus1.getStatus())
                .setPort(serverStatus1.getPort()),
            new ServerStatusPacket.ServerStatus()
                .setStatus(serverStatus2.getStatus())
                .setPort(serverStatus2.getPort()));
  }
}
