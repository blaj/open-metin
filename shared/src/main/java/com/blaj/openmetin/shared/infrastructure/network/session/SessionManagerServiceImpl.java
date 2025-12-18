package com.blaj.openmetin.shared.infrastructure.network.session;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.service.SessionFactoryService;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SessionManagerServiceImpl<T extends Session> implements SessionManagerService<T> {

  private final AtomicLong sessionIdGenerator = new AtomicLong(1);
  private final Map<Long, T> sessions = new ConcurrentHashMap<>();
  private final Map<Integer, T> sessionsByPid = new ConcurrentHashMap<>();

  private final SessionFactoryService<T> sessionFactoryService;

  public T createSession(Channel channel) {
    var sessionId = sessionIdGenerator.getAndIncrement();
    var session = sessionFactoryService.createSession(sessionId, channel);

    sessions.put(sessionId, session);
    log.debug("Created session {} for {}", sessionId, channel.remoteAddress());

    return session;
  }

  @Override
  public Optional<T> getSession(long sessionId) {
    return Optional.ofNullable(sessions.get(sessionId));
  }

  @Override
  public Optional<T> getSessionByPid(int pid) {
    return Optional.ofNullable(sessionsByPid.get(pid));
  }

  @Override
  public Optional<T> getSessionByAccountId(long accountId) {
    return sessions.values().stream()
        .filter(session -> session.getAccountId().equals(accountId))
        .findFirst();
  }

  @Override
  public void linkSessionToPid(long sessionId, int pid) {
    Optional.ofNullable(sessions.get(sessionId))
        .ifPresent(
            session -> {
              session.setPid(pid);
              sessionsByPid.put(pid, session);
              log.debug("Linked session {} to player {}", sessionId, pid);
            });
  }

  @Override
  public void removeSession(long sessionId) {
    Optional.ofNullable(sessions.remove(sessionId))
        .map(Session::getPid)
        .ifPresent(sessionsByPid::remove);

    log.debug("Removed session {}", sessionId);
  }

  @Override
  public Map<Long, T> getAllSessions() {
    return Map.copyOf(sessions);
  }

  @Override
  public int getSessionCount() {
    return sessions.size();
  }
}
