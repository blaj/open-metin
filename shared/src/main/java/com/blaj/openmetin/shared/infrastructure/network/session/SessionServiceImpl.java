package com.blaj.openmetin.shared.infrastructure.network.session;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.model.Packet;
import com.blaj.openmetin.shared.common.model.Session;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

  private final SessionManagerService<? extends Session> sessionManagerService;

  @Override
  public void sendPacketAsync(long sessionId, Packet packet) {
    sessionManagerService
        .getSession(sessionId)
        .ifPresent(
            session -> {
              session.getChannel().writeAndFlush(packet);
              log.debug(
                  "Sent packet {} to session {}", packet.getClass().getSimpleName(), sessionId);
            });
  }

  @Override
  public void sendPacketSync(long sessionId, Packet packet) {
    sessionManagerService
        .getSession(sessionId)
        .ifPresent(
            session -> {
              try {
                var channelFuture = session.getChannel().writeAndFlush(packet);
                var success = channelFuture.await(5, TimeUnit.SECONDS);

                if (success && channelFuture.isSuccess()) {
                  log.debug(
                      "Packet {} sent successfully to session {}",
                      packet.getClass().getSimpleName(),
                      sessionId);
                } else {
                  log.warn(
                      "Failed to send packet {} to session {}",
                      packet.getClass().getSimpleName(),
                      sessionId,
                      channelFuture.cause());
                }
              } catch (InterruptedException e) {
                log.error("Interrupted while sending packet", e);
                Thread.currentThread().interrupt();
              }
            });
  }
}
