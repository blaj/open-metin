package com.blaj.openmetin.game.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-metin.trust-store")
public record TrustStoreProperties(String path, String password) {}
