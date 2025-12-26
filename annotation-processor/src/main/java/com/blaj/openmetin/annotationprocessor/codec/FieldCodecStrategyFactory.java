package com.blaj.openmetin.annotationprocessor.codec;

import com.blaj.openmetin.annotationprocessor.codec.strategy.BooleanFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ByteArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ByteFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.DoubleFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.EnumFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.FloatFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.IntArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.IntFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.LongArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.LongFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ObjectArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ObjectFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ShortFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.StringArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.UByteArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.UByteFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.UIntegerArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.UIntegerFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ULongArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.ULongFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.UShortArrayFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.strategy.UShortFieldCodecStrategy;
import com.blaj.openmetin.annotationprocessor.codec.utils.StringFieldCodecStrategy;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;

public class FieldCodecStrategyFactory {

  private final List<FieldCodecStrategy> fieldCodecStrategies;

  public FieldCodecStrategyFactory(ProcessingEnvironment processingEnvironment) {
    this.fieldCodecStrategies =
        List.of(
            new BooleanFieldCodecStrategy(),
            new ByteArrayFieldCodecStrategy(),
            new ByteFieldCodecStrategy(),
            new DoubleFieldCodecStrategy(),
            new EnumFieldCodecStrategy(processingEnvironment),
            new FloatFieldCodecStrategy(),
            new IntArrayFieldCodecStrategy(),
            new IntFieldCodecStrategy(),
            new LongArrayFieldCodecStrategy(),
            new LongFieldCodecStrategy(),
            new ObjectArrayFieldCodecStrategy(processingEnvironment, this),
            new ObjectFieldCodecStrategy(processingEnvironment, this),
            new ShortFieldCodecStrategy(),
            new StringArrayFieldCodecStrategy(),
            new StringFieldCodecStrategy(),
            new UByteArrayFieldCodecStrategy(),
            new UByteFieldCodecStrategy(),
            new UIntegerArrayFieldCodecStrategy(),
            new UIntegerFieldCodecStrategy(),
            new ULongArrayFieldCodecStrategy(),
            new ULongFieldCodecStrategy(),
            new UShortFieldCodecStrategy(),
            new UShortArrayFieldCodecStrategy());
  }

  public FieldCodecStrategy get(FieldContext fieldContext) {
    return fieldCodecStrategies.stream()
        .filter(fieldCodecStrategy -> fieldCodecStrategy.isSupported(fieldContext))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No strategy found for type: " + fieldContext.fieldType()));
  }
}
