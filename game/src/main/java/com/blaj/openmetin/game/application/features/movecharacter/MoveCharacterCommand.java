package com.blaj.openmetin.game.application.features.movecharacter;

import com.blaj.openmetin.game.domain.enums.character.CharacterMovementType;
import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record MoveCharacterCommand(
    CharacterMovementType movementType,
    short argument,
    short rotation,
    int positionX,
    int positionY,
    long time,
    long sessionId)
    implements Command<Void> {}
