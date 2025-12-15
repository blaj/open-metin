package com.blaj.openmetin.annotationprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.annotationprocessor.codec.CodecGenerator;
import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GeneratePacketCodecProcessorTest {

  private GeneratePacketCodecProcessor processor;

  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private RoundEnvironment roundEnvironment;
  @Mock private Messager messager;
  @Mock private TypeElement typeElement;
  @Mock private Element interfaceElement;
  @Mock private PacketHeader packetHeaderAnnotation;
  @Mock private CodecGenerator codecGenerator;
  @Mock private Filer filer;

  @BeforeEach
  public void beforeEach() throws Exception {
    processor = new GeneratePacketCodecProcessor();

    when(processingEnvironment.getMessager()).thenReturn(messager);
    when(processingEnvironment.getFiler()).thenReturn(filer);

    processor.init(processingEnvironment);

    injectMockCodecGenerator();
  }

  @Test
  public void givenNoAnnotatedElements_whenProcess_thenReturnsTrue() throws IOException {
    // given
    when(roundEnvironment.getElementsAnnotatedWith(GeneratePacketCodec.class))
        .thenReturn(Collections.emptySet());

    // when
    var result = processor.process(Collections.emptySet(), roundEnvironment);

    // then
    assertThat(result).isTrue();
    verify(messager, never()).printMessage(any(), anyString(), any());
    verify(codecGenerator, never()).generate(any());
  }

  @Test
  public void givenClassWithoutPacketHeader_whenProcess_thenPrintsErrorAndContinues()
      throws IOException {
    // given
    doReturn(Set.of(typeElement))
        .when(roundEnvironment)
        .getElementsAnnotatedWith(GeneratePacketCodec.class);
    when(typeElement.getKind()).thenReturn(ElementKind.CLASS);
    when(typeElement.getAnnotation(PacketHeader.class)).thenReturn(null);

    // when
    var result = processor.process(Collections.emptySet(), roundEnvironment);

    // then
    assertThat(result).isTrue();
    verify(messager)
        .printMessage(
            eq(Kind.ERROR),
            eq("@GeneratePacketCodec requires @PacketHeader annotation"),
            eq(typeElement));
    verify(codecGenerator, never()).generate(any());
  }

  @Test
  public void givenInterface_whenProcess_thenPrintsErrorAndDoesNotCallGenerator()
      throws IOException {
    // given
    doReturn(Set.of(interfaceElement))
        .when(roundEnvironment)
        .getElementsAnnotatedWith(GeneratePacketCodec.class);
    when(interfaceElement.getKind()).thenReturn(ElementKind.INTERFACE);

    // when
    var result = processor.process(Collections.emptySet(), roundEnvironment);

    // then
    assertThat(result).isTrue();
    verify(messager)
        .printMessage(
            eq(Kind.ERROR),
            eq("@GeneratePacketCodec can only be applied to classes"),
            eq(interfaceElement));
    verify(codecGenerator, never()).generate(any());
  }

  @Test
  public void givenGeneratorException_whenProcess_thenPrintsErrorAndContinues() throws IOException {
    // given
    doReturn(Set.of(typeElement))
        .when(roundEnvironment)
        .getElementsAnnotatedWith(GeneratePacketCodec.class);
    when(typeElement.getKind()).thenReturn(ElementKind.CLASS);
    when(typeElement.getAnnotation(PacketHeader.class)).thenReturn(packetHeaderAnnotation);
    doThrow(new IOException("message")).when(codecGenerator).generate(typeElement);

    // when
    var result = processor.process(Collections.emptySet(), roundEnvironment);

    // then
    assertThat(result).isTrue();
    verify(messager)
        .printMessage(eq(Kind.ERROR), eq("Failed to generate codec: message"), eq(typeElement));
  }

  @Test
  public void givenValid_whenProcess_thenReturnsTrue() throws IOException {
    // given
    doReturn(Set.of(typeElement))
        .when(roundEnvironment)
        .getElementsAnnotatedWith(GeneratePacketCodec.class);
    when(typeElement.getKind()).thenReturn(ElementKind.CLASS);
    when(typeElement.getAnnotation(PacketHeader.class)).thenReturn(packetHeaderAnnotation);

    // when
    var result = processor.process(Collections.emptySet(), roundEnvironment);

    // then
    assertThat(result).isTrue();
    verify(codecGenerator).generate(typeElement);
    verify(messager, never()).printMessage(eq(Kind.ERROR), anyString(), any());
  }

  private void injectMockCodecGenerator() throws Exception {
    var field = GeneratePacketCodecProcessor.class.getDeclaredField("codecGenerator");
    field.setAccessible(true);
    field.set(processor, codecGenerator);
  }
}
