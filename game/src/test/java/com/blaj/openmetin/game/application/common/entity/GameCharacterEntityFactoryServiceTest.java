package com.blaj.openmetin.game.application.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.application.common.game.GameEntityVidAllocator;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameCharacterEntityFactoryServiceTest {

  private GameCharacterEntityFactoryService gameCharacterEntityFactoryService;

  @Mock private GameEntityVidAllocator gameEntityVidAllocator;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    gameCharacterEntityFactoryService =
        new GameCharacterEntityFactoryService(gameEntityVidAllocator);
  }

  @Test
  public void givenValid_whenCreate_thenReturnGameCharacterEntity() {
    // given
    var sessionId = 123L;
    var gameSession = new GameSession(sessionId, channel);
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(31)
            .playTime(32432L)
            .st(33)
            .ht(44)
            .dx(76)
            .iq(63)
            .bodyPart(47)
            .hairPart(102)
            .positionX(32423)
            .positionY(43654)
            .build();

    // when
    var gameCharacterEntity = gameCharacterEntityFactoryService.create(gameSession, characterDto);

    // then
    assertThat(gameCharacterEntity).isNotNull();
    assertThat(gameCharacterEntity.getSession()).isEqualTo(gameSession);
    assertThat(gameCharacterEntity.getCharacterDto()).isEqualTo(characterDto);
    assertThat(gameCharacterEntity.getEmpire()).isEqualTo(characterDto.getEmpire());
    assertThat(gameCharacterEntity.getPositionX()).isEqualTo(characterDto.getPositionX());
    assertThat(gameCharacterEntity.getPositionY()).isEqualTo(characterDto.getPositionY());
  }
}
