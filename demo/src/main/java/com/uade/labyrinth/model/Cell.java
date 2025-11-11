package com.uade.labyrinth.model;

import org.springframework.data.neo4j.core.schema.*;
import java.util.*;

@Node("Cell")
public class Cell {
  @Id public String id;
  public int r, c;
  public int weight = 1;

  @Relationship(type="OPEN", direction = Relationship.Direction.OUTGOING)
  public List<Open> open = new ArrayList<>();
}
