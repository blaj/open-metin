package com.blaj.openmetin.game.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.entity.ServerStatus;
import com.blaj.openmetin.game.domain.entity.ServerStatus.Status;
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

public class ServerStatusRepositoryImplTest {

  private static final RedisContainer redis =
      new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

  private ServerStatusRepositoryImpl serverStatusRepository;
  private ObjectMapper objectMapper;
  private RedisTemplate<String, Object> redisTemplate;
  private LettuceConnectionFactory connectionFactory;

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

    serverStatusRepository = new ServerStatusRepositoryImpl(redisTemplate, objectMapper);
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @AfterEach
  public void afterEach() {
    connectionFactory.destroy();
  }

  @Test
  public void givenNonExistingServerStatuses_whenGetServerStatuses_thenReturnEmptyList() {
    // given

    // when
    var serverStatuses = serverStatusRepository.getServerStatuses();

    // then
    assertThat(serverStatuses).isEmpty();
  }

  @Test
  public void givenValid_whenGetServerStatuses_thenReturnServerStatuses() {
    // given
    var serverStatus1 =
        ServerStatus.builder().channelIndex(1).port(111).status(Status.UNKNOWN).build();
    var serverStatus2 =
        ServerStatus.builder().channelIndex(2).port(222).status(Status.FULL).build();

    serverStatusRepository.saveServerStatus(serverStatus1);
    serverStatusRepository.saveServerStatus(serverStatus2);

    // when
    var serverStatuses = serverStatusRepository.getServerStatuses();

    // then
    assertThat(serverStatuses).isNotEmpty();
    assertThat(serverStatuses).hasSize(2);
    assertThat(serverStatuses).contains(serverStatus1, serverStatus2);
  }
}
