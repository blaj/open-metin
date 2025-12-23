package com.blaj.openmetin.shared.common.model;

import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import java.util.Optional;

public interface Packet {

  default int getHeader() {
    return Optional.ofNullable(getClass().getAnnotation(PacketHeader.class))
        .map(PacketHeader::header)
        .orElseThrow(
            () -> new IllegalStateException("Packet must be annotated with @PacketHeader"));
  }

  default PacketDirection[] getDirection() {
    return Optional.ofNullable(getClass().getAnnotation(PacketHeader.class))
        .map(PacketHeader::direction)
        .orElseThrow(
            () -> new IllegalStateException("Packet must be annotated with @PacketHeader"));
  }

  default boolean requiresSequence() {
    return Optional.ofNullable(getClass().getAnnotation(PacketHeader.class))
        .map(PacketHeader::isSequence)
        .orElseThrow(
            () -> new IllegalStateException("Packet must be annotated with @PacketHeader"));
  }
}
