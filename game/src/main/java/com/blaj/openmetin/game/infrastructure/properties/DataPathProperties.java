package com.blaj.openmetin.game.infrastructure.properties;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openmetin.data")
public record DataPathProperties(Path baseDirectory) {

  public Path atlasInfoFile() {
    return mapsDirectory().resolve("atlasinfo.txt");
  }

  public Path mapsDirectory() {
    return baseDirectory.resolve("maps");
  }

  public Path serverAttrFile(String mapName) {
    return mapsDirectory().resolve(mapName).resolve("server_attr");
  }

  public Path townFile(String mapName) {
    return mapsDirectory().resolve(mapName).resolve("town.txt");
  }

  public Path spawnRegenFile(String mapName) {
    return mapsDirectory().resolve(mapName).resolve("regen.txt");
  }

  public Path spawnNpcFile(String mapName) {
    return mapsDirectory().resolve(mapName).resolve("npc.txt");
  }

  public Path spawnStoneFile(String mapName) {
    return mapsDirectory().resolve(mapName).resolve("stone.txt");
  }

  public Path spawnBossFile(String mapName) {
    return mapsDirectory().resolve(mapName).resolve("boss.txt");
  }

  public Path groupFile() {
    return baseDirectory.resolve("group.txt");
  }

  public Path groupCollectionFile() {
    return baseDirectory.resolve("group_group.txt");
  }
}
