package com.blaj.openmetin.authentication.infrastructure.config;

import com.blaj.openmetin.authentication.infrastructure.converter.RequestEnumConverter;
import com.blaj.openmetin.authentication.infrastructure.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final SecurityProperties securityProperties;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(securityProperties.allowedOrigins())
        .allowedMethods(securityProperties.allowedMethods());
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverterFactory(new RequestEnumConverter());
  }
}
