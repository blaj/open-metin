package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;

public class ByteArrayFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.BYTE_ARRAY;
  }

  @Override
  public void generateDecodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext context) {
    if (context.arrayLength() <= 0) {
      return;
    }

    var arrayVarName = context.fieldName() + "Bytes";

    methodSpecBuilder.addStatement("byte[] $L = new byte[$L]", arrayVarName, context.arrayLength());
    methodSpecBuilder.addStatement("in.readBytes($L)", arrayVarName);
    methodSpecBuilder.addStatement(
        "$L.$L($L)", context.getElementVariableName(), context.getSetterName(), arrayVarName);
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext context) {
    if (context.arrayLength() <= 0) {
      return;
    }

    methodSpecBuilder.addStatement(
        "out.writeBytes($L.$L())", context.getElementVariableName(), context.getGetterName());
  }
}
