package com.blaj.openmetin.shared.infrastructure.network.handler;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PacketHandlerFactoryService {

  private final Map<Class<? extends Packet>, PacketHandlerService<?>> packetHandlerServiceMap;

  public PacketHandlerFactoryService(Set<PacketHandlerService<?>> packetHandlerServices) {
    packetHandlerServiceMap = createPacketHandlerServiceMap(packetHandlerServices);
  }

  @SuppressWarnings("unchecked")
  public <T extends Packet> Optional<PacketHandlerService<T>> getPacketHandlerService(
      Class<T> packetClass) {
    return Optional.ofNullable((PacketHandlerService<T>) packetHandlerServiceMap.get(packetClass));
  }

  private Map<Class<? extends Packet>, PacketHandlerService<?>> createPacketHandlerServiceMap(
      Set<PacketHandlerService<?>> packetHandlerServices) {
    return packetHandlerServices.stream()
        .collect(Collectors.toMap(PacketHandlerService::getPacketType, Function.identity()));
  }
}
