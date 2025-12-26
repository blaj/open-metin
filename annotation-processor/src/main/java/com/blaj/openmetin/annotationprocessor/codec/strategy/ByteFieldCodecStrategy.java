package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;

public class ByteFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.BYTE;
  }

  @Override
  public void generateDecodingMethod(MethodSpec.Builder method, FieldContext context) {
    method.addStatement(
        "$L.$L(in.readByte())", context.getElementVariableName(), context.getSetterName());
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder method, FieldContext context) {
    method.addStatement(
        "out.writeByte($L.$L())", context.getElementVariableName(), context.getGetterName());
  }
}
