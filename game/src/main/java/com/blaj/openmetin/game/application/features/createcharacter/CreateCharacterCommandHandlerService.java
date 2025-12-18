package com.blaj.openmetin.game.application.features.createcharacter;

import com.blaj.openmetin.game.application.common.character.mapper.SimpleCharacterPacketMapper;
import com.blaj.openmetin.game.application.common.character.service.CharacterCreationTimeService;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.game.domain.repository.BannedWordRepository;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.application.common.config.TcpConfig;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import com.blaj.openmetin.shared.infrastructure.network.utils.NetworkUtils;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCharacterCommandHandlerService
    implements RequestHandler<CreateCharacterCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final CharacterService characterService;
  private final CharacterCreationTimeService characterCreationTimeService;
  private final CharacterRepository characterRepository;
  private final BannedWordRepository bannedWordRepository;
  private final TcpConfig tcpConfig;

  private static boolean isNameValid(String name) {
    return name.length() >= 2
        && name.length() - 1 <= CharacterConstants.CHARACTER_NAME_MAX_LENGTH
        && name.chars().allMatch(Character::isLetterOrDigit);
  }

  @Override
  public Void handle(CreateCharacterCommand request) {
    var session = sessionManagerService.getSession(request.sessionId()).orElseThrow();

    if (session.getAccountId() == null) {
      log.warn("Character create before authorization for session {}", session.getId());
      session.getChannel().close();
      return null;
    }

    if (!isNameValid(request.name())) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 1));
      return null;
    }

    if (bannedWordRepository.existsByWord(request.name())) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 1));
      return null;
    }

    if (request.shape() > 1) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 1));
      return null;
    }

    if (characterRepository.existsByName(request.name())) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 0));
      return null;
    }

    if (characterRepository.existsBySlotAndAccountId(request.slot(), session.getAccountId())) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 0));
      return null;
    }

    if (characterRepository.countByAccountId(session.getAccountId())
        >= CharacterConstants.MAX_CHARACTERS_PER_ACCOUNT) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 0));
      return null;
    }

    if (!characterCreationTimeService.tryConsume(
        session.getAccountId(),
        Duration.ofSeconds(CharacterConstants.INTERVAL_BETWEEN_CHARACTER_CREATE_IN_SECONDS))) {
      sessionService.sendPacketAsync(
          request.sessionId(), new CreateCharacterFailurePacket().setError((short) 1));
      return null;
    }

    var createdCharacterDto =
        characterService.create(
            session.getAccountId(),
            request.name(),
            request.classType(),
            request.shape(),
            request.slot());

    var simpleCharacterPacket = SimpleCharacterPacketMapper.map(createdCharacterDto);
    simpleCharacterPacket.setIp(
        NetworkUtils.ipToInt(
            NetworkUtils.resolveAdvertisedAddress(
                tcpConfig.host(), NetworkUtils.getLocalAddress(session.getChannel()))));
    simpleCharacterPacket.setPort(tcpConfig.port());

    sessionService.sendPacketAsync(
        request.sessionId(),
        new CreateCharacterSuccessPacket()
            .setSlot(request.slot())
            .setSimpleCharacterPacket(simpleCharacterPacket));

    return null;
  }
}
