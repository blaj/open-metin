package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategyFactory;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.blaj.openmetin.annotationprocessor.codec.TypeChecker;
import com.blaj.openmetin.annotationprocessor.codec.utils.FieldContextUtils;
import com.palantir.javapoet.MethodSpec;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

public class ObjectArrayFieldCodecStrategy implements FieldCodecStrategy {

  private final ProcessingEnvironment processingEnvironment;
  private final FieldCodecStrategyFactory fieldCodecStrategyFactory;
  private final TypeChecker typeChecker;

  public ObjectArrayFieldCodecStrategy(
      ProcessingEnvironment processingEnvironment,
      FieldCodecStrategyFactory fieldCodecStrategyFactory) {
    this.processingEnvironment = processingEnvironment;
    this.fieldCodecStrategyFactory = fieldCodecStrategyFactory;
    this.typeChecker = new TypeChecker(processingEnvironment);
  }

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.OBJECT_ARRAY;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.arrayLength() <= 0) {
      return;
    }

    var elementTypeName = getElementTypeName(fieldContext.typeMirror().toString());
    var varName = fieldContext.fieldName() + "Array";
    var nextDepth = fieldContext.nestingDepth() + 1;
    var elementVarName = generateElementVariableName(nextDepth);

    var parentElement =
        processingEnvironment
            .getElementUtils()
            .getTypeElement(fieldContext.parentClassName().reflectionName());
    var elementElement = findNestedType(parentElement, elementTypeName);

    if (elementElement == null) {
      return;
    }

    var elementClassName = fieldContext.parentClassName().nestedClass(elementTypeName);
    var elementFields =
        FieldContextUtils.extractFieldContexts(
            elementElement, elementClassName, nextDepth, typeChecker, processingEnvironment);

    methodSpecBuilder.addStatement(
        "$T[] $L = new $T[$L]",
        elementClassName,
        varName,
        elementClassName,
        fieldContext.arrayLength());

    methodSpecBuilder.beginControlFlow("for (int i = 0; i < $L; i++)", fieldContext.arrayLength());
    methodSpecBuilder.addStatement(
        "$T $L = new $T()", elementClassName, elementVarName, elementClassName);

    for (var elementField : elementFields) {
      var fieldCodecStrategy = fieldCodecStrategyFactory.get(elementField);
      fieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, elementField);
    }

    methodSpecBuilder.addStatement("$L[i] = $L", varName, elementVarName);
    methodSpecBuilder.endControlFlow();

    methodSpecBuilder.addStatement(
        "$L.$L($L)", fieldContext.getElementVariableName(), fieldContext.getSetterName(), varName);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    if (fieldContext.arrayLength() <= 0) {
      return;
    }

    var elementTypeName = getElementTypeName(fieldContext.typeMirror().toString());
    var nextDepth = fieldContext.nestingDepth() + 1;
    var elementVarName = generateElementVariableName(nextDepth);

    var parentElement =
        processingEnvironment
            .getElementUtils()
            .getTypeElement(fieldContext.parentClassName().reflectionName());
    var elementElement = findNestedType(parentElement, elementTypeName);

    if (elementElement == null) {
      return;
    }

    var elementClassName = fieldContext.parentClassName().nestedClass(elementTypeName);
    var elementFields =
        FieldContextUtils.extractFieldContexts(
            elementElement, elementClassName, nextDepth, typeChecker, processingEnvironment);

    methodSpecBuilder.beginControlFlow("for (int i = 0; i < $L; i++)", fieldContext.arrayLength());

    methodSpecBuilder.addStatement(
        "$T $L = $L.$L()[i]",
        elementClassName,
        elementVarName,
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());

    for (var elementField : elementFields) {
      var fieldCodecStrategy = fieldCodecStrategyFactory.get(elementField);
      fieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, elementField);
    }

    methodSpecBuilder.endControlFlow();
  }

  private String generateElementVariableName(int depth) {
    return "element" + depth;
  }

  private String getElementTypeName(String arrayType) {
    var withoutBrackets = arrayType.replace("[]", "");
    return withoutBrackets.substring(withoutBrackets.lastIndexOf('.') + 1);
  }

  private TypeElement findNestedType(TypeElement parent, String nestedName) {
    return parent.getEnclosedElements().stream()
        .filter(
            enclosedElement ->
                enclosedElement.getKind() == ElementKind.CLASS
                    || enclosedElement.getKind() == ElementKind.RECORD)
        .filter(enclosedElement -> enclosedElement.getSimpleName().toString().equals(nestedName))
        .map(enclosedElement -> (TypeElement) enclosedElement)
        .findFirst()
        .orElse(null);
  }
}
