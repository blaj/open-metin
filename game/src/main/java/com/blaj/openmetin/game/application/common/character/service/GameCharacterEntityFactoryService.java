package com.blaj.openmetin.game.application.common.character.service;

import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.model.GameCharacterEntity;
import com.blaj.openmetin.shared.common.model.Session;
import org.springframework.stereotype.Service;

@Service
public class GameCharacterEntityFactoryService {

  public GameCharacterEntity create(Session session, CharacterDto characterDto) {
    return GameCharacterEntity.builder()
        .session(session)
        .characterDto(characterDto)
        .empire(characterDto.getEmpire())
        .positionX(characterDto.getPositionX())
        .positionY(characterDto.getPositionY())
        .build();
  }
}
