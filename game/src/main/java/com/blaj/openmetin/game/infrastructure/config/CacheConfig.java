package com.blaj.openmetin.game.infrastructure.config;

import com.blaj.openmetin.game.application.common.account.AccountDto;
import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private final JsonMapper jsonMapper;

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    var redisTemplate = new RedisTemplate<String, Object>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJacksonJsonRedisSerializer(jsonMapper));

    return redisTemplate;
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    var config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new JacksonJsonRedisSerializer<>(jsonMapper, Object.class)));

    var charactersListType =
        jsonMapper.getTypeFactory().constructCollectionType(List.class, CharacterDto.class);
    var charactersConfig =
        config
            .entryTtl(Duration.ofMinutes(30))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new JacksonJsonRedisSerializer<>(jsonMapper, charactersListType)));

    var accountDtoType = jsonMapper.getTypeFactory().constructType(AccountDto.class);
    var accountsConfig =
        config
            .entryTtl(Duration.ofDays(7))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new JacksonJsonRedisSerializer<>(jsonMapper, accountDtoType)));

    var monsterDefinitionsListType =
        jsonMapper.getTypeFactory().constructCollectionType(List.class, MonsterDefinition.class);
    var monsterDefinitionsConfig =
        config
            .entryTtl(Duration.ofDays(7))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new JacksonJsonRedisSerializer<>(jsonMapper, monsterDefinitionsListType)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration("characters", charactersConfig)
        .withCacheConfiguration("accounts", accountsConfig)
        .withCacheConfiguration("monster_definitions", monsterDefinitionsConfig)
        .build();
  }
}
