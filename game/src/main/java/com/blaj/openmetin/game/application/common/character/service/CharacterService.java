package com.blaj.openmetin.game.application.common.character.service;

import com.blaj.openmetin.game.application.common.character.dto.CharacterDto;
import com.blaj.openmetin.game.application.common.character.mapper.CharacterDtoMapper;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterService {

  private final CharacterRepository characterRepository;

  @Cacheable(value = "characters", key = "#accountId")
  public List<CharacterDto> getCharacters(long accountId) {
    return characterRepository.findAllByAccountId(accountId).stream()
        .map(CharacterDtoMapper::map)
        .toList();
  }
}
