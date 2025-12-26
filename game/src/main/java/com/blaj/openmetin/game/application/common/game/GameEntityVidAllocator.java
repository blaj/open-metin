package com.blaj.openmetin.game.application.common.game;

import org.joou.UInteger;

public interface GameEntityVidAllocator {

  UInteger allocate();

  void release(UInteger vid);
}
