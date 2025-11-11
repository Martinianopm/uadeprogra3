package com.uade.labyrinth.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

  private final Neo4jClient neo;

  public DebugController(Neo4jClient neo) { this.neo = neo; }

  @GetMapping("/ping")
  @Transactional(readOnly = true)
  public Map<String,Object> ping() {
    Long count = neo.query("MATCH (n) RETURN count(n) AS c")
        .fetchAs(Long.class).one().orElse(0L);
    return Map.of("neo4j", "ok", "nodes", count);
  }
}
