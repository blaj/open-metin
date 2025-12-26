package com.blaj.openmetin.game.application.features.createcharacter;

import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import org.joou.UByte;

public record CreateCharacterCommand(
    UByte slot, String name, ClassType classType, UByte shape, long sessionId)
    implements Command<Void> {}
