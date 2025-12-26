package com.blaj.openmetin.shared.application.common.config;

import java.net.InetAddress;
import org.joou.UShort;

public interface TcpConfig {

  InetAddress host();

  UShort port();

  UShort idleSeconds();
}
