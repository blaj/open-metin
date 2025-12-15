package com.blaj.openmetin.shared.infrastructure.network.utils;

import io.netty.channel.Channel;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NetworkUtils {

  public static InetAddress resolveAdvertisedAddress(
      InetAddress mapHostAddress, InetAddress connectionLocalAddress) {

    if (mapHostAddress.isAnyLocalAddress()) {
      return connectionLocalAddress;
    }

    return mapHostAddress;
  }

  public static int ipToInt(InetAddress address) {
    if (!(address instanceof Inet4Address)) {
      throw new IllegalArgumentException("Only IPv4 addresses are supported");
    }

    var bytes = address.getAddress();
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static InetAddress getLocalAddress(Channel channel) {
    var localAddress = (InetSocketAddress) channel.localAddress();
    return localAddress.getAddress();
  }
}
