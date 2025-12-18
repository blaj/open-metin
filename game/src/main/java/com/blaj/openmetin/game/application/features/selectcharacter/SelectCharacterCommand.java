package com.blaj.openmetin.game.application.features.selectcharacter;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record SelectCharacterCommand(short slot, long sessionId) implements Command<Void> {}
