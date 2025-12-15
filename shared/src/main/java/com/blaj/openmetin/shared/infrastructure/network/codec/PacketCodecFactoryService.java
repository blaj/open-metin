package com.blaj.openmetin.shared.infrastructure.network.codec;

import com.blaj.openmetin.shared.common.abstractions.PacketDecoderService;
import com.blaj.openmetin.shared.common.abstractions.PacketEncoderService;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PacketCodecFactoryService {

  private final Map<Class<?>, PacketEncoderService<?>> packetEncoderServiceMap;
  private final Map<Integer, PacketDecoderService<?>> packetDecoderServiceMap;

  public PacketCodecFactoryService(
      Set<PacketEncoderService<?>> packetEncoderServices,
      Set<PacketDecoderService<?>> packetDecoderServices) {
    packetEncoderServiceMap = createPacketEncoderServiceMap(packetEncoderServices);
    packetDecoderServiceMap = createPacketDecoderServiceMap((packetDecoderServices));
  }

  public Optional<PacketDecoderService<?>> getPacketDecoderService(int header) {
    return Optional.ofNullable(packetDecoderServiceMap.get(header));
  }

  @SuppressWarnings("unchecked")
  public <T extends Packet> Optional<PacketEncoderService<T>> getPacketEncoderService(
      Class<T> packetClass) {
    return Optional.ofNullable(packetEncoderServiceMap.get(packetClass))
        .map(packetEncoderService -> (PacketEncoderService<T>) packetEncoderService);
  }

  @SuppressWarnings("unchecked")
  public Optional<PacketEncoderService<Packet>> getPacketEncoderServiceForPacket(Packet packet) {
    return Optional.ofNullable(packetEncoderServiceMap.get(packet.getClass()))
        .map(encoder -> (PacketEncoderService<Packet>) encoder);
  }

  private Map<Class<?>, PacketEncoderService<?>> createPacketEncoderServiceMap(
      Set<PacketEncoderService<?>> packetEncoderServices) {
    return packetEncoderServices.stream()
        .collect(Collectors.toMap(PacketEncoderService::getPacketClass, Function.identity()));
  }

  private Map<Integer, PacketDecoderService<?>> createPacketDecoderServiceMap(
      Set<PacketDecoderService<?>> packetDecoderServices) {
    return packetDecoderServices.stream()
        .collect(Collectors.toMap(PacketDecoderService::getHeader, Function.identity()));
  }
}
