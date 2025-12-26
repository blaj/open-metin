package com.blaj.openmetin.authentication.application.features.login;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import java.net.InetSocketAddress;
import org.joou.UInteger;

public record LoginCommand(
    String username,
    String password,
    UInteger[] encryptKeys,
    long sessionId,
    InetSocketAddress socketAddress)
    implements Command<Void> {}
