package com.blaj.openmetin.authentication.application.features.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.authentication.domain.entity.Account;
import com.blaj.openmetin.authentication.domain.repository.AccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetAccountByIdQueryHandlerServiceTest {

  private GetAccountByIdQueryHandlerService getAccountByIdQueryHandlerService;

  @Mock private AccountRepository accountRepository;

  @BeforeEach
  public void beforeEach() {
    getAccountByIdQueryHandlerService = new GetAccountByIdQueryHandlerService(accountRepository);
  }

  @Test
  public void givenNonExistingAccount_whenHandle_thenReturnEmptyOptional() {
    // given
    var nonExistingId = 123L;
    var getAccountByIdQuery = new GetAccountByIdQuery(nonExistingId);

    given(accountRepository.findById(nonExistingId)).willReturn(Optional.empty());

    // when
    var result = getAccountByIdQueryHandlerService.handle(getAccountByIdQuery);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenValid_whenHandle_thenReturnDtoOptional() {
    // given
    var existingId = 123L;
    var getAccountByIdQuery = new GetAccountByIdQuery(existingId);
    var account =
        Account.builder()
            .id(123L)
            .username("username")
            .email("mail@mail.com")
            .deleteCode("1234567")
            .build();

    given(accountRepository.findById(existingId)).willReturn(Optional.of(account));

    // when
    var result = getAccountByIdQueryHandlerService.handle(getAccountByIdQuery);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result).contains(AccountDtoMapper.map(account));
  }
}
