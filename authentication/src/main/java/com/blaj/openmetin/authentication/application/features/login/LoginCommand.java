package com.blaj.openmetin.authentication.application.features.login;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;
import java.net.InetSocketAddress;

public record LoginCommand(
    String username,
    String password,
    long[] encryptKeys,
    long sessionId,
    InetSocketAddress socketAddress)
    implements Command<Void> {}
