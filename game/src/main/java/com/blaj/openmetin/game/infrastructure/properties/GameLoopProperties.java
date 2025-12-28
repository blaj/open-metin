package com.blaj.openmetin.game.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openmetin.game-loop")
public record GameLoopProperties(boolean enabled, int targetTps, int fixedTimestepMs) {}
