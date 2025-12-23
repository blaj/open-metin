package com.blaj.openmetin.game.application.common.game;

public interface GameEntityVidAllocator {

  int allocate();

  void release(int vid);
}
