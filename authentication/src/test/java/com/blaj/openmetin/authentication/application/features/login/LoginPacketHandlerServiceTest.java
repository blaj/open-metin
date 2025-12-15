package com.blaj.openmetin.authentication.application.features.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoginPacketHandlerServiceTest {

  private LoginPacketHandlerService loginPacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @Captor private ArgumentCaptor<LoginCommand> loginCommandArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    loginPacketHandlerService = new LoginPacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendCommand() {
    // given
    var loginPacket =
        new LoginPacket()
            .setUsername("username")
            .setPassword("password")
            .setEncryptKeys(new long[] {1, 2, 3, 4});
    var session = new Session(123L, channel);
    var remoteAddress = InetSocketAddress.createUnresolved("localhost", 123);

    given(channel.remoteAddress()).willReturn(remoteAddress);

    // when
    loginPacketHandlerService.handle(loginPacket, session);

    // then
    then(mediator).should().sendAsync(loginCommandArgumentCaptor.capture());

    var loginCommand = loginCommandArgumentCaptor.getValue();
    assertThat(loginCommand.username()).isEqualTo(loginPacket.getUsername());
    assertThat(loginCommand.password()).isEqualTo(loginPacket.getPassword());
    assertThat(loginCommand.encryptKeys()).isEqualTo(loginPacket.getEncryptKeys());
    assertThat(loginCommand.sessionId()).isEqualTo(session.getId());
    assertThat(loginCommand.socketAddress()).isEqualTo(remoteAddress);
  }

  @Test
  public void whenGetPacketType_thenReturnLoginPacketClass() {
    // given

    // when
    var packetTypeClass = loginPacketHandlerService.getPacketType();

    // then
    assertThat(packetTypeClass).isEqualTo(LoginPacket.class);
  }
}
