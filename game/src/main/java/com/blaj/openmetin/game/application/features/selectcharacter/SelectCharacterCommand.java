package com.blaj.openmetin.game.application.features.selectcharacter;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import org.joou.UByte;

public record SelectCharacterCommand(UByte slot, long sessionId) implements Command<Void> {}
