package com.blaj.openmetin.game.application.common.character.dto;

import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UByte;
import org.joou.UInteger;
import org.joou.UShort;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class SimpleCharacterPacket {

  @PacketField(position = 0)
  private Long id = 0L;

  @PacketField(position = 1, length = CharacterConstants.CHARACTER_NAME_MAX_LENGTH)
  private String name = "";

  @PacketField(position = 2)
  private ClassType classType = ClassType.WARRIOR_MALE;

  @PacketField(position = 3)
  private UByte level = UByte.valueOf(0);

  @PacketField(position = 4)
  private UInteger playtime = UInteger.valueOf(0);

  @PacketField(position = 5)
  private UByte st = UByte.valueOf(0);

  @PacketField(position = 6)
  private UByte ht = UByte.valueOf(0);

  @PacketField(position = 7)
  private UByte dx = UByte.valueOf(0);

  @PacketField(position = 8)
  private UByte iq = UByte.valueOf(0);

  @PacketField(position = 9)
  private UShort bodyPart = UShort.valueOf(0);

  @PacketField(position = 10)
  private UByte nameChange = UByte.valueOf(0);

  @PacketField(position = 11)
  private UShort hairPart = UShort.valueOf(0);

  @PacketField(position = 12, arrayLength = 4)
  private byte[] dummy = new byte[4];

  @PacketField(position = 13)
  private int positionX = 0;

  @PacketField(position = 14)
  private int positionY = 0;

  @PacketField(position = 15)
  private int ip = 0;

  @PacketField(position = 16)
  private UShort port = UShort.valueOf(0);

  @PacketField(position = 17)
  private UByte skillGroup = UByte.valueOf(0);
}
