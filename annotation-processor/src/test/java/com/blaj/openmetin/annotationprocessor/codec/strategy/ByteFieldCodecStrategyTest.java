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
public class ByteFieldCodecStrategyTest {

  private ByteFieldCodecStrategy byteFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    byteFieldCodecStrategy = new ByteFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"BYTE"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = byteFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldTypeWithUnsignedFieldContext_whenIsSupported_thenReturnFalse() {
    // given
    var supportedFieldType = FieldType.BYTE;
    var fieldContext =
        new FieldContext(
            "testField",
            supportedFieldType,
            typeMirror,
            0,
            0,
            0,
            true,
            0,
            parentClassName,
            processingEnvironment);

    // when
    var isSupported = byteFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.BYTE;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = byteFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenValid_whenGenerateDecodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.BYTE,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    byteFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.readByte())"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateDecodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.BYTE,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    byteFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.readByte())"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()));
  }

  @Test
  public void givenValid_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.BYTE,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    byteFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeByte($L.$L())"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getGetterName()));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateEncodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.BYTE,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    byteFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeByte($L.$L())"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getGetterName()));
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
