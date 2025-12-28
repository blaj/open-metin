package com.blaj.openmetin.game.domain.model.spatial;

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
  private List<BaseGameEntity> entities = new ArrayList<>();

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
  }
}
