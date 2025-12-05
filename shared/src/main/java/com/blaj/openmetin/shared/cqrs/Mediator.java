package com.blaj.openmetin.shared.cqrs;

public interface Mediator {
  <T> T send(Request<T> request);
}
