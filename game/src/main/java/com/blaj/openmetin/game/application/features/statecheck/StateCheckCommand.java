package com.blaj.openmetin.game.application.features.statecheck;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record StateCheckCommand(long sessionId) implements Command<Void> {}
