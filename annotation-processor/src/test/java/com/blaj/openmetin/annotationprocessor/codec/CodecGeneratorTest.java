package com.blaj.openmetin.annotationprocessor.codec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CodecGeneratorTest {

  private CodecGenerator codecGenerator;

  @Mock private ProcessingEnvironment processingEnvironment;
  @Mock private FieldCodecStrategyFactory fieldCodecStrategyFactory;
  @Mock private Elements elements;
  @Mock private Filer filer;
  @Mock private Messager messager;
  @Mock private TypeElement typeElement;
  @Mock private Name typeName;
  @Mock private PackageElement packageElement;
  @Mock private Name packageName;
  @Mock private PacketHeader packetHeader;
  @Mock private JavaFileObject javaFileObject;
  @Mock private FieldCodecStrategy fieldCodecStrategy;
  @Mock private java.io.Writer writer;

  @BeforeEach
  public void beforeEach() throws IOException {
    when(processingEnvironment.getElementUtils()).thenReturn(elements);
    when(processingEnvironment.getFiler()).thenReturn(filer);
    when(processingEnvironment.getMessager()).thenReturn(messager);
    when(processingEnvironment.getTypeUtils()).thenReturn(mock(javax.lang.model.util.Types.class));

    doReturn(javaFileObject).when(filer).createSourceFile(anyString(), any(Element[].class));
    doReturn(writer).when(javaFileObject).openWriter();

    codecGenerator = new CodecGenerator(processingEnvironment, fieldCodecStrategyFactory);
  }

  @Test
  public void
      givenValidTypeElement_whenGenerate_thenGeneratesBothDecoderAndEncoderWithCorrectNames()
          throws IOException {
    // given
    setupTypeElement("com.example", "HandshakePacket");
    setupPacketHeader(0xFF, PacketDirection.INCOMING, false);

    when(typeElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    // when
    codecGenerator.generate(typeElement);

    // then
    verify(filer)
        .createSourceFile(eq("com.example.HandshakePacketDecoderService"), any(Element[].class));
    verify(filer)
        .createSourceFile(eq("com.example.HandshakePacketEncoderService"), any(Element[].class));

    verify(messager)
        .printMessage(
            eq(Kind.NOTE), eq("Generated decoder: com.example.HandshakePacketDecoderService"));
    verify(messager)
        .printMessage(
            eq(Kind.NOTE), eq("Generated encoder: com.example.HandshakePacketEncoderService"));
  }

  @Test
  public void givenTypeElementWithFields_whenGenerate_thenCallsStrategyForDecodingAndEncoding()
      throws IOException {
    // given
    setupTypeElement("com.example", "TestPacket");
    setupPacketHeader(0x01, PacketDirection.INCOMING, false);

    var field1 = mock(VariableElement.class);
    var field2 = mock(VariableElement.class);

    setupFieldElement(field1, "field1", 0);
    setupFieldElement(field2, "field2", 1);

    doReturn(List.of(field1, field2)).when(typeElement).getEnclosedElements();
    when(fieldCodecStrategyFactory.get(any())).thenReturn(fieldCodecStrategy);

    // when
    codecGenerator.generate(typeElement);

    // then
    verify(fieldCodecStrategy, times(2)).generateDecodingMethod(any(), any());
    verify(fieldCodecStrategy, times(2)).generateEncodingMethod(any(), any());
    verify(filer, times(2)).createSourceFile(anyString(), any(Element[].class));
  }

  @Test
  public void givenPacketWithSequenceTrue_whenGenerate_thenGeneratesSuccessfully()
      throws IOException {
    // given
    setupTypeElement("com.example", "SequencePacket");
    setupPacketHeader(0x20, PacketDirection.OUTGOING, true);

    when(typeElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    // when
    codecGenerator.generate(typeElement);

    // then
    verify(filer, times(2)).createSourceFile(anyString(), any(Element[].class));
    verify(messager, times(2)).printMessage(eq(Kind.NOTE), anyString());
  }

  @Test
  public void givenIOExceptionDuringGeneration_whenGenerate_thenThrowsIOException()
      throws IOException {
    // given
    setupTypeElement("com.example", "TestPacket");
    setupPacketHeader(0x01, PacketDirection.INCOMING, false);

    when(typeElement.getEnclosedElements()).thenReturn(Collections.emptyList());

    doThrow(new IOException("Test exception"))
        .when(filer)
        .createSourceFile(anyString(), any(Element[].class));

    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(
        IOException.class,
        () -> {
          codecGenerator.generate(typeElement);
        });
  }

  private void setupTypeElement(String packageNameStr, String classNameStr) {
    when(typeElement.getSimpleName()).thenReturn(typeName);
    when(typeName.toString()).thenReturn(classNameStr);

    when(elements.getPackageOf(typeElement)).thenReturn(packageElement);
    when(packageElement.getQualifiedName()).thenReturn(packageName);
    when(packageName.toString()).thenReturn(packageNameStr);
  }

  private void setupPacketHeader(int header, PacketDirection direction, boolean isSequence) {
    when(typeElement.getAnnotation(PacketHeader.class)).thenReturn(packetHeader);
    when(packetHeader.header()).thenReturn(header);
    when(packetHeader.direction()).thenReturn(direction);
    when(packetHeader.isSequence()).thenReturn(isSequence);
  }

  private void setupFieldElement(VariableElement field, String fieldName, int position) {
    Name name = mock(Name.class);
    var typeMirror = mock(TypeMirror.class);
    var packetFieldAnnotation = mock(PacketField.class);

    when(field.getKind()).thenReturn(ElementKind.FIELD);
    when(field.getSimpleName()).thenReturn(name);
    when(name.toString()).thenReturn(fieldName);
    when(field.asType()).thenReturn(typeMirror);
    when(field.getAnnotation(PacketField.class)).thenReturn(packetFieldAnnotation);

    when(packetFieldAnnotation.position()).thenReturn(position);
    when(packetFieldAnnotation.length()).thenReturn(0);
    when(packetFieldAnnotation.arrayLength()).thenReturn(0);
    when(packetFieldAnnotation.unsigned()).thenReturn(false);

    when(typeMirror.toString()).thenReturn("int");
  }
}
