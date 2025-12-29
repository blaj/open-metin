package com.blaj.openmetin.game.infrastructure.scheduling;

import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Generated // TODO: write tests!
@Slf4j
@Service
@RequiredArgsConstructor
public class QuadTreeMetricsService {

  private final GameWorldService gameWorldService;
  private final MeterRegistry meterRegistry;

  private final Map<String, QuadTreeStats> statsMap = new ConcurrentHashMap<>();
  private final Set<String> registeredMaps = ConcurrentHashMap.newKeySet();

  @Scheduled(fixedRate = 5000)
  public void exportQuadTreeMetrics() {
    gameWorldService
        .getMaps()
        .forEach(
            (mapName, map) -> {
              var stats = statsMap.computeIfAbsent(mapName, k -> new QuadTreeStats());

              if (registeredMaps.add(mapName)) {
                registerGaugesForMap(mapName, stats);
                log.info("Registered gauges for map: {}", mapName);
              }

              stats.reset();
              traverseQuadTree(map.getQuadTree(), 0, stats);

              if (stats.getLeafNodes().get() > 0) {
                double avg = (double) stats.getTotalEntities().get() / stats.getLeafNodes().get();
                stats.getAvgEntitiesPerLeaf().set(avg);
              }

              log.debug(
                  "Map '{}': {} entities, depth {}",
                  mapName,
                  stats.getTotalEntities().get(),
                  stats.getMaxDepth().get());
            });
  }

  private void registerGaugesForMap(String mapName, QuadTreeStats stats) {
    var tags = Tags.of("map", mapName);

    Gauge.builder("quadtree.entities.total", stats.getTotalEntities(), AtomicInteger::get)
        .tags(tags)
        .description("Total entities on map")
        .register(meterRegistry);

    Gauge.builder("quadtree.depth.max", stats.getMaxDepth(), AtomicInteger::get)
        .tags(tags)
        .description("Maximum depth of QuadTree")
        .register(meterRegistry);

    Gauge.builder("quadtree.nodes.total", stats.getTotalNodes(), AtomicInteger::get)
        .tags(tags)
        .description("Total nodes in QuadTree")
        .register(meterRegistry);

    Gauge.builder("quadtree.nodes.subdivided", stats.getSubdividedNodes(), AtomicInteger::get)
        .tags(tags)
        .description("Number of subdivided nodes")
        .register(meterRegistry);

    Gauge.builder("quadtree.nodes.leaf", stats.getLeafNodes(), AtomicInteger::get)
        .tags(tags)
        .description("Number of leaf nodes")
        .register(meterRegistry);

    Gauge.builder(
            "quadtree.entities.per.leaf.avg", stats.getAvgEntitiesPerLeaf(), AtomicReference::get)
        .tags(tags)
        .description("Average entities per leaf node")
        .register(meterRegistry);
  }

  private void traverseQuadTree(QuadTree node, int depth, QuadTreeStats stats) {
    stats.getMaxDepth().updateAndGet(current -> Math.max(current, depth));
    stats.getTotalNodes().incrementAndGet();

    if (node.isSubdivided()) {
      stats.getSubdividedNodes().incrementAndGet();
      traverseQuadTree(node.getNorthWestQuadTree(), depth + 1, stats);
      traverseQuadTree(node.getNorthEastQuadTree(), depth + 1, stats);
      traverseQuadTree(node.getSouthWestQuadTree(), depth + 1, stats);
      traverseQuadTree(node.getSouthEastQuadTree(), depth + 1, stats);
    } else {
      stats.getLeafNodes().incrementAndGet();
      var entityCount = node.getEntities().size();
      stats.getTotalEntities().addAndGet(entityCount);

      node.getEntities()
          .forEach(
              entity -> {
                String type = entity.getType().name();
                stats.getEntitiesByType().merge(type, 1, Integer::sum);
              });
    }
  }

  @Generated // TODO: write tests!
  @Getter
  private static class QuadTreeStats {
    private final AtomicInteger totalEntities = new AtomicInteger(0);
    private final AtomicInteger maxDepth = new AtomicInteger(0);
    private final AtomicInteger totalNodes = new AtomicInteger(0);
    private final AtomicInteger subdividedNodes = new AtomicInteger(0);
    private final AtomicInteger leafNodes = new AtomicInteger(0);
    private final AtomicReference<Double> avgEntitiesPerLeaf = new AtomicReference<>(0.0);
    private final Map<String, Integer> entitiesByType = new ConcurrentHashMap<>();

    public void reset() {
      totalEntities.set(0);
      maxDepth.set(0);
      totalNodes.set(0);
      subdividedNodes.set(0);
      leafNodes.set(0);
      avgEntitiesPerLeaf.set(0.0);
      entitiesByType.clear();
    }
  }
}
