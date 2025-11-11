package com.uade.labyrinth.model;

import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
public class Open {
  @Id @GeneratedValue public Long id;
  public int cost = 1;
  @TargetNode public Cell to;
}
