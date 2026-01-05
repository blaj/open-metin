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
public class GroupFileLoaderServiceTest {

  private GroupFileLoaderService groupFileLoaderService;

  @Mock private DataPathProperties dataPathProperties;

  @TempDir private Path tempDir;

  private Path groupFile;

  @BeforeEach
  public void beforeEach() {
    groupFile = tempDir.resolve("group.txt");
    given(dataPathProperties.groupFile()).willReturn(groupFile);

    groupFileLoaderService = new GroupFileLoaderService(dataPathProperties);
  }

  @Test
  public void givenNoGroupFile_whenLoad_thenReturnEmptyMap() {
    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenValidGroup_whenLoad_thenReturnSpawnGroup() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Test Leader" 101
            Vnum 1001
            1 "Member1" 201
            2 "Member2" 202
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L)).isNotNull();
    assertThat(result.get(1001L).id()).isEqualTo(1001L);
    assertThat(result.get(1001L).name()).isEqualTo("TestGroup");
    assertThat(result.get(1001L).leaderId()).isEqualTo(101L);
    assertThat(result.get(1001L).membersIds()).containsExactly(201L, 202L);
  }

  @Test
  public void givenGroupWithoutQuotes_whenLoad_thenReturnSpawnGroup() throws IOException {
    // given
    var groupContent =
        """
        Group SimpleGroup
        {
            Leader SimpleLeader 101
            Vnum 1001
            1 SimpleMember1 201
            2 SimpleMember2 202
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L)).isNotNull();
    assertThat(result.get(1001L).leaderId()).isEqualTo(101L);
    assertThat(result.get(1001L).membersIds()).containsExactly(201L, 202L);
  }

  @Test
  public void givenMultipleGroups_whenLoad_thenReturnAllGroups() throws IOException {
    // given
    var groupContent =
        """
        Group Group1
        {
            Leader "Leader1" 101
            Vnum 1001
            1 "Member1" 201
        }

        Group Group2
        {
            Leader "Leader2" 102
            Vnum 1002
            1 "Member2" 202
            2 "Member3" 203
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(1001L).name()).isEqualTo("Group1");
    assertThat(result.get(1002L).name()).isEqualTo("Group2");
    assertThat(result.get(1001L).membersIds()).hasSize(1);
    assertThat(result.get(1002L).membersIds()).hasSize(2);
  }

  @Test
  public void givenDuplicateVnum_whenLoad_thenKeepFirstOccurrence() throws IOException {
    // given
    var groupContent =
        """
        Group FirstGroup
        {
            Leader "Leader1" 101
            Vnum 1001
            1 "Member1" 201
        }

        Group SecondGroup
        {
            Leader "Leader2" 102
            Vnum 1001
            1 "Member2" 202
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L).name()).isEqualTo("FirstGroup");
    assertThat(result.get(1001L).leaderId()).isEqualTo(101L);
  }

  @Test
  public void givenCommentsAndEmptyLines_whenLoad_thenSkipThemAndReturnGroups() throws IOException {
    // given
    var groupContent =
        """
        # This is a comment
        // Another comment

        Group TestGroup
        {
            # Comment in block
            Leader "Leader" 101
            Vnum 1001
            // Another comment
            1 "Member1" 201
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L)).isNotNull();
  }

  @Test
  public void givenMissingGroupName_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupContent =
        """
        {
            Leader "Leader" 101
            Vnum 1001
            1 "Member1" 201
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenMissingVnum_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" 101
            1 "Member1" 201
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenMissingLeader_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Vnum 1001
            1 "Member1" 201
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenUnclosedBlock_whenLoad_thenParseBlockAnyway() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" 101
            Vnum 1001
            1 "Member1" 201
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L)).isNotNull();
  }

  @Test
  public void givenInvalidVnumFormat_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" 101
            Vnum invalid_number
            1 "Member1" 201
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenInvalidLeaderIdFormat_whenLoad_thenSkipBlock() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" invalid_id
            Vnum 1001
            1 "Member1" 201
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenInvalidMemberIdFormat_whenLoad_thenReturnGroupWithoutInvalidMember()
      throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" 101
            Vnum 1001
            1 "Member1" invalid_member_id
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L)).isNotNull();
    assertThat(result.get(1001L).membersIds()).isEmpty();
  }

  @Test
  public void givenMembersInWrongOrder_whenLoad_thenSortByIndex() throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" 101
            Vnum 1001
            3 "Member3" 203
            1 "Member1" 201
            2 "Member2" 202
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L).membersIds()).containsExactly(201L, 202L, 203L);
  }

  @Test
  public void givenGroupWithNoMembers_whenLoad_thenReturnGroupWithEmptyMembersList()
      throws IOException {
    // given
    var groupContent =
        """
        Group TestGroup
        {
            Leader "Leader" 101
            Vnum 1001
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L).membersIds()).isEmpty();
  }

  @Test
  public void givenDirectoryInsteadOfFile_whenLoad_thenHandleIOExceptionGracefully()
      throws IOException {
    // given
    Files.createDirectories(groupFile);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenMultipleMalformedBlocks_whenLoad_thenSkipMalformedAndReturnValidOnes()
      throws IOException {
    // given
    var groupContent =
        """
        Group ValidGroup1
        {
            Leader "Leader1" 101
            Vnum 1001
            1 "Member1" 201
        }

        Group InvalidGroup
        {
            Leader "Leader2" invalid_id
            Vnum 1002
        }

        Group ValidGroup2
        {
            Leader "Leader3" 103
            Vnum 1003
            1 "Member3" 203
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(1001L)).isNotNull();
    assertThat(result.get(1003L)).isNotNull();
    assertThat(result.get(1002L)).isNull();
  }

  @Test
  public void givenGroupWithMixedQuotedAndUnquotedNames_whenLoad_thenParseCorrectly()
      throws IOException {
    // given
    var groupContent =
        """
        Group MixedGroup
        {
            Leader QuotedLeader 101
            Vnum 1001
            1 "Quoted Member" 201
            2 UnquotedMember 202
        }
        """;
    Files.writeString(groupFile, groupContent);

    // when
    var result = groupFileLoaderService.load();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(1001L).membersIds()).containsExactly(201L, 202L);
  }
}
