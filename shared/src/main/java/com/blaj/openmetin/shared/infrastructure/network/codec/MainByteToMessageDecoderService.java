package com.blaj.openmetin.shared.infrastructure.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MainByteToMessageDecoderService extends ByteToMessageDecoder {

  private final PacketCodecFactoryService packetCodecFactoryService;

  @Override
  protected void decode(
      ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> outList) {
    if (byteBuf.readableBytes() < 1) {
      return;
    }

    byteBuf.markReaderIndex();
    var header = byteBuf.readByte() & 0xFF;

    var packetDecoderOptional = packetCodecFactoryService.getPacketDecoderService(header);

    if (packetDecoderOptional.isEmpty()) {
      log.warn(
          "Unknown packet header from {}: 0x{}",
          channelHandlerContext.channel().remoteAddress(),
          String.format("%02X", header));

      byteBuf.resetReaderIndex();
      byteBuf.skipBytes(byteBuf.readableBytes());

      return;
    }

    try {
      var decoder = packetDecoderOptional.get();
      var packet = decoder.decode(byteBuf);
      outList.add(packet);

      log.debug(
          "Decoded packet: {} (header: 0x{}, from: {})",
          packet.getClass().getSimpleName(),
          String.format("%02X", header),
          channelHandlerContext.channel().remoteAddress());
    } catch (IndexOutOfBoundsException e) {
      log.debug(
          "Not enough data to decode packet 0x{}, waiting for more...",
          String.format("%02X", header));

      byteBuf.resetReaderIndex();
    } catch (Exception e) {
      log.error(
          "Failed to decode packet with header 0x{} from {}",
          String.format("%02X", header),
          channelHandlerContext.channel().remoteAddress(),
          e);

      byteBuf.resetReaderIndex();
      channelHandlerContext.close();
    }
  }
}
