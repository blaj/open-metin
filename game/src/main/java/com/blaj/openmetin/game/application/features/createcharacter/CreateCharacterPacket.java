package com.blaj.openmetin.game.application.features.createcharacter;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x04, direction = PacketDirection.INCOMING, isSequence = true)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CreateCharacterPacket implements Packet {

  @PacketField(position = 0, unsigned = true)
  private short slot;

  @PacketField(position = 1, length = CharacterConstants.CHARACTER_NAME_MAX_LENGTH)
  private String name;

  @PacketField(position = 2, unsigned = true)
  private int classType;

  @PacketField(position = 3, unsigned = true)
  private short shape;

  @PacketField(position = 4, unsigned = true)
  private short st;

  @PacketField(position = 5, unsigned = true)
  private short ht;

  @PacketField(position = 6, unsigned = true)
  private short dx;

  @PacketField(position = 7, unsigned = true)
  private short iq;
}
