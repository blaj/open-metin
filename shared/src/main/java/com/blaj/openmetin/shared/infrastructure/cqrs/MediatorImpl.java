package com.blaj.openmetin.shared.infrastructure.cqrs;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediatorImpl implements Mediator {
  private final Map<Class<?>, RequestHandler<?, ?>> requestHandlerMap = new ConcurrentHashMap<>();
  private final List<PipelineBehavior<?, ?>> behaviorList;
  private final Executor virtualThreadExecutor;

  public MediatorImpl(
      List<RequestHandler<?, ?>> requestHandlers,
      List<PipelineBehavior<?, ?>> behaviorList,
      @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
    requestHandlers.forEach(this::register);

    this.behaviorList = behaviorList;
    this.virtualThreadExecutor = virtualThreadExecutor;
  }

  @Override
  public <R> R send(Request<R> request) {
    return executeRequest(request);
  }

  @Override
  public <T> CompletableFuture<T> sendAsync(Request<T> request) {
    return CompletableFuture.supplyAsync(() -> executeRequest(request), virtualThreadExecutor)
        .whenComplete(
            (result, throwable) -> {
              if (throwable != null) {
                log.error(
                    "Error executing async request: {}",
                    request.getClass().getSimpleName(),
                    throwable);
              }
            });
  }

  @SuppressWarnings("unchecked")
  private <R> R executeRequest(Request<R> request) {
    Supplier<R> terminal =
        () -> {
          var handler =
              (RequestHandler<Request<R>, R>)
                  Optional.ofNullable(requestHandlerMap.get(request.getClass()))
                      .orElseThrow(
                          () ->
                              new IllegalStateException(
                                  "No handler for " + request.getClass().getName()));

          return handler.handle(request);
        };

    var pipeline =
        IntStream.iterate(behaviorList.size() - 1, i -> i >= 0, i -> i - 1)
            .parallel()
            .mapToObj(i -> (PipelineBehavior<Request<R>, R>) behaviorList.get(i))
            .reduce(
                terminal, (next, behavior) -> () -> behavior.handle(request, next), (a, b) -> a);

    return pipeline.get();
  }

  private void register(RequestHandler<?, ?> requestHandler) {
    var handlerType =
        Optional.ofNullable(requestHandler)
            .map(RequestHandler::getClass)
            .map(ResolvableType::forClass)
            .map(resolvableType -> resolvableType.as(RequestHandler.class))
            .filter(
                resolvableType ->
                    resolvableType != ResolvableType.NONE && resolvableType.hasGenerics())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Cannot infer request type for class "
                            + requestHandler.getClass().getName()));

    var requestType =
        Optional.ofNullable(handlerType.getGeneric(0).resolve())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Cannot resolve request type for class "
                            + requestHandler.getClass().getName()));

    requestHandlerMap.put(requestType, requestHandler);
  }
}
