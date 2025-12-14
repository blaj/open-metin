package com.blaj.openmetin.annotationprocessor.codec.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
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
public class EnumFieldCodecStrategyTest {

  private EnumFieldCodecStrategy enumFieldCodecStrategy;
  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");
  private ClassName byteEnumUtilsClassName =
      ClassName.get("com.blaj.openmetin.shared.common.utils", "ByteEnumUtils");

  @Mock private MethodSpec.Builder methodSpecBuilder;
  @Mock private TypeMirror typeMirror;
  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private Types types;
  @Mock private Elements elements;
  @Mock private TypeElement typeElement;
  @Mock private Name typeName;
  @Mock private PackageElement packageElement;
  @Mock private Name packageName;

  @BeforeEach
  public void beforeEach() {
    enumFieldCodecStrategy = new EnumFieldCodecStrategy(processingEnvironment);
  }

  @ParameterizedTest
  @EnumSource(
      value = FieldType.class,
      names = {"ENUM"},
      mode = Mode.EXCLUDE)
  public void givenNotSupportedFieldType_whenIsSupported_thenReturnFalse(FieldType fieldType) {
    // given
    var fieldContext = createFieldContext(fieldType);

    // when
    var isSupported = enumFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isFalse();
  }

  @Test
  public void givenSupportedFieldType_whenIsSupported_thenReturnTrue() {
    // given
    var supportedFieldType = FieldType.ENUM;
    var fieldContext = createFieldContext(supportedFieldType);

    // when
    var isSupported = enumFieldCodecStrategy.isSupported(fieldContext);

    // then
    assertThat(isSupported).isTrue();
  }

  @Test
  public void givenTopLevelEnum_whenGenerateDecodingMethod_thenAddsCorrectStatement() {
    // given
    setupTopLevelEnum("com.example.enums", "GameState");

    var fieldContext =
        new FieldContext(
            "gameState",
            FieldType.ENUM,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var expectedEnumClass = ClassName.get("com.example.enums", "GameState");

    // when
    enumFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($T.fromValue($T.class, in.readByte()))"),
            eq("packet"),
            eq("setGameState"),
            eq(byteEnumUtilsClassName),
            eq(expectedEnumClass));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void givenTopLevelEnumWithNestingDepth_whenGenerateDecodingMethod_thenAddsCorrectStatement(
      int nestingDepth) {
    // given
    setupTopLevelEnum("com.example.enums", "GameState");

    var fieldContext =
        new FieldContext(
            "gameState",
            FieldType.ENUM,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    var expectedEnumClass = ClassName.get("com.example.enums", "GameState");

    // when
    enumFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($T.fromValue($T.class, in.readByte()))"),
            eq("element" + nestingDepth),
            eq("setGameState"),
            eq(byteEnumUtilsClassName),
            eq(expectedEnumClass));
  }

  @Test
  public void givenNestedEnum_whenGenerateDecodingMethod_thenAddsCorrectStatementWithNestedClass() {
    // given
    setupNestedEnum("com.example", "Character", "Race");

    var fieldContext =
        new FieldContext(
            "race",
            FieldType.ENUM,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    var expectedEnumClass = ClassName.get("com.example", "Character").nestedClass("Race");

    // when
    enumFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($T.fromValue($T.class, in.readByte()))"),
            eq("packet"),
            eq("setRace"),
            eq(byteEnumUtilsClassName),
            eq(expectedEnumClass));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void
      givenNestedEnumWithNestingDepth_whenGenerateDecodingMethod_thenAddsCorrectStatementWithNestedClass(
          int nestingDepth) {
    // given
    setupNestedEnum("com.example", "Character", "Race");

    var fieldContext =
        new FieldContext(
            "race",
            FieldType.ENUM,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    var expectedEnumClass = ClassName.get("com.example", "Character").nestedClass("Race");

    // when
    enumFieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("$L.$L($T.fromValue($T.class, in.readByte()))"),
            eq("element" + nestingDepth),
            eq("setRace"),
            eq(byteEnumUtilsClassName),
            eq(expectedEnumClass));
  }

  @Test
  public void givenEnumField_whenGenerateEncodingMethod_thenAddsCorrectNullSafeStatement() {
    // given
    var fieldContext =
        new FieldContext(
            "gameState",
            FieldType.ENUM,
            typeMirror,
            0,
            0,
            0,
            false,
            0,
            parentClassName,
            processingEnvironment);

    // when
    enumFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeByte($L.$L() != null ? $L.$L().getValue() : (byte) 0)"),
            eq("packet"),
            eq("getGameState"),
            eq("packet"),
            eq("getGameState"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void
      givenEnumFieldWithNestingDepth_whenGenerateEncodingMethod_thenAddsCorrectNullSafeStatement(
          int nestingDepth) {
    // given
    var fieldContext =
        new FieldContext(
            "gameState",
            FieldType.ENUM,
            typeMirror,
            0,
            0,
            0,
            false,
            nestingDepth,
            parentClassName,
            processingEnvironment);

    // when
    enumFieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, fieldContext);

    // then
    verify(methodSpecBuilder)
        .addStatement(
            eq("out.writeByte($L.$L() != null ? $L.$L().getValue() : (byte) 0)"),
            eq("element" + nestingDepth),
            eq("getGameState"),
            eq("element" + nestingDepth),
            eq("getGameState"));
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

  private void setupTopLevelEnum(String packageNameStr, String enumName) {
    when(processingEnvironment.getTypeUtils()).thenReturn(types);
    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(types.asElement(typeMirror)).thenReturn(typeElement);

    when(typeElement.getEnclosingElement()).thenReturn(mock(Element.class));

    when(elements.getPackageOf(typeElement)).thenReturn(packageElement);
    when(packageElement.getQualifiedName()).thenReturn(packageName);
    when(packageName.toString()).thenReturn(packageNameStr);

    when(typeElement.getSimpleName()).thenReturn(typeName);
    when(typeName.toString()).thenReturn(enumName);
  }

  private void setupNestedEnum(String packageNameStr, String outerClassName, String innerEnumName) {
    var enclosingTypeElement = mock(TypeElement.class);
    var outerName = mock(Name.class);

    when(processingEnvironment.getTypeUtils()).thenReturn(types);
    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(types.asElement(typeMirror)).thenReturn(typeElement);

    when(typeElement.getEnclosingElement()).thenReturn(enclosingTypeElement);

    when(elements.getPackageOf(enclosingTypeElement)).thenReturn(packageElement);
    when(packageElement.getQualifiedName()).thenReturn(packageName);
    when(packageName.toString()).thenReturn(packageNameStr);

    when(enclosingTypeElement.getSimpleName()).thenReturn(outerName);
    when(outerName.toString()).thenReturn(outerClassName);

    when(typeElement.getSimpleName()).thenReturn(typeName);
    when(typeName.toString()).thenReturn(innerEnumName);
  }
}
