package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;

public class ShortFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.SHORT;
  }

  @Override
  public void generateDecodingMethod(MethodSpec.Builder method, FieldContext context) {
    if (context.isUnsigned()) {
      method.addStatement(
          "$L.$L((short) (in.readByte() & 0xFF))",
          context.getElementVariableName(),
          context.getSetterName());
    } else {
      method.addStatement(
          "$L.$L(in.readShort())", context.getElementVariableName(), context.getSetterName());
    }
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder method, FieldContext context) {
    if (context.isUnsigned()) {
      method.addStatement(
          "out.writeByte((byte) ($L.$L() & 0xFF))",
          context.getElementVariableName(),
          context.getGetterName());
    } else {
      method.addStatement(
          "out.writeShort($L.$L())", context.getElementVariableName(), context.getGetterName());
    }
  }
}
