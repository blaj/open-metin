package com.blaj.openmetin.annotationprocessor.codec.utils;

import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.TypeChecker;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.palantir.javapoet.ClassName;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class FieldContextUtils {

  private FieldContextUtils() {}

  public static List<FieldContext> extractFieldContexts(
      TypeElement typeElement,
      ClassName parentClass,
      int nestingDepth,
      TypeChecker typeChecker,
      ProcessingEnvironment processingEnvironment) {
    return typeElement.getEnclosedElements().stream()
        .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.FIELD)
        .map(enclosedElement -> (VariableElement) enclosedElement)
        .flatMap(
            variableElement ->
                Optional.ofNullable(variableElement.getAnnotation(PacketField.class))
                    .map(
                        annotation -> {
                          var fieldType =
                              FieldTypeUtils.fromTypeMirror(variableElement.asType(), typeChecker);

                          return new FieldContext(
                              variableElement.getSimpleName().toString(),
                              fieldType,
                              variableElement.asType(),
                              annotation.position(),
                              annotation.length(),
                              annotation.arrayLength(),
                              nestingDepth,
                              parentClass,
                              processingEnvironment);
                        })
                    .stream())
        .sorted(Comparator.comparingInt(FieldContext::position))
        .toList();
  }
}
