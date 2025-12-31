package com.blaj.openmetin.game.domain.model.spawn;

import java.util.List;

public record SpawnGroupCollection(long id, String name, List<Entry> entries) {

  public record Entry(long id, float probability) {}
}
