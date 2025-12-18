package com.blaj.openmetin.authentication.application.features.account;

import com.blaj.openmetin.authentication.domain.entity.Account;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccountDtoMapper {

  public static AccountDto map(Account account) {
    return Optional.ofNullable(account)
        .map(a -> new AccountDto(a.getId(), a.getUsername(), a.getEmail(), a.getDeleteCode()))
        .orElse(null);
  }
}
