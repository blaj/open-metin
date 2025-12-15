package com.blaj.openmetin.shared.infrastructure.config;

import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateTimeUtilsConfig {

  @PostConstruct
  public void init() {
    DateTimeUtils.initialize();
  }
}
