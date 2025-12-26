package com.blaj.openmetin.game.application.common.character.dto;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UByte;
import org.joou.UInteger;
import org.joou.UShort;

@GeneratePacketCodec
@PacketHeader(header = 0x01, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class SpawnCharacterPacket implements Packet {

  @PacketField(position = 0)
  private UInteger vid;

  @PacketField(position = 1)
  private float angle;

  @PacketField(position = 2)
  private int positionX;

  @PacketField(position = 3)
  private int positionY;

  @PacketField(position = 4)
  private int positionZ;

  @PacketField(position = 5)
  private UByte characterType;

  @PacketField(position = 6)
  private UShort classType;

  @PacketField(position = 7)
  private UByte moveSpeed;

  @PacketField(position = 8)
  private UByte attackSpeed;

  @PacketField(position = 9)
  private UByte state;

  @PacketField(position = 10, arrayLength = 2)
  private UInteger[] affects = new UInteger[2];
}
