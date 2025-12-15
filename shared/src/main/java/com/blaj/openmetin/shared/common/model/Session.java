package com.blaj.openmetin.shared.common.model;

import com.blaj.openmetin.shared.common.enums.Phase;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class Session {
  private final long id;
  private final Channel channel;

  private volatile Long handshake = null;
  private volatile Long lastHandshakeTime = null;
  private volatile boolean handshaking = false;
  private volatile boolean authed = false;
  private volatile Integer pid = null;
  private volatile Integer sequenceIndex = null;
  private volatile Phase phase = Phase.HANDSHAKE;
  private volatile String username = null;
  private volatile Long accountId = null;
}
