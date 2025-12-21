package com.blaj.openmetin.game.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-metin.rest-client")
public record RestClientProperties(
    String authenticationUrl,
    String authenticationBasicAuthUsername,
    String authenticationBasicAuthPassword) {}
