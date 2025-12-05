package com.blaj.openmetin.shared.cqrs;

import java.util.function.Supplier;

public interface PipelineBehavior<T extends Request<R>, R> {
  R handle(T request, Supplier<R> next);
}