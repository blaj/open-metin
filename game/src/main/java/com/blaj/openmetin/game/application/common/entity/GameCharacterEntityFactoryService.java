package com.blaj.openmetin.game.application.common.entity;

import com.blaj.openmetin.game.application.common.game.GameEntityVidAllocator;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameCharacterEntityFactoryService {

  private final GameEntityVidAllocator gameEntityVidAllocator;

  public GameCharacterEntity create(GameSession gameSession, CharacterDto characterDto) {
    return GameCharacterEntity.builder()
        .vid(gameEntityVidAllocator.allocate())
        .session(gameSession)
        .characterDto(characterDto)
        .empire(characterDto.getEmpire())
        .positionX(characterDto.getPositionX())
        .positionY(characterDto.getPositionY())
        .entityClass(characterDto.getClassType().getValue())
        .build();
  }
}
