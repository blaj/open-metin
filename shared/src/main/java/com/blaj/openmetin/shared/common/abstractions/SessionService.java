package com.blaj.openmetin.shared.common.abstractions;

import com.blaj.openmetin.shared.common.model.Packet;

public interface SessionService {

  void sendPacketAsync(long sessionId, Packet packet);

  void sendPacketSync(long sessionId, Packet packet);
}
