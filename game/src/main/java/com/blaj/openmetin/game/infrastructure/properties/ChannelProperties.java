package com.blaj.openmetin.game.infrastructure.properties;

import com.blaj.openmetin.game.application.common.config.ChannelPropertiesConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.joou.UByte;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "open-metin")
public record ChannelProperties(UByte channelIndex) implements ChannelPropertiesConfig {}
