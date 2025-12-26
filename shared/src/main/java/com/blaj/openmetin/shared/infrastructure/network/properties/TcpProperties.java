package com.blaj.openmetin.shared.infrastructure.network.properties;

import com.blaj.openmetin.shared.application.common.config.TcpConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.InetAddress;
import org.joou.UShort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "open-metin.tcp")
public record TcpProperties(
    @NotNull InetAddress host, UShort port, UShort idleSeconds)
    implements TcpConfig {}
