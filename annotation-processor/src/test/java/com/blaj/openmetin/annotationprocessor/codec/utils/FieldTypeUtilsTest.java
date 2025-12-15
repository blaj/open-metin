package com.blaj.openmetin.annotationprocessor.codec.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.blaj.openmetin.annotationprocessor.codec.TypeChecker;
import java.util.stream.Stream;
import javax.lang.model.type.TypeMirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FieldTypeUtilsTest {

  @Mock private TypeMirror typeMirror;
  @Mock private TypeChecker typeChecker;

  private static Stream<Arguments> provideTypeNamesForFromString() {
    return Stream.of(
        Arguments.of("byte", FieldType.BYTE),
        Arguments.of("short", FieldType.SHORT),
        Arguments.of("int", FieldType.INT),
        Arguments.of("long", FieldType.LONG),
        Arguments.of("boolean", FieldType.BOOLEAN),
        Arguments.of("float", FieldType.FLOAT),
        Arguments.of("double", FieldType.DOUBLE),
        Arguments.of("String", FieldType.STRING),
        Arguments.of("java.lang.String", FieldType.STRING),
        Arguments.of("byte[]", FieldType.BYTE_ARRAY),
        Arguments.of("short[]", FieldType.SHORT_ARRAY),
        Arguments.of("int[]", FieldType.INT_ARRAY),
        Arguments.of("long[]", FieldType.LONG_ARRAY),
        Arguments.of("String[]", FieldType.STRING_ARRAY));
  }

  private static Stream<Arguments> providePrimitiveTypes() {
    return Stream.of(
        Arguments.of("byte", FieldType.BYTE),
        Arguments.of("short", FieldType.SHORT),
        Arguments.of("int", FieldType.INT),
        Arguments.of("long", FieldType.LONG),
        Arguments.of("float", FieldType.FLOAT),
        Arguments.of("double", FieldType.DOUBLE),
        Arguments.of("boolean", FieldType.BOOLEAN));
  }

  private static Stream<Arguments> providePrimitiveArrayTypes() {
    return Stream.of(
        Arguments.of("byte[]", FieldType.BYTE_ARRAY),
        Arguments.of("short[]", FieldType.SHORT_ARRAY),
        Arguments.of("int[]", FieldType.INT_ARRAY),
        Arguments.of("long[]", FieldType.LONG_ARRAY));
  }

  @Test
  public void givenUnknownTypeName_whenFromString_thenReturnsUnknown() {
    // given

    // when
    var result = FieldTypeUtils.fromString("UnknownType");

    // then
    assertThat(result).isEqualTo(FieldType.UNKNOWN);
  }

  @ParameterizedTest
  @MethodSource("provideTypeNamesForFromString")
  public void givenTypeName_whenFromString_thenReturnsCorrectFieldType(
      String typeName, FieldType expectedType) {
    // given

    // when
    var result = FieldTypeUtils.fromString(typeName);

    // then
    assertThat(result).isEqualTo(expectedType);
  }

  @ParameterizedTest
  @MethodSource("providePrimitiveTypes")
  public void givenPrimitiveType_whenFromTypeMirror_thenReturnsCorrectType(
      String typeName, FieldType expectedType) {
    // given
    when(typeMirror.toString()).thenReturn(typeName);
    when(typeChecker.implementsByteEnum(typeMirror)).thenReturn(false);

    // when
    var result = FieldTypeUtils.fromTypeMirror(typeMirror, typeChecker);

    // then
    assertThat(result).isEqualTo(expectedType);
  }

  @Test
  public void givenCustomObjectArray_whenFromTypeMirror_thenReturnsObjectArray() {
    // given
    when(typeMirror.toString()).thenReturn("com.example.CustomClass[]");
    when(typeChecker.implementsByteEnum(typeMirror)).thenReturn(false);

    // when
    var result = FieldTypeUtils.fromTypeMirror(typeMirror, typeChecker);

    // then
    assertThat(result).isEqualTo(FieldType.OBJECT_ARRAY);
  }

  @Test
  public void givenEnumImplementingByteEnum_whenFromTypeMirror_thenReturnsEnum() {
    // given
    when(typeMirror.toString()).thenReturn("com.example.GameState");
    when(typeChecker.implementsByteEnum(typeMirror)).thenReturn(true);

    // when
    var result = FieldTypeUtils.fromTypeMirror(typeMirror, typeChecker);

    // then
    assertThat(result).isEqualTo(FieldType.ENUM);
  }

  @ParameterizedTest
  @MethodSource("providePrimitiveArrayTypes")
  public void givenPrimitiveArrayType_whenFromTypeMirror_thenReturnsCorrectArrayType(
      String typeName, FieldType expectedType) {
    // given
    when(typeMirror.toString()).thenReturn(typeName);
    when(typeChecker.implementsByteEnum(typeMirror)).thenReturn(false);

    // when
    var result = FieldTypeUtils.fromTypeMirror(typeMirror, typeChecker);

    // then
    assertThat(result).isEqualTo(expectedType);
  }
}
