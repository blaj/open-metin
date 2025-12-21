package com.blaj.openmetin.game.application.features.selectempire;

import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record SelectEmpireCommand(Empire empire, long sessionId) implements Command<Void> {}
