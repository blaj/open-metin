package com.blaj.openmetin.annotationprocessor;

import com.blaj.openmetin.annotationprocessor.codec.CodecGenerator;
import com.blaj.openmetin.annotationprocessor.codec.FieldCodecStrategyFactory;
import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("com.blaj.openmetin.contracts.annotation.GeneratePacketCodec")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@AutoService(Processor.class)
public class GeneratePacketCodecProcessor extends AbstractProcessor {

  private static final Set<ElementKind> supportedElementKinds =
      Set.of(ElementKind.CLASS, ElementKind.RECORD);

  private CodecGenerator codecGenerator;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    codecGenerator =
        new CodecGenerator(
            processingEnvironment, new FieldCodecStrategyFactory(processingEnvironment));
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
    for (var element : roundEnvironment.getElementsAnnotatedWith(GeneratePacketCodec.class)) {
      if (!supportedElementKinds.contains(element.getKind())) {
        processingEnv
            .getMessager()
            .printMessage(
                Kind.ERROR, "@GeneratePacketCodec can only be applied to classes", element);
        continue;
      }

      var typeElement = (TypeElement) element;
      if (typeElement.getAnnotation(PacketHeader.class) == null) {
        processingEnv
            .getMessager()
            .printMessage(
                Kind.ERROR, "@GeneratePacketCodec requires @PacketHeader annotation", element);
        continue;
      }

      try {
        codecGenerator.generate(typeElement);
      } catch (IOException e) {
        processingEnv
            .getMessager()
            .printMessage(Kind.ERROR, "Failed to generate codec: " + e.getMessage(), element);
      }
    }

    return true;
  }
}
