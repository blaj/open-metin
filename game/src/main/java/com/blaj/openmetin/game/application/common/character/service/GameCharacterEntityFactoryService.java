package com.blaj.openmetin.game.application.common.character.service;

import com.blaj.openmetin.game.application.common.game.GameEntityVidAllocator;
import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.model.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.GameSession;
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
        .build();
  }
}
