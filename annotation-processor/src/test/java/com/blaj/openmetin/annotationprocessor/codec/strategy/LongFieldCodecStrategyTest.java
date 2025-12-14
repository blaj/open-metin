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
public class LongFieldCodecStrategyTest {

  private LongFieldCodecStrategy longFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    longFieldCodecStrategy = new LongFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"LONG"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = longFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.LONG;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = longFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenNotUnsigned_whenGenerateDecodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(eq("$L.$L(in.readLong())"), eq("packet"), eq("setHeader"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNotUnsignedNestingDepth_whenGenerateDecodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(eq("$L.$L(in.readLong())"), eq("element" + nestingDepth), eq("setHeader"));
  }

  @Test
  public void givenUnsigned_whenGenerateDecodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            true,
            0,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.readInt() & 0xFFFFFFFFL)"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenUnsignedNestingDepth_whenGenerateDecodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            true,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L(in.readInt() & 0xFFFFFFFFL)"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()));
  }

  @Test
  public void givenNotUnsigned_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(eq("out.writeLong($L.$L())"), eq("packet"), eq("getHeader"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenNotUnsignedNestingDepth_whenGenerateEncodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(eq("out.writeLong($L.$L())"), eq("element" + nestingDepth), eq("getHeader"));
  }

  @Test
  public void givenUnsigned_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            true,
            0,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeInt((int) ($L.$L() & 0xFFFFFFFFL))"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getGetterName()));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenUnsignedNestingDepth_whenGenerateEncodingMethod_thenAddCorrectStatement(
      int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.LONG,
            typeMirror,
            0,
            0,
            0,
            true,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    longFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeInt((int) ($L.$L() & 0xFFFFFFFFL))"),
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
