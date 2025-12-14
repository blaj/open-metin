package com.blaj.openmetin.authentication.application.features.closeconnection;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CloseConnectionCommandHandlerServiceTest {

  private CloseConnectionCommandHandlerService closeConnectionCommandHandlerService;

  @Mock private SessionManagerService sessionManagerService;
  @Mock private Session session;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    closeConnectionCommandHandlerService =
        new CloseConnectionCommandHandlerService(sessionManagerService);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenDoesNothing() {
    // given
    var command = new CloseConnectionCommand(999L);

    given(sessionManagerService.getSessionByAccountId(999L)).willReturn(Optional.empty());

    // when
    closeConnectionCommandHandlerService.handle(command);

    // then
    then(channel).should(never()).close();
    then(sessionManagerService).should(never()).removeSession(1L);
  }

  @Test
  public void givenExistingSession_whenHandle_thenClosesChannelAndRemovesSession() {
    // given
    var command = new CloseConnectionCommand(123L);

    given(session.getChannel()).willReturn(channel);
    given(session.getId()).willReturn(1L);
    given(sessionManagerService.getSessionByAccountId(123L)).willReturn(Optional.of(session));

    // when
    closeConnectionCommandHandlerService.handle(command);

    // then
    then(channel).should().close();
    then(sessionManagerService).should().removeSession(1L);
  }
}
