package com.blaj.openmetin.shared.infrastructure.config;

import com.blaj.openmetin.shared.infrastructure.converter.NumberToUByteConverter;
import com.blaj.openmetin.shared.infrastructure.converter.NumberToUIntegerConverter;
import com.blaj.openmetin.shared.infrastructure.converter.NumberToULongConverter;
import com.blaj.openmetin.shared.infrastructure.converter.NumberToUShortConverter;
import com.blaj.openmetin.shared.infrastructure.converter.StringToUByteConverter;
import com.blaj.openmetin.shared.infrastructure.converter.StringToUIntegerConverter;
import com.blaj.openmetin.shared.infrastructure.converter.StringToULongConverter;
import com.blaj.openmetin.shared.infrastructure.converter.StringToUShortConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class UnsignedConverterConfig {

  @Bean
  public ConversionService conversionService() {
    var conversionService = new DefaultConversionService();
    conversionService.addConverter(new StringToUByteConverter());
    conversionService.addConverter(new StringToUShortConverter());
    conversionService.addConverter(new StringToUIntegerConverter());
    conversionService.addConverter(new StringToULongConverter());
    conversionService.addConverter(new NumberToUByteConverter());
    conversionService.addConverter(new NumberToUShortConverter());
    conversionService.addConverter(new NumberToUIntegerConverter());
    conversionService.addConverter(new NumberToULongConverter());

    return conversionService;
  }
}
