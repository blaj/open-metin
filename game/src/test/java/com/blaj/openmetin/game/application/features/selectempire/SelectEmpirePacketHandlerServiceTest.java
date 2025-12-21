package com.blaj.openmetin.game.application.features.selectempire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.empire.EmpirePacket;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SelectEmpirePacketHandlerServiceTest {

  private SelectEmpirePacketHandlerService selectEmpirePacketHandlerService;

  @Mock private Mediator mediator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    selectEmpirePacketHandlerService = new SelectEmpirePacketHandlerService(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSendAsync() {
    // given
    var sessionId = 123L;
    var empirePacket = new EmpirePacket().setEmpire(Empire.SHINSOO);
    var gameSession = new GameSession(sessionId, channel);

    // when
    selectEmpirePacketHandlerService.handle(empirePacket, gameSession);

    // then
    then(mediator).should().sendAsync(new SelectEmpireCommand(empirePacket.getEmpire(), sessionId));
  }

  @Test
  public void givenValid_whenGetPacketType_thenReturnEmpirePacketClass() {
    // given

    // when
    var packetType = selectEmpirePacketHandlerService.getPacketType();

    // then
    assertThat(packetType).isEqualTo(EmpirePacket.class);
  }
}
