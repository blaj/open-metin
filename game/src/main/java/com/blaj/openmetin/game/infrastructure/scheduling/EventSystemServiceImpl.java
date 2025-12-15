package com.blaj.openmetin.game.infrastructure.scheduling;

import com.blaj.openmetin.game.application.common.eventsystem.EventSystemService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventSystemServiceImpl implements EventSystemService {

  private final Map<Long, ScheduledEvent> pendingEvents = new ConcurrentHashMap<>();
  private final AtomicLong nextEventId = new AtomicLong(1);
  private final TaskScheduler taskScheduler;

  public EventSystemServiceImpl(@Qualifier("eventTaskScheduler") TaskScheduler taskScheduler) {
    this.taskScheduler = taskScheduler;
  }

  @Override
  public long scheduleEvent(Supplier<Duration> callback, Duration initialDelay) {
    var eventId = nextEventId.getAndIncrement();

    var future =
        taskScheduler.schedule(
            () -> executeEvent(eventId, callback), Instant.now().plus(initialDelay));

    pendingEvents.put(eventId, new ScheduledEvent(eventId, future, callback));
    log.debug("Scheduled event {} with initial delay {}ms", eventId, initialDelay);

    return eventId;
  }

  @Override
  public void cancelEvent(long eventId) {
    Optional.ofNullable(pendingEvents.remove(eventId))
        .ifPresent(
            event -> {
              event.future().cancel(false);
              log.debug("Cancelled event {}", eventId);
            });
  }

  @Override
  public int getPendingEventsCount() {
    return pendingEvents.size();
  }

  private void executeEvent(long eventId, Supplier<Duration> callback) {
    try {
      var nextTimeout = Optional.ofNullable(callback).map(Supplier::get).orElse(Duration.ZERO);

      if (nextTimeout.isZero()) {
        pendingEvents.remove(eventId);
        log.debug("Event {} finished (returned 0)", eventId);
      } else {
        Optional.ofNullable(pendingEvents.get(eventId))
            .ifPresent(
                oldEvent -> {
                  var future =
                      taskScheduler.schedule(
                          () -> executeEvent(eventId, callback), Instant.now().plus(nextTimeout));

                  pendingEvents.put(eventId, new ScheduledEvent(eventId, future, callback));
                  log.debug("Rescheduled event {} for {}ms", eventId, nextTimeout);
                });
      }
    } catch (Exception e) {
      log.error("Error executing event {}", eventId, e);
      pendingEvents.remove(eventId);
    }
  }

  private record ScheduledEvent(long id, ScheduledFuture<?> future, Supplier<Duration> callback) {}
}
