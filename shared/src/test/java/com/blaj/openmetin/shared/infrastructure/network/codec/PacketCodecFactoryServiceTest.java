package com.blaj.openmetin.shared.infrastructure.network.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.shared.common.abstractions.PacketDecoderService;
import com.blaj.openmetin.shared.common.abstractions.PacketEncoderService;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PacketCodecFactoryServiceTest {

  @Mock private PacketEncoderService<TestPacket> testPacketPacketEncoderService;
  @Mock private PacketDecoderService<TestPacket> testPacketPacketDecoderService;

  private PacketCodecFactoryService packetCodecFactoryService;

  @BeforeEach
  public void beforeEach() {
    given(testPacketPacketEncoderService.getPacketClass()).willReturn(TestPacket.class);
    given(testPacketPacketDecoderService.getHeader()).willReturn(100);

    packetCodecFactoryService =
        new PacketCodecFactoryService(
            Set.of(testPacketPacketEncoderService), Set.of(testPacketPacketDecoderService));
  }

  @Test
  public void givenUnregisteredDecoder_whenGetPacketDecoderService_thenReturnsEmpty() {
    // given

    // when
    var result = packetCodecFactoryService.getPacketDecoderService(999);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenRegisteredDecoder_whenGetPacketDecoderService_thenReturnsDecoder() {
    // given

    // when
    var result = packetCodecFactoryService.getPacketDecoderService(100);

    // then
    assertThat(result).isPresent();
    assertThat(result).contains(testPacketPacketDecoderService);
  }

  @Test
  public void givenUnregisteredEncoder_whenGetPacketEncoderService_thenReturnsEmpty() {
    // given

    // when
    var result = packetCodecFactoryService.getPacketEncoderService(Packet.class);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenRegisteredEncoder_whenGetPacketEncoderService_thenReturnsEncoder() {
    // given

    // when
    var result = packetCodecFactoryService.getPacketEncoderService(TestPacket.class);

    // then
    assertThat(result).isPresent();
    assertThat(result).contains(testPacketPacketEncoderService);
  }

  @Test
  public void givenUnregisteredEncoder_whenGetPacketEncoderServiceForPacket_thenReturnsEmpty() {
    // given
    var packet = new Packet() {};

    // when
    var result = packetCodecFactoryService.getPacketEncoderServiceForPacket(packet);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenRegisteredEncoder_whenGetPacketEncoderServiceForPacket_thenReturnsEncoder() {
    // given
    var packet = new TestPacket();

    // when
    var result = packetCodecFactoryService.getPacketEncoderServiceForPacket(packet);

    // then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testPacketPacketEncoderService);
  }

  static class TestPacket implements Packet {}
}
