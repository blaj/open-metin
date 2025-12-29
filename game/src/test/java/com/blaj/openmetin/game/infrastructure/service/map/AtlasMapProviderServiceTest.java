package com.blaj.openmetin.game.infrastructure.service.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.game.infrastructure.exception.AtlasInfoParseException;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AtlasMapProviderServiceTest {

  private AtlasMapProviderService atlasMapProviderService;

  @Mock private DataPathProperties dataPathProperties;

  @TempDir private Path tempDir;

  @BeforeEach
  public void beforeEach() {
    atlasMapProviderService = new AtlasMapProviderService(dataPathProperties);
  }

  @Test
  public void givenAtlasInfoFileNotExists_whenGetAll_thenReturnEmptyList() {
    // given
    var atlasInfoPath = tempDir.resolve("atlasinfo.txt");
    given(dataPathProperties.atlasInfoFile()).willReturn(atlasInfoPath);

    // when
    var result = atlasMapProviderService.getAll();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenEmptyAtlasInfoFile_whenGetAll_thenReturnEmptyList() throws IOException {
    // given
    var atlasInfoPath = tempDir.resolve("atlasinfo.txt");
    Files.writeString(atlasInfoPath, "");
    given(dataPathProperties.atlasInfoFile()).willReturn(atlasInfoPath);

    // when
    var result = atlasMapProviderService.getAll();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenInvalidLineFormat_whenGetAll_thenThrowException() throws IOException {
    // given
    var atlasInfoPath = tempDir.resolve("atlasinfo.txt");
    var content = "invalid_line_format";
    Files.writeString(atlasInfoPath, content);
    given(dataPathProperties.atlasInfoFile()).willReturn(atlasInfoPath);

    // when
    var thrownException =
        assertThrows(AtlasInfoParseException.class, () -> atlasMapProviderService.getAll());

    // then
    assertThat(thrownException).hasMessageContaining("Invalid format");
  }

  @Test
  public void givenFileWithCommentsAndEmptyLines_whenGetAll_thenSkipThemAndReturnMaps()
      throws IOException {
    // given
    var atlasInfoPath = tempDir.resolve("atlasinfo.txt");
    var content =
        """
        # This is a comment
        map1 1000 2000 4 5

        # Another comment
        map2 3000 4000 6 7
        """;
    Files.writeString(atlasInfoPath, content);
    given(dataPathProperties.atlasInfoFile()).willReturn(atlasInfoPath);
    given(dataPathProperties.townFile(anyString())).willReturn(tempDir.resolve("nonexistent.txt"));

    // when
    var result = atlasMapProviderService.getAll();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("map1");
    assertThat(result.get(1).getName()).isEqualTo("map2");
  }

  @Test
  public void givenValidAtlasInfoWithoutTownFile_whenGetAll_thenReturnMapsWithoutTownCoordinates()
      throws IOException {
    // given
    var atlasInfoPath = tempDir.resolve("atlasinfo.txt");
    var content = "map1 1000 2000 4 5";
    Files.writeString(atlasInfoPath, content);
    given(dataPathProperties.atlasInfoFile()).willReturn(atlasInfoPath);
    given(dataPathProperties.townFile("map1")).willReturn(tempDir.resolve("Town.txt"));

    // when
    var result = atlasMapProviderService.getAll();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("map1");
    assertThat(result.get(0).getCoordinates().x()).isEqualTo(1000);
    assertThat(result.get(0).getCoordinates().y()).isEqualTo(2000);
    assertThat(result.get(0).getWidth()).isEqualTo(4);
    assertThat(result.get(0).getHeight()).isEqualTo(5);
    assertThat(result.get(0).getTownCoordinates()).isNull();
  }

  @Test
  public void givenValidAtlasInfoWithTownFile_whenGetAll_thenReturnMapsWithTownCoordinates()
      throws IOException {
    // given
    var atlasInfoPath = tempDir.resolve("atlasinfo.txt");
    var content = "map1 1000 2000 4 5";
    Files.writeString(atlasInfoPath, content);

    var townPath = tempDir.resolve("town.txt");
    var townContent = "100 200";
    Files.writeString(townPath, townContent);

    given(dataPathProperties.atlasInfoFile()).willReturn(atlasInfoPath);
    given(dataPathProperties.townFile("map1")).willReturn(townPath);

    // when
    var result = atlasMapProviderService.getAll();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("map1");
    assertThat(result.get(0).getTownCoordinates()).isNotNull();
    assertThat(result.get(0).getTownCoordinates().jinno().x()).isEqualTo(11000);
    assertThat(result.get(0).getTownCoordinates().jinno().y()).isEqualTo(22000);
    assertThat(result.get(0).getTownCoordinates().shinsoo().x()).isEqualTo(11000);
    assertThat(result.get(0).getTownCoordinates().shinsoo().y()).isEqualTo(22000);
    assertThat(result.get(0).getTownCoordinates().chunjo().x()).isEqualTo(11000);
    assertThat(result.get(0).getTownCoordinates().chunjo().y()).isEqualTo(22000);
    assertThat(result.get(0).getTownCoordinates().common().x()).isEqualTo(11000);
    assertThat(result.get(0).getTownCoordinates().common().y()).isEqualTo(22000);
  }

  @Test
  public void givenTownFileNotExists_whenGetTownCoordinates_thenReturnEmpty() {
    // given
    var mapName = "map1";
    var townPath = tempDir.resolve("town.txt");
    given(dataPathProperties.townFile(mapName)).willReturn(townPath);

    // when
    var result = atlasMapProviderService.getTownCoordinates(mapName);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenTownFileWithZeroCoordinates_whenGetTownCoordinates_thenThrowException()
      throws IOException {
    // given
    var mapName = "map1";
    var townPath = tempDir.resolve("town.txt");
    Files.writeString(townPath, "# Only comments");
    given(dataPathProperties.townFile(mapName)).willReturn(townPath);

    // when
    var thrownException =
        assertThrows(
            AtlasInfoParseException.class,
            () -> atlasMapProviderService.getTownCoordinates(mapName));

    // then
    assertThat(thrownException).hasMessageContaining("must contain 1 or 4 coordinate lines");
  }

  @Test
  public void givenTownFileWithTwoCoordinates_whenGetTownCoordinates_thenThrowException()
      throws IOException {
    // given
    var mapName = "map1";
    var townPath = tempDir.resolve("town.txt");
    var content =
        """
        100 200
        300 400
        """;
    Files.writeString(townPath, content);
    given(dataPathProperties.townFile(mapName)).willReturn(townPath);

    // when
    var thrownException =
        assertThrows(
            AtlasInfoParseException.class,
            () -> atlasMapProviderService.getTownCoordinates(mapName));

    // then
    assertThat(thrownException)
        .hasMessageContaining("must contain 1 or 4 coordinate lines, found 2");
  }

  @Test
  public void givenTownFileWithInvalidLine_whenGetTownCoordinates_thenSkipInvalidLine()
      throws IOException {
    // given
    var mapName = "map1";
    var townPath = tempDir.resolve("town.txt");
    var content =
        """
        invalid
        100 200
        300 400
        500 600
        700 800
        """;
    Files.writeString(townPath, content);
    given(dataPathProperties.townFile(mapName)).willReturn(townPath);

    // when
    var result = atlasMapProviderService.getTownCoordinates(mapName);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().jinno().x()).isEqualTo(100);
    assertThat(result.get().shinsoo().x()).isEqualTo(300);
    assertThat(result.get().chunjo().x()).isEqualTo(500);
    assertThat(result.get().common().x()).isEqualTo(700);
  }

  @Test
  public void
      givenTownFileWithOneCoordinate_whenGetTownCoordinates_thenReturnTownCoordinatesWithSameValue()
          throws IOException {
    // given
    var mapName = "map1";
    var townPath = tempDir.resolve("town.txt");
    var content = "100 200";
    Files.writeString(townPath, content);
    given(dataPathProperties.townFile(mapName)).willReturn(townPath);

    // when
    var result = atlasMapProviderService.getTownCoordinates(mapName);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().jinno().x()).isEqualTo(100);
    assertThat(result.get().jinno().y()).isEqualTo(200);
    assertThat(result.get().shinsoo().x()).isEqualTo(100);
    assertThat(result.get().shinsoo().y()).isEqualTo(200);
    assertThat(result.get().chunjo().x()).isEqualTo(100);
    assertThat(result.get().chunjo().y()).isEqualTo(200);
    assertThat(result.get().common().x()).isEqualTo(100);
    assertThat(result.get().common().y()).isEqualTo(200);
  }

  @Test
  public void givenTownFileWithFourCoordinates_whenGetTownCoordinates_thenReturnTownCoordinates()
      throws IOException {
    // given
    var mapName = "map1";
    var townPath = tempDir.resolve("town.txt");
    var content =
        """
        100 200
        300 400
        500 600
        700 800
        """;
    Files.writeString(townPath, content);
    given(dataPathProperties.townFile(mapName)).willReturn(townPath);

    // when
    var result = atlasMapProviderService.getTownCoordinates(mapName);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().jinno().x()).isEqualTo(100);
    assertThat(result.get().jinno().y()).isEqualTo(200);
    assertThat(result.get().shinsoo().x()).isEqualTo(300);
    assertThat(result.get().shinsoo().y()).isEqualTo(400);
    assertThat(result.get().chunjo().x()).isEqualTo(500);
    assertThat(result.get().chunjo().y()).isEqualTo(600);
    assertThat(result.get().common().x()).isEqualTo(700);
    assertThat(result.get().common().y()).isEqualTo(800);
  }
}
