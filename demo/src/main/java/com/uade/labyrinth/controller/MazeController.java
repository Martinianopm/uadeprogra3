package com.uade.labyrinth.controller;

import com.uade.labyrinth.service.MazeService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/maze")
public class MazeController {

  private final MazeService svc;

  public MazeController(MazeService svc){
    this.svc = svc;
  }

  @PostMapping("/generate")
  public Map<String,Object> generate(@RequestBody Map<String,Object> req){
    int rows = ((Number)req.getOrDefault("rows",20)).intValue();
    int cols = ((Number)req.getOrDefault("cols",20)).intValue();
    String method = (String) req.getOrDefault("method","dfs");
    String id = svc.generateAndSave(rows, cols, method);
    return Map.of("mazeId", id, "rows", rows, "cols", cols, "method", method);
  }

  @PostMapping("/solve")
  public Map<String,Object> solve(@RequestBody Map<String,Object> req){
    String id = (String) req.get("mazeId");
    @SuppressWarnings("unchecked")
    Map<String,Number> s = (Map<String,Number>) req.get("start");
    @SuppressWarnings("unchecked")
    Map<String,Number> t = (Map<String,Number>) req.get("end");
    String algo = (String) req.getOrDefault("algo","bfs");
    if(!"bfs".equalsIgnoreCase(algo))
      throw new IllegalArgumentException("Por ahora solo bfs");

    return svc.solveBfs(
      id,
      s.get("r").intValue(), s.get("c").intValue(),
      t.get("r").intValue(), t.get("c").intValue()
    );
  }

  @GetMapping("/{mazeId}")
  public Map<String,Object> get(@PathVariable String mazeId){
    return svc.getMaze(mazeId);
  }
}
