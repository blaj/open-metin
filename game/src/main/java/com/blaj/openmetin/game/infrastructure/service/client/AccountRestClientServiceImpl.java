package com.blaj.openmetin.game.infrastructure.service.client;

import com.blaj.openmetin.game.application.common.account.AccountDto;
import com.blaj.openmetin.game.application.common.account.AccountRestClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
public class AccountRestClientServiceImpl implements AccountRestClientService {

  private final RestClient restClient;

  public AccountRestClientServiceImpl(
      @Qualifier("authenticationRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  @Cacheable(value = "accounts", key = "#accountId")
  public AccountDto getAccountCached(long accountId) {
    try {
      return restClient.get().uri("/accounts/{id}", accountId).retrieve().body(AccountDto.class);
    } catch (RestClientResponseException e) {
      log.error("Error while getting account", e);

      return null;
    }
  }
}
