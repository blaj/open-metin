package com.blaj.openmetin.game.infrastructure.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.CollectionLikeType;
import tools.jackson.databind.type.TypeFactory;

@ExtendWith(MockitoExtension.class)
public class EnumSetJsonbAttributeConverterTest {

  private EnumSetJsonbAttributeConverter<TestEnum> enumSetJsonbAttributeConverter;

  @Mock private JsonMapper jsonMapper;
  @Mock private TypeFactory typeFactory;
  @Mock private CollectionLikeType collectionLikeType;

  @BeforeEach
  public void beforeEach() {
    enumSetJsonbAttributeConverter = new TestEnumSetJsonbAttributeConverter(jsonMapper);
  }

  @Test
  public void givenNull_whenConvertToDatabaseColumn_thenReturnNull() {
    // given
    EnumSet<TestEnum> attribute = null;

    // when
    var value = enumSetJsonbAttributeConverter.convertToDatabaseColumn(attribute);

    // then
    assertThat(value).isNull();
  }

  @Test
  public void givenEmptySet_whenConvertToDatabaseColumn_thenReturnNull() {
    // given
    var attribute = EnumSet.noneOf(TestEnum.class);

    // when
    var value = enumSetJsonbAttributeConverter.convertToDatabaseColumn(attribute);

    // then
    assertThat(value).isNull();
  }

  @Test
  public void givenValid_whenConvertToDatabaseColumn_thenReturnValue() {
    // given
    var attribute = EnumSet.of(TestEnum.TEST);

    given(jsonMapper.writeValueAsString(attribute)).willReturn("test");

    // when
    var value = enumSetJsonbAttributeConverter.convertToDatabaseColumn(attribute);

    // then
    assertThat(value).isEqualTo("test");
  }

  @Test
  public void givenNull_whenConvertToEntityAttribute_thenReturnNoneOf() {
    // given
    String data = null;

    // when
    var value = enumSetJsonbAttributeConverter.convertToEntityAttribute(data);

    // then
    assertThat(value).isEqualTo(EnumSet.noneOf(TestEnum.class));
  }

  @Test
  public void givenBlank_whenConvertToEntityAttribute_thenReturnNoneOf() {
    // given
    String data = "";

    // when
    var value = enumSetJsonbAttributeConverter.convertToEntityAttribute(data);

    // then
    assertThat(value).isEqualTo(EnumSet.noneOf(TestEnum.class));
  }

  @Test
  public void givenJsonMapperReturnNull_whenConvertToEntityAttribute_thenReturnNoneOf() {
    // given
    String data = "test";

    given(jsonMapper.getTypeFactory()).willReturn(typeFactory);
    given(typeFactory.constructCollectionLikeType(List.class, TestEnum.class))
        .willReturn(collectionLikeType);
    given(jsonMapper.readValue(data, collectionLikeType)).willReturn(null);

    // when
    var value = enumSetJsonbAttributeConverter.convertToEntityAttribute(data);

    // then
    assertThat(value).isEqualTo(EnumSet.noneOf(TestEnum.class));
  }

  @Test
  public void givenJsonMapperReturnEmptyList_whenConvertToEntityAttribute_thenReturnEnumSet() {
    // given
    String data = "test";

    given(jsonMapper.getTypeFactory()).willReturn(typeFactory);
    given(typeFactory.constructCollectionLikeType(List.class, TestEnum.class))
        .willReturn(collectionLikeType);
    given(jsonMapper.readValue(data, collectionLikeType)).willReturn(List.of());

    // when
    var value = enumSetJsonbAttributeConverter.convertToEntityAttribute(data);

    // then
    assertThat(value).isEqualTo(EnumSet.noneOf(TestEnum.class));
  }

  @Test
  public void givenValid_whenConvertToEntityAttribute_thenReturnEnumSet() {
    // given
    String data = "test";

    given(jsonMapper.getTypeFactory()).willReturn(typeFactory);
    given(typeFactory.constructCollectionLikeType(List.class, TestEnum.class))
        .willReturn(collectionLikeType);
    given(jsonMapper.readValue(data, collectionLikeType)).willReturn(List.of(TestEnum.TEST));

    // when
    var value = enumSetJsonbAttributeConverter.convertToEntityAttribute(data);

    // then
    assertThat(value).contains(TestEnum.TEST);
  }

  private enum TestEnum {
    TEST
  }

  private static class TestEnumSetJsonbAttributeConverter
      extends EnumSetJsonbAttributeConverter<TestEnum> {

    public TestEnumSetJsonbAttributeConverter(JsonMapper jsonMapper) {
      super(TestEnum.class, jsonMapper);
    }
  }
}
