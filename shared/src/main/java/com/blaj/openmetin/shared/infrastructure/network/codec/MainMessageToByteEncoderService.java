package com.blaj.openmetin.shared.infrastructure.network.codec;

import com.blaj.openmetin.shared.common.model.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MainMessageToByteEncoderService extends MessageToByteEncoder<Packet> {

  private final PacketCodecFactoryService packetCodecFactoryService;

  @Override
  protected void encode(
      ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) {
    var packetEncoderServiceOptional =
        packetCodecFactoryService.getPacketEncoderServiceForPacket(packet);

    if (packetEncoderServiceOptional.isEmpty()) {
      log.error(
          "No encoder found for packet: {} to {}",
          packet.getClass().getSimpleName(),
          channelHandlerContext.channel().remoteAddress());
      return;
    }

    try {
      var packetEncoderService = packetEncoderServiceOptional.get();

      byteBuf.writeByte(packetEncoderService.getHeader() & 0xFF);
      packetEncoderService.encode(packet, byteBuf);

      log.debug(
          "Encoded packet: {} (header: 0x{}, to: {})",
          packet.getClass().getSimpleName(),
          String.format("%02X", packetEncoderService.getHeader()),
          channelHandlerContext.channel().remoteAddress());

    } catch (Exception e) {
      log.error(
          "Failed to encode packet: {} to {}",
          packet.getClass().getSimpleName(),
          channelHandlerContext.channel().remoteAddress(),
          e);
    }
  }
}
