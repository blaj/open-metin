package com.blaj.openmetin.game.application.features.tokenlogin;

import com.blaj.openmetin.game.application.common.character.dto.CharacterListPacket;
import com.blaj.openmetin.game.application.common.character.mapper.SimpleCharacterPacketMapper;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.application.common.empire.EmpirePacket;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.application.common.config.TcpConfig;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.domain.repository.LoginTokenRepository;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import com.blaj.openmetin.shared.infrastructure.network.utils.NetworkUtils;
import java.util.random.RandomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenLoginCommandHandlerService implements RequestHandler<TokenLoginCommand, Void> {

  private final LoginTokenRepository loginTokenRepository;
  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final CharacterService characterService;
  private final TcpConfig tcpConfig;

  @Override
  public Void handle(TokenLoginCommand request) {
    var session = sessionManagerService.getSession(request.sessionId()).orElse(null);

    if (session == null) {
      return null;
    }

    var loginToken = loginTokenRepository.getLoginToken(request.key());

    if (loginToken == null) {
      log.warn("Login token not exists for key {}", request.key());
      // TODO: use async close
      session.getChannel().close();
      return null;
    }

    if (!loginToken.getUsername().equals(request.username())) {
      log.warn(
          "Received invalid login key, username does not match {} != {}",
          loginToken.getUsername(),
          request.username());
      // TODO: use async close
      session.getChannel().close();
      return null;
    }

    var characterListPacket = new CharacterListPacket();
    characterListPacket.setHandle(session.getId());
    characterListPacket.setRandomKey(RandomGenerator.getDefault().nextInt());

    var characters = characterService.getCharacters(loginToken.getAccountId());

    characters.forEach(
        characterDto -> {
          characterListPacket.getSimpleCharacterPackets()[characterDto.getSlot()] =
              SimpleCharacterPacketMapper.map(characterDto);
          characterListPacket.getSimpleCharacterPackets()[characterDto.getSlot()].setIp(
              NetworkUtils.ipToInt(
                  NetworkUtils.resolveAdvertisedAddress(
                      tcpConfig.host(), NetworkUtils.getLocalAddress(session.getChannel()))));
          characterListPacket.getSimpleCharacterPackets()[characterDto.getSlot()].setPort(
              tcpConfig.port());
        });

    var empire = Empire.SHINSOO;
    if (!characters.isEmpty()) {
      empire = characters.getFirst().getEmpire();
    }

    session.setAccountId(loginToken.getAccountId());
    session.setPhase(Phase.SELECT_CHARACTER);

    sessionService.sendPacketAsync(session.getId(), new EmpirePacket().setEmpire(empire));
    sessionService.sendPacketAsync(session.getId(), new PhasePacket().setPhase(session.getPhase()));
    sessionService.sendPacketAsync(session.getId(), characterListPacket);

    return null;
  }
}
