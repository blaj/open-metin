package com.blaj.openmetin.game.domain.model.spawn;

import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointDirection;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import java.util.List;

public record SpawnPoint(
    SpawnPointType type,
    boolean isAggressive,
    int x,
    int y,
    int rangeX,
    int rangeY,
    SpawnPointDirection spawnPointDirection,
    int respawnTime,
    List<Long> groups,
    long monsterId,
    MonsterGroup currentGroup,
    short change,
    short maxAmount) {}
