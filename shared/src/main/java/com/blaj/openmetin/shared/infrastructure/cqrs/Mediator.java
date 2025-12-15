package com.blaj.openmetin.shared.infrastructure.cqrs;

import java.util.concurrent.CompletableFuture;

public interface Mediator {
  <T> T send(Request<T> request);

  <T> CompletableFuture<T> sendAsync(Request<T> request);
}
