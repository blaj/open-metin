package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IntArrayFieldCodecStrategyTest {

  private IntArrayFieldCodecStrategy intArrayFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;

  @BeforeEach
  public void beforeEach() {
    intArrayFieldCodecStrategy = new IntArrayFieldCodecStrategy();
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"INT_ARRAY"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType, 4, false);

    // when
    var isSupported = intArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.INT_ARRAY;
    var fieldContext = createFieldContext(supportedFieldType, 4, false);

    // when
    var isSupported = intArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenArrayLengthZero_whenGenerateDecodingMethod_thenDoesNotGenerateCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, 0, false);

    // when
    intArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder)
        .should(never())
        .addStatement(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void givenArrayLengthNegative_whenGenerateDecodingMethod_thenDoesNotGenerateCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, -1, false);

    // when
    intArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder)
        .should(never())
        .addStatement(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void givenValidContextWithSignedInt_whenGenerateDecodingMethod_thenGeneratesCorrectCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, 5, false);

    // when
    intArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder).should().addStatement("int[] $L = new int[$L]", "testFieldArray", 5);
    then(methodSpecBuilder).should().beginControlFlow("for (int i = 0; i < $L; i++)", 5);
    then(methodSpecBuilder).should().addStatement("$L[i] = in.readIntLE()", "testFieldArray");
    then(methodSpecBuilder).should().endControlFlow();
    then(methodSpecBuilder)
        .should()
        .addStatement("$L.$L($L)", "packet", "setTestField", "testFieldArray");
  }

  @Test
  public void
      givenValidContextWithUnsignedInt_whenGenerateDecodingMethod_thenGeneratesCorrectCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, 3, true);

    // when
    intArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder).should().addStatement("int[] $L = new int[$L]", "testFieldArray", 3);
    then(methodSpecBuilder).should().beginControlFlow("for (int i = 0; i < $L; i++)", 3);
    then(methodSpecBuilder)
        .should()
        .addStatement("$L[i] = in.readShortLE() & 0xFFFF", "testFieldArray");
    then(methodSpecBuilder).should().endControlFlow();
    then(methodSpecBuilder)
        .should()
        .addStatement("$L.$L($L)", "packet", "setTestField", "testFieldArray");
  }

  @Test
  public void givenArrayLengthZero_whenGenerateEncodingMethod_thenDoesNotGenerateCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, 0, false);

    // when
    intArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder)
        .should(never())
        .beginControlFlow(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void givenArrayLengthNegative_whenGenerateEncodingMethod_thenDoesNotGenerateCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, -1, false);

    // when
    intArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder)
        .should(never())
        .beginControlFlow(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void givenValidContextWithSignedInt_whenGenerateEncodingMethod_thenGeneratesCorrectCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, 5, false);

    // when
    intArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder).should().beginControlFlow("for (int i = 0; i < $L; i++)", 5);
    then(methodSpecBuilder)
        .should()
        .addStatement("out.writeIntLE($L.$L()[i])", "packet", "getTestField");
    then(methodSpecBuilder).should().endControlFlow();
  }

  @Test
  public void
      givenValidContextWithUnsignedInt_whenGenerateEncodingMethod_thenGeneratesCorrectCode() {
    // given
    var fieldContext = createFieldContext(FieldType.INT_ARRAY, 3, true);

    // when
    intArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    then(methodSpecBuilder).should().beginControlFlow("for (int i = 0; i < $L; i++)", 3);
    then(methodSpecBuilder)
        .should()
        .addStatement("out.writeShortLE((short) ($L.$L()[i] & 0xFFFF))", "packet", "getTestField");
    then(methodSpecBuilder).should().endControlFlow();
  }

  private FieldContext createFieldContext(
      FieldType fieldType, int arrayLength, boolean isUnsigned) {
    return new FieldContext(
        "testField",
        fieldType,
        typeMirror,
        0,
        0,
        arrayLength,
        isUnsigned,
        0,
        parentClassName,
        processingEnvironment);
  }
}
