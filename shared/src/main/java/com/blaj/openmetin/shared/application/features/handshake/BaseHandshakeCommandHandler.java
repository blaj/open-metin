package com.blaj.openmetin.shared.application.features.handshake;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joou.UInteger;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseHandshakeCommandHandler<T extends Session>
    implements RequestHandler<HandshakeCommand, Void> {

  private final SessionService sessionService;
  private final SessionManagerService<T> sessionManagerService;

  @Override
  public Void handle(HandshakeCommand request) {
    sessionManagerService
        .getSession(request.sessionId())
        .ifPresent(
            session -> {
              if (!session.isHandshaking()) {
                log.info("Received handshake while not handshaking!");
                session.getChannel().close();
                return;
              }

              if (request.handshake() != session.getHandshake()) {
                log.info(
                    "Received wrong handshake ({} != {})",
                    session.getHandshake(),
                    request.handshake());
                session.getChannel().close();
                return;
              }

              var currentTime = DateTimeUtils.getUnixTime();
              var difference = currentTime - (request.time().longValue() + request.delta());

              if (difference >= 0 && difference <= 50) {
                log.info("Handshake done");
                session.setHandshaking(false);
                onSuccessHandshake(session);
              } else {
                var newDelta = (currentTime - request.time().longValue()) / 2;

                if (newDelta < 0) {
                  newDelta =
                      (currentTime - Optional.ofNullable(session.getLastHandshakeTime()).orElse(0L))
                          / 2;
                }

                session.setLastHandshakeTime(currentTime);

                sessionService.sendPacketAsync(
                    request.sessionId(),
                    new HandshakePacket()
                        .setHandshake(request.handshake())
                        .setTime(UInteger.valueOf(currentTime))
                        .setDelta((int) newDelta));
              }
            });

    return null;
  }

  protected abstract void onSuccessHandshake(Session session);
}
