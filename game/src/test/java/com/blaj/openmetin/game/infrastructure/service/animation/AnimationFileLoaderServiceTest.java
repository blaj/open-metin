package com.blaj.openmetin.game.infrastructure.service.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.blaj.openmetin.game.infrastructure.exception.AnimationParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AnimationFileLoaderServiceTest {

  private AnimationFileLoaderService animationFileLoaderService;

  @TempDir private Path tempDir;

  @BeforeEach
  public void beforeEach() {
    animationFileLoaderService = new AnimationFileLoaderService();
  }

  @Test
  public void givenNonExistingFile_whenLoadAnimation_thenReturnEmpty() {
    // given
    var animationPath = tempDir.resolve("non-existing.msa");

    // when
    var result = animationFileLoaderService.loadAnimation(animationPath);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenFileWithInvalidScriptType_whenLoadAnimation_thenThrowException()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("invalid-script-type.msa");
    var content =
        """
        ScriptType InvalidType
        MotionDuration 1.0
        Accumulation 0.0 -500.0 0.0
        """;
    Files.writeString(animationPath, content);

    // when
    var thrownException =
        assertThrows(
            AnimationParseException.class,
            () -> animationFileLoaderService.loadAnimation(animationPath));

    // then
    assertThat(thrownException)
        .hasMessageContaining("Expected ScriptType 'MotionData' but got 'InvalidType'");
  }

  @Test
  public void givenFileWithMissingScriptType_whenLoadAnimation_thenThrowException()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("missing-script-type.msa");
    var content =
        """
        MotionDuration 1.0
        Accumulation 0.0 -500.0 0.0
        """;
    Files.writeString(animationPath, content);

    // when
    var thrownException =
        assertThrows(
            NullPointerException.class,
            () -> animationFileLoaderService.loadAnimation(animationPath));

    // then
    assertThat(thrownException).isNotNull();
  }

  @Test
  public void givenFileWithMissingMotionDuration_whenLoadAnimation_thenThrowException()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("missing-motion-duration.msa");
    var content =
        """
        ScriptType MotionData
        Accumulation 0.0 -500.0 0.0
        """;
    Files.writeString(animationPath, content);

    // when
    var thrownException =
        assertThrows(
            AnimationParseException.class,
            () -> animationFileLoaderService.loadAnimation(animationPath));

    // then
    assertThat(thrownException).hasMessageContaining("Missing MotionDuration");
  }

  @Test
  public void givenFileWithMissingAccumulation_whenLoadAnimation_thenThrowException()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("missing-accumulation.msa");
    var content =
        """
        ScriptType MotionData
        MotionDuration 1.0
        """;
    Files.writeString(animationPath, content);

    // when
    var thrownException =
        assertThrows(
            AnimationParseException.class,
            () -> animationFileLoaderService.loadAnimation(animationPath));

    // then
    assertThat(thrownException).hasMessageContaining("Missing Accumulation");
  }

  @Test
  public void givenFileWithInvalidAccumulation_whenLoadAnimation_thenThrowException()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("invalid-accumulation.msa");
    var content =
        """
        ScriptType MotionData
        MotionDuration 1.0
        Accumulation 0.0 -500.0
        """;
    Files.writeString(animationPath, content);

    // when
    var thrownException =
        assertThrows(
            AnimationParseException.class,
            () -> animationFileLoaderService.loadAnimation(animationPath));

    // then
    assertThat(thrownException).hasMessageContaining("Missing Accumulation");
  }

  @Test
  public void givenFileWithCommentsAndEmptyLines_whenLoadAnimation_thenReturnAnimation()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("with-comments.msa");
    var content =
        """
        # This is a comment
        ScriptType MotionData

        # Another comment
        MotionDuration 1.5
        Accumulation 10.0 -500.0 20.0

        """;
    Files.writeString(animationPath, content);

    // when
    var result = animationFileLoaderService.loadAnimation(animationPath);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get().duration()).isEqualTo(1.5f);
    assertThat(result.get().accumulationX()).isEqualTo(10.0f);
    assertThat(result.get().accumulationY()).isEqualTo(-500.0f);
    assertThat(result.get().accumulationZ()).isEqualTo(20.0f);
  }

  @Test
  public void givenFileWithIncompleteLinesWithoutValue_whenLoadAnimation_thenIgnoreThem()
      throws IOException {
    // given
    var animationPath = tempDir.resolve("incomplete-lines.msa");
    var content =
        """
        ScriptType MotionData
        InvalidLine
        MotionDuration 1.0
        AnotherInvalidLine
        Accumulation 0.0 -500.0 0.0
        """;
    Files.writeString(animationPath, content);

    // when
    var result = animationFileLoaderService.loadAnimation(animationPath);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get().duration()).isEqualTo(1.0f);
    assertThat(result.get().accumulationX()).isEqualTo(0.0f);
    assertThat(result.get().accumulationY()).isEqualTo(-500.0f);
    assertThat(result.get().accumulationZ()).isEqualTo(0.0f);
  }

  @Test
  public void givenValidFile_whenLoadAnimation_thenReturnAnimation() throws IOException {
    // given
    var animationPath = tempDir.resolve("valid-animation.msa");
    var content =
        """
        ScriptType MotionData
        MotionDuration 2.5
        Accumulation 15.0 -300.0 25.5
        """;
    Files.writeString(animationPath, content);

    // when
    var result = animationFileLoaderService.loadAnimation(animationPath);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get().duration()).isEqualTo(2.5f);
    assertThat(result.get().accumulationX()).isEqualTo(15.0f);
    assertThat(result.get().accumulationY()).isEqualTo(-300.0f);
    assertThat(result.get().accumulationZ()).isEqualTo(25.5f);
  }
}
