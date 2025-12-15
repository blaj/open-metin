package com.blaj.openmetin.game.infrastructure.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
public class EventSystemServiceImplTest {

  private EventSystemServiceImpl eventSystemService;

  @Mock private TaskScheduler taskScheduler;
  @Mock private ScheduledFuture<?> scheduledFuture;

  @Captor private ArgumentCaptor<Runnable> runnableArgumentCaptor;
  @Captor private ArgumentCaptor<Instant> instantArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    eventSystemService = new EventSystemServiceImpl(taskScheduler);
  }

  @Test
  void givenCallback_whenScheduleEvent_thenEventIsScheduled() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);
    var initialDelay = Duration.ofSeconds(10);

    doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));

    // when
    var eventId = eventSystemService.scheduleEvent(callback, initialDelay);

    // then
    then(taskScheduler).should().schedule(any(Runnable.class), any(Instant.class));

    assertThat(eventId).isEqualTo(1L);
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(1);
  }

  @Test
  void givenMultipleCallbacks_whenScheduleEvent_thenEventIdsAreIncremented() {
    // given
    Supplier<Duration> callback1 = () -> Duration.ofSeconds(10);
    Supplier<Duration> callback2 = () -> Duration.ofSeconds(20);

    doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));

    // when
    var eventId1 = eventSystemService.scheduleEvent(callback1, Duration.ofSeconds(5));
    var eventId2 = eventSystemService.scheduleEvent(callback2, Duration.ofSeconds(5));

    // then
    assertThat(eventId1).isEqualTo(1L);
    assertThat(eventId2).isEqualTo(2L);
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(2);
  }

  @Test
  void givenScheduledEvent_whenCancelEvent_thenEventIsRemoved() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);

    doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));

    var eventId = eventSystemService.scheduleEvent(callback, Duration.ofSeconds(10));

    // when
    eventSystemService.cancelEvent(eventId);

    // then
    then(scheduledFuture).should().cancel(false);

    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
  }

  @Test
  void givenNonExistentEvent_whenCancelEvent_thenNoException() {
    // when
    eventSystemService.cancelEvent(999L);

    // then
    then(scheduledFuture).shouldHaveNoInteractions();

    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
  }

  @Test
  void givenNoEvents_whenGetPendingEventsCount_thenReturnZero() {
    // when
    int count = eventSystemService.getPendingEventsCount();

    // then
    assertThat(count).isEqualTo(0);
  }

  @Test
  void givenCallbackReturnsZero_whenExecuteEvent_thenEventIsRemoved() {
    // given
    Supplier<Duration> callback = () -> Duration.ZERO;

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(runnableArgumentCaptor.capture(), any(Instant.class));

    // when
    eventSystemService.scheduleEvent(callback, Duration.ofSeconds(1));
    var scheduledTask = runnableArgumentCaptor.getValue();
    scheduledTask.run();

    // then
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
  }

  @Test
  void givenCallbackReturnsNull_whenExecuteEvent_thenEventIsRemoved() {
    // given
    Supplier<Duration> callback = () -> null;

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(runnableArgumentCaptor.capture(), any(Instant.class));

    // when
    eventSystemService.scheduleEvent(callback, Duration.ofSeconds(1));
    var scheduledTask = runnableArgumentCaptor.getValue();
    scheduledTask.run();

    // then
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
  }

  @Test
  void givenCallbackReturnsNonZero_whenExecuteEvent_thenEventIsRescheduled() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(runnableArgumentCaptor.capture(), any(Instant.class));

    // when
    eventSystemService.scheduleEvent(callback, Duration.ofSeconds(10));
    var scheduledTask = runnableArgumentCaptor.getValue();
    scheduledTask.run();

    // then
    then(taskScheduler).should(times(2)).schedule(any(Runnable.class), any(Instant.class));

    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(1);
  }

  @Test
  void givenCallbackThrowsException_whenExecuteEvent_thenEventIsRemoved() {
    // given
    Supplier<Duration> callback =
        () -> {
          throw new RuntimeException("Test exception");
        };

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(runnableArgumentCaptor.capture(), any(Instant.class));

    // when
    eventSystemService.scheduleEvent(callback, Duration.ofSeconds(1));
    var scheduledTask = runnableArgumentCaptor.getValue();
    scheduledTask.run();

    // then
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
  }

  @Test
  void givenScheduledEvent_whenScheduleEventWithInitialDelay_thenCorrectInstantIsUsed() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);
    var initialDelay = Duration.ofSeconds(10);

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(any(Runnable.class), instantArgumentCaptor.capture());

    var beforeSchedule = Instant.now();

    // when
    eventSystemService.scheduleEvent(callback, initialDelay);

    // then
    var scheduledInstant = instantArgumentCaptor.getValue();
    var afterSchedule = Instant.now().plus(initialDelay);

    assertThat(scheduledInstant).isBetween(beforeSchedule.plus(initialDelay), afterSchedule);
  }

  @Test
  void givenCallbackReturnsNonZero_whenReschedule_thenNewDelayIsUsed() {
    // given
    Duration nextDelay = Duration.ofSeconds(45);
    Supplier<Duration> callback = () -> nextDelay;

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(runnableArgumentCaptor.capture(), instantArgumentCaptor.capture());

    // when
    eventSystemService.scheduleEvent(callback, Duration.ofSeconds(10));
    Runnable scheduledTask = runnableArgumentCaptor.getValue();

    var beforeReschedule = Instant.now();
    scheduledTask.run();

    // then
    assertThat(instantArgumentCaptor.getAllValues()).hasSize(2);
    var rescheduledInstant = instantArgumentCaptor.getAllValues().get(1);
    var afterReschedule = Instant.now().plus(nextDelay);

    assertThat(rescheduledInstant).isBetween(beforeReschedule.plus(nextDelay), afterReschedule);
  }

  @Test
  void givenEventCancelledBeforeReschedule_whenExecuteEvent_thenEventIsNotRescheduled() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(runnableArgumentCaptor.capture(), any(Instant.class));

    // when
    var eventId = eventSystemService.scheduleEvent(callback, Duration.ofSeconds(10));
    eventSystemService.cancelEvent(eventId);
    var scheduledTask = runnableArgumentCaptor.getValue();
    scheduledTask.run();

    // then
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
    then(taskScheduler).should(times(1)).schedule(any(Runnable.class), any(Instant.class));
  }

  @Test
  void givenMultipleEvents_whenCancelOneEvent_thenOtherEventsRemain() {
    // given
    Supplier<Duration> callback1 = () -> Duration.ofSeconds(10);
    Supplier<Duration> callback2 = () -> Duration.ofSeconds(20);
    Supplier<Duration> callback3 = () -> Duration.ofSeconds(30);

    doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));

    var eventId1 = eventSystemService.scheduleEvent(callback1, Duration.ofSeconds(5));
    var eventId2 = eventSystemService.scheduleEvent(callback2, Duration.ofSeconds(5));
    var eventId3 = eventSystemService.scheduleEvent(callback3, Duration.ofSeconds(5));

    // when
    eventSystemService.cancelEvent(eventId2);

    // then
    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(2);
  }

  @Test
  void givenScheduledEvent_whenCancelSameEventTwice_thenNoException() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);

    doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));

    var eventId = eventSystemService.scheduleEvent(callback, Duration.ofSeconds(10));

    // when
    eventSystemService.cancelEvent(eventId);
    eventSystemService.cancelEvent(eventId);

    // then
    then(scheduledFuture).should(times(1)).cancel(false);

    assertThat(eventSystemService.getPendingEventsCount()).isEqualTo(0);
  }

  @Test
  void givenZeroInitialDelay_whenScheduleEvent_thenEventIsScheduledImmediately() {
    // given
    Supplier<Duration> callback = () -> Duration.ofSeconds(30);
    var initialDelay = Duration.ZERO;

    doReturn(scheduledFuture)
        .when(taskScheduler)
        .schedule(any(Runnable.class), instantArgumentCaptor.capture());

    var beforeSchedule = Instant.now();

    // when
    eventSystemService.scheduleEvent(callback, initialDelay);

    // then
    var scheduledInstant = instantArgumentCaptor.getValue();
    var afterSchedule = Instant.now();

    assertThat(scheduledInstant).isBetween(beforeSchedule, afterSchedule);
  }
}
