package com.blaj.openmetin.annotationprocessor.codec.strategy;

import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategyFactory;
import com.blaj.openmetin.annotationprocessor.codec.FieldContext;
import com.blaj.openmetin.annotationprocessor.codec.FieldType;
import com.blaj.openmetin.annotationprocessor.codec.TypeChecker;
import com.blaj.openmetin.annotationprocessor.codec.utils.FieldContextUtils;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class ObjectFieldCodecStrategy implements FieldCodecStrategy {

  private final ProcessingEnvironment processingEnvironment;
  private final FieldCodecStrategyFactory fieldCodecStrategyFactory;
  private final TypeChecker typeChecker;

  public ObjectFieldCodecStrategy(
      ProcessingEnvironment processingEnvironment,
      FieldCodecStrategyFactory fieldCodecStrategyFactory) {
    this.processingEnvironment = processingEnvironment;
    this.fieldCodecStrategyFactory = fieldCodecStrategyFactory;
    this.typeChecker = new TypeChecker(processingEnvironment);
  }

  @Override
  public boolean isSupported(FieldContext fieldContext) {
    return fieldContext.fieldType() == FieldType.OBJECT;
  }

  @Override
  public void generateDecodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    var fullTypeName = fieldContext.typeMirror().toString();
    var elementTypeName = getElementTypeName(fieldContext.typeMirror().toString());
    var nextDepth = fieldContext.nestingDepth() + 1;
    var elementVarName = generateElementVariableName(nextDepth);

    var parentElement =
        processingEnvironment
            .getElementUtils()
            .getTypeElement(fieldContext.parentClassName().reflectionName());
    var elementElement = findNestedType(parentElement, elementTypeName);

    if (elementElement == null) {
      elementElement = processingEnvironment.getElementUtils().getTypeElement(fullTypeName);
    }

    if (elementElement == null) {
      processingEnvironment
          .getMessager()
          .printMessage(
              Kind.ERROR,
              "Cannot find type: " + fullTypeName + " (searched as nested and top-level)");
      return;
    }

    var elementClassName = ClassName.bestGuess(fullTypeName);
    var elementFields =
        FieldContextUtils.extractFieldContexts(
            elementElement, elementClassName, nextDepth, typeChecker, processingEnvironment);

    methodSpecBuilder.addStatement(
        "$T $L = new $T()", elementClassName, elementVarName, elementClassName);

    for (var elementField : elementFields) {
      var fieldCodecStrategy = fieldCodecStrategyFactory.get(elementField);
      fieldCodecStrategy.generateDecodingMethod(methodSpecBuilder, elementField);
    }

    methodSpecBuilder.addStatement(
        "$L.$L($L)",
        fieldContext.getElementVariableName(),
        fieldContext.getSetterName(),
        elementVarName);
  }

  @Override
  public void generateEncodingMethod(
      MethodSpec.Builder methodSpecBuilder, FieldContext fieldContext) {
    var fullTypeName = fieldContext.typeMirror().toString();
    var elementTypeName = getElementTypeName(fieldContext.typeMirror().toString());
    var nextDepth = fieldContext.nestingDepth() + 1;
    var elementVarName = generateElementVariableName(nextDepth);

    var parentElement =
        processingEnvironment
            .getElementUtils()
            .getTypeElement(fieldContext.parentClassName().reflectionName());
    var elementElement = findNestedType(parentElement, elementTypeName);

    if (elementElement == null) {
      elementElement = processingEnvironment.getElementUtils().getTypeElement(fullTypeName);
    }

    if (elementElement == null) {
      processingEnvironment
          .getMessager()
          .printMessage(
              Kind.ERROR,
              "Cannot find type: " + fullTypeName + " (searched as nested and top-level)");
      return;
    }

    var elementClassName = ClassName.bestGuess(fullTypeName);
    var elementFields =
        FieldContextUtils.extractFieldContexts(
            elementElement, elementClassName, nextDepth, typeChecker, processingEnvironment);

    methodSpecBuilder.addStatement(
        "$T $L = $L.$L()",
        elementClassName,
        elementVarName,
        fieldContext.getElementVariableName(),
        fieldContext.getGetterName());

    for (var elementField : elementFields) {
      var fieldCodecStrategy = fieldCodecStrategyFactory.get(elementField);
      fieldCodecStrategy.generateEncodingMethod(methodSpecBuilder, elementField);
    }
  }

  private String generateElementVariableName(int depth) {
    return "element" + depth;
  }

  private String getElementTypeName(String typeName) {
    return typeName.substring(typeName.lastIndexOf('.') + 1);
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
