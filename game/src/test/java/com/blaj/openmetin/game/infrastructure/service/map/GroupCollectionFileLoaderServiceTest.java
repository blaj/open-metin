package com.blaj.openmetin.game.infrastructure.service.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
public class GroupCollectionFileLoaderServiceTest {

  private GroupCollectionFileLoaderService groupCollectionFileLoaderService;

  @Mock private DataPathProperties dataPathProperties;

  @TempDir private Path tempDir;

  private Path groupCollectionFile;

  @BeforeEach
  public void beforeEach() {
    groupCollectionFile = tempDir.resolve("group_group.txt");
    given(dataPathProperties.groupCollectionFile()).willReturn(groupCollectionFile);

    groupCollectionFileLoaderService = new GroupCollectionFileLoaderService(dataPathProperties);
  }

  @Test
  public void givenNoGroupCollectionFile_whenLoad_thenReturnEmptyMap() {
    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenValidGroupCollection_whenLoad_thenReturnSpawnGroupCollection()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 50
            2 102 30
            3 103 20
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L)).isNotNull();
    assertThat(result.get(2001L).id()).isEqualTo(2001L);
    assertThat(result.get(2001L).name()).isEqualTo("TestCollection");
    assertThat(result.get(2001L).entries()).hasSize(3);
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
    assertThat(result.get(2001L).entries().get(0).probability()).isEqualTo(50);
    assertThat(result.get(2001L).entries().get(1).id()).isEqualTo(102L);
    assertThat(result.get(2001L).entries().get(1).probability()).isEqualTo(30);
    assertThat(result.get(2001L).entries().get(2).id()).isEqualTo(103L);
    assertThat(result.get(2001L).entries().get(2).probability()).isEqualTo(20);
  }

  @Test
  public void givenMultipleGroupCollections_whenLoad_thenReturnAllCollections() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group Collection1
        {
            Vnum 2001
            1 101 50
        }

        Group Collection2
        {
            Vnum 2002
            1 201 60
            2 202 40
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(2001L).name()).isEqualTo("Collection1");
    assertThat(result.get(2002L).name()).isEqualTo("Collection2");
    assertThat(result.get(2001L).entries()).hasSize(1);
    assertThat(result.get(2002L).entries()).hasSize(2);
  }

  @Test
  public void givenDuplicateVnum_whenLoad_thenKeepFirstOccurrence() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group FirstCollection
        {
            Vnum 2001
            1 101 50
        }

        Group SecondCollection
        {
            Vnum 2001
            1 201 60
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L).name()).isEqualTo("FirstCollection");
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
  }

  @Test
  public void givenCommentsAndEmptyLines_whenLoad_thenSkipThemAndReturnCollections()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        # This is a comment
        // Another comment

        Group TestCollection
        {
            # Comment in block
            Vnum 2001
            // Another comment
            1 101 50
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L)).isNotNull();
  }

  @Test
  public void givenMissingGroupName_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupCollectionContent =
        """
        {
            Vnum 2001
            1 101 50
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenMissingVnum_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            1 101 50
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenUnclosedBlock_whenLoad_thenParseBlockAnyway() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 50
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L)).isNotNull();
  }

  @Test
  public void givenInvalidVnumFormat_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum invalid_number
            1 101 50
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenInvalidEntryFormat_whenLoad_thenReturnCollectionWithoutInvalidEntry()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 50
            invalid entry format
            2 102 30
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L)).isNotNull();
    assertThat(result.get(2001L).entries()).hasSize(2);
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
    assertThat(result.get(2001L).entries().get(1).id()).isEqualTo(102L);
  }

  @Test
  public void givenEntriesInWrongOrder_whenLoad_thenSortByIndex() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            3 103 20
            1 101 50
            2 102 30
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L).entries()).hasSize(3);
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
    assertThat(result.get(2001L).entries().get(1).id()).isEqualTo(102L);
    assertThat(result.get(2001L).entries().get(2).id()).isEqualTo(103L);
  }

  @Test
  public void givenCollectionWithNoEntries_whenLoad_thenReturnCollectionWithEmptyEntriesList()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L)).isNotNull();
    assertThat(result.get(2001L).entries()).isEmpty();
  }

  @Test
  public void givenDirectoryInsteadOfFile_whenLoad_thenHandleIOExceptionGracefully()
      throws IOException {
    // given
    Files.createDirectories(groupCollectionFile);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenMultipleMalformedBlocks_whenLoad_thenSkipMalformedAndReturnValidOnes()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group ValidCollection1
        {
            Vnum 2001
            1 101 50
        }

        Group InvalidCollection
        {
            Vnum invalid_number
            1 201 60
        }

        Group ValidCollection2
        {
            Vnum 2003
            1 301 70
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(2001L)).isNotNull();
    assertThat(result.get(2003L)).isNotNull();
    assertThat(result.get(2002L)).isNull();
  }

  @Test
  public void givenEntryWithInvalidGroupId_whenLoad_thenReturnCollectionWithoutInvalidEntry()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 50
            2 invalid_id 30
            3 103 20
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L).entries()).hasSize(2);
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
    assertThat(result.get(2001L).entries().get(1).id()).isEqualTo(103L);
  }

  @Test
  public void givenEntryWithInvalidProbability_whenLoad_thenReturnCollectionWithoutInvalidEntry()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 50
            2 102 invalid_prob
            3 103 20
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L).entries()).hasSize(2);
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
    assertThat(result.get(2001L).entries().get(1).id()).isEqualTo(103L);
  }

  @Test
  public void givenVnumOutOfRange_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 99999999999999999999999999999999
            1 101 50
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenSingleEntry_whenLoad_thenReturnCollectionWithSingleEntry() throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 100
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L).entries()).hasSize(1);
    assertThat(result.get(2001L).entries().get(0).id()).isEqualTo(101L);
    assertThat(result.get(2001L).entries().get(0).probability()).isEqualTo(100);
  }

  @Test
  public void givenEntriesWithZeroProbability_whenLoad_thenIncludeThemInCollection()
      throws IOException {
    // given
    var groupCollectionContent =
        """
        Group TestCollection
        {
            Vnum 2001
            1 101 0
            2 102 50
        }
        """;
    Files.writeString(groupCollectionFile, groupCollectionContent);

    // when
    var result = groupCollectionFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(2001L).entries()).hasSize(2);
    assertThat(result.get(2001L).entries().get(0).probability()).isEqualTo(0);
    assertThat(result.get(2001L).entries().get(1).probability()).isEqualTo(50);
  }
}
