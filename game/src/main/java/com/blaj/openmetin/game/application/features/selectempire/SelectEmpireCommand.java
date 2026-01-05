package com.blaj.openmetin.game.application.features.selectempire;

import com.blaj.openmetin.game.domain.enums.character.Empire;
import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record SelectEmpireCommand(Empire empire, long sessionId) implements Command<Void> {}
