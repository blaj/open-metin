package com.blaj.openmetin.game.application.features.tokenlogin;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import org.joou.UInteger;

public record TokenLoginCommand(String username, UInteger key, UInteger[] encryptKeys, long sessionId)
    implements Command<Void> {}
