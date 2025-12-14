package com.blaj.openmetin.annotationprocessor.codec.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.blaj.openmetin.annotationprocessor.codec.TypeChecker;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.palantir.javapoet.ClassName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FieldContextUtilsTest {

  private ClassName parentClassName = ClassName.get("com.example", "TestPacket");
  private MockedStatic<FieldTypeUtils> fieldTypeUtilsMockedStatic;

  @Mock private TypeElement typeElement;
  @Mock private TypeChecker typeChecker;
  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private VariableElement field1;
  @Mock private VariableElement field2;
  @Mock private VariableElement field3;
  @Mock private TypeMirror typeMirror1;
  @Mock private TypeMirror typeMirror2;
  @Mock private TypeMirror typeMirror3;
  @Mock private Name name1;
  @Mock private Name name2;
  @Mock private Name name3;
  @Mock private PacketField annotation1;
  @Mock private PacketField annotation2;
  @Mock private PacketField annotation3;

  @BeforeEach
  public void beforeEach() {
    fieldTypeUtilsMockedStatic = mockStatic(FieldTypeUtils.class);
  }

  @AfterEach
  public void afterEach() {
    fieldTypeUtilsMockedStatic.close();
  }

  @Test
  public void givenTypeElementWithNoElements_whenExtractFieldContexts_thenReturnsEmptyList() {
    // given
    when(typeElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    // when
    var result =
        FieldContextUtils.extractFieldContexts(
            typeElement, parentClassName, 0, typeChecker, processingEnvironment);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenTypeElementWithNonFieldElements_whenExtractFieldContexts_thenReturnsEmptyList() {
    // given
    var method = mock(Element.class);
    when(method.getKind()).thenReturn(ElementKind.METHOD);

    var constructor = mock(Element.class);
    when(constructor.getKind()).thenReturn(ElementKind.CONSTRUCTOR);

    doReturn(List.of(method, constructor)).when(typeElement).getEnclosedElements();

    // when
    var result =
        FieldContextUtils.extractFieldContexts(
            typeElement, parentClassName, 0, typeChecker, processingEnvironment);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void
      givenFieldsWithoutPacketFieldAnnotation_whenExtractFieldContexts_thenReturnsEmptyList() {
    // given
    when(field1.getKind()).thenReturn(ElementKind.FIELD);
    when(field1.getAnnotation(PacketField.class)).thenReturn(null);

    when(field2.getKind()).thenReturn(ElementKind.FIELD);
    when(field2.getAnnotation(PacketField.class)).thenReturn(null);

    doReturn(List.of(field1, field2)).when(typeElement).getEnclosedElements();

    // when
    var result =
        FieldContextUtils.extractFieldContexts(
            typeElement, parentClassName, 0, typeChecker, processingEnvironment);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenValid_whenExtractFieldContexts_thenMapsAllParametersCorrectly() {
    // given
    setupField(field1, name1, "name", typeMirror1, annotation1, 0, 24, 0, false, FieldType.STRING);
    setupField(
        field2, name2, "items", typeMirror2, annotation2, 1, 0, 10, false, FieldType.INT_ARRAY);
    setupField(field3, name3, "id", typeMirror3, annotation3, 2, 0, 0, true, FieldType.LONG);

    doReturn(Arrays.asList(field1, field2, field3)).when(typeElement).getEnclosedElements();

    // when
    var result =
        FieldContextUtils.extractFieldContexts(
            typeElement, parentClassName, 0, typeChecker, processingEnvironment);

    // then
    assertThat(result).hasSize(3);

    assertThat(result.get(0).fieldName()).isEqualTo("name");
    assertThat(result.get(0).length()).isEqualTo(24);
    assertThat(result.get(0).arrayLength()).isEqualTo(0);
    assertThat(result.get(0).isUnsigned()).isFalse();
    assertThat(result.get(0).fieldType()).isEqualTo(FieldType.STRING);

    assertThat(result.get(1).fieldName()).isEqualTo("items");
    assertThat(result.get(1).length()).isEqualTo(0);
    assertThat(result.get(1).arrayLength()).isEqualTo(10);
    assertThat(result.get(1).isUnsigned()).isFalse();
    assertThat(result.get(1).fieldType()).isEqualTo(FieldType.INT_ARRAY);

    assertThat(result.get(2).fieldName()).isEqualTo("id");
    assertThat(result.get(2).length()).isEqualTo(0);
    assertThat(result.get(2).arrayLength()).isEqualTo(0);
    assertThat(result.get(2).isUnsigned()).isTrue();
    assertThat(result.get(2).fieldType()).isEqualTo(FieldType.LONG);
  }

  private void setupField(
      VariableElement field,
      Name name,
      String fieldName,
      TypeMirror typeMirror,
      PacketField annotation,
      int position,
      int length,
      int arrayLength,
      boolean unsigned,
      FieldType fieldType) {
    when(field.getKind()).thenReturn(ElementKind.FIELD);
    when(field.getSimpleName()).thenReturn(name);
    when(name.toString()).thenReturn(fieldName);
    when(field.asType()).thenReturn(typeMirror);
    when(field.getAnnotation(PacketField.class)).thenReturn(annotation);

    when(annotation.position()).thenReturn(position);
    when(annotation.length()).thenReturn(length);
    when(annotation.arrayLength()).thenReturn(arrayLength);
    when(annotation.unsigned()).thenReturn(unsigned);

    fieldTypeUtilsMockedStatic
        .when(() -> FieldTypeUtils.fromTypeMirror(typeMirror, typeChecker))
        .thenReturn(fieldType);
  }
}
