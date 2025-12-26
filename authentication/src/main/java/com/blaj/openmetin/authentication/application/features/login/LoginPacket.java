package com.blaj.openmetin.authentication.application.features.login;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UInteger;

@GeneratePacketCodec
@PacketHeader(header = 0x6f, direction = PacketDirection.INCOMING, isSequence = true)
@Getter
@Setter
@Accessors(chain = true)
public class LoginPacket implements Packet {

  @PacketField(position = 0, length = 31)
  private String username;

  @PacketField(position = 1, length = 17)
  private String password;

  @PacketField(position = 2, arrayLength = 4)
  private UInteger[] encryptKeys = new UInteger[4];
}
