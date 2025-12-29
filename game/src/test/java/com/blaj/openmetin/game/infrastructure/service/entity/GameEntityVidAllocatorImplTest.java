package com.blaj.openmetin.game.infrastructure.service.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameEntityVidAllocatorImplTest {

  private GameEntityVidAllocatorImpl gameEntityVidAllocator;

  @BeforeEach
  public void beforeEach() {
    gameEntityVidAllocator = new GameEntityVidAllocatorImpl();
  }

  @Test
  public void givenNewAllocator_whenAllocateFirstVid_thenReturnsOne() {
    // given

    // when
    var vid = gameEntityVidAllocator.allocate();

    // then
    assertThat(vid).isEqualTo(1);
  }

  @Test
  public void givenNewAllocator_whenAllocateMultipleTimes_thenReturnsSequentialVids() {
    // given

    // when
    var vid1 = gameEntityVidAllocator.allocate();
    var vid2 = gameEntityVidAllocator.allocate();
    var vid3 = gameEntityVidAllocator.allocate();

    // then
    assertThat(vid1).isEqualTo(1);
    assertThat(vid2).isEqualTo(2);
    assertThat(vid3).isEqualTo(3);
  }

  @Test
  public void givenAllocatedVid_whenReleaseAndAllocateAgain_thenReusesReleasedVid() {
    // given
    var vid1 = gameEntityVidAllocator.allocate();
    var vid2 = gameEntityVidAllocator.allocate();

    // when
    gameEntityVidAllocator.release(vid1);
    var reusedVid = gameEntityVidAllocator.allocate();

    // then
    assertThat(reusedVid).isEqualTo(vid1);
  }

  @Test
  public void givenMultipleReleasedVids_whenAllocate_thenReusesInFifoOrder() {
    // given
    var vid1 = gameEntityVidAllocator.allocate();
    var vid2 = gameEntityVidAllocator.allocate();
    var vid3 = gameEntityVidAllocator.allocate();

    gameEntityVidAllocator.release(vid1);
    gameEntityVidAllocator.release(vid2);
    gameEntityVidAllocator.release(vid3);

    // when
    var reused1 = gameEntityVidAllocator.allocate();
    var reused2 = gameEntityVidAllocator.allocate();
    var reused3 = gameEntityVidAllocator.allocate();

    // then
    assertThat(reused1).isEqualTo(vid1);
    assertThat(reused2).isEqualTo(vid2);
    assertThat(reused3).isEqualTo(vid3);
  }

  @Test
  public void givenEmptyFreeQueue_whenAllocate_thenIncrementsCounter() {
    // given
    gameEntityVidAllocator.allocate();
    gameEntityVidAllocator.allocate();

    // when
    var vid3 = gameEntityVidAllocator.allocate();

    // then
    assertThat(vid3).isEqualTo(3);
  }

  @Test
  public void givenMixedAllocateAndReleaseOperations_whenExecute_thenWorksCorrectly() {
    // given & when
    var vid1 = gameEntityVidAllocator.allocate();
    var vid2 = gameEntityVidAllocator.allocate();
    gameEntityVidAllocator.release(vid1);
    var vid3 = gameEntityVidAllocator.allocate();
    var vid4 = gameEntityVidAllocator.allocate();
    gameEntityVidAllocator.release(vid3);
    gameEntityVidAllocator.release(vid2);
    var vid5 = gameEntityVidAllocator.allocate();
    var vid6 = gameEntityVidAllocator.allocate();

    // then
    assertThat(vid1).isEqualTo(1);
    assertThat(vid2).isEqualTo(2);
    assertThat(vid3).isEqualTo(1);
    assertThat(vid4).isEqualTo(3);
    assertThat(vid5).isEqualTo(1);
    assertThat(vid6).isEqualTo(2);
  }

  @Test
  public void givenConcurrentAllocations_whenAllocateFromMultipleThreads_thenAllVidsAreUnique()
      throws InterruptedException {
    // given
    var threadCount = 100;
    var allocationsPerThread = 100;
    var executor = Executors.newFixedThreadPool(threadCount);
    var startLatch = new CountDownLatch(1);
    var completionLatch = new CountDownLatch(threadCount);
    var allocatedVids = ConcurrentHashMap.<Integer>newKeySet();

    // when
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              for (int j = 0; j < allocationsPerThread; j++) {
                var vid = gameEntityVidAllocator.allocate();
                allocatedVids.add(vid);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              completionLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    completionLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // then
    var expectedCount = threadCount * allocationsPerThread;
    assertThat(allocatedVids).hasSize(expectedCount);
    assertThat(allocatedVids).allMatch(vid -> vid > 0 && vid <= expectedCount);
  }

  @Test
  public void givenConcurrentAllocateAndReleaseOperations_whenExecute_thenMaintainsConsistency()
      throws InterruptedException {
    // given
    var threadCount = 50;
    var operationsPerThread = 200;
    var executor = Executors.newFixedThreadPool(threadCount);
    var completionLatch = new CountDownLatch(threadCount);
    var allAllocatedVids = Collections.synchronizedList(new ArrayList<Integer>());

    // when
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              var localVids = new ArrayList<Integer>();
              for (int j = 0; j < operationsPerThread; j++) {
                var vid = gameEntityVidAllocator.allocate();
                localVids.add(vid);
                allAllocatedVids.add(vid);

                if (j % 3 == 0 && !localVids.isEmpty()) {
                  var toRelease = localVids.remove(0);
                  gameEntityVidAllocator.release(toRelease);
                }
              }
            } finally {
              completionLatch.countDown();
            }
          });
    }

    completionLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // then
    assertThat(allAllocatedVids).isNotEmpty();
    assertThat(allAllocatedVids).allMatch(vid -> vid > 0);
  }

  @Test
  public void givenReleasedVid_whenReleaseSameVidMultipleTimes_thenCanAllocateItMultipleTimes() {
    // given
    var vid = gameEntityVidAllocator.allocate();

    // when
    gameEntityVidAllocator.release(vid);
    gameEntityVidAllocator.release(vid);
    gameEntityVidAllocator.release(vid);

    var reused1 = gameEntityVidAllocator.allocate();
    var reused2 = gameEntityVidAllocator.allocate();
    var reused3 = gameEntityVidAllocator.allocate();

    // then
    assertThat(reused1).isEqualTo(vid);
    assertThat(reused2).isEqualTo(vid);
    assertThat(reused3).isEqualTo(vid);
  }

  @Test
  public void givenManyAllocations_whenReleaseAllAndAllocateAgain_thenReusesAllVids() {
    // given
    var count = 1000;
    var allocatedVids =
        IntStream.range(0, count).mapToObj(i -> gameEntityVidAllocator.allocate()).toList();

    // when
    allocatedVids.forEach(gameEntityVidAllocator::release);

    var reusedVids =
        IntStream.range(0, count).mapToObj(i -> gameEntityVidAllocator.allocate()).toList();

    // then
    assertThat(reusedVids).containsExactlyElementsOf(allocatedVids);
  }
}
