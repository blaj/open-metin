package com.blaj.openmetin.game.infrastructure.repository;

import com.blaj.openmetin.game.domain.entity.ServerStatus;
import com.blaj.openmetin.game.domain.repository.ServerStatusRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ServerStatusRepositoryImpl implements ServerStatusRepository {

  private static final String serverStatusKey = "server-status:";

  private static final Duration serverStatusDuration = Duration.ofMinutes(2);

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void saveServerStatus(ServerStatus serverStatus) {
    redisTemplate
        .opsForValue()
        .set(serverStatusKey + serverStatus.getChannelIndex(), serverStatus, serverStatusDuration);
  }

  @Override
  public List<ServerStatus> getServerStatuses() {
    Set<String> keys = new HashSet<>();

    redisTemplate.execute(
        (RedisCallback<Set<String>>)
            connection -> {
              var scanOptions =
                  ScanOptions.scanOptions().match(serverStatusKey + "*").count(100).build();
              var cursor = connection.scan(scanOptions);

              while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
              }

              cursor.close();

              return keys;
            });

    if (keys.isEmpty()) {
      return Collections.emptyList();
    }

    var values = redisTemplate.opsForValue().multiGet(keys);

    return values.stream()
        .filter(Objects::nonNull)
        .map(value -> objectMapper.convertValue(value, ServerStatus.class))
        .toList();
  }
}
