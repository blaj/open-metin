package com.blaj.openmetin.annotationprocessor.codec.utils;

import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.blaj.openmetin.annotationprocessor.codec.TypeChecker;
import java.util.Arrays;
import java.util.Set;
import javax.lang.model.type.TypeMirror;

public class FieldTypeUtils {

  private FieldTypeUtils() {}

  public static FieldType fromString(String typeName) {
    return Arrays.stream(FieldType.values())
        .filter(type -> type.matches(typeName))
        .findFirst()
        .orElse(FieldType.UNKNOWN);
  }

  public static FieldType fromTypeMirror(TypeMirror typeMirror, TypeChecker typeChecker) {
    var typeName = getSimpleTypeName(typeMirror);

    if (typeChecker.implementsByteEnum(typeMirror)) {
      return FieldType.ENUM;
    }

    if (isObjectArray(typeName)) {
      return FieldType.OBJECT_ARRAY;
    }

    if (isObject(typeName, typeMirror)) {
      return FieldType.OBJECT;
    }

    return fromString(typeName);
  }

  private static String getSimpleTypeName(TypeMirror type) {
    var fullName = type.toString();

    if (fullName.endsWith("[]")) {
      return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    return switch (fullName) {
      case "byte", "short", "int", "long", "float", "double", "boolean" -> fullName;
      case "java.lang.String" -> "String";
      default -> fullName.substring(fullName.lastIndexOf('.') + 1);
    };
  }

  private static boolean isObjectArray(String typeName) {
    return typeName.endsWith("[]")
        && !Set.of("byte[]", "short[]", "int[]", "long[]", "String[]").contains(typeName);
  }

  private static boolean isObject(String typeName, TypeMirror typeMirror) {
    if (typeName.endsWith("[]")) {
      return false;
    }

    if (Set.of("byte", "short", "int", "long", "float", "double", "boolean").contains(typeName)) {
      return false;
    }

    if (typeName.equals("String") || typeMirror.toString().equals("java.lang.String")) {
      return false;
    }

    return true;
  }
}
