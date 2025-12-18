package com.blaj.openmetin.shared.common.service;

import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;

public interface SessionFactoryService<T extends Session> {

  T createSession(long sessionId, Channel channel);
}
