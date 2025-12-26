package com.blaj.openmetin.game.application.features.entergame;

import com.blaj.openmetin.game.application.common.character.dto.CharacterAdditionalDataPacket;
import com.blaj.openmetin.game.application.common.character.dto.SpawnCharacterPacket;
import com.blaj.openmetin.game.application.common.config.ChannelPropertiesConfig;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joou.UByte;
import org.joou.UInteger;
import org.joou.UShort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntergameCommandHandlerService implements RequestHandler<EntergameCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final ChannelPropertiesConfig channelPropertiesConfig;

  @Override
  public Void handle(EntergameCommand request) {
    var session =
        sessionManagerService
            .getSession(request.sessionId())
            .orElseThrow(() -> new EntityNotFoundException("Session not exists"));

    if (session.getAccountId() == null) {
      log.warn("Character create before authorization for session {}", session.getId());
      session.getChannel().close();
      return null;
    }

    session.setPhase(Phase.IN_GAME);

    sessionService.sendPacketAsync(session.getId(), new PhasePacket().setPhase(session.getPhase()));
    sessionService.sendPacketAsync(
        session.getId(),
        new GameTimePacket().setServerTime(UInteger.valueOf(DateTimeUtils.getUnixTime())));
    sessionService.sendPacketAsync(
        session.getId(),
        new ChannelPacket().setChannelNo(channelPropertiesConfig.channelIndex()));

    var gameCharacterEntity = session.getGameCharacterEntity();

    sessionService.sendPacketAsync(
        session.getId(),
        new SpawnCharacterPacket()
            .setVid(gameCharacterEntity.getVid())
            .setAngle(0)
            .setPositionX(gameCharacterEntity.getPositionX())
            .setPositionY(gameCharacterEntity.getPositionY())
            .setPositionZ(0)
            .setCharacterType(UByte.valueOf(gameCharacterEntity.getType().ordinal()))
            .setClassType(
                UShort.valueOf(gameCharacterEntity.getCharacterDto().getClassType().getValue()))
            .setMoveSpeed(UByte.valueOf(gameCharacterEntity.getMovementSpeed()))
            .setAttackSpeed(UByte.valueOf(gameCharacterEntity.getAttackSpeed()))
            .setState(UByte.valueOf(0))
            .setAffects(new UInteger[2]));

    sessionService.sendPacketAsync(
        session.getId(),
        new CharacterAdditionalDataPacket()
            .setVid(gameCharacterEntity.getVid())
            .setName(gameCharacterEntity.getCharacterDto().getName())
            .setParts(
                new UShort[] {
                  UShort.valueOf(0), UShort.valueOf(0), UShort.valueOf(1001), UShort.valueOf(1001)
                }) // TODO: change after inventory implement
            .setEmpire(gameCharacterEntity.getEmpire())
            .setGuildId(UInteger.valueOf(0)) // TODO: change after guild implement
            .setLevel(
                UInteger.valueOf(gameCharacterEntity.getCharacterDto().getLevel().byteValue()))
            .setRankPoints((short) 0) // TODO
            .setPkMode(UByte.valueOf(0)) // TODO
            .setMountVnum(UInteger.valueOf(0))); // TODO: change after mount system implement

    return null;
  }
}
