package com.blaj.openmetin.authentication.application.common.port;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;

public interface AuthenticationAmqpEvents {

  void publish(AuthenticationCloseConnectionEvent authenticationCloseConnectionEvent);
}
