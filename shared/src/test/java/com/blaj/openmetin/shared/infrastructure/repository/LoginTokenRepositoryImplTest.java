package com.blaj.openmetin.shared.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.shared.domain.entity.LoginToken;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.ObjectMapper;

public class LoginTokenRepositoryImplTest {

  private static final RedisContainer redis =
      new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));
  private ObjectMapper objectMapper;
  private RedisTemplate<String, Object> redisTemplate;
  private LettuceConnectionFactory connectionFactory;
  private LoginTokenRepositoryImpl repository;

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  @BeforeAll
  public static void beforeAll() {
    redis.start();
  }

  @AfterAll
  public static void afterAll() {
    redis.stop();
  }

  @BeforeEach
  public void beforeEach() {
    objectMapper = new ObjectMapper();

    var config = new RedisStandaloneConfiguration();
    config.setHostName(redis.getHost());
    config.setPort(redis.getFirstMappedPort());

    connectionFactory = new LettuceConnectionFactory(config);
    connectionFactory.afterPropertiesSet();

    redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper));
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper));
    redisTemplate.afterPropertiesSet();

    repository = new LoginTokenRepositoryImpl(redisTemplate, objectMapper);
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @AfterEach
  public void afterEach() {
    connectionFactory.destroy();
  }

  @Test
  public void givenLoginToken_whenSaveAndGet_thenReturnsToken() {
    // given
    var loginKey = 12345L;
    var loginToken = new LoginToken();

    // when
    repository.saveLoginToken(loginKey, loginToken);
    var result = repository.getLoginToken(loginKey);

    // then
    assertThat(result).isNotNull();
  }

  @Test
  public void givenAccountId_whenGetAttempts_thenIncrementsCounter() {
    // given
    var accountId = 999L;

    // when
    var attempt1 = repository.getAttempts(accountId);
    var attempt2 = repository.getAttempts(accountId);
    var attempt3 = repository.getAttempts(accountId);

    // then
    assertThat(attempt1).isEqualTo(1L);
    assertThat(attempt2).isEqualTo(2L);
    assertThat(attempt3).isEqualTo(3L);
  }

  @Test
  public void givenLoginKey_whenSaveAndGet_thenReturnsLoginKey() {
    // given
    var accountId = 999L;
    var loginKey = 12345L;

    // when
    repository.saveLoginKey(accountId, loginKey);
    var result = repository.getLoginKey(accountId);

    // then
    assertThat(result).isEqualTo(loginKey);
  }

  @Test
  public void givenLoginKey_whenCheckExists_thenReturnsTrue() {
    // given
    var accountId = 999L;
    var loginKey = 12345L;

    // when
    repository.saveLoginKey(accountId, loginKey);
    var exists = repository.loginKeyExists(accountId);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  public void givenNoLoginKey_whenCheckExists_thenReturnsFalse() {
    // given
    var accountId = 999L;

    // when
    var exists = repository.loginKeyExists(accountId);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  public void givenLoginKey_whenDelete_thenKeyNoLongerExists() {
    // given
    var accountId = 999L;
    var loginKey = 12345L;
    repository.saveLoginKey(accountId, loginKey);

    // when
    repository.deleteLoginKey(accountId);

    // then
    assertThat(repository.loginKeyExists(accountId)).isFalse();
  }

  @Test
  public void givenAttempts_whenDelete_thenAttemptsReset() {
    // given
    var accountId = 999L;
    repository.getAttempts(accountId);
    repository.getAttempts(accountId);

    // when
    repository.deleteAttempts(accountId);
    var newAttempts = repository.getAttempts(accountId);

    // then
    assertThat(newAttempts).isEqualTo(1L);
  }
}
