package com.blaj.openmetin.authentication.application.features.account;

import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts")
public class GetAccountByIdController {

  private final Mediator mediator;

  @GetMapping("/{id}")
  public ResponseEntity<AccountDto> get(@PathVariable long id) {
    return ResponseEntity.of(mediator.send(new GetAccountByIdQuery(id)));
  }
}
