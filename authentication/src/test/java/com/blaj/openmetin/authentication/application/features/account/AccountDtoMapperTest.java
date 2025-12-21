package com.blaj.openmetin.authentication.application.features.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.authentication.domain.entity.Account;
import org.junit.jupiter.api.Test;

public class AccountDtoMapperTest {

  @Test
  public void givenNull_whenMap_thenReturnNull() {
    // given
    var account = (Account) null;

    // when
    var dto = AccountDtoMapper.map(account);

    // then
    assertThat(dto).isNull();
  }

  @Test
  public void givenValid_whenMap_thenReturnDto() {
    // given
    var account =
        Account.builder()
            .id(123L)
            .username("username")
            .email("mail@mail.com")
            .deleteCode("1234567")
            .build();

    // when
    var dto = AccountDtoMapper.map(account);

    // then
    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(account.getId());
    assertThat(dto.username()).isEqualTo(account.getUsername());
    assertThat(dto.email()).isEqualTo(account.getEmail());
    assertThat(dto.deleteCode()).isEqualTo(account.getDeleteCode());
  }
}
