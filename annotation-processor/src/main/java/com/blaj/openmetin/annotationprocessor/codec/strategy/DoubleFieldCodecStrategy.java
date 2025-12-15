package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;

public class DoubleFieldCodecStrategy extends PrimitiveFieldCodecStrategy {

  @Override
  protected String getReadMethod() {
    return "readDoubleLE";
  }

  @Override
  protected String getWriteMethod() {
    return "writeDoubleLE";
  }

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.DOUBLE;
  }
}
