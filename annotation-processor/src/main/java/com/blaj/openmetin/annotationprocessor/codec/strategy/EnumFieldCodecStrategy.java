package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public class EnumFieldCodecStrategy implements FieldCodecStrategy {

  private static final ClassName byteEnumUtilsClassName =
      ClassName.get("com.blaj.openmetin.shared.common.utils", "ByteEnumUtils");

  private final ProcessingEnvironment processingEnvironment;

  public EnumFieldCodecStrategy(ProcessingEnvironment processingEnvironment) {
    this.processingEnvironment = processingEnvironment;
  }

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.ENUM;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    var enumClass = getEnumClassName(fieldContext);

    methodSpecBuilder.addStatement(
        "$L.$L($T.fromValue($T.class, in.readByte()))",
        fieldContext.getElementVariableName(),
        fieldContext.getSetterName(),
        byteEnumUtilsClassName,
        enumClass);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    methodSpecBuilder.addStatement(
        "out.writeByte($L.$L() != null ? $L.$L().getValue() : (byte) 0)",
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName(),
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());
  }

  private ClassName getEnumClassName(FieldContext fieldContext) {
    var typeElement =
        (TypeElement) processingEnvironment.getTypeUtils().asElement(fieldContext.typeMirror());

    if (typeElement.getEnclosingElement() instanceof TypeElement enclosingType) {
      var packageName =
          processingEnvironment
              .getElementUtils()
              .getPackageOf(enclosingType)
              .getQualifiedName()
              .toString();
      var outerClassName = enclosingType.getSimpleName().toString();
      var innerClassName = typeElement.getSimpleName().toString();

      return ClassName.get(packageName, outerClassName).nestedClass(innerClassName);
    } else {
      var packageName =
          processingEnvironment
              .getElementUtils()
              .getPackageOf(typeElement)
              .getQualifiedName()
              .toString();
      var simpleClassName = typeElement.getSimpleName().toString();

      return ClassName.get(packageName, simpleClassName);
    }
  }
}
