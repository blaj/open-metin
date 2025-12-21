package com.blaj.openmetin.game.application.common.empire;

import com.blaj.openmetin.game.domain.entity.Character.Empire;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelectEmpireService {

  private final RedisTemplate<String, Object> redisTemplate;

  private static String cacheKey(long accountId) {
    return "account:" + accountId + ":empire";
  }

  public void setCache(long accountId, Empire empire) {
    redisTemplate.opsForValue().set(cacheKey(accountId), empire);
  }

  public Empire getFromCache(long accountId) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(cacheKey(accountId)))
        .map(empire -> Empire.valueOf(empire.toString()))
        .orElse(null);
  }
}
