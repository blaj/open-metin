package com.blaj.openmetin.game.application.common.character.service;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterCreationTimeService {

  private static final String CONSUME_LUA_SCRIPT =
      """
      local last = redis.call('GET', KEYS[1])
      local now = tonumber(ARGV[1])
      local window = tonumber(ARGV[2])
      if last and (now - tonumber(last)) < window then
        return 0
      else
        redis.call('SET', KEYS[1], now, 'EX', window)
        return 1
      end
      """;

  private final RedisTemplate<String, Object> redisTemplate;
  private final Clock clock;

  private static String redisKey(long accountId) {
    return "account:" + accountId + ":character_create";
  }

  public boolean tryConsume(long accountId, Duration window) {
    var nowSeconds = clock.instant().getEpochSecond();
    var windowSeconds = window.getSeconds();

    var script = new DefaultRedisScript<Long>();
    script.setScriptText(CONSUME_LUA_SCRIPT);
    script.setResultType(Long.class);

    var result =
        redisTemplate.execute(script, List.of(redisKey(accountId)), nowSeconds, windowSeconds);

    return result != null && result == 1L;
  }
}
