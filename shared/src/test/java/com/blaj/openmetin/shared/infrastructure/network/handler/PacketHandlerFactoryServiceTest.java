package com.blaj.openmetin.shared.infrastructure.network.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PacketHandlerFactoryServiceTest {

  private PacketHandlerFactoryService packetHandlerFactoryService;

  @Mock private PacketHandlerService<TestPacket1> testPacket1PacketHandlerService;

  @BeforeEach
  public void beforeEach() {
    given(testPacket1PacketHandlerService.getPacketType()).willReturn(TestPacket1.class);

    packetHandlerFactoryService =
        new PacketHandlerFactoryService(Set.of(testPacket1PacketHandlerService));
  }

  @Test
  public void givenUnregisteredHandler_whenGetPacketHandlerService_thenReturnsEmpty() {
    // given

    // when
    var result = packetHandlerFactoryService.getPacketHandlerService(TestPacket2.class);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenRegisteredHandler_whenGetPacketHandlerService_thenReturnsHandler() {
    // given

    // when
    var result = packetHandlerFactoryService.getPacketHandlerService(TestPacket1.class);

    // then
    assertThat(result).isPresent();
    assertThat(result).contains(testPacket1PacketHandlerService);
  }

  static class TestPacket1 implements Packet {}

  static class TestPacket2 implements Packet {}
}
