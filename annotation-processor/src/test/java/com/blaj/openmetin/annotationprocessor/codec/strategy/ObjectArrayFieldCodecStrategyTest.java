package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategyFactory;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
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
public class ObjectArrayFieldCodecStrategyTest {

  private ObjectArrayFieldCodecStrategy objectArrayFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private FieldCodecStrategyFactory fieldCodecStrategyFactory;
  @Mock private Elements elements;
  @Mock private TypeElement parentTypeElement;
  @Mock private TypeElement nestedTypeElement;
  @Mock private Name nestedTypeName;
  @Mock private FieldCodecStrategy mockPrimitiveStrategy;

  @BeforeEach
  public void beforeEach() {
    objectArrayFieldCodecStrategy =
        new ObjectArrayFieldCodecStrategy(processingEnvironment, fieldCodecStrategyFactory);
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"OBJECT_ARRAY"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = objectArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var fieldContext = createFieldContext(FieldType.OBJECT_ARRAY);

    // when
    var isSupported = objectArrayFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenZeroArrayLength_whenGenerateDecodingMethod_thenDoesNotGenerateAnyCode() {
    // given
    var fieldContext =
        new FieldContext(
            "characters",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    objectArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, never()).addStatement(anyString(), any());
    verify(methodSpecBuilder, never()).beginControlFlow(anyString(), any());
  }

  @Test
  public void givenNestedTypeNotFound_whenGenerateDecodingMethod_thenDoesNotGenerateAnyCode() {
    // given
    when(typeMirror.toString()).thenReturn("com.example.SimpleCharacterPacket[]");
    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(parentTypeElement);
    when(parentTypeElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    var fieldContext =
        new FieldContext(
            "characters",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            4,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    objectArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, never()).addStatement(anyString(), any());
    verify(methodSpecBuilder, never()).beginControlFlow(anyString(), any());
  }

  @Test
  public void givenValid_whenGenerateDecodingMethod_thenGeneratesCompleteDecodingFlow() {
    // given
    setupValidNestedType("SimpleCharacterPacket");
    when(typeMirror.toString()).thenReturn("com.example.SimpleCharacterPacket[]");

    var fieldContext =
        new FieldContext(
            "characters",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            4,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var expectedElementClass = parentClassName.nestedClass("SimpleCharacterPacket");

    // when
    objectArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then - verify complete flow
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T[] $L = new $T[$L]"),
            eq(expectedElementClass),
            eq("charactersArray"),
            eq(expectedElementClass),
            eq(4));

    verify(methodSpecBuilder).beginControlFlow(eq("for (int i = 0; i < $L; i++)"), eq(4));

    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = new $T()"),
            eq(expectedElementClass),
            eq("element1"),
            eq(expectedElementClass));

    verify(methodSpecBuilder).addStatement(eq("$L[i] = $L"), eq("charactersArray"), eq("element1"));
    verify(methodSpecBuilder).endControlFlow();

    verify(methodSpecBuilder)
        .addStatement(eq("$L.$L($L)"), eq("packet"), eq("setCharacters"), eq("charactersArray"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void
      givenValidWithNestingDepth_whenGenerateDecodingMethod_thenGeneratesCompleteDecodingFlow(
          int nestingDepth) {
    // given
    setupValidNestedType("Equipment");
    when(typeMirror.toString()).thenReturn("com.example.Equipment[]");

    var fieldContext =
        new FieldContext(
            "equipment",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            10,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    var expectedElementClass = parentClassName.nestedClass("Equipment");

    // when
    objectArrayFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = new $T()"),
            eq(expectedElementClass),
            eq("element" + (nestingDepth + 1)),
            eq(expectedElementClass));

    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($L)"),
            eq("element" + nestingDepth),
            eq("setEquipment"),
            eq("equipmentArray"));
  }

  @Test
  public void givenZeroArrayLength_whenGenerateEncodingMethod_thenDoesNotGenerateAnyCode() {
    // given
    var fieldContext =
        new FieldContext(
            "characters",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    objectArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, never()).addStatement(anyString(), any());
    verify(methodSpecBuilder, never()).beginControlFlow(anyString(), any());
  }

  @Test
  public void givenNestedTypeNotFound_whenGenerateEncodingMethod_thenDoesNotGenerateAnyCode() {
    // given
    when(typeMirror.toString()).thenReturn("com.example.SimpleCharacterPacket[]");
    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(parentTypeElement);
    when(parentTypeElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    var fieldContext =
        new FieldContext(
            "characters",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            4,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    objectArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder, never()).addStatement(anyString(), any());
    verify(methodSpecBuilder, never()).beginControlFlow(anyString(), any());
  }

  @Test
  public void givenValid_whenGenerateEncodingMethod_thenGeneratesCompleteEncodingFlow() {
    // given
    setupValidNestedType("SimpleCharacterPacket");
    when(typeMirror.toString()).thenReturn("com.example.SimpleCharacterPacket[]");

    var fieldContext =
        new FieldContext(
            "characters",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            4,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var expectedElementClass = parentClassName.nestedClass("SimpleCharacterPacket");

    // when
    objectArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then - verify complete flow
    verify(methodSpecBuilder).beginControlFlow(eq("for (int i = 0; i < $L; i++)"), eq(4));

    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = $L.$L()[i]"),
            eq(expectedElementClass),
            eq("element1"),
            eq("packet"),
            eq("getCharacters"));

    verify(methodSpecBuilder).endControlFlow();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void
      givenValidWithNestingDepth_whenGenerateEncodingMethod_thenGeneratesCompleteEncodingFlow(
          int nestingDepth) {
    // given
    setupValidNestedType("Equipment");
    when(typeMirror.toString()).thenReturn("com.example.Equipment[]");

    var fieldContext =
        new FieldContext(
            "equipment",
            FieldType.OBJECT_ARRAY,
            typeMirror,
            0,
            0,
            10,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    var expectedElementClass = parentClassName.nestedClass("Equipment");

    // when
    objectArrayFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = $L.$L()[i]"),
            eq(expectedElementClass),
            eq("element" + (nestingDepth + 1)),
            eq("element" + nestingDepth),
            eq("getEquipment"));
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

  private void setupValidNestedType(String typeName) {
    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(parentTypeElement);

    when(nestedTypeElement.getKind()).thenReturn(ElementKind.CLASS);
    when(nestedTypeElement.getSimpleName()).thenReturn(nestedTypeName);
    when(nestedTypeName.toString()).thenReturn(typeName);

    doReturn(List.of(nestedTypeElement)).when(parentTypeElement).getEnclosedElements();
  }
}
