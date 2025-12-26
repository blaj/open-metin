package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;
import org.joou.ULong;

public class ULongFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.ULONG;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "$L.$L($T.valueOf(in.readLongLE()))",
        fieldContext.getElementVariableName(),
        fieldContext.getSetterName(),
        ULong.class);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "out.writeLongLE($L.$L().longValue())",
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());
  }
}
