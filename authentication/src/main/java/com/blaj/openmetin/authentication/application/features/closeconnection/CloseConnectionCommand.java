package com.blaj.openmetin.authentication.application.features.closeconnection;

import com.blaj.openmetin.shared.infrastructure.cqrs.Command;

public record CloseConnectionCommand(long accountId) implements Command<Void> {}
