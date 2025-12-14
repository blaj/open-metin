package com.blaj.openmetin.shared.infrastructure.network.codec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.shared.common.abstractions.PacketEncoderService;
import com.blaj.openmetin.shared.common.model.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MainMessageToByteEncoderServiceTest {

  private MainMessageToByteEncoderService mainMessageToByteEncoderService;

  @Mock private PacketCodecFactoryService packetCodecFactoryService;
  @Mock private ChannelHandlerContext channelHandlerContext;
  @Mock private Channel channel;
  @Mock private SocketAddress remoteAddress;
  @Mock private ByteBuf byteBuf;
  @Mock private PacketEncoderService<Packet> packetEncoderService;

  @BeforeEach
  public void beforeEach() {
    mainMessageToByteEncoderService =
        new MainMessageToByteEncoderService(packetCodecFactoryService);

    given(channelHandlerContext.channel()).willReturn(channel);
    given(channel.remoteAddress()).willReturn(remoteAddress);
  }

  @Test
  public void givenEncoderFound_whenEncode_thenWritesHeaderAndEncodesPacket() throws Exception {
    // given
    var packet = new TestPacket();

    given(packetCodecFactoryService.getPacketEncoderServiceForPacket(packet))
        .willReturn(Optional.of(packetEncoderService));
    given(packetEncoderService.getHeader()).willReturn(0x01);

    // when
    mainMessageToByteEncoderService.encode(channelHandlerContext, packet, byteBuf);

    // then
    then(byteBuf).should().writeByte((byte) 0x01);
    then(packetEncoderService).should().encode(packet, byteBuf);
  }

  @Test
  public void givenEncoderNotFound_whenEncode_thenDoesNotWriteAnything() throws Exception {
    // given
    var packet = new TestPacket();

    given(packetCodecFactoryService.getPacketEncoderServiceForPacket(packet))
        .willReturn(Optional.empty());

    // when
    mainMessageToByteEncoderService.encode(channelHandlerContext, packet, byteBuf);

    // then
    then(byteBuf).should(never()).writeByte(any(Byte.class));
    then(packetEncoderService).should(never()).encode(any(), any());
  }

  @Test
  public void givenEncoderThrowsException_whenEncode_thenCatchesException() throws Exception {
    // given
    var packet = new TestPacket();

    given(packetCodecFactoryService.getPacketEncoderServiceForPacket(packet))
        .willReturn(Optional.of(packetEncoderService));
    given(packetEncoderService.getHeader()).willReturn(0x01);

    doThrow(new RuntimeException("Encoding failed"))
        .when(packetEncoderService)
        .encode(eq(packet), eq(byteBuf));

    // when
    mainMessageToByteEncoderService.encode(channelHandlerContext, packet, byteBuf);

    // then
    then(byteBuf).should().writeByte((byte) 0x01);
  }

  @Test
  public void givenEncoderFound_whenEncode_thenCallsEncoderWithCorrectHeader() throws Exception {
    // given
    var packet = new TestPacket();
    var expectedHeader = 0x42;

    given(packetCodecFactoryService.getPacketEncoderServiceForPacket(packet))
        .willReturn(Optional.of(packetEncoderService));
    given(packetEncoderService.getHeader()).willReturn(expectedHeader);

    // when
    mainMessageToByteEncoderService.encode(channelHandlerContext, packet, byteBuf);

    // then
    then(byteBuf).should().writeByte(expectedHeader);
    then(packetEncoderService).should().encode(packet, byteBuf);
  }

  static class TestPacket implements Packet {}
}
