package com.blaj.openmetin.annotationprocessor.codec;

import com.palantir.javapoet.ClassName;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

public record FieldContext(
    String fieldName,
    FieldType fieldType,
    TypeMirror typeMirror,
    int position,
    int length,
    int arrayLength,
    boolean isUnsigned,
    int nestingDepth,
    ClassName parentClassName,
    ProcessingEnvironment processingEnvironment) {

  public String getSetterName() {
    return "set" + capitalize(fieldName);
  }

  public String getGetterName() {
    return "get" + capitalize(fieldName);
  }

  public String getElementVariableName() {
    if (nestingDepth == 0) {
      return "packet";
    } else {
      return "element" + nestingDepth;
    }
  }

  private String capitalize(String value) {
    return Optional.ofNullable(value)
        .filter(str -> !str.isEmpty())
        .map(str -> str.substring(0, 1).toUpperCase() + str.substring(1))
        .orElse(null);
  }
}
