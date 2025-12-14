package com.blaj.openmetin.shared.infrastructure.network.session;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionManagerServiceImplTest {

  private SessionManagerServiceImpl sessionManagerService;

  @Mock private Channel channel1;
  @Mock private Channel channel2;

  @BeforeEach
  public void beforeEach() {
    sessionManagerService = new SessionManagerServiceImpl();
  }

  @Test
  public void whenCreateSession_thenReturnsNewSession() {
    // given

    // when
    var session = sessionManagerService.createSession(channel1);

    // then
    assertThat(session).isNotNull();
    assertThat(session.getId()).isEqualTo(1L);
    assertThat(session.getChannel()).isEqualTo(channel1);
  }

  @Test
  public void givenMultipleSessions_whenCreateSession_thenIncrementsSessionId() {
    // given

    // when
    var session1 = sessionManagerService.createSession(channel1);
    var session2 = sessionManagerService.createSession(channel2);

    // then
    assertThat(session1.getId()).isEqualTo(1L);
    assertThat(session2.getId()).isEqualTo(2L);
  }

  @Test
  public void givenExistingSession_whenGetSession_thenReturnsSession() {
    // given
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
    var session = sessionManagerService.createSession(channel1);

    // when
    sessionManagerService.removeSession(session.getId());

    // then
    assertThat(sessionManagerService.getSession(session.getId())).isEmpty();
  }

  @Test
  public void givenLinkedSession_whenRemoveSession_thenRemovesFromBothMaps() {
    // given
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
    sessionManagerService.createSession(channel1);

    // when
    var allSessions = sessionManagerService.getAllSessions();

    // then
    assertThat(allSessions).isUnmodifiable();
  }

  @Test
  public void givenMultipleSessions_whenGetSessionCount_thenReturnsCorrectCount() {
    // given
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
