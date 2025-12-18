package com.blaj.openmetin.authentication.application.features.account;

import com.blaj.openmetin.authentication.domain.repository.AccountRepository;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAccountByIdQueryHandlerService
    implements RequestHandler<GetAccountByIdQuery, Optional<AccountDto>> {

  private final AccountRepository accountRepository;

  @Override
  public Optional<AccountDto> handle(GetAccountByIdQuery request) {
    return accountRepository.findById(request.accountId()).map(AccountDtoMapper::map);
  }
}
