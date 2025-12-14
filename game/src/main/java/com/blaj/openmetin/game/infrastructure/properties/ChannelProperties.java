package com.blaj.openmetin.game.infrastructure.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "open-metin")
public record ChannelProperties(@Min(0) @Max(65535) int channelIndex) {}
