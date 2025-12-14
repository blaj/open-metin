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
public class BooleanFieldCodecStrategyTest {

  private BooleanFieldCodecStrategy booleanFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    booleanFieldCodecStrategy = new BooleanFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"BOOLEAN"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = booleanFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.BOOLEAN;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = booleanFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenValid_whenGenerateDecodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "isActive",
            FieldType.BOOLEAN,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    booleanFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.$L())"),
            eq("packet"),
            eq("setIsActive"),
            eq(booleanFieldCodecStrategy.getReadMethod()));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateDecodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "isActive",
            FieldType.BOOLEAN,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    booleanFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.$L())"),
            eq("element" + nestingDepth),
            eq("setIsActive"),
            eq(booleanFieldCodecStrategy.getReadMethod()));
  }

  @Test
  public void givenValid_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "isActive",
            FieldType.BOOLEAN,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    booleanFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.$L($L.$L())"),
            eq(booleanFieldCodecStrategy.getWriteMethod()),
            eq("packet"),
            eq("getIsActive"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateEncodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "isActive",
            FieldType.BOOLEAN,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    booleanFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.$L($L.$L())"),
            eq(booleanFieldCodecStrategy.getWriteMethod()),
            eq("element" + nestingDepth),
            eq("getIsActive"));
  }

  @Test
  public void whenGetReadMethod_shouldReturnReadBoolean() {
    // given

    // when
    var readMethod = booleanFieldCodecStrategy.getReadMethod();

    // then
    assertThat(readMethod).isEqualTo("readBoolean");
  }

  @Test
  public void whenGetWriteMethod_shouldReturnWriteBoolean() {
    // given

    // when
    var writeMethod = booleanFieldCodecStrategy.getWriteMethod();

    // then
    assertThat(writeMethod).isEqualTo("writeBoolean");
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
