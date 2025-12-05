package com.blaj.openmetin.authentication.infrastructure.config;

import com.blaj.openmetin.shared.network.service.NettyServerService;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

  @Bean
  public SmartLifecycle nettyServerServiceSmartLifecycle(NettyServerService nettyServerService) {
    return new SmartLifecycle() {
      private volatile boolean isRunning = false;

      @Override
      public void start() {
        nettyServerService.start();
        isRunning = true;
      }

      @Override
      public void stop() {
        nettyServerService.stop();
        isRunning = false;
      }

      @Override
      public boolean isRunning() {
        return isRunning;
      }
    };
  }
}
