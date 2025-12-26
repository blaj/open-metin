package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;
import org.joou.UShort;

public class UShortFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.USHORT;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "$L.$L($T.valueOf(in.readUnsignedShortLE()))",
        fieldContext.getElementVariableName(),
        fieldContext.getSetterName(),
        UShort.class);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "out.writeShortLE($L.$L().intValue())",
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());
  }
}
