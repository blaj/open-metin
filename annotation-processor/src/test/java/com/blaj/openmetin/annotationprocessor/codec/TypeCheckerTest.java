package com.blaj.openmetin.annotationprocessor.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TypeCheckerTest {

  private TypeChecker typeChecker;

  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private Types types;
  @Mock private TypeMirror typeMirror;
  @Mock private TypeElement typeElement;
  @Mock private TypeMirror interfaceTypeMirror;
  @Mock private TypeElement interfaceTypeElement;
  @Mock private Name qualifiedName;

  @BeforeEach
  public void beforeEach() {
    typeChecker = new TypeChecker(processingEnvironment);

    when(processingEnvironment.getTypeUtils()).thenReturn(types);
  }

  @Test
  public void givenNullTypeElement_whenImplementsByteEnum_thenReturnsFalse() {
    // given
    when(types.asElement(typeMirror)).thenReturn(null);

    // when
    var result = typeChecker.implementsByteEnum(typeMirror);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenNonEnumClass_whenImplementsByteEnum_thenReturnsFalse() {
    // given
    when(types.asElement(typeMirror)).thenReturn(typeElement);
    when(typeElement.getKind()).thenReturn(ElementKind.CLASS);

    // when
    var result = typeChecker.implementsByteEnum(typeMirror);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenEnumWithNoInterfaces_whenImplementsByteEnum_thenReturnsFalse() {
    // given
    when(types.asElement(typeMirror)).thenReturn(typeElement);
    when(typeElement.getKind()).thenReturn(ElementKind.ENUM);
    when(typeElement.getInterfaces()).thenReturn(Collections.emptyList());

    // when
    var result = typeChecker.implementsByteEnum(typeMirror);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenEnumImplementingByteEnum_whenImplementsByteEnum_thenReturnsTrue() {
    // given
    when(types.asElement(typeMirror)).thenReturn(typeElement);
    when(typeElement.getKind()).thenReturn(ElementKind.ENUM);
    doReturn(Collections.singletonList(interfaceTypeMirror)).when(typeElement).getInterfaces();
    when(types.asElement(interfaceTypeMirror)).thenReturn(interfaceTypeElement);
    when(interfaceTypeElement.getQualifiedName()).thenReturn(qualifiedName);
    when(qualifiedName.toString()).thenReturn("com.blaj.openmetin.contracts.enums.ByteEnum");

    // when
    var result = typeChecker.implementsByteEnum(typeMirror);

    // then
    assertThat(result).isTrue();
  }
}
