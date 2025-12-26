package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;
import org.joou.UByte;

public class UByteFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.UBYTE;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "$L.$L($T.valueOf(in.readUnsignedByte()))",
        fieldContext.getElementVariableName(),
        fieldContext.getSetterName(),
        UByte.class);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "out.writeByte($L.$L().shortValue())",
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());
  }
}
