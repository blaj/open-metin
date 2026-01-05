package com.blaj.openmetin.game.domain.model.spawn;

import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointDirection;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SpawnPoint {
  private SpawnPointType type;
  private boolean isAggressive;
  private int x;
  private int y;
  private int rangeX;
  private int rangeY;
  private SpawnPointDirection spawnPointDirection;
  private int respawnTime;
  private List<Long> groups;
  private long monsterId;
  private MonsterGroup currentGroup;
  private short chance;
  private short maxAmount;
}
