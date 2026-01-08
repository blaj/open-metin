package com.blaj.openmetin.game.infrastructure.service.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.game.application.common.monster.MonsterDefinitionService;
import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnimationProviderServiceImplTest {

  private AnimationProviderServiceImpl animationProviderService;

  @Mock private AnimationFileLoaderService animationFileLoaderService;
  @Mock private AnimationCacheService animationCacheService;
  @Mock private MonsterDefinitionService monsterDefinitionService;
  @Mock private DataPathProperties dataPathProperties;

  @Mock private Animation animation;
  @Mock private MonsterDefinition monsterDefinition;

  @BeforeEach
  public void beforeEach() {
    animationProviderService =
        new AnimationProviderServiceImpl(
            animationFileLoaderService,
            animationCacheService,
            monsterDefinitionService,
            dataPathProperties);
  }

  @Test
  public void givenAnimationFilesNotFound_whenLoadAnimations_thenDoNotCacheAnimations() {
    // given
    given(dataPathProperties.baseDirectory()).willReturn(Path.of("/data"));
    given(animationFileLoaderService.loadAnimation(any(Path.class))).willReturn(Optional.empty());

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should(never())
        .put(any(Long.class), any(AnimationType.class), any(AnimationSubType.class), any());
  }

  @Test
  public void givenAnimationFiles_whenLoadAnimations_thenLoadAndCacheAllCharacterAnimations() {
    // given
    given(dataPathProperties.baseDirectory()).willReturn(Path.of("/data"));
    given(animationFileLoaderService.loadAnimation(any(Path.class)))
        .willReturn(Optional.of(animation));

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should(times(16))
        .put(any(Long.class), any(AnimationType.class), any(AnimationSubType.class), eq(animation));

    then(animationFileLoaderService)
        .should()
        .loadAnimation(Path.of("/data/pc/warrior/general/walk.msa"));
    then(animationFileLoaderService)
        .should()
        .loadAnimation(Path.of("/data/pc2/assassin/general/run.msa"));

    then(animationCacheService)
        .should()
        .put(eq(0L), eq(AnimationType.WALK), eq(AnimationSubType.GENERAL), eq(animation));
    then(animationCacheService)
        .should()
        .put(eq(5L), eq(AnimationType.RUN), eq(AnimationSubType.GENERAL), eq(animation));
  }

  @Test
  public void givenNoAnimation_whenGetAnimation_thenReturnEmpty() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    given(animationCacheService.get(entityId, animationType, animationSubType))
        .willReturn(Optional.empty());

    // when
    var result = animationProviderService.getAnimation(entityId, animationType, animationSubType);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenAnimation_whenGetAnimation_thenReturnAnimation() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    given(animationCacheService.get(entityId, animationType, animationSubType))
        .willReturn(Optional.of(animation));

    // when
    var result = animationProviderService.getAnimation(entityId, animationType, animationSubType);

    // then
    assertThat(result).contains(animation);
  }

  @Test
  void givenMonsterWithBlankFolder_whenLoadMonsterAnimations_thenDoesNotLoadAnimation() {
    // given
    given(dataPathProperties.baseDirectory()).willReturn(Path.of("/data"));
    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getFolder()).willReturn("");
    given(animationFileLoaderService.loadAnimation(any(Path.class))).willReturn(Optional.empty());

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(times(16)).loadAnimation(any(Path.class));
  }

  @Test
  void givenMonsterInMonster1Folder_whenLoadMonsterAnimations_thenLoadsAnimation()
      throws IOException {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(atLeastOnce()).loadAnimation(any(Path.class));
  }

  @Test
  void givenMonsterInMonster2Folder_whenLoadMonsterAnimations_thenLoadsAnimation()
      throws IOException {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster2").resolve("wolf");
    Files.createDirectories(monsterFolder);

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(102L);
    given(monsterDefinition.getFolder()).willReturn("wolf");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(atLeastOnce()).loadAnimation(any(Path.class));
  }

  @Test
  void givenMonsterFolderNotFound_whenLoadMonsterAnimations_thenLogsWarning() {
    // given
    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(103L);
    given(monsterDefinition.getName()).willReturn("TestMonster");
    given(monsterDefinition.getFolder()).willReturn("non_existing");
    given(dataPathProperties.baseDirectory()).willReturn(Path.of("/non/existing/path"));

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(times(16)).loadAnimation(any(Path.class));
  }

  @Test
  void givenNoMotlistFile_whenLoadMonsterAnimation_thenLogsWarning() throws Exception {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(times(16)).loadAnimation(any(Path.class));
  }

  @Test
  void givenMotlistWithInvalidLines_whenLoadMonsterAnimation_thenSkipsInvalidLines()
      throws Exception {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    var motlistContent =
        """
      general walk dog_walk.msa
      invalid line with two parts
      attack run dog_run.msa 1.0
      general dance dog_dance.msa 1.0
      """;
    Files.writeString(monsterFolder.resolve("motlist.txt"), motlistContent);

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);
    given(animationFileLoaderService.loadAnimation(any(Path.class))).willReturn(Optional.empty());

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(times(16)).loadAnimation(any(Path.class));
  }

  @Test
  void givenMotlistWithNonGeneralLines_whenLoadMonsterAnimation_thenSkipsNonGeneralLines()
      throws Exception {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    var motlistContent =
        """
      special walk special_walk.msa 1.0
      attack run attack_run.msa 1.0
      """;
    Files.writeString(monsterFolder.resolve("motlist.txt"), motlistContent);

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationFileLoaderService).should(times(16)).loadAnimation(any(Path.class));
  }

  @Test
  void givenMotlistWithRunAction_whenLoadMonsterAnimation_thenLoadsRunAnimation() throws Exception {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    var motlistContent = "general run dog_run.msa 1.0\n";
    Files.writeString(monsterFolder.resolve("motlist.txt"), motlistContent);
    Files.createFile(monsterFolder.resolve("dog_run.msa"));

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);
    given(animationFileLoaderService.loadAnimation(any(Path.class)))
        .willReturn(Optional.of(animation));

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should()
        .put(eq(101L), eq(AnimationType.RUN), eq(AnimationSubType.GENERAL), eq(animation));
  }

  @Test
  void givenMotlistWithWalkAction_whenLoadMonsterAnimation_thenLoadsWalkAnimation()
      throws Exception {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    var motlistContent = "general walk dog_walk.msa 1.0\n";
    Files.writeString(monsterFolder.resolve("motlist.txt"), motlistContent);
    Files.createFile(monsterFolder.resolve("dog_walk.msa"));

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);
    given(animationFileLoaderService.loadAnimation(any(Path.class)))
        .willReturn(Optional.of(animation));

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should()
        .put(eq(101L), eq(AnimationType.WALK), eq(AnimationSubType.GENERAL), eq(animation));
  }

  @Test
  void givenMotlistWithRunAndWalk_whenLoadMonsterAnimation_thenLoadsBothAnimations()
      throws Exception {
    // given
    var testDir = Files.createTempDirectory("monster_test");
    var monsterFolder = testDir.resolve("monster").resolve("dog");
    Files.createDirectories(monsterFolder);

    var motlistContent =
        """
      general walk dog_walk.msa 1.0
      general run dog_run.msa 1.0
      """;
    Files.writeString(monsterFolder.resolve("motlist.txt"), motlistContent);
    Files.createFile(monsterFolder.resolve("dog_walk.msa"));
    Files.createFile(monsterFolder.resolve("dog_run.msa"));

    given(monsterDefinitionService.getMonsterDefinitions()).willReturn(List.of(monsterDefinition));
    given(monsterDefinition.getId()).willReturn(101L);
    given(monsterDefinition.getFolder()).willReturn("dog");
    given(dataPathProperties.baseDirectory()).willReturn(testDir);
    given(animationFileLoaderService.loadAnimation(any(Path.class)))
        .willReturn(Optional.of(animation));

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should()
        .put(eq(101L), eq(AnimationType.WALK), eq(AnimationSubType.GENERAL), eq(animation));
    then(animationCacheService)
        .should()
        .put(eq(101L), eq(AnimationType.RUN), eq(AnimationSubType.GENERAL), eq(animation));
  }
}
