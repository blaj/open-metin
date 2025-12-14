package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
public class StringArrayFieldCodecStrategyTest {

  private StringArrayFieldCodecStrategy stringArrayFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");
  private ClassName packetCodecUtilsClassName =
      ClassName.get("com.blaj.openmetin.shared.infrastructure.network.codec", "PacketCodecUtils");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    stringArrayFieldCodecStrategy = new StringArrayFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"STRING_ARRAY"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = stringArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.STRING_ARRAY;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = stringArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void given0Length_whenGenerateDecodingMethod_thenDoNothing() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            0,
            1,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, times(0)).addStatement(any(), any(), any(), any(), any(), any());
  }

  @Test
  public void given0ArrayLength_whenGenerateDecodingMethod_thenDoNothing() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            1,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, times(0)).addStatement(any(), any(), any(), any(), any(), any());
  }

  @Test
  public void givenValid_whenGenerateDecodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            2,
            1,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($T.readFixedStringArray(in, $L, $L))"),
            eq("packet"),
            eq("setHeader"),
            eq(packetCodecUtilsClassName),
            eq(1),
            eq(2));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenValid_whenGenerateDecodingMethod_thenAddCorrectStatement(int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            2,
            1,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($T.readFixedStringArray(in, $L, $L))"),
            eq("element" + nestingDepth),
            eq("setHeader"),
            eq(packetCodecUtilsClassName),
            eq(1),
            eq(2));
  }

  @Test
  public void given0Length_whenGenerateEncodingMethod_thenDoNothing() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            0,
            1,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, times(0)).addStatement(any(), any(), any(), any(), any(), any());
  }

  @Test
  public void given0ArrayLength_whenGenerateEncodingMethod_thenDoNothing() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            1,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, times(0)).addStatement(any(), any(), any(), any(), any(), any());
  }

  @Test
  public void givenValid_whenGenerateEncodingMethod_thenAddCorrectStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            2,
            1,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T.writeFixedStringArray(out, $L.$L(), $L, $L)"),
            eq(packetCodecUtilsClassName),
            eq("packet"),
            eq("getHeader"),
            eq(1),
            eq(2));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenValid_whenGenerateEncodingMethod_thenAddCorrectStatement(int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "header",
            FieldType.STRING_ARRAY,
            typeMirror,
            0,
            2,
            1,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    stringArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T.writeFixedStringArray(out, $L.$L(), $L, $L)"),
            eq(packetCodecUtilsClassName),
            eq("element" + nestingDepth),
            eq("getHeader"),
            eq(1),
            eq(2));
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
