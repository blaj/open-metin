package com.blaj.openmetin.game.application.features.statecheck;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.domain.entity.ServerStatus.Status;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UInteger;
import org.joou.UShort;

@GeneratePacketCodec
@PacketHeader(header = 0xD2, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class ServerStatusPacket implements Packet {

  @PacketField(position = 0)
  private UInteger size;

  @PacketField(position = 1, arrayLength = 1)
  private ServerStatus[] statuses;

  @Getter
  @Setter
  @Accessors(chain = true)
  @EqualsAndHashCode
  public static class ServerStatus {

    @PacketField(position = 0)
    private UShort port;

    @PacketField(position = 1)
    private Status status;
  }
}
