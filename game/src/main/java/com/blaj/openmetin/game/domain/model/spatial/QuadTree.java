package com.blaj.openmetin.game.domain.model.spatial;

import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class QuadTree {

  public static final int DEFAULT_QUAD_TREE_CAPACITY = 20;

  private static final int MIN_QUAD_SIZE = 16;

  private int x;
  private int y;
  private int width;
  private int height;
  private long capacity;
  private Rectangle rectangle;
  private List<BaseGameEntity> entities;
  private boolean isSubdivided;

  private QuadTree northWestQuadTree;
  private QuadTree northEastQuadTree;
  private QuadTree southWestQuadTree;
  private QuadTree southEastQuadTree;

  public QuadTree(int x, int y, int width, int height, long capacity) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.capacity = capacity;
    this.rectangle = new Rectangle(x, y, width, height);
    this.entities = new ArrayList<>();
    this.isSubdivided = false;
  }

  public boolean insert(BaseGameEntity entity) {
    if (!rectangle.contains(entity.getPositionX(), entity.getPositionY())) {
      return false;
    }

    if (entities.size() < capacity && !isSubdivided) {
      entity.setLastPositionX(entity.getPositionX());
      entity.setLastPositionY(entity.getPositionY());
      entity.setLastQuadTree(this);

      entities.add(entity);
      return true;
    }

    if (!isSubdivided) {
      if (width > MIN_QUAD_SIZE && height > MIN_QUAD_SIZE) {
        subdivide();
      } else {
        capacity++;

        return insert(entity);
      }
    }

    return northWestQuadTree.insert(entity)
        || northEastQuadTree.insert(entity)
        || southWestQuadTree.insert(entity)
        || southEastQuadTree.insert(entity);
  }

  public boolean remove(BaseGameEntity entity) {
    if (isSubdivided) {
      return northWestQuadTree.remove(entity)
          || northEastQuadTree.remove(entity)
          || southWestQuadTree.remove(entity)
          || southEastQuadTree.remove(entity);
    }

    if (entities.remove(entity)) {
      entity.setLastQuadTree(null);

      return true;
    }

    return false;
  }

  public void queryAround(
      List<BaseGameEntity> objects, int x, int y, int radius, EntityType filter) {
    if (!circleIntersects(x, y, radius)) {
      return;
    }

    if (isSubdivided) {
      northEastQuadTree.queryAround(objects, x, y, radius, filter);
      northWestQuadTree.queryAround(objects, x, y, radius, filter);
      southEastQuadTree.queryAround(objects, x, y, radius, filter);
      southWestQuadTree.queryAround(objects, x, y, radius, filter);
    } else {
      for (var entity : entities) {
        if (filter != null && entity.getType() != filter) {
          continue;
        }

        var distanceSquared =
            Math.pow(entity.getPositionX() - x, 2) + Math.pow(entity.getPositionY() - y, 2);

        if (distanceSquared <= Math.pow(radius, 2)) {
          objects.add(entity);
        }
      }
    }
  }

  public void updatePosition(BaseGameEntity entity) {
    if (entity.getLastQuadTree() == null) {
      insert(entity);

      return;
    }

    var lastQuadTree = entity.getLastQuadTree();

    if (!lastQuadTree.getRectangle().contains(entity.getPositionX(), entity.getPositionY())) {
      lastQuadTree.remove(entity);

      insert(entity);
    }
  }

  private boolean circleIntersects(int x, int y, int radius) {
    var halfWidth = width / 2;
    var halfHeight = height / 2;
    var centerX = this.x + halfWidth;
    var centerY = this.y + halfHeight;

    var xDist = Math.abs(centerX - x);
    var yDist = Math.abs(centerY - y);

    var edges = Math.pow(xDist - halfWidth, 2) + Math.pow(yDist - halfHeight, 2);

    if (xDist > radius + halfWidth || yDist > radius + halfHeight) {
      return false;
    }

    if (xDist <= halfWidth || yDist <= halfHeight) {
      return true;
    }

    return edges <= Math.pow(radius, 2);
  }

  private void subdivide() {
    var halfWidth1 = width / 2;
    var halfHeight1 = height / 2;

    var halfWidth2 = width > 2 && width % 2 > 0 ? width / 2 + width % 2 : halfWidth1;
    var halfHeight2 = height > 2 && height % 2 > 0 ? height / 2 + height % 2 : halfHeight1;

    northWestQuadTree = new QuadTree(x, y, halfWidth1, halfHeight1, capacity);
    northEastQuadTree = new QuadTree(x, y + halfHeight1, halfWidth1, halfHeight2, capacity);
    southWestQuadTree = new QuadTree(x + halfWidth1, y, halfWidth2, halfHeight1, capacity);
    southEastQuadTree =
        new QuadTree(x + halfWidth1, y + halfHeight1, halfWidth2, halfHeight2, capacity);
    isSubdivided = true;

    for (var entity : entities) {
      entity.setLastQuadTree(null);

      var addedOnNw = northWestQuadTree.insert(entity);
      var addedOnNe = false;
      var addedOnSw = false;
      var addedOnSe = false;

      if (!addedOnNw) {
        addedOnNe = northEastQuadTree.insert(entity);
      }

      if (!addedOnNe) {
        addedOnSw = southWestQuadTree.insert(entity);
      }

      if (!addedOnSw) {
        addedOnSe = southEastQuadTree.insert(entity);
      }

      assert addedOnNw || addedOnNe || addedOnSw || addedOnSe
          : "Entity must be added to any quadrant";
    }

    entities.clear();
  }
}
