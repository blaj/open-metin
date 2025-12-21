package com.blaj.openmetin.annotationprocessor.codec.utils;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;

public class StringFieldCodecStrategy implements FieldCodecStrategy {

  private static final ClassName packetCodecUtilsClassName =
      ClassName.get("com.blaj.openmetin.shared.infrastructure.network.codec", "PacketCodecUtils");

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.STRING;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.length() > 0) {
      methodSpecBuilder.addStatement(
          "$L.$L($T.readFixedString(in, $L))",
          fieldContext.getElementVariableName(),
          fieldContext.getSetterName(),
          packetCodecUtilsClassName,
          fieldContext.length());
    } else {
      methodSpecBuilder.addStatement(
          "$L.$L($T.readString(in))",
          fieldContext.getElementVariableName(),
          fieldContext.getSetterName(),
          packetCodecUtilsClassName);
    }
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.length() > 0) {
      methodSpecBuilder.addStatement(
          "$T.writeFixedString(out, $L.$L(), $L)",
          packetCodecUtilsClassName,
          fieldContext.getElementVariableName(),
          fieldContext.getGetterName(),
          fieldContext.length());
    } else {
      methodSpecBuilder.addStatement(
          "$T.writeString(out, $L.$L())",
          packetCodecUtilsClassName,
          fieldContext.getElementVariableName(),
          fieldContext.getGetterName());
    }
  }
}
