package com.blaj.openmetin.authentication.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-metin.web.security")
public record SecurityProperties(
    String[] allowedOrigins,
    String[] allowedMethods,
    String basicAuthUsername,
    String basicAuthPassword) {}
