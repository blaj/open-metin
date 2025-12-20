package com.blaj.openmetin.authentication.application.features.account;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.blaj.openmetin.authentication.utils.CustomWebMvcTest;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@CustomWebMvcTest(GetAccountByIdController.class)
public class GetAccountByIdControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private Mediator mediator;

  @Test
  public void givenNonExistingAccount_whenGet_thenReturnNotFound() throws Exception {
    // given
    var accountId = 999L;
    given(mediator.send(new GetAccountByIdQuery(accountId))).willReturn(Optional.empty());

    // when & then
    mockMvc.perform(get("/api/v1/accounts/{id}", accountId)).andExpect(status().isNotFound());

    then(mediator).should().send(new GetAccountByIdQuery(accountId));
  }

  @Test
  public void givenExistingAccount_whenGet_thenReturnAccountDto() throws Exception {
    // given
    var accountId = 123L;
    var accountDto = new AccountDto(accountId, "testuser", "test@example.com", "1234567");
    given(mediator.send(new GetAccountByIdQuery(accountId))).willReturn(Optional.of(accountDto));

    // when & then
    mockMvc
        .perform(get("/api/v1/accounts/{id}", accountId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(accountId))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));

    then(mediator).should().send(new GetAccountByIdQuery(accountId));
  }
}
