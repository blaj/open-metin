package com.blaj.openmetin.game.application.features.deletecharacter;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import org.joou.UByte;

public record DeleteCharacterCommand(UByte slot, String deleteCode, long sessionId)
    implements Command<Void> {}
