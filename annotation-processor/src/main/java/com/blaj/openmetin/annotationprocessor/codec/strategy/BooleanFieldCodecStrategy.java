package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;

public class BooleanFieldCodecStrategy extends PrimitiveFieldCodecStrategy {

  @Override
  protected String getReadMethod() {
    return "readBoolean";
  }

  @Override
  protected String getWriteMethod() {
    return "writeBoolean";
  }

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.BOOLEAN;
  }
}
