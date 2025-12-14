package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;

public class LongFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.LONG;
  }

  @Override
  public void generateDecodingMethod(MethodSpec.Builder method, FieldContext context) {
    if (context.isUnsigned()) {
      method.addStatement(
          "$L.$L(in.readInt() & 0xFFFFFFFFL)",
          context.getElementVariableName(),
          context.getSetterName());
    } else {
      method.addStatement(
          "$L.$L(in.readLong())", context.getElementVariableName(), context.getSetterName());
    }
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder method, FieldContext context) {
    if (context.isUnsigned()) {
      method.addStatement(
          "out.writeInt((int) ($L.$L() & 0xFFFFFFFFL))",
          context.getElementVariableName(),
          context.getGetterName());
    } else {
      method.addStatement(
          "out.writeLong($L.$L())", context.getElementVariableName(), context.getGetterName());
    }
  }
}
