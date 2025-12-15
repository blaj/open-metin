package com.blaj.openmetin.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.blaj.openmetin.shared")
@ConfigurationPropertiesScan("com.blaj.openmetin.shared")
public class SharedAutoConfiguration {}
