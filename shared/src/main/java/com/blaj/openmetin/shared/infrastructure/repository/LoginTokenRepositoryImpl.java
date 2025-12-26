package com.blaj.openmetin.shared.infrastructure.repository;

import com.blaj.openmetin.shared.domain.entity.LoginToken;
import com.blaj.openmetin.shared.domain.repository.LoginTokenRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.joou.UInteger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class LoginTokenRepositoryImpl implements LoginTokenRepository {

  private static final String loginTokenKey = "login:token:";
  private static final String attemptsKey = "login:attempts:";
  private static final String loginKeyKey = "login:key:";

  private static final Duration loginTokenDuration = Duration.ofSeconds(30);
  private static final Duration attemptsDuration = Duration.ofMinutes(1);
  private static final Duration loginKeyDuration = Duration.ofSeconds(30);

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void saveLoginToken(UInteger loginKey, LoginToken loginToken) {
    redisTemplate.opsForValue().set(loginTokenKey + loginKey, loginToken, loginTokenDuration);
  }

  @Override
  public void saveLoginKey(long accountId, UInteger loginKey) {
    redisTemplate.opsForValue().set(loginKeyKey + accountId, loginKey, loginKeyDuration);
  }

  @Override
  public LoginToken getLoginToken(UInteger loginKey) {
    return objectMapper.convertValue(
        redisTemplate.opsForValue().get(loginTokenKey + loginKey), LoginToken.class);
  }

  @Override
  public Long getAttempts(long accountId) {
    var attempts = redisTemplate.opsForValue().increment(attemptsKey + accountId);
    redisTemplate.expire(attemptsKey + accountId, attemptsDuration);

    return attempts;
  }

  @Override
  public Long getLoginKey(long accountId) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(loginKeyKey + accountId))
        .map(loginKey -> (Number) loginKey)
        .map(Number::longValue)
        .orElse(null);
  }

  @Override
  public boolean loginKeyExists(long accountId) {
    return redisTemplate.hasKey(loginKeyKey + accountId);
  }

  @Override
  public void deleteLoginKey(long accountId) {
    redisTemplate.delete(loginKeyKey + accountId);
  }

  @Override
  public void deleteAttempts(long accountId) {
    redisTemplate.delete(attemptsKey + accountId);
  }
}
