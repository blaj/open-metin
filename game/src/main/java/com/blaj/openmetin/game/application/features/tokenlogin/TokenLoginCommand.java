package com.blaj.openmetin.game.application.features.tokenlogin;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record TokenLoginCommand(String username, long key, long[] encryptKeys, long sessionId)
    implements Command<Void> {}
