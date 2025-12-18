package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
public class ByteArrayFieldCodecStrategyTest {

  private ByteArrayFieldCodecStrategy byteArrayFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    byteArrayFieldCodecStrategy = new ByteArrayFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"BYTE_ARRAY"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType, 4);

    // when
    var isSupported = byteArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.BYTE_ARRAY;
    var fieldContext = createFieldContext(supportedFieldType, 4);

    // when
    var isSupported = byteArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -10})
  public void givenInvalidArrayLength_whenGenerateDecodingMethod_thenDoNothing(int arrayLength) {
    // given
    var fieldContext = createFieldContext(FieldType.BYTE_ARRAY, arrayLength);

    // when
    byteArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verifyNoInteractions(methodSpecBuilder);
  }

  @Test
  public void givenValidArrayLength_whenGenerateDecodingMethod_thenAddCorrectStatements() {
    // given
    var fieldContext =
        new FieldContext(
            "dummy",
            FieldType.BYTE_ARRAY,
            typeMirror,
            0,
            0,
            4,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    byteArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder).addStatement(eq("byte[] $L = new byte[$L]"), eq("dummyBytes"), eq(4));
    verify(methodSpecBuilder).addStatement(eq("in.readBytes($L)"), eq("dummyBytes"));
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($L)"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()),
            eq("dummyBytes"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNestingDepth_whenGenerateDecodingMethod_thenAddCorrectStatements(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "data",
            FieldType.BYTE_ARRAY,
            typeMirror,
            0,
            0,
            8,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    byteArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder).addStatement(eq("byte[] $L = new byte[$L]"), eq("dataBytes"), eq(8));
    verify(methodSpecBuilder).addStatement(eq("in.readBytes($L)"), eq("dataBytes"));
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($L)"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()),
            eq("dataBytes"));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -10})
  public void givenInvalidArrayLength_whenGenerateEncodingMethod_thenDoNothing(int arrayLength) {
    // given
    var fieldContext = createFieldContext(FieldType.BYTE_ARRAY, arrayLength);

    // when
    byteArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verifyNoInteractions(methodSpecBuilder);
  }

  @Test
  public void givenValidArrayLength_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "dummy",
            FieldType.BYTE_ARRAY,
            typeMirror,
            0,
            0,
            4,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    byteArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeBytes($L.$L())"),
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
            "data",
            FieldType.BYTE_ARRAY,
            typeMirror,
            0,
            0,
            8,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    byteArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeBytes($L.$L())"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getGetterName()));
  }

  private FieldContext createFieldContext(FieldType fieldType, int arrayLength) {
    return new FieldContext(
        "testField",
        fieldType,
        typeMirror,
        0,
        0,
        arrayLength,
        false,
        0,
        parentClassName,
        processingEnvironment);
  }
}
