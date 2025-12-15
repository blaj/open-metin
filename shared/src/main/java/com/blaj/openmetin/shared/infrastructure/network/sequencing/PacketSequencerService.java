package com.blaj.openmetin.shared.infrastructure.network.sequencing;

import com.blaj.openmetin.shared.common.model.Session;
import org.springframework.stereotype.Service;

@Service
public class PacketSequencerService {
  private static final byte[] table = init();

  private static byte[] init() {
    var bytesTable = new byte[256];

    for (int i = 0; i < 256; i++) {
      bytesTable[i] = (byte) i;
    }

    return bytesTable;
  }

  public boolean consumeInbound(byte actual, Session session) {
    var expected = table[session.getSequenceIndex() & 0xFF];

    if (actual != expected) {
      return false;
    }

    session.setSequenceIndex((session.getSequenceIndex() + 1) & 0xFF);

    return true;
  }

  public byte nextOutbound(Session session) {
    var out = table[session.getSequenceIndex() & 0xFF];

    session.setSequenceIndex((session.getSequenceIndex() + 1) & 0xFF);

    return out;
  }

  public void reset(Session session) {
    session.setSequenceIndex(0);
  }
}
