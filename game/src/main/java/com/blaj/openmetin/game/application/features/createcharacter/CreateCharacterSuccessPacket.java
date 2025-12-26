package com.blaj.openmetin.game.application.features.createcharacter;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.application.common.character.dto.SimpleCharacterPacket;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UByte;

@GeneratePacketCodec
@PacketHeader(header = 0x08, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CreateCharacterSuccessPacket implements Packet {

  @PacketField(position = 0)
  private UByte slot;

  @PacketField(position = 1)
  private SimpleCharacterPacket simpleCharacterPacket;
}
