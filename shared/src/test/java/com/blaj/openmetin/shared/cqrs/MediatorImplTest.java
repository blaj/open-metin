package com.blaj.openmetin.shared.cqrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MediatorImplTest {

  @Nested
  public class ConstructorTests {

    @Test
    public void givenInvalidHandler_whenConstructor_thenThrowException() {
      // given
      @SuppressWarnings("rawtypes")
      class RawBaseHandler implements RequestHandler {
        @Override
        public Object handle(Request request) {
          return null;
        }
      }

      class InvalidHandler extends RawBaseHandler {}

      RequestHandler<?, ?> invalidHandler = new InvalidHandler();

      // when
      var thrownException =
          assertThrows(
              IllegalArgumentException.class,
              () -> new MediatorImpl(List.of(invalidHandler), Collections.emptyList()));

      // then
      assertThat(thrownException)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Cannot infer request type for")
          .hasMessageContaining(String.valueOf(invalidHandler.getClass()));
    }

    @Test
    public void givenMultipleHandlers_whenConstructor_thenAllHandlersAreRegistered() {
      // given
      var handler1 = new FirstTestRequestHandler();
      var handler2 = new SecondTestRequestHandler();

      List<RequestHandler<?, ?>> handlers = List.of(handler1, handler2);
      List<PipelineBehavior<?, ?>> behaviors = Collections.emptyList();

      // when
      var mediator = new MediatorImpl(handlers, behaviors);

      var result1 = mediator.send(new FirstTestRequest("test"));
      var result2 = mediator.send(new SecondTestRequest(5));

      // then
      assertThat(result1).isEqualTo("handled: test");
      assertThat(result2).isEqualTo(10);
    }
  }

  @Nested
  public class SendTests {

    @Mock FirstTestRequestHandler mockFirstTestRequestHandler;
    @Mock FirstTestRequestPipelineBehavior mockFirstTestRequestPipelineBehavior;

    @Test
    public void givenNoHandlerForRequest_whenSend_thenThrowsIllegalStateException() {
      // given
      var mediator = new MediatorImpl(Collections.emptyList(), Collections.emptyList());
      var request = new FirstTestRequest("test");

      // when
      var thrownException = assertThrows(IllegalStateException.class, () -> mediator.send(request));

      // then
      assertThat(thrownException)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No handler for")
          .hasMessageContaining(FirstTestRequest.class.getName());
    }

    @Test
    public void givenHandlerReturningNull_whenSend_thenReturnsNull() {
      // given
      var request = new FirstTestRequest("test");
      var mediator =
          new MediatorImpl(List.of(mockFirstTestRequestHandler), Collections.emptyList());

      given(mockFirstTestRequestHandler.handle(request)).willReturn(null);

      // when
      var result = mediator.send(request);

      // then
      assertThat(result).isNull();
    }

    @Test
    public void givenHandlerThrowingException_whenSend_thenExceptionIsPropagated() {
      // given
      var request = new FirstTestRequest("test");
      var mediator =
          new MediatorImpl(
              List.of(new FirstTestRequestHandler()),
              List.of(mockFirstTestRequestPipelineBehavior));

      given(mockFirstTestRequestPipelineBehavior.handle(eq(request), any()))
          .willThrow(new RuntimeException("behavior error"));

      // when
      var thrownException = assertThrows(RuntimeException.class, () -> mediator.send(request));

      // then
      assertThat(thrownException).isInstanceOf(RuntimeException.class).hasMessage("behavior error");
    }

    @Test
    public void givenBehaviorThrowingException_whenSend_thenExceptionIsPropagated() {}

    @Test
    public void givenRegisteredHandler_whenSend_thenHandlerExecutes() {
      // given
      var handler = new FirstTestRequestHandler();
      var mediator = new MediatorImpl(List.of(handler), Collections.emptyList());
      var request = new FirstTestRequest("test data");

      // when
      var result = mediator.send(request);

      // then
      assertThat(result).isEqualTo("handled: test data");
    }

    @Test
    public void givenHandlerAndBehavior_whenSend_thenBehaviorWrapsHandler() {
      // given
      var handler = new FirstTestRequestHandler();
      var pipelineBehavior = new FirstTestRequestPipelineBehavior();
      var mediator = new MediatorImpl(List.of(handler), List.of(pipelineBehavior));
      var request = new FirstTestRequest("test data");

      // when
      var result = mediator.send(request);

      // then
      assertThat(result).isEqualTo("before-handled: test data-after");
    }
  }

  private record FirstTestRequest(String data) implements Request<String> {}

  private record SecondTestRequest(Integer data) implements Request<Integer> {}

  private static class FirstTestRequestHandler implements RequestHandler<FirstTestRequest, String> {

    @Override
    public String handle(FirstTestRequest request) {
      return "handled: " + request.data();
    }
  }

  private static class SecondTestRequestHandler
      implements RequestHandler<SecondTestRequest, Integer> {

    @Override
    public Integer handle(SecondTestRequest request) {
      return request.data() * 2;
    }
  }

  private static class FirstTestRequestPipelineBehavior
      implements PipelineBehavior<FirstTestRequest, String> {

    @Override
    public String handle(FirstTestRequest request, Supplier<String> next) {
      return "before-" + next.get() + "-after";
    }
  }
}
