package com.blaj.openmetin.game.application.common.character.dto;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UInteger;

@GeneratePacketCodec
@PacketHeader(header = 0x20, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CharacterListPacket implements Packet {

  @PacketField(position = 0, arrayLength = 4)
  private SimpleCharacterPacket[] simpleCharacterPackets = new SimpleCharacterPacket[4];

  @PacketField(position = 1, arrayLength = 4)
  private UInteger[] guildIds = new UInteger[4];

  @PacketField(position = 2, arrayLength = 4, length = 13)
  private String[] guildNames = new String[4];

  @PacketField(position = 3)
  private UInteger handle = UInteger.valueOf(0);

  @PacketField(position = 4)
  private UInteger randomKey = UInteger.valueOf(0);

  {
    for (var i = 0; i < simpleCharacterPackets.length; i++) {
      simpleCharacterPackets[i] = new SimpleCharacterPacket();
    }

    Arrays.fill(guildIds, 0);
    Arrays.fill(guildNames, "\0".repeat(13));
  }
}
