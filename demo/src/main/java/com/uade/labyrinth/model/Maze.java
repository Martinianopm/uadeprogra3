package com.uade.labyrinth.model;

import org.springframework.data.neo4j.core.schema.*;
import java.time.Instant;
import java.util.*;

@Node("Maze")
public class Maze {
  @Id public String id;
  public int rows, cols;
  public boolean weighted;
  public Instant createdAt = Instant.now();

  @Relationship(type="HAS")
  public List<Cell> cells = new ArrayList<>();
}
