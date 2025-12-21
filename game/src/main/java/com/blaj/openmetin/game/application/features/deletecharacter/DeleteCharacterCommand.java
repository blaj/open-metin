package com.blaj.openmetin.game.application.features.deletecharacter;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record DeleteCharacterCommand(short slot, String deleteCode, long sessionId)
    implements Command<Void> {}
