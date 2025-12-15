package com.blaj.openmetin.shared.infrastructure.cqrs;

public interface RequestHandler<T extends  Request<R>, R> {
  R handle(T request);
}
