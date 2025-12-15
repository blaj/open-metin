package com.blaj.openmetin.shared.application.common.config;

import java.net.InetAddress;

public interface TcpConfig {

  InetAddress host();

  int port();

  int idleSeconds();
}
