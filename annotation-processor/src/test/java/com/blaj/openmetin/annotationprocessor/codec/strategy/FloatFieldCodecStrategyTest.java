package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FloatFieldCodecStrategyTest {

  private FloatFieldCodecStrategy floatFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    floatFieldCodecStrategy = new FloatFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"FLOAT"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = floatFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.FLOAT;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = floatFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenValid_whenGenerateDecodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.FLOAT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    floatFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.$L())"),
            eq("packet"),
            eq("setHeader"),
            eq(floatFieldCodecStrategy.getReadMethod()));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateDecodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.FLOAT,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    floatFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.$L())"),
            eq("element" + nestingDepth),
            eq("setHeader"),
            eq(floatFieldCodecStrategy.getReadMethod()));
  }

  @Test
  public void givenValid_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.FLOAT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    floatFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.$L($L.$L())"),
            eq(floatFieldCodecStrategy.getWriteMethod()),
            eq("packet"),
            eq("getHeader"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateEncodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.FLOAT,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    floatFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.$L($L.$L())"),
            eq(floatFieldCodecStrategy.getWriteMethod()),
            eq("element" + nestingDepth),
            eq("getHeader"));
  }

  @Test
  public void whenGetReadMethod_shouldReturnReadDouble() {
    // given

    // when
    var readMethod = floatFieldCodecStrategy.getReadMethod();

    // then
    assertThat(readMethod).isEqualTo("readFloat");
  }

  @Test
  public void whenGetWriteMethod_shouldReturnWriteDouble() {
    // given

    // when
    var writeMethod = floatFieldCodecStrategy.getWriteMethod();

    // then
    assertThat(writeMethod).isEqualTo("writeFloat");
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
