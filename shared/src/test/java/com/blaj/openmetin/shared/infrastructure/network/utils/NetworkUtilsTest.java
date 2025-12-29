package com.blaj.openmetin.shared.infrastructure.network.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import io.netty.channel.Channel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkUtilsTest {

  @Mock private Channel channel;

  @Test
  public void givenAnyLocalAddress_whenResolveAdvertisedAddress_thenReturnConnectionLocalAddress()
      throws UnknownHostException {
    // given
    var anyAddress = InetAddress.getByName("0.0.0.0");
    var connectionLocalAddress = InetAddress.getByName("192.168.1.10");

    // when
    var result = NetworkUtils.resolveAdvertisedAddress(anyAddress, connectionLocalAddress);

    // then
    assertThat(result).isEqualTo(connectionLocalAddress);
  }

  @Test
  public void
      givenIPv6AnyLocalAddress_whenResolveAdvertisedAddress_thenReturnConnectionLocalAddress()
          throws UnknownHostException {
    // given
    var anyAddress = InetAddress.getByName("::");
    var connectionLocalAddress = InetAddress.getByName("192.168.1.10");

    // when
    var result = NetworkUtils.resolveAdvertisedAddress(anyAddress, connectionLocalAddress);

    // then
    assertThat(result).isEqualTo(connectionLocalAddress);
  }

  @Test
  public void givenSpecificAddress_whenResolveAdvertisedAddress_thenReturnSpecificAddress()
      throws UnknownHostException {
    // given
    var specificAddress = InetAddress.getByName("10.0.0.5");
    var connectionLocalAddress = InetAddress.getByName("192.168.1.10");

    // when
    var result = NetworkUtils.resolveAdvertisedAddress(specificAddress, connectionLocalAddress);

    // then
    assertThat(result).isEqualTo(specificAddress);
  }

  @Test
  public void givenPublicIP_whenResolveAdvertisedAddress_thenReturnPublicIP()
      throws UnknownHostException {
    // given
    var publicIp = InetAddress.getByName("8.8.8.8");
    var localAddress = InetAddress.getByName("192.168.1.10");

    // when
    var result = NetworkUtils.resolveAdvertisedAddress(publicIp, localAddress);

    // then
    assertThat(result).isEqualTo(publicIp);
  }

  @Test
  public void givenIPv4Address_whenIpToInt_thenReturnIntRepresentation()
      throws UnknownHostException {
    // given
    var ip = InetAddress.getByName("192.168.1.100");

    // when
    var result = NetworkUtils.ipToInt(ip);

    // then
    assertThat(result).isEqualTo(1677830336); // 192.168.1.100 as Little Endian int
  }

  @Test
  public void givenLoopbackAddress_whenIpToInt_thenReturnCorrectInt() throws UnknownHostException {
    // given
    var ip = InetAddress.getByName("127.0.0.1");

    // when
    var result = NetworkUtils.ipToInt(ip);

    // then
    assertThat(result).isEqualTo(16777343); // 127.0.0.1 as Little Endian int
  }

  @Test
  public void givenZeroAddress_whenIpToInt_thenReturnZero() throws UnknownHostException {
    // given
    var ip = InetAddress.getByName("0.0.0.0");

    // when
    var result = NetworkUtils.ipToInt(ip);

    // then
    assertThat(result).isEqualTo(0);
  }

  @Test
  public void givenMaxIPv4Address_whenIpToInt_thenReturnNegativeOne() throws UnknownHostException {
    // given
    var ip = InetAddress.getByName("255.255.255.255");

    // when
    var result = NetworkUtils.ipToInt(ip);

    // then
    assertThat(result).isEqualTo(-1); // 0xFFFFFFFF as signed int
  }

  @Test
  public void givenIPv6Address_whenIpToInt_thenThrowException() throws UnknownHostException {
    // given
    var ipv6 = InetAddress.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> NetworkUtils.ipToInt(ipv6));

    // then
    assertThat(thrownException).hasMessage("Only IPv4 addresses are supported");
  }

  @Test
  public void givenIPv6LoopbackAddress_whenIpToInt_thenThrowException()
      throws UnknownHostException {
    // given
    var ipv6 = InetAddress.getByName("::1");

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> NetworkUtils.ipToInt(ipv6));

    // then
    assertThat(thrownException).hasMessage("Only IPv4 addresses are supported");
  }

  @Test
  public void givenChannel_whenGetLocalAddress_thenReturnLocalAddress()
      throws UnknownHostException {
    // given
    var expectedAddress = InetAddress.getByName("192.168.1.50");
    var socketAddress = new InetSocketAddress(expectedAddress, 8080);

    given(channel.localAddress()).willReturn(socketAddress);

    // when
    var result = NetworkUtils.getLocalAddress(channel);

    // then
    assertThat(result).isEqualTo(expectedAddress);
  }

  @Test
  public void givenChannelWithIPv6_whenGetLocalAddress_thenReturnIPv6Address()
      throws UnknownHostException {
    // given
    var expectedAddress = InetAddress.getByName("::1");
    var socketAddress = new InetSocketAddress(expectedAddress, 8080);

    given(channel.localAddress()).willReturn(socketAddress);

    // when
    var result = NetworkUtils.getLocalAddress(channel);

    // then
    assertThat(result).isEqualTo(expectedAddress);
  }

  @Test
  public void givenChannelWithLoopback_whenGetLocalAddress_thenReturnLoopbackAddress()
      throws UnknownHostException {
    // given
    var expectedAddress = InetAddress.getByName("127.0.0.1");
    var socketAddress = new InetSocketAddress(expectedAddress, 8080);

    given(channel.localAddress()).willReturn(socketAddress);

    // when
    var result = NetworkUtils.getLocalAddress(channel);

    // then
    assertThat(result).isEqualTo(expectedAddress);
  }

  @Test
  public void givenConversionRoundTrip_whenIpToIntAndBack_thenReturnOriginalIP()
      throws UnknownHostException {
    // given
    var originalIp = InetAddress.getByName("10.20.30.40");

    // when
    var intValue = NetworkUtils.ipToInt(originalIp);
    var bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(intValue).array();
    var convertedBack = InetAddress.getByAddress(bytes);

    // then
    assertThat(convertedBack).isEqualTo(originalIp);
  }

  @Test
  public void givenMultipleAddresses_whenIpToInt_thenEachHasUniqueValue()
      throws UnknownHostException {
    // given
    var ip1 = InetAddress.getByName("192.168.1.1");
    var ip2 = InetAddress.getByName("192.168.1.2");
    var ip3 = InetAddress.getByName("10.0.0.1");

    // when
    var int1 = NetworkUtils.ipToInt(ip1);
    var int2 = NetworkUtils.ipToInt(ip2);
    var int3 = NetworkUtils.ipToInt(ip3);

    // then
    assertThat(int1).isNotEqualTo(int2);
    assertThat(int1).isNotEqualTo(int3);
    assertThat(int2).isNotEqualTo(int3);
  }
}
