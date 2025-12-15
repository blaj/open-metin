package com.blaj.openmetin.game.application.features.pong;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0xFE, direction = PacketDirection.INCOMING)
@Getter
@Setter
@Accessors(chain = true)
public class PongPacket implements Packet {}
