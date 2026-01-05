package com.blaj.openmetin.game.application.features.createcharacter;

import com.blaj.openmetin.game.domain.enums.character.ClassType;
import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record CreateCharacterCommand(
    short slot, String name, ClassType classType, short shape, long sessionId)
    implements Command<Void> {}
