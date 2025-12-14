package com.blaj.openmetin.annotationprocessor.codec;

import java.util.Arrays;

public enum FieldType {
  BYTE("byte"),
  SHORT("short"),
  INT("int"),
  LONG("long"),
  BOOLEAN("boolean"),
  FLOAT("float"),
  DOUBLE("double"),
  STRING("String", "java.lang.String"),

  BYTE_ARRAY("byte[]"),
  SHORT_ARRAY("short[]"),
  INT_ARRAY("int[]"),
  LONG_ARRAY("long[]"),
  STRING_ARRAY("String[]"),

  ENUM("enum"),
  OBJECT_ARRAY("object[]"),
  UNKNOWN("unknown");

  private final String[] typeNames;

  FieldType(String... typeNames) {
    this.typeNames = typeNames;
  }

  public boolean matches(String typeName) {
    return Arrays.asList(typeNames).contains(typeName);
  }
}
