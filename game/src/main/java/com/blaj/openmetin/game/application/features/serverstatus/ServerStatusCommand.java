package com.blaj.openmetin.game.application.features.serverstatus;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record ServerStatusCommand(int channelIndex) implements Command<Void> {}
