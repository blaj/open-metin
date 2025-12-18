package com.blaj.openmetin.authentication.application.features.account;

import com.blaj.openmetin.shared.infrastructure.cqrs.Query;
import java.util.Optional;

public record GetAccountByIdQuery(long accountId) implements Query<Optional<AccountDto>> {}
