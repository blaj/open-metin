package com.blaj.openmetin.game.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.account.AccountDto;
import com.blaj.openmetin.game.infrastructure.service.client.AccountRestClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
public class AccountRestClientServiceImplTest {

  private AccountRestClientServiceImpl accountRestClientService;

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  @BeforeEach
  void beforeEach() {
    accountRestClientService = new AccountRestClientServiceImpl(restClient);
  }

  @Test
  void givenRestClientThrowsException_whenGetAccountCached_thenReturnNull() {
    // given
    var accountId = 456L;
    var exception =
        new RestClientResponseException("Not Found", 404, "Not Found", null, null, null);

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri("/accounts/{id}", accountId)).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(AccountDto.class)).willThrow(exception);

    // when
    var result = accountRestClientService.getAccountCached(accountId);

    // then
    assertThat(result).isNull();

    then(restClient).should().get();
    then(requestHeadersUriSpec).should().uri("/accounts/{id}", accountId);
    then(requestHeadersSpec).should().retrieve();
    then(responseSpec).should().body(AccountDto.class);
  }

  @Test
  void givenServerError_whenGetAccountCached_thenReturnNull() {
    // given
    var accountId = 789L;
    var exception =
        new RestClientResponseException(
            "Internal Server Error", 500, "Internal Server Error", null, null, null);

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri("/accounts/{id}", accountId)).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(AccountDto.class)).willThrow(exception);

    // when
    var result = accountRestClientService.getAccountCached(accountId);

    // then
    assertThat(result).isNull();
  }

  @Test
  void givenNonExistentAccount_whenGetAccountCached_thenReturnNull() {
    // given
    var accountId = 999L;
    var exception =
        new RestClientResponseException("Not Found", 404, "Not Found", null, null, null);

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri("/accounts/{id}", accountId)).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(AccountDto.class)).willThrow(exception);

    // when
    var result = accountRestClientService.getAccountCached(accountId);

    // then
    assertThat(result).isNull();
  }

  @Test
  void givenValid_whenGetAccountCached_thenReturnAccountDto() {
    // given
    var accountId = 123L;
    var expectedAccount = new AccountDto(accountId, "username", "mail@mail.com", "1234567");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri("/accounts/{id}", accountId)).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(AccountDto.class)).willReturn(expectedAccount);

    // when
    var result = accountRestClientService.getAccountCached(accountId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(accountId);
    assertThat(result.username()).isEqualTo("username");
    assertThat(result.email()).isEqualTo("mail@mail.com");
    assertThat(result.deleteCode()).isEqualTo("1234567");

    then(restClient).should().get();
    then(requestHeadersUriSpec).should().uri("/accounts/{id}", accountId);
    then(requestHeadersSpec).should().retrieve();
    then(responseSpec).should().body(AccountDto.class);
  }
}
