package com.blaj.openmetin.shared.application.features.handshake;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record HandshakeCommand(long handshake, long time, int delta, long sessionId)
    implements Command<Void> {}
