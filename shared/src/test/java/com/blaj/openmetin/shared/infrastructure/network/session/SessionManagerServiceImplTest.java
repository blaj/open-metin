package com.blaj.openmetin.shared.infrastructure.network.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.service.SessionFactoryService;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionManagerServiceImplTest {

  private SessionManagerServiceImpl<Session> sessionManagerService;

  @Mock private SessionFactoryService<Session> sessionFactoryService;
  @Mock private Channel channel1;
  @Mock private Channel channel2;

  @BeforeEach
  public void beforeEach() {
    sessionManagerService = new SessionManagerServiceImpl<>(sessionFactoryService);
  }

  @Test
  public void whenCreateSession_thenReturnsNewSession() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    // when
    var session = sessionManagerService.createSession(channel1);

    // then
    assertThat(session).isNotNull();
    assertThat(session).isEqualTo(givenSession);
  }

  @Test
  public void givenMultipleSessions_whenCreateSession_thenIncrementsSessionId() {
    // given
    var sessionId1 = 1L;
    var sessionId2 = 2L;
    var givenSession1 = new Session(sessionId1, channel1);
    var givenSession2 = new Session(sessionId2, channel2);

    given(sessionFactoryService.createSession(sessionId1, channel1)).willReturn(givenSession1);
    given(sessionFactoryService.createSession(sessionId2, channel2)).willReturn(givenSession2);

    // when
    var session1 = sessionManagerService.createSession(channel1);
    var session2 = sessionManagerService.createSession(channel2);

    // then
    assertThat(session1).isEqualTo(givenSession1);
    assertThat(session2).isEqualTo(givenSession2);
  }

  @Test
  public void givenExistingSession_whenGetSession_thenReturnsSession() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    var session = sessionManagerService.createSession(channel1);

    // when
    var result = sessionManagerService.getSession(session.getId());

    // then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(session);
  }

  @Test
  public void givenNonExistingSession_whenGetSession_thenReturnsEmpty() {
    // given

    // when
    var result = sessionManagerService.getSession(999L);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenLinkedSession_whenGetSessionByPid_thenReturnsSession() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    var session = sessionManagerService.createSession(channel1);
    sessionManagerService.linkSessionToPid(session.getId(), 100);

    // when
    var result = sessionManagerService.getSessionByPid(100);

    // then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(session);
  }

  @Test
  public void givenNonLinkedPid_whenGetSessionByPid_thenReturnsEmpty() {
    // given

    // when
    var result = sessionManagerService.getSessionByPid(999);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenSessionWithAccountId_whenGetSessionByAccountId_thenReturnsSession() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    var session = sessionManagerService.createSession(channel1);
    session.setAccountId(12345L);

    // when
    var result = sessionManagerService.getSessionByAccountId(12345L);

    // then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(session);
  }

  @Test
  public void givenNoSessionWithAccountId_whenGetSessionByAccountId_thenReturnsEmpty() {
    // given

    // when
    var result = sessionManagerService.getSessionByAccountId(99999L);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenSession_whenLinkSessionToPid_thenLinksCorrectly() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    var session = sessionManagerService.createSession(channel1);

    // when
    sessionManagerService.linkSessionToPid(session.getId(), 100);

    // then
    assertThat(session.getPid()).isEqualTo(100);
    assertThat(sessionManagerService.getSessionByPid(100)).isPresent();
  }

  @Test
  public void givenNonExistingSession_whenLinkSessionToPid_thenDoesNothing() {
    // given

    // when
    sessionManagerService.linkSessionToPid(999L, 100);

    // then
    assertThat(sessionManagerService.getSessionByPid(100)).isEmpty();
  }

  @Test
  public void givenSession_whenRemoveSession_thenRemovesSession() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    var session = sessionManagerService.createSession(channel1);

    // when
    sessionManagerService.removeSession(session.getId());

    // then
    assertThat(sessionManagerService.getSession(session.getId())).isEmpty();
  }

  @Test
  public void givenLinkedSession_whenRemoveSession_thenRemovesFromBothMaps() {
    // given
    var sessionId = 1L;
    var givenSession = new Session(sessionId, channel1);

    given(sessionFactoryService.createSession(sessionId, channel1)).willReturn(givenSession);

    var session = sessionManagerService.createSession(channel1);
    sessionManagerService.linkSessionToPid(session.getId(), 100);

    // when
    sessionManagerService.removeSession(session.getId());

    // then
    assertThat(sessionManagerService.getSession(session.getId())).isEmpty();
    assertThat(sessionManagerService.getSessionByPid(100)).isEmpty();
  }

  @Test
  public void givenMultipleSessions_whenGetAllSessions_thenReturnsAllSessions() {
    // given
    var sessionId1 = 1L;
    var sessionId2 = 2L;
    var givenSession1 = new Session(sessionId1, channel1);
    var givenSession2 = new Session(sessionId2, channel2);

    given(sessionFactoryService.createSession(sessionId1, channel1)).willReturn(givenSession1);
    given(sessionFactoryService.createSession(sessionId2, channel2)).willReturn(givenSession2);

    var session1 = sessionManagerService.createSession(channel1);
    var session2 = sessionManagerService.createSession(channel2);

    // when
    var allSessions = sessionManagerService.getAllSessions();

    // then
    assertThat(allSessions).hasSize(2);
    assertThat(allSessions).containsValues(session1, session2);
  }

  @Test
  public void whenGetAllSessions_thenReturnsImmutableCopy() {
    // given
    var sessionId1 = 1L;
    var sessionId2 = 2L;
    var givenSession1 = new Session(sessionId1, channel1);
    var givenSession2 = new Session(sessionId2, channel2);

    given(sessionFactoryService.createSession(sessionId1, channel1)).willReturn(givenSession1);
    given(sessionFactoryService.createSession(sessionId2, channel2)).willReturn(givenSession2);

    sessionManagerService.createSession(channel1);
    sessionManagerService.createSession(channel2);

    // when
    var allSessions = sessionManagerService.getAllSessions();

    // then
    assertThat(allSessions).isUnmodifiable();
    assertThat(allSessions).hasSize(2);
  }

  @Test
  public void givenMultipleSessions_whenGetSessionCount_thenReturnsCorrectCount() {
    // given
    var sessionId1 = 1L;
    var sessionId2 = 2L;
    var givenSession1 = new Session(sessionId1, channel1);
    var givenSession2 = new Session(sessionId2, channel2);

    given(sessionFactoryService.createSession(sessionId1, channel1)).willReturn(givenSession1);
    given(sessionFactoryService.createSession(sessionId2, channel2)).willReturn(givenSession2);

    sessionManagerService.createSession(channel1);
    sessionManagerService.createSession(channel2);

    // when
    var count = sessionManagerService.getSessionCount();

    // then
    assertThat(count).isEqualTo(2);
  }

  @Test
  public void givenNoSessions_whenGetSessionCount_thenReturnsZero() {
    // given

    // when
    var count = sessionManagerService.getSessionCount();

    // then
    assertThat(count).isZero();
  }
}
