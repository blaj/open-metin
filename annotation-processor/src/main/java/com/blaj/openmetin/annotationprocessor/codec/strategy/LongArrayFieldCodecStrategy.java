package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;

public class LongArrayFieldCodecStrategy implements FieldCodecStrategy {

  private static final ClassName packetCodecUtilsClassName =
      ClassName.get("com.blaj.openmetin.shared.infrastructure.network.codec", "PacketCodecUtils");

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.LONG_ARRAY;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.arrayLength() <= 0) {
      return;
    }

    if (fieldContext.isUnsigned()) {
      methodSpecBuilder.addStatement(
          "$L.$L($T.readFixedUnsignedIntArray(in, $L))",
          fieldContext.getElementVariableName(),
          fieldContext.getSetterName(),
          packetCodecUtilsClassName,
          fieldContext.arrayLength());
    } else {
      methodSpecBuilder.addStatement(
          "$L.$L($T.readFixedLongArray(in, $L))",
          fieldContext.getElementVariableName(),
          fieldContext.getSetterName(),
          packetCodecUtilsClassName,
          fieldContext.arrayLength());
    }
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.arrayLength() <= 0) {
      return;
    }

    if (fieldContext.isUnsigned()) {
      methodSpecBuilder.addStatement(
          "$T.writeFixedUnsignedIntArray(out, $L.$L(), $L)",
          packetCodecUtilsClassName,
          fieldContext.getElementVariableName(),
          fieldContext.getGetterName(),
          fieldContext.arrayLength());
    } else {
      methodSpecBuilder.addStatement(
          "$T.writeFixedLongArray(out, $L.$L(), $L)",
          packetCodecUtilsClassName,
          fieldContext.getElementVariableName(),
          fieldContext.getGetterName(),
          fieldContext.arrayLength());
    }
  }
}
