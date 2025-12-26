package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.MethodSpec.Builder;
import org.joou.UByte;

public class UByteArrayFieldCodecStrategy implements FieldCodecStrategy {

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.UBYTE_ARRAY;
  }

  @Override
  public void generateDecodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext context) {
    if (context.arrayLength() <= 0) {
      return;
    }

    var arrayVarName = context.fieldName() + "Array";

    methodSpecBuilder.addStatement(
        "var $L = new $T[$L]",
        arrayVarName,
        UByte.class,
        context.arrayLength());

    methodSpecBuilder.beginControlFlow(
        "for (var i = 0; i < $L; i++)", context.arrayLength());
    methodSpecBuilder.addStatement(
        "$L[i] = $T.valueOf(in.readUnsignedByte())",
        arrayVarName,
        UByte.class);
    methodSpecBuilder.endControlFlow();

    methodSpecBuilder.addStatement(
        "$L.$L($L)", context.getElementVariableName(), context.getSetterName(), arrayVarName);
  }

  @Override
  public void generateEncodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext context) {
    if (context.arrayLength() <= 0) {
      return;
    }

    methodSpecBuilder.beginControlFlow(
        "for (var i = 0; i < $L; i++)", context.arrayLength());
    methodSpecBuilder.addStatement(
        "out.writeByte($L.$L()[i].shortValue())", context.getElementVariableName(), context.getGetterName());
    methodSpecBuilder.endControlFlow();
  }
}
