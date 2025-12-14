package com.blaj.openmetin.annotationprocessor.codec;

import com.palantir.javapoet.MethodSpec;

public interface FieldCodecStrategy {

  boolean isSupported(FieldContext fieldContext);

  void generateDecodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext);

  void generateEncodingMethod(MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext);
}
