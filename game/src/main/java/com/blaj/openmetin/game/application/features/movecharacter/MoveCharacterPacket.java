package com.blaj.openmetin.game.application.features.movecharacter;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.domain.enums.character.CharacterMovementType;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x07, direction = PacketDirection.INCOMING, isSequence = true)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class MoveCharacterPacket implements Packet {

  @PacketField(position = 0)
  private CharacterMovementType movementType;

  @PacketField(position = 1, unsigned = true)
  private short argument;

  @PacketField(position = 2, unsigned = true)
  private short rotation;

  @PacketField(position = 3)
  private int positionX;

  @PacketField(position = 4)
  private int positionY;

  @PacketField(position = 5, unsigned = true)
  private long time;
}
