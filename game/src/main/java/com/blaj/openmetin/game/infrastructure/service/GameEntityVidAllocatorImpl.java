package com.blaj.openmetin.game.infrastructure.service;

import com.blaj.openmetin.game.application.common.game.GameEntityVidAllocator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class GameEntityVidAllocatorImpl implements GameEntityVidAllocator {

  private final ConcurrentLinkedQueue<Integer> toFreeQueue = new ConcurrentLinkedQueue<>();
  private final AtomicInteger counter = new AtomicInteger(0);

  @Override
  public int allocate() {
    var reusedFromQueue = toFreeQueue.poll();

    if (reusedFromQueue != null) {
      return reusedFromQueue;
    }

    return counter.incrementAndGet();
  }

  @Override
  public void release(int vid) {
    toFreeQueue.offer(vid);
  }
}
