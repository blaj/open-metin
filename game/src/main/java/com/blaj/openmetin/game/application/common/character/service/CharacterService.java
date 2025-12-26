package com.blaj.openmetin.game.application.common.character.service;

import com.blaj.openmetin.game.application.common.character.mapper.CharacterDtoMapper;
import com.blaj.openmetin.game.application.common.empire.SelectEmpireService;
import com.blaj.openmetin.game.domain.config.EmpireSpawnConfigs;
import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import com.blaj.openmetin.shared.domain.entity.IdEntity;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.joou.UByte;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterService {

  private final CharacterRepository characterRepository;
  private final SelectEmpireService selectEmpireService;

  @Cacheable(value = "characters", key = "#accountId")
  public List<CharacterDto> getCharacters(long accountId) {
    return characterRepository.findAllByAccountId(accountId).stream()
        .map(CharacterDtoMapper::map)
        .toList();
  }

  public Optional<CharacterDto> getCharacter(long accountId, UByte slot) {
    return getCharacters(accountId).stream()
        .filter(characterDto -> characterDto.getSlot().equals(slot))
        .findFirst();
  }

  @CacheEvict(value = "characters", key = "#accountId")
  public void changeEmpire(long accountId, Empire empire) {}

  @CacheEvict(value = "characters", key = "#accountId")
  public CharacterDto create(
      long accountId, String name, ClassType classType, UByte shape, UByte slot) {
    var empire =
        getCharacters(accountId).stream()
            .map(CharacterDto::getEmpire)
            .findFirst()
            .orElse(selectEmpireService.getFromCache(accountId));

    var jobConfig = classType.getJobType().getJobConfig();
    var empireSpawnsConfig = EmpireSpawnConfigs.getSpawnPosition(empire);

    var character =
        Character.builder()
            .name(name)
            .classType(classType)
            .empire(empire)
            .positionX(empireSpawnsConfig.x())
            .positionY(empireSpawnsConfig.y())
            .st(UByte.valueOf(jobConfig.st()))
            .ht(UByte.valueOf(jobConfig.ht()))
            .dx(UByte.valueOf(jobConfig.dx()))
            .iq(UByte.valueOf(jobConfig.iq()))
            .health((long) jobConfig.startHp())
            .mana((long) jobConfig.startSp())
            .slot(slot)
            .basePart(shape)
            .accountId(accountId)
            .build();

    return CharacterDtoMapper.map(characterRepository.save(character));
  }

  @CacheEvict(value = "characters", key = "#accountId")
  public void delete(long accountId, UByte slot) {
    characterRepository
        .findByAccountIdAndSlot(accountId, slot)
        .map(IdEntity::getId)
        .ifPresent(characterRepository::deleteById);
  }
}
