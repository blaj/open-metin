package com.blaj.openmetin.game.application.features.tokenlogin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenLoginPacketHandlerServiceTest {

  private TokenLoginPacketHandlerService tokenLoginPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<TokenLoginCommand> tokenLoginCommandArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    tokenLoginPacketHandlerService = new TokenLoginPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSend() {
    // given
    var tokenLoginPacket =
        new TokenLoginPacket()
            .setUsername("username")
            .setKey(5435L)
            .setEncryptKeys(new long[] {214, 345, 651, 23235});
    var session = new Session(33L, channel);

    // when
    tokenLoginPacketHandlerService.handle(tokenLoginPacket, session);

    // then
    then(mediator).should().sendAsync(tokenLoginCommandArgumentCaptor.capture());

    var tokenLoginCommand = tokenLoginCommandArgumentCaptor.getValue();
    assertThat(tokenLoginCommand.username()).isEqualTo(tokenLoginPacket.getUsername());
    assertThat(tokenLoginCommand.key()).isEqualTo(tokenLoginPacket.getKey());
    assertThat(tokenLoginCommand.encryptKeys()).isEqualTo(tokenLoginPacket.getEncryptKeys());
    assertThat(tokenLoginCommand.sessionId()).isEqualTo(session.getId());
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnPacketType() {
    // given

    // when
    var packetType = tokenLoginPacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(TokenLoginPacket.class);
  }
}
