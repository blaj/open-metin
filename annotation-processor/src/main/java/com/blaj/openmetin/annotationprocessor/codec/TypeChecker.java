package com.blaj.openmetin.annotationprocessor.codec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class TypeChecker {
  private static final String BYTE_ENUM_CLASS = "com.blaj.openmetin.contracts.enums.ByteEnum";

  private final ProcessingEnvironment processingEnv;

  public TypeChecker(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  public boolean implementsByteEnum(TypeMirror typeMirror) {
    var typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

    if (typeElement == null || typeElement.getKind() != ElementKind.ENUM) {
      return false;
    }

    return typeElement.getInterfaces().stream()
        .map(interfaceType -> (TypeElement) processingEnv.getTypeUtils().asElement(interfaceType))
        .anyMatch(
            interfaceElement ->
                interfaceElement != null
                    && interfaceElement.getQualifiedName().toString().equals(BYTE_ENUM_CLASS));
  }
}
