package com.blaj.openmetin.shared.application.features.phase;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0xfd, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class PhasePacket implements Packet {

  @PacketField(position = 0)
  private Phase phase;
}
