package com.blaj.openmetin.annotationprocessor.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.blaj.openmetin.annotationprocessor.codec.strategy.BooleanFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ByteArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ByteFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.DoubleFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.EnumFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.FloatFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.IntFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.LongArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.LongFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ObjectArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ObjectFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ShortFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.StringArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.utils.StringFieldCodecStrategy;
import com.palantir.javapoet.ClassName;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FieldCodecStrategyFactoryTest {

  private FieldCodecStrategyFactory fieldCodecStrategyFactory;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private TypeMirror typeMirror;

  private static Stream<Arguments> provideFieldTypeAndExpectedStrategy() {
    return Stream.of(
        Arguments.of(FieldType.BOOLEAN, BooleanFieldCodecStrategy.class),
        Arguments.of(FieldType.BYTE_ARRAY, ByteArrayFieldCodecStrategy.class),
        Arguments.of(FieldType.BYTE, ByteFieldCodecStrategy.class),
        Arguments.of(FieldType.SHORT, ShortFieldCodecStrategy.class),
        Arguments.of(FieldType.INT, IntFieldCodecStrategy.class),
        Arguments.of(FieldType.LONG, LongFieldCodecStrategy.class),
        Arguments.of(FieldType.FLOAT, FloatFieldCodecStrategy.class),
        Arguments.of(FieldType.DOUBLE, DoubleFieldCodecStrategy.class),
        Arguments.of(FieldType.STRING, StringFieldCodecStrategy.class),
        Arguments.of(FieldType.LONG_ARRAY, LongArrayFieldCodecStrategy.class),
        Arguments.of(FieldType.STRING_ARRAY, StringArrayFieldCodecStrategy.class),
        Arguments.of(FieldType.ENUM, EnumFieldCodecStrategy.class),
        Arguments.of(FieldType.OBJECT_ARRAY, ObjectArrayFieldCodecStrategy.class),
        Arguments.of(FieldType.OBJECT, ObjectFieldCodecStrategy.class));
  }

  @BeforeEach
  public void beforeEach() {
    fieldCodecStrategyFactory = new FieldCodecStrategyFactory(processingEnvironment);
  }

  @Test
  public void givenUnknownFieldType_whenGet_thenThrowsIllegalArgumentException() {
    // given
    var fieldContext = createFieldContext(FieldType.UNKNOWN);

    // when
    var thrownException =
        assertThrows(
            IllegalArgumentException.class, () -> fieldCodecStrategyFactory.get(fieldContext));

    // then
    assertThat(thrownException.getMessage()).isEqualTo("No strategy found for type: UNKNOWN");
  }

  @ParameterizedTest
  @MethodSource("provideFieldTypeAndExpectedStrategy")
  public void givenValid_whenGet_thenReturnsCorrectStrategy(
      FieldType fieldType, Class<? extends FieldCodecStrategy> expectedStrategyClass) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var fieldCodecStrategy = fieldCodecStrategyFactory.get(fieldContext);

    // then
    assertThat(fieldCodecStrategy).isInstanceOf(expectedStrategyClass);
  }

  private FieldContext createFieldContext(FieldType fieldType) {
    return new FieldContext(
        "testField",
        fieldType,
        typeMirror,
        0,
        0,
        0,
        false,
        0,
        parentClassName,
        processingEnvironment);
  }
}
