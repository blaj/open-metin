package com.blaj.openmetin.shared.common.abstractions;

import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.Optional;

public interface SessionManagerService<T extends Session> {

  AttributeKey<Session> sessionKey = AttributeKey.valueOf("session");

  T createSession(Channel channel);

  Optional<T> getSession(long sessionId);

  Optional<T> getSessionByPid(int pid);

  Optional<T> getSessionByAccountId(long accountId);

  void linkSessionToPid(long sessionId, int pid);

  void removeSession(long sessionId);

  Map<Long, T> getAllSessions();

  int getSessionCount();
}
