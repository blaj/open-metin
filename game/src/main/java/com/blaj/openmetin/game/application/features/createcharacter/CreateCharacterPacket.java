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
import org.joou.UByte;
import org.joou.UShort;

@GeneratePacketCodec
@PacketHeader(header = 0x04, direction = PacketDirection.INCOMING, isSequence = true)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CreateCharacterPacket implements Packet {

  @PacketField(position = 0)
  private UByte slot;

  @PacketField(position = 1, length = CharacterConstants.CHARACTER_NAME_MAX_LENGTH)
  private String name;

  @PacketField(position = 2)
  private UShort classType;

  @PacketField(position = 3)
  private UByte shape;

  @PacketField(position = 4)
  private UByte st;

  @PacketField(position = 5)
  private UByte ht;

  @PacketField(position = 6)
  private UByte dx;

  @PacketField(position = 7)
  private UByte iq;
}
