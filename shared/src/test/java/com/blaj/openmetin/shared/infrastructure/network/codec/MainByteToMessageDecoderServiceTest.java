package com.blaj.openmetin.shared.infrastructure.network.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.shared.common.abstractions.PacketDecoderService;
import com.blaj.openmetin.shared.common.model.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MainByteToMessageDecoderServiceTest {

  private MainByteToMessageDecoderService mainByteToMessageDecoderService;
  private ByteBuf byteBuf;
  private List<Object> outList;

  @Mock private PacketCodecFactoryService packetCodecFactoryService;
  @Mock private ChannelHandlerContext channelHandlerContext;
  @Mock private Channel channel;
  @Mock private SocketAddress remoteAddress;
  @Mock private PacketDecoderService<Packet> packetDecoderService;

  @BeforeEach
  public void beforeEach() {
    mainByteToMessageDecoderService =
        new MainByteToMessageDecoderService(packetCodecFactoryService);
    byteBuf = Unpooled.buffer();
    outList = new ArrayList<>();

    given(channelHandlerContext.channel()).willReturn(channel);
    given(channel.remoteAddress()).willReturn(remoteAddress);
  }

  @AfterEach
  public void afterEach() {
    byteBuf.release();
  }

  @Test
  public void givenDecoderNotFound_whenDecode_thenSkipsAllBytes() {
    // given
    byteBuf = Unpooled.buffer();
    byteBuf.writeByte(0x99);
    byteBuf.writeInt(12345);

    given(packetCodecFactoryService.getPacketDecoderService(0x99)).willReturn(Optional.empty());

    // when
    mainByteToMessageDecoderService.decode(channelHandlerContext, byteBuf, outList);

    // then
    assertThat(outList).isEmpty();
    assertThat(byteBuf.readableBytes()).isZero();
  }

  @Test
  public void givenIndexOutOfBoundsException_whenDecode_thenResetsReaderIndex() {
    // given
    byteBuf.writeByte(0x01);

    given(packetCodecFactoryService.getPacketDecoderService(0x01))
        .willReturn(Optional.of(packetDecoderService));
    given(packetDecoderService.decode(byteBuf))
        .willThrow(new IndexOutOfBoundsException("Not enough data"));

    // when
    mainByteToMessageDecoderService.decode(channelHandlerContext, byteBuf, outList);

    // then
    assertThat(outList).isEmpty();
    assertThat(byteBuf.readerIndex()).isZero();

    then(channelHandlerContext).should(never()).close();
  }

  @Test
  public void givenOtherException_whenDecode_thenResetsReaderIndexAndClosesChannel() {
    // given
    byteBuf.writeByte(0x01);

    given(packetCodecFactoryService.getPacketDecoderService(0x01))
        .willReturn(Optional.of(packetDecoderService));
    given(packetDecoderService.decode(byteBuf)).willThrow(new RuntimeException("Decoding failed"));

    // when
    mainByteToMessageDecoderService.decode(channelHandlerContext, byteBuf, outList);

    // then
    assertThat(outList).isEmpty();
    assertThat(byteBuf.readerIndex()).isZero();

    then(channelHandlerContext).should().close();
  }

  @Test
  public void givenNoReadableBytes_whenDecode_thenDoesNothing() {
    // given

    // when
    mainByteToMessageDecoderService.decode(channelHandlerContext, byteBuf, outList);

    // then
    assertThat(outList).isEmpty();
  }

  @Test
  public void givenDecoderFound_whenDecode_thenDecodesPacketAndAddsToOutList() {
    // given
    byteBuf.writeByte(0x01);
    var expectedPacket = new TestPacket();

    given(packetCodecFactoryService.getPacketDecoderService(0x01))
        .willReturn(Optional.of(packetDecoderService));
    given(packetDecoderService.decode(byteBuf)).willReturn(expectedPacket);

    // when
    mainByteToMessageDecoderService.decode(channelHandlerContext, byteBuf, outList);

    // then
    assertThat(outList).containsExactly(expectedPacket);
  }

  @Test
  public void givenMultiplePackets_whenDecode_thenDecodesOnlyFirst() {
    // given
    byteBuf.writeByte(0x01);
    byteBuf.writeByte(0x02);
    var expectedPacket = new TestPacket();

    given(packetCodecFactoryService.getPacketDecoderService(0x01))
        .willReturn(Optional.of(packetDecoderService));
    given(packetDecoderService.decode(byteBuf)).willReturn(expectedPacket);

    // when
    mainByteToMessageDecoderService.decode(channelHandlerContext, byteBuf, outList);

    // then
    assertThat(outList).containsExactly(expectedPacket);
    assertThat(byteBuf.readableBytes()).isEqualTo(1);
  }

  static class TestPacket implements Packet {}
}
