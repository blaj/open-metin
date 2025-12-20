package com.blaj.openmetin.game.application.common.character.service;

import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.model.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.GameSession;
import org.springframework.stereotype.Service;

@Service
public class GameCharacterEntityFactoryService {

  public GameCharacterEntity create(GameSession gameSession, CharacterDto characterDto) {
    return GameCharacterEntity.builder()
        .session(gameSession)
        .characterDto(characterDto)
        .empire(characterDto.getEmpire())
        .positionX(characterDto.getPositionX())
        .positionY(characterDto.getPositionY())
        .build();
  }
}
