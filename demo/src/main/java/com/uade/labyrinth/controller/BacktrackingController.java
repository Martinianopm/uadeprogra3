package com.uade.labyrinth.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/backtracking")
public class BacktrackingController {
  @PostMapping("/nqueens")
  public Map<String,Object> nqueens(@RequestBody Map<String,Object> req){
    int n = ((Number)req.getOrDefault("n",8)).intValue();
    int[] col = new int[n]; Arrays.fill(col,-1);
    boolean ok = solve(0,n,col,new boolean[n],new boolean[2*n],new boolean[2*n]);
    List<String> board = new ArrayList<>();
    if(ok){
      for(int r=0;r<n;r++){
        char[] row = new char[n]; Arrays.fill(row,'.'); row[col[r]]='Q';
        board.add(new String(row));
      }
    }
    List<Integer> sol = new ArrayList<>();
    for(int x: col) sol.add(x);
    return Map.of("solution", sol, "board", board);
  }

  private boolean solve(int r,int n,int[] col,boolean[] usedC,boolean[] d1,boolean[] d2){
    if(r==n) return true;
    for(int c=0;c<n;c++){
      int id1=r-c+n, id2=r+c;
      if(usedC[c]||d1[id1]||d2[id2]) continue;
      usedC[c]=d1[id1]=d2[id2]=true; col[r]=c;
      if(solve(r+1,n,col,usedC,d1,d2)) return true;
      usedC[c]=d1[id1]=d2[id2]=false; col[r]=-1;
    }
    return false;
  }
}
