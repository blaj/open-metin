package com.blaj.openmetin.game.domain.config;

import com.blaj.openmetin.game.domain.enums.character.Empire;
import com.blaj.openmetin.game.domain.model.map.Coordinates;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class EmpireSpawnConfigs {

  private static final Map<Empire, Coordinates> SPAWN_POSITIONS =
      Map.of(
          Empire.NEUTRAL, new Coordinates(0, 0),
          Empire.SHINSOO, new Coordinates(475000, 966100),
          Empire.CHUNJO, new Coordinates(60000, 156000),
          Empire.JINNO, new Coordinates(963400, 278200));

  public static Coordinates getSpawnPosition(Empire empire) {
    return SPAWN_POSITIONS.get(empire);
  }
}
