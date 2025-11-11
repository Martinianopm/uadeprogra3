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
  private final GeneratorPrim prim = new GeneratorPrim();
  private final GeneratorKruskal kruskal = new GeneratorKruskal();
  private final Random rng = new Random();

  public MazeService(Neo4jClient neo){ this.neo = neo; }

  /** Genera laberinto (DFS/Prim/Kruskal) y persiste nodos/relaciones en Neo4j. */
  @Transactional
  public String generateAndSave(int rows, int cols, String method, boolean weighted){
    String id = "MZ-" + UUID.randomUUID();

    // Maze
    neo.query("""
      MERGE (m:Maze {id:$id})
      SET m.rows = $r, m.cols = $c, m.weighted = $w, m.createdAt = datetime()
    """)
      .bind(id).to("id")
      .bind(rows).to("r")
      .bind(cols).to("c")
      .bind(weighted).to("w")
      .run();

    // Cells
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        String cid = cid(id, r, c);
        neo.query("""
          MERGE (c:Cell {id:$cid})
          SET c.r = $r, c.c = $c, c.weight = 1
          WITH c
          MATCH (m:Maze {id:$mid})
          MERGE (m)-[:HAS]->(c)
        """)
          .bind(cid).to("cid")
          .bind(r).to("r")
          .bind(c).to("c")
          .bind(id).to("mid")
          .run();
      }
    }

    // Elegir generador
    List<int[]> edges;
    String m = (method == null) ? "dfs" : method.toLowerCase();
    switch (m) {
      case "prim"    -> edges = prim.generateEdges(rows, cols);
      case "kruskal" -> edges = kruskal.generateEdges(rows, cols);
      default        -> edges = dfs.generateEdges(rows, cols);
    }

    // Persistir aristas (OPEN) ida y vuelta
    for (int[] e : edges) {
      String a = cid(id, e[0], e[1]), b = cid(id, e[2], e[3]);
      int cost = weighted ? (1 + rng.nextInt(9)) : 1; // 1..9 si weighted
      neo.query("""
        MATCH (u:Cell {id:$a}), (v:Cell {id:$b})
        MERGE (u)-[:OPEN {cost:$c}]->(v)
        MERGE (v)-[:OPEN {cost:$c}]->(u)
      """)
        .bind(a).to("a")
        .bind(b).to("b")
        .bind(cost).to("c")
        .run();
    }
    return id;
  }

  /** BFS: grafo no ponderado. */
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

    if (!vis.contains(t)) {
      return Map.of("path", List.of(), "visited", visited, "length", 0, "cost", 0);
    }

    List<Map<String,Integer>> path = reconstruct(prev, s, t);
    int cost = Math.max(0, path.size()-1);
    return Map.of("path", path, "visited", visited, "length", path.size(), "cost", cost);
  }

  /** Dijkstra: grafo ponderado (usa e.cost). */
  @Transactional(readOnly = true)
  public Map<String,Object> solveDijkstra(String mazeId, int sr,int sc,int tr,int tc){
    Map<String,List<int[]>> adj = loadAdj(mazeId); // int[]{r,c,cost}
    String s = key(sr,sc), t = key(tr,tc);

    final int INF = Integer.MAX_VALUE/4;
    Map<String,Integer> dist = new HashMap<>();
    Map<String,String> prev = new HashMap<>();
    PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

    // inicializar distancias para nodos que aparecen como origen
    for (String u : adj.keySet()) dist.put(u, INF);
    dist.put(s, 0);
    pq.add(s);

    Set<String> settled = new HashSet<>();
    while(!pq.isEmpty()){
      String u = pq.poll();
      if(!settled.add(u)) continue;
      if(u.equals(t)) break;

      for(int[] v : adj.getOrDefault(u, List.of())){
        String vk = key(v[0], v[1]);
        int du = dist.getOrDefault(u, INF);
        if (du >= INF) continue;
        int alt = du + v[2];
        if(alt < dist.getOrDefault(vk, INF)){
          dist.put(vk, alt);
          prev.put(vk, u);
          pq.add(vk);
        }
      }
    }

    int best = dist.getOrDefault(t, INF);
    if (best >= INF) {
      return Map.of("path", List.of(), "visited", settled.size(), "length", 0, "cost", -1);
    }

    List<Map<String,Integer>> path = reconstruct(prev, s, t);
    return Map.of("path", path, "visited", settled.size(), "length", path.size(), "cost", best);
  }

  /** Meta + edges para el front. */
  @Transactional(readOnly = true)
  public Map<String,Object> getMaze(String mazeId){
    var metaOpt = neo.query("""
        MATCH (m:Maze {id:$id}) RETURN m.rows AS rows, m.cols AS cols
      """).bind(mazeId).to("id").fetch().one();

    if (metaOpt.isEmpty()) throw new IllegalArgumentException("Maze no encontrado: " + mazeId);
    Map<String,Object> meta = metaOpt.get();
    int rows = ((Number) meta.get("rows")).intValue();
    int cols = ((Number) meta.get("cols")).intValue();

    Collection<Map<String,Object>> es = neo.query("""
        MATCH (m:Maze {id:$id})-[:HAS]->(u:Cell)-[:OPEN]->(v:Cell)
        WITH u,v WHERE u.r < v.r OR (u.r = v.r AND u.c < v.c)
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

  /** Adyacencias con costo desde Neo4j: key "r,c" -> lista de [vr,vc,cost] */
  private Map<String, List<int[]>> loadAdj(String mazeId){
    Collection<Map<String, Object>> rows = neo.query("""
      MATCH (m:Maze {id:$id})-[:HAS]->(u:Cell)-[e:OPEN]->(v:Cell)
      RETURN u.r AS ur, u.c AS uc, v.r AS vr, v.c AS vc, e.cost AS cost
    """).bind(mazeId).to("id").fetch().all();

    return rows.stream().collect(Collectors.groupingBy(
      r -> ((Number) r.get("ur")).intValue() + "," + ((Number) r.get("uc")).intValue(),
      Collectors.mapping(r -> new int[]{
        ((Number) r.get("vr")).intValue(),
        ((Number) r.get("vc")).intValue(),
        ((Number) r.get("cost")).intValue()
      }, Collectors.toList())
    ));
  }

  /** Reconstruye camino desde s hasta t usando predecesores. */
  private List<Map<String,Integer>> reconstruct(Map<String,String> prev, String s, String t){
    List<Map<String,Integer>> path = new ArrayList<>();
    if (t == null || s == null) return path;

    String x = t;
    while (x != null){
      String[] p = x.split(",");
      if (p.length != 2) break;
      path.add(Map.of("r", Integer.parseInt(p[0]), "c", Integer.parseInt(p[1])));
      if (x.equals(s)) break;
      x = prev.get(x);
    }
    // si no llegamos a 's', no hay camino válido
    if (path.isEmpty() || !keyToString(path.get(0)).equals(t) || !keyEquals(path.get(path.size()-1), s)){
      return List.of();
    }
    Collections.reverse(path);
    return path;
  }

  private boolean keyEquals(Map<String,Integer> cell, String key){
    String[] p = key.split(",");
    if (p.length != 2) return false;
    return cell.get("r") == Integer.parseInt(p[0]) && cell.get("c") == Integer.parseInt(p[1]);
  }

  private String keyToString(Map<String,Integer> cell){
    return cell.get("r") + "," + cell.get("c");
  }

  private String cid(String mazeId,int r,int c){ return mazeId+"-"+r+"-"+c; }
  private String key(int r,int c){ return r+","+c; }
}
