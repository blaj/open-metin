package com.blaj.openmetin.game.domain.enums.entity;

import lombok.Getter;

@Getter
public enum AiFlag {
  AGGRESSIVE(1 << 0), // 1
  NO_MOVE(1 << 1), // 2
  COWARD(1 << 2), // 4
  NO_ATTACK_SHINSOO(1 << 3), // 8
  NO_ATTACK_CHUNJO(1 << 4), // 16
  NO_ATTACK_JINNO(1 << 5), // 32
  ATTACK_MOB(1 << 6), // 64
  BERSERK(1 << 7), // 128
  STONE_SKIN(1 << 8), // 256
  GOD_SPEED(1 << 9), // 512
  DEATH_BLOW(1 << 10), // 1024
  REVIVE(1 << 11); // 2048

  private final int value;

  AiFlag(int value) {
    this.value = value;
  }
}
