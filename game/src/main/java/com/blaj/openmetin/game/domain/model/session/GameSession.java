package com.blaj.openmetin.game.domain.model.session;

import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.shared.common.model.Session;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSession extends Session {

  private GameCharacterEntity gameCharacterEntity;

  public GameSession(long id, Channel channel) {
    super(id, channel);
  }
}
