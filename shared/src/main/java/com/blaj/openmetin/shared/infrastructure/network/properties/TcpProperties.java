package com.blaj.openmetin.shared.infrastructure.network.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "open-metin.tcp")
public record TcpProperties(
    @NotBlank String host, @Min(0) @Max(65535) int port, @Min(0) int idleSeconds) {}
