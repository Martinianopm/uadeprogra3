package com.uade.labyrinth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class StartupConstraints implements CommandLineRunner {
  private final Neo4jClient neo;
  public StartupConstraints(Neo4jClient neo){ this.neo = neo; }

  @Override public void run(String... args) {
    neo.query("CREATE CONSTRAINT maze_id IF NOT EXISTS FOR (m:Maze) REQUIRE m.id IS UNIQUE").run();
    neo.query("CREATE CONSTRAINT cell_id IF NOT EXISTS FOR (c:Cell) REQUIRE c.id IS UNIQUE").run();
  }
}
