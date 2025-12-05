package com.blaj.openmetin.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpenmetinGameApplication {

  public static void main(String[] args) {
    SpringApplication.run(OpenmetinGameApplication.class, args);
  }
}
