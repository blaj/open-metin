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
@PacketHeader(header = 0x88, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CharacterAdditionalDataPacket implements Packet {

  @PacketField(position = 6)
  public short rankPoints;
  @PacketField(position = 7, unsigned = true)
  public short pkMode;
  @PacketField(position = 8, unsigned = true)
  public long mountVnum;
  @PacketField(position = 0, unsigned = true)
  private long vid;
  @PacketField(position = 1, length = CharacterConstants.CHARACTER_NAME_MAX_LENGTH)
  private String name;
  @PacketField(position = 2, arrayLength = 4, unsigned = true)
  private int[] parts = new int[4];
  @PacketField(position = 3)
  private Empire empire;
  @PacketField(position = 4, unsigned = true)
  private long guildId;
  @PacketField(position = 5, unsigned = true)
  private long level;
}
