package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.palantir.javapoet.MethodSpec;

public abstract class PrimitiveFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public void generateDecodingMethod(MethodSpec.Builder method, FieldContext context) {
    method.addStatement(
        "$L.$L(in.$L())",
        context.getElementVariableName(),
        context.getSetterName(),
        getReadMethod());
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder method, FieldContext context) {
    method.addStatement(
        "out.$L($L.$L())",
        getWriteMethod(),
        context.getElementVariableName(),
        context.getGetterName());
  }

  protected abstract String getReadMethod();

  protected abstract String getWriteMethod();
}
