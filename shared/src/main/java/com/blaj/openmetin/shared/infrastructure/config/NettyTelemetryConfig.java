package com.blaj.openmetin.shared.infrastructure.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.netty.v4_1.NettyServerTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyTelemetryConfig {

  @Bean
  public NettyServerTelemetry nettyServerTelemetry(OpenTelemetry openTelemetry) {
    return NettyServerTelemetry.create(openTelemetry);
  }
}
