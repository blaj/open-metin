package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;

public class IntArrayFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.INT_ARRAY;
  }

  @Override
  public void generateDecodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext context) {
    if (context.arrayLength() <= 0) {
      return;
    }

    var arrayVarName = context.fieldName() + "Array";

    methodSpecBuilder.addStatement("int[] $L = new int[$L]", arrayVarName, context.arrayLength());

    methodSpecBuilder.beginControlFlow("for (int i = 0; i < $L; i++)", context.arrayLength());
    methodSpecBuilder.addStatement("$L[i] = in.readIntLE()", arrayVarName);
    methodSpecBuilder.endControlFlow();

    methodSpecBuilder.addStatement(
        "$L.$L($L)", context.getElementVariableName(), context.getSetterName(), arrayVarName);
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext context) {
    if (context.arrayLength() <= 0) {
      return;
    }

    methodSpecBuilder.beginControlFlow("for (int i = 0; i < $L; i++)", context.arrayLength());
    methodSpecBuilder.addStatement(
        "out.writeIntLE($L.$L()[i])", context.getElementVariableName(), context.getGetterName());
    methodSpecBuilder.endControlFlow();
  }
}
