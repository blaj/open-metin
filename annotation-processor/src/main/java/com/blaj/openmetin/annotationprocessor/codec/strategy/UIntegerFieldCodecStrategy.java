package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;
import org.joou.UInteger;

public class UIntegerFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.UINTEGER;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "$L.$L($T.valueOf(in.readUnsignedIntLE()))",
        fieldContext.getElementVariableName(),
        fieldContext.getSetterName(),
        UInteger.class);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "out.writeIntLE($L.$L().intValue())",
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());
  }
}
