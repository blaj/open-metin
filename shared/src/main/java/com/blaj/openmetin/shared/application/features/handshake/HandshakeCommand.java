package com.blaj.openmetin.shared.application.features.handshake;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import org.joou.UInteger;

public record HandshakeCommand(UInteger handshake, UInteger time, int delta, long sessionId)
    implements Command<Void> {}
