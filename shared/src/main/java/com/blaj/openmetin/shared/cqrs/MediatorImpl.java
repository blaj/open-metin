package com.blaj.openmetin.shared.cqrs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

@Service
public class MediatorImpl implements Mediator {
  private final Map<Class<?>, RequestHandler<?, ?>> requestHandlerMap = new ConcurrentHashMap<>();
  private final List<PipelineBehavior<?, ?>> behaviorList;

  public MediatorImpl(
      List<RequestHandler<?, ?>> requestHandlers, List<PipelineBehavior<?, ?>> behaviorList) {
    requestHandlers.forEach(this::register);

    this.behaviorList = behaviorList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R send(Request<R> request) {
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
    var resolvableType = ResolvableType.forClass(requestHandler.getClass());

    Arrays.stream(resolvableType.getInterfaces())
        .filter(
            requestHandlerInterface ->
                requestHandlerInterface.toClass().equals(RequestHandler.class))
        .findFirst()
        .map(requestHandlerResolvableType -> requestHandlerResolvableType.getGeneric(0))
        .map(ResolvableType::toClass)
        .ifPresentOrElse(
            clazz -> requestHandlerMap.put(clazz, requestHandler),
            () -> {
              throw new IllegalArgumentException(
                  "Cannot infer request type for " + requestHandler.getClass());
            });
  }
}
