package com.blaj.openmetin.game.application.common.empire;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.entity.Character.Empire;
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

public class SelectEmpireServiceTest {

  private static final RedisContainer redis =
      new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

  private ObjectMapper objectMapper;
  private RedisTemplate<String, Object> redisTemplate;
  private LettuceConnectionFactory connectionFactory;

  private SelectEmpireService selectEmpireService;

  @DynamicPropertySource
  public static void redisProperties(DynamicPropertyRegistry registry) {
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

    selectEmpireService = new SelectEmpireService(redisTemplate);

    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @AfterEach
  public void afterEach() {
    connectionFactory.destroy();
  }

  @Test
  public void givenNonExistingCache_whenGetFromCache_thenReturnNull() {
    // given
    var accountId = 123L;

    // when
    var empire = selectEmpireService.getFromCache(accountId);

    // then
    assertThat(empire).isNull();
  }

  @Test
  public void givenValid_whenGetFromCache_thenReturnEmpire() {
    // given
    var accountId = 123L;
    var cachedEmpire = Empire.JINNO;

    selectEmpireService.setCache(accountId, cachedEmpire);

    // when
    var empire = selectEmpireService.getFromCache(accountId);

    // then
    assertThat(empire).isEqualTo(cachedEmpire);
  }
}
