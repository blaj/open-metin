package com.blaj.openmetin.game.application.common.eventsystem;

import java.time.Duration;
import java.util.function.Supplier;

public interface EventSystemService {

  long scheduleEvent(Supplier<Duration> callback, Duration initialDelay);

  void cancelEvent(long eventId);

  int getPendingEventsCount();
}
