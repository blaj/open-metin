package com.blaj.openmetin.game.application.common.character.dto;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x20, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CharacterListPacket implements Packet {

  @PacketField(position = 0, arrayLength = 4)
  private SimpleCharacterPacket[] simpleCharacterPackets = new SimpleCharacterPacket[4];

  @PacketField(position = 1, arrayLength = 4, unsigned = true)
  private long[] guildIds = new long[4];

  @PacketField(position = 2, arrayLength = 4, length = 13)
  private String[] guildNames = new String[4];

  @PacketField(position = 3, unsigned = true)
  private long handle = 0L;

  @PacketField(position = 4, unsigned = true)
  private long randomKey = 0L;

  {
    for (var i = 0; i < simpleCharacterPackets.length; i++) {
      simpleCharacterPackets[i] = new SimpleCharacterPacket();
    }

    Arrays.fill(guildIds, 0);
    Arrays.fill(guildNames, "\0".repeat(13));
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  @EqualsAndHashCode
  public static class SimpleCharacterPacket {

    @PacketField(position = 0, unsigned = true)
    private long id = 0L;

    @PacketField(position = 1, length = CharacterConstants.CHARACTER_NAME_MAX_LENGTH)
    private String name = "";

    @PacketField(position = 2)
    private ClassType classType = ClassType.WARRIOR_MALE;

    @PacketField(position = 3, unsigned = true)
    private short level = 0;

    @PacketField(position = 4, unsigned = true)
    private long playtime = 0L;

    @PacketField(position = 5, unsigned = true)
    private short st = 0;

    @PacketField(position = 6, unsigned = true)
    private short ht = 0;

    @PacketField(position = 7, unsigned = true)
    private short dx = 0;

    @PacketField(position = 8, unsigned = true)
    private short iq = 0;

    @PacketField(position = 9, unsigned = true)
    private int bodyPart = 0;

    @PacketField(position = 10, unsigned = true)
    private short nameChange = 0;

    @PacketField(position = 11, unsigned = true)
    private int hairPart = 0;

    @PacketField(position = 12, arrayLength = 4)
    private byte[] dummy = new byte[4];

    @PacketField(position = 13)
    private int positionX = 0;

    @PacketField(position = 14)
    private int positionY = 0;

    @PacketField(position = 15)
    private int ip = 0;

    @PacketField(position = 16, unsigned = true)
    private int port = 0;

    @PacketField(position = 17, unsigned = true)
    private short skillGroup = 0;
  }
}
