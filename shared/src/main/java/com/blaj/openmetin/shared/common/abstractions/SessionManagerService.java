package com.blaj.openmetin.shared.common.abstractions;

import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.Optional;

public interface SessionManagerService {
  AttributeKey<Session> sessionKey = AttributeKey.valueOf("session");

  Session createSession(Channel channel);

  Optional<Session> getSession(long sessionId);

  Optional<Session> getSessionByPid(int pid);

  Optional<Session> getSessionByAccountId(long accountId);

  void linkSessionToPid(long sessionId, int pid);

  void removeSession(long sessionId);

  Map<Long, Session> getAllSessions();

  int getSessionCount();
}
