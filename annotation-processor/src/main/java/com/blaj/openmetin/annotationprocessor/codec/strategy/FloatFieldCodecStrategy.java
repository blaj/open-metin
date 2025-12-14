package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;

public class FloatFieldCodecStrategy extends PrimitiveFieldCodecStrategy {

  @Override
  protected String getReadMethod() {
    return "readFloat";
  }

  @Override
  protected String getWriteMethod() {
    return "writeFloat";
  }

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.FLOAT;
  }
}
