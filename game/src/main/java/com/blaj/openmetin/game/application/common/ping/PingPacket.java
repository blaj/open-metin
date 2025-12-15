package com.blaj.openmetin.game.application.common.ping;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x2C, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
public class PingPacket implements Packet {}
