package com.blaj.openmetin.game.application.features.serverstatus;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import org.joou.UByte;

public record ServerStatusCommand(UByte channelIndex) implements Command<Void> {}
