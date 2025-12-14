package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;

public class StringArrayFieldCodecStrategy implements FieldCodecStrategy {

  private static final ClassName packetCodecUtilsClassName =
      ClassName.get("com.blaj.openmetin.shared.infrastructure.network.codec", "PacketCodecUtils");

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.STRING_ARRAY;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.arrayLength() > 0 && fieldContext.length() > 0) {
      methodSpecBuilder.addStatement(
          "$L.$L($T.readFixedStringArray(in, $L, $L))",
          fieldContext.getElementVariableName(),
          fieldContext.getSetterName(),
          packetCodecUtilsClassName,
          fieldContext.arrayLength(),
          fieldContext.length());
    }
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.arrayLength() > 0 && fieldContext.length() > 0) {
      methodSpecBuilder.addStatement(
          "$T.writeFixedStringArray(out, $L.$L(), $L, $L)",
          packetCodecUtilsClassName,
          fieldContext.getElementVariableName(),
          fieldContext.getGetterName(),
          fieldContext.arrayLength(),
          fieldContext.length());
    }
  }
}
