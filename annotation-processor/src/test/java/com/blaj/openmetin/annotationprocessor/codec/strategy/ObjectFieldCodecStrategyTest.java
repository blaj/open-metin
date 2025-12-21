package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategyFactory;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import java.util.Collections;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ObjectFieldCodecStrategyTest {

  private ObjectFieldCodecStrategy objectFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");

  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private FieldCodecStrategyFactory fieldCodecStrategyFactory;
  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private Elements elements;
  @Mock private Messager messager;

  @BeforeEach
  public void beforeEach() {
    objectFieldCodecStrategy =
        new ObjectFieldCodecStrategy(processingEnvironment, fieldCodecStrategyFactory);
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"OBJECT"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = objectFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.OBJECT;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = objectFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenTypeNotFound_whenGenerateDecodingMethod_thenPrintErrorAndReturn() {
    // given
    var fieldContext =
        new FieldContext(
            "nestedObject",
            FieldType.OBJECT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(processingEnvironment.getMessager()).thenReturn(messager);
    when(typeMirror.toString()).thenReturn("com.example.NonExistentType");
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(mock(TypeElement.class));
    when(elements.getTypeElement("com.example.NonExistentType")).thenReturn(null);

    // when
    objectFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(messager)
        .printMessage(
            eq(Kind.ERROR),
            eq("Cannot find type: com.example.NonExistentType (searched as nested and top-level)"));
  }

  @Test
  public void givenValidNestedType_whenGenerateDecodingMethod_thenGenerateCorrectStatements() {
    // given
    var fieldContext =
        new FieldContext(
            "nestedObject",
            FieldType.OBJECT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var parentElement = mock(TypeElement.class);
    var nestedElement = mock(TypeElement.class);

    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(typeMirror.toString()).thenReturn("com.example.NestedType");
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(parentElement);
    when(parentElement.getEnclosedElements()).thenReturn(Collections.emptyList());
    when(elements.getTypeElement("com.example.NestedType")).thenReturn(nestedElement);
    when(nestedElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    // when
    objectFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = new $T()"), any(ClassName.class), eq("element1"), any(ClassName.class));
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($L)"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()),
            eq("element1"));
  }

  @Test
  public void givenTypeNotFound_whenGenerateEncodingMethod_thenPrintErrorAndReturn() {
    // given
    var fieldContext =
        new FieldContext(
            "nestedObject",
            FieldType.OBJECT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(processingEnvironment.getMessager()).thenReturn(messager);
    when(typeMirror.toString()).thenReturn("com.example.NonExistentType");
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(mock(TypeElement.class));
    when(elements.getTypeElement("com.example.NonExistentType")).thenReturn(null);

    // when
    objectFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(messager)
        .printMessage(
            eq(Kind.ERROR),
            eq("Cannot find type: com.example.NonExistentType (searched as nested and top-level)"));
  }

  @Test
  public void givenValidNestedType_whenGenerateEncodingMethod_thenGenerateCorrectStatements() {
    // given
    var fieldContext =
        new FieldContext(
            "nestedObject",
            FieldType.OBJECT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var parentElement = mock(TypeElement.class);
    var nestedElement = mock(TypeElement.class);

    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(typeMirror.toString()).thenReturn("com.example.NestedType");
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(parentElement);
    when(parentElement.getEnclosedElements()).thenReturn(Collections.emptyList());
    when(elements.getTypeElement("com.example.NestedType")).thenReturn(nestedElement);
    when(nestedElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    // when
    objectFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = $L.$L()"),
            any(ClassName.class),
            eq("element1"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getGetterName()));
  }

  @Test
  public void givenTopLevelType_whenGenerateDecodingMethod_thenFindTypeAndGenerateStatements() {
    // given
    var fieldContext =
        new FieldContext(
            "externalObject",
            FieldType.OBJECT,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var parentElement = mock(TypeElement.class);
    var topLevelElement = mock(TypeElement.class);
    var nestedFieldCodecStrategy = mock(FieldCodecStrategy.class);

    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(typeMirror.toString()).thenReturn("com.example.ExternalType");
    when(elements.getTypeElement("com.example.TestPacket")).thenReturn(parentElement);
    when(parentElement.getEnclosedElements()).thenReturn(Collections.emptyList());
    when(elements.getTypeElement("com.example.ExternalType")).thenReturn(topLevelElement);
    when(topLevelElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    // when
    objectFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$T $L = new $T()"), any(ClassName.class), eq("element1"), any(ClassName.class));
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($L)"),
            eq(fieldContext.getElementVariableName()),
            eq(fieldContext.getSetterName()),
            eq("element1"));
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
