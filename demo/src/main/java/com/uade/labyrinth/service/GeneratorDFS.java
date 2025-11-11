package com.uade.labyrinth.service;

import java.util.*;

public class GeneratorDFS {
  private final Random rng = new Random();

  /** Devuelve aristas abiertas como [r1,c1,r2,c2] */
  public List<int[]> generateEdges(int rows, int cols){
    boolean[][] vis = new boolean[rows][cols];
    List<int[]> edges = new ArrayList<>();
    Deque<int[]> st = new ArrayDeque<>();
    st.push(new int[]{0,0});
    vis[0][0] = true;

    int[][] D = {{1,0},{-1,0},{0,1},{0,-1}};
    while(!st.isEmpty()){
      int[] u = st.peek();
      List<int[]> neigh = new ArrayList<>();
      for (int[] d: D){
        int nr=u[0]+d[0], nc=u[1]+d[1];
        if(0<=nr && nr<rows && 0<=nc && nc<cols && !vis[nr][nc]) neigh.add(new int[]{nr,nc});
      }
      if(neigh.isEmpty()){ st.pop(); continue; }
      int[] v = neigh.get(rng.nextInt(neigh.size()));
      vis[v[0]][v[1]] = true;
      edges.add(new int[]{u[0],u[1],v[0],v[1]});
      st.push(v);
    }
    return edges;
  }
}
