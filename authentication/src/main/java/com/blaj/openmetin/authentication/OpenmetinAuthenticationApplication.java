package com.blaj.openmetin.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpenmetinAuthenticationApplication {

  public static void main(String[] args) {
    SpringApplication.run(OpenmetinAuthenticationApplication.class, args);
  }
}
