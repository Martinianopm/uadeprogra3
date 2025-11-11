package com.uade.labyrinth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MazeService {
  private final Neo4jClient neo;
  private final GeneratorDFS dfs = new GeneratorDFS();

  public MazeService(Neo4jClient neo){ this.neo = neo; }

  /** Genera laberinto (DFS) y persiste nodos/relaciones en Neo4j. */
  @Transactional
  public String generateAndSave(int rows, int cols, String method){
    String id = "MZ-" + UUID.randomUUID();

    // Maze
    neo.query("""
      MERGE (m:Maze {id:$id})
      SET m.rows=$r, m.cols=$c, m.weighted=false, m.createdAt=datetime()
      """).bind(id).to("id")
         .bind(rows).to("r")
         .bind(cols).to("c")
         .run();

    // Cells
    for(int r=0;r<rows;r++){
      for(int c=0;c<cols;c++){
        String cid = cid(id,r,c);
        neo.query("""
          MERGE (c:Cell {id:$cid}) SET c.r=$r, c.c=$c, c.weight=1
          WITH c MATCH (m:Maze {id:$mid}) MERGE (m)-[:HAS]->(c)
        """).bind(cid).to("cid")
           .bind(r).to("r")
           .bind(c).to("c")
           .bind(id).to("mid")
           .run();
      }
    }

    // Edges (OPEN) ida y vuelta
    var edges = dfs.generateEdges(rows, cols); // por ahora solo dfs
    for (int[] e: edges){
      String a = cid(id,e[0],e[1]), b = cid(id,e[2],e[3]);
      neo.query("""
        MATCH (u:Cell {id:$a}), (v:Cell {id:$b})
        MERGE (u)-[:OPEN {cost:1}]->(v)
        MERGE (v)-[:OPEN {cost:1}]->(u)
      """).bind(a).to("a")
         .bind(b).to("b")
         .run();
    }
    return id;
  }

  /** Resuelve con BFS en grafo no ponderado almacenado en Neo4j. */
  @Transactional(readOnly = true)
  public Map<String,Object> solveBfs(String mazeId, int sr,int sc,int tr,int tc){
    Map<String,List<int[]>> adj = loadAdj(mazeId); // key="r,c" -> [{r,c,cost}]
    String s = key(sr,sc), t = key(tr,tc);

    Deque<String> q = new ArrayDeque<>();
    Map<String,String> prev = new HashMap<>();
    Set<String> vis = new HashSet<>();
    q.add(s); vis.add(s);
    int visited = 0;

    while(!q.isEmpty()){
      String u = q.poll();
      visited++;
      if(u.equals(t)) break;
      for(int[] v : adj.getOrDefault(u, List.of())){
        String vk = key(v[0],v[1]);
        if(vis.add(vk)){ prev.put(vk,u); q.add(vk); }
      }
    }

    List<Map<String,Integer>> path = new ArrayList<>();
    if(!vis.contains(t)) return Map.of("path", path, "visited", visited, "length", 0, "cost", 0);

    for(String x=t; x!=null; x=prev.get(x)){
      String[] p = x.split(",");
      path.add(Map.of("r", Integer.parseInt(p[0]), "c", Integer.parseInt(p[1])));
    }
    Collections.reverse(path);
    return Map.of("path", path, "visited", visited, "length", path.size(), "cost", path.size()-1);
  }

  /** Devuelve meta + edges del laberinto para render en el front. */
  @Transactional(readOnly = true)
  public Map<String,Object> getMaze(String mazeId){
    // meta (rows/cols)
    var metaOpt = neo.query("""
        MATCH (m:Maze {id:$id})
        RETURN m.rows AS rows, m.cols AS cols
      """).bind(mazeId).to("id").fetch().one();

    if (metaOpt.isEmpty()) throw new IllegalArgumentException("Maze no encontrado: " + mazeId);
    Map<String,Object> meta = metaOpt.get();
    int rows = ((Number) meta.get("rows")).intValue();
    int cols = ((Number) meta.get("cols")).intValue();

    // edges únicos (grafo es bidireccional: filtramos un lado)
    Collection<Map<String,Object>> es = neo.query("""
        MATCH (m:Maze {id:$id})-[:HAS]->(u:Cell)-[:OPEN]->(v:Cell)
        WITH u,v
        WHERE u.r < v.r OR (u.r = v.r AND u.c < v.c)
        RETURN u.r AS r1, u.c AS c1, v.r AS r2, v.c AS c2
      """).bind(mazeId).to("id").fetch().all();

    List<Map<String,Integer>> edges = new ArrayList<>();
    for (var r : es){
      edges.add(Map.of(
        "r1", ((Number)r.get("r1")).intValue(),
        "c1", ((Number)r.get("c1")).intValue(),
        "r2", ((Number)r.get("r2")).intValue(),
        "c2", ((Number)r.get("c2")).intValue()
      ));
    }
    return Map.of("mazeId", mazeId, "rows", rows, "cols", cols, "edges", edges);
  }

  /** Carga adyacencias OPEN desde Neo4j y devuelve listas por nodo origen. */
  private Map<String, List<int[]>> loadAdj(String mazeId){
    // En algunas versiones, all() devuelve Collection<Map<String,Object>>
    Collection<Map<String, Object>> rows = neo.query("""
      MATCH (m:Maze {id:$id})-[:HAS]->(u:Cell)-[e:OPEN]->(v:Cell)
      RETURN u.r AS ur, u.c AS uc, v.r AS vr, v.c AS vc, e.cost AS cost
    """).bind(mazeId).to("id")
      .fetch().all();

    return rows.stream().collect(Collectors.groupingBy(
      r -> ((Number) r.get("ur")).intValue() + "," + ((Number) r.get("uc")).intValue(),
      Collectors.mapping(r -> new int[]{
        ((Number) r.get("vr")).intValue(),
        ((Number) r.get("vc")).intValue(),
        ((Number) r.get("cost")).intValue()
      }, Collectors.toList())
    ));
  }

  private String cid(String mazeId,int r,int c){ return mazeId+"-"+r+"-"+c; }
  private String key(int r,int c){ return r+","+c; }
}
