package com.blaj.openmetin.game.infrastructure.scheduling;

import com.blaj.openmetin.game.application.features.serverstatus.ServerStatusCommand;
import com.blaj.openmetin.game.infrastructure.properties.ChannelProperties;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackgroundJobSchedulingService {

  private final ChannelProperties channelProperties;
  private final Mediator mediator;

  @Scheduled(fixedDelay = 60_000)
  public void updateServerStatus() {
    mediator.send(new ServerStatusCommand(channelProperties.channelIndex()));
  }
}
