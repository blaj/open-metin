package com.blaj.openmetin.game.domain.model.map;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Map {

  public static final int MAP_UNIT = 25600;
  public static final int SPAWN_POSITION_MULTIPLIER = 100;
  public static final int SPAWN_BASE_OFFSET = 5;
  public static final int SPAWN_ROTATION_SLICE_DEGREES = 45;

  private String name;
  private Coordinates coordinates;
  private int width;
  private int height;
  @Setter private MapAttributeSet mapAttributeSet;

  private final TownCoordinates townCoordinates;
  private final QuadTree quadTree;
  private final List<BaseGameEntity> entities;
  private final Queue<BaseGameEntity> pendingSpawns;
  private final Queue<BaseGameEntity> pendingRemovals;
  private final List<SpawnPoint> spawnPoints;

  public Map(
      String name,
      Coordinates coordinates,
      int width,
      int height,
      TownCoordinates townCoordinates) {
    this.name = name;
    this.coordinates = coordinates;
    this.width = width;
    this.height = height;
    this.townCoordinates =
        Optional.ofNullable(townCoordinates)
            .map(
                tc ->
                    new TownCoordinates(
                        coordinates.add(tc.jinno().multiply(SPAWN_POSITION_MULTIPLIER)),
                        coordinates.add(tc.chunjo().multiply(SPAWN_POSITION_MULTIPLIER)),
                        coordinates.add(tc.shinsoo().multiply(SPAWN_POSITION_MULTIPLIER)),
                        coordinates.add(tc.common().multiply(SPAWN_POSITION_MULTIPLIER))))
            .orElse(null);
    this.quadTree =
        new QuadTree(
            coordinates.x(),
            coordinates.y(),
            width * MAP_UNIT,
            height * MAP_UNIT,
            QuadTree.DEFAULT_QUAD_TREE_CAPACITY);
    this.entities = new CopyOnWriteArrayList<>();
    this.pendingSpawns = new ConcurrentLinkedQueue<>();
    this.pendingRemovals = new ConcurrentLinkedQueue<>();
    this.spawnPoints = new ArrayList<>();
  }

  public int getUnitX() {
    return coordinates.x() / MAP_UNIT;
  }

  public int getUnitY() {
    return coordinates.y() / MAP_UNIT;
  }

  public boolean hasAnyMapAttributeOnCoordinates(
      Coordinates coordinates, EnumSet<MapAttribute> mapAttributes) {
    return Optional.ofNullable(mapAttributeSet)
        .map(mas -> mas.hasAnyAttribute(coordinates, mapAttributes))
        .orElse(false);
  }

  public boolean isPositionInside(Coordinates coordinates) {
    return isPositionInside(coordinates.x(), coordinates.y());
  }

  public boolean isPositionInside(int x, int y) {
    return x >= coordinates.x()
        && x < coordinates.x() + width * MAP_UNIT
        && y >= coordinates.y()
        && y < coordinates.y() + height * MAP_UNIT;
  }

  public boolean hasAttributeOnStraightPath(
      Coordinates startCoordinates, Coordinates endCoordinates, EnumSet<MapAttribute> flags) {
    int deltaX = endCoordinates.x() - startCoordinates.x();
    int deltaY = endCoordinates.y() - startCoordinates.y();

    if (deltaX == 0 && deltaY == 0) {
      return hasAnyMapAttributeOnCoordinates(startCoordinates, flags);
    }

    final int samples = 100;
    for (int i = 1; i <= samples; i++) {
      var progress = (double) i / samples;
      int sampleX = startCoordinates.x() + (int) Math.round(deltaX * progress);
      int sampleY = startCoordinates.y() + (int) Math.round(deltaY * progress);

      if (sampleX < 0 || sampleY < 0) {
        continue;
      }

      var sampleCoordinates = new Coordinates(sampleX, sampleY);
      if (hasAnyMapAttributeOnCoordinates(sampleCoordinates, flags)) {
        return true;
      }
    }

    return false;
  }
}
