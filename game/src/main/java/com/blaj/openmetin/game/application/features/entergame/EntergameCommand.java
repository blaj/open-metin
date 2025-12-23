package com.blaj.openmetin.game.application.features.entergame;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record EntergameCommand(long sessionId) implements Command<Void> {}
