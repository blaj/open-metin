package com.blaj.openmetin.shared.infrastructure.cqrs;

import java.util.function.Supplier;

public interface PipelineBehavior<T extends Request<R>, R> {
  R handle(T request, Supplier<R> next);
}