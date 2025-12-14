package com.blaj.openmetin.shared.domain.entity;

import java.io.Serializable;
import java.net.InetSocketAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginToken implements Serializable {

  private long accountId;

  private String username;

  private InetSocketAddress socketAddress;
}
