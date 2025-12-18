package com.blaj.openmetin.game.application.common.character.dto;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x71, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CharacterBasicDataPacket implements Packet {

  @PacketField(position = 0, unsigned = true)
  private long vid;

  @PacketField(position = 1, unsigned = true)
  private int classType;

  @PacketField(position = 2, length = CharacterConstants.CHARACTER_NAME_MAX_LENGTH)
  private String name;

  @PacketField(position = 3)
  private int positionX;

  @PacketField(position = 4)
  private int positionY;

  @PacketField(position = 5)
  private int positionZ;

  @PacketField(position = 6)
  private Empire empire;

  @PacketField(position = 7, unsigned = true)
  private short skillGroup;
}
