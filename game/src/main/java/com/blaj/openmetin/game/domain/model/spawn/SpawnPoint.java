package com.blaj.openmetin.game.domain.model.spawn;

import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointDirection;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SpawnPoint {
  private SpawnPointType type;
  private boolean aggressive;
  private int x;
  private int y;
  private int rangeX;
  private int rangeY;
  private SpawnPointDirection direction = SpawnPointDirection.RANDOM;
  private int respawnTime;
  private List<Integer> groups = new ArrayList<>();
  private long monsterId;
  private MonsterGroup currentGroup;
  private short chance;
  private short maxAmount;
}
