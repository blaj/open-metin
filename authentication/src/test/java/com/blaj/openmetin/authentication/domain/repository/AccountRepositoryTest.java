package com.blaj.openmetin.authentication.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.authentication.domain.entity.Account;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
public class AccountRepositoryTest {

  @Autowired AccountRepository accountRepository;

  @Test
  public void givenNonExistingUsername_whenFindByUsername_thenReturnEmptyOptional() {
    // given
    var nonExistingUsername =
        accountRepository
                .save(
                    Account.builder()
                        .username("username")
                        .password("password")
                        .email("mail@mail.com")
                        .lastLoginAt(LocalDateTime.now())
                        .deleteCode("1234567")
                        .build())
                .getUsername()
            + "nonExisting";

    // when
    var account = accountRepository.findByUsername(nonExistingUsername);

    // then
    assertThat(account).isEmpty();
  }

  @Test
  public void givenExistingUsername_whenFindByUsername_thenReturnEntity() {
    // given
    var entity =
        accountRepository.save(
            Account.builder()
                .username("username")
                .password("password")
                .email("mail@mail.com")
                .lastLoginAt(LocalDateTime.now())
                .deleteCode("1234567")
                .build());
    var existingUsername = entity.getUsername();

    // when
    var account = accountRepository.findByUsername(existingUsername);

    // then
    assertThat(account).contains(entity);
  }
}
