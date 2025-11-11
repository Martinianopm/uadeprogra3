package com.uade.labyrinth.service;

import java.util.*;

public class GeneratorPrim {
  private final Random rng = new Random();

  /** Randomized Prim para grilla. Retorna aristas [r1,c1,r2,c2]. */
  public List<int[]> generateEdges(int rows, int cols){
    boolean[][] inTree = new boolean[rows][cols];
    List<int[]> edges = new ArrayList<>();
    List<int[]> frontier = new ArrayList<>();

    int r=0,c=0;
    inTree[r][c]=true;
    addFrontier(r,c,rows,cols,inTree,frontier);

    while(!frontier.isEmpty()){
      int[] f = frontier.remove(rng.nextInt(frontier.size())); // [nr,nc,pr,pc]
      int nr=f[0], nc=f[1], pr=f[2], pc=f[3];
      if(inTree[nr][nc]) continue;
      inTree[nr][nc]=true;
      edges.add(new int[]{pr,pc,nr,nc});
      addFrontier(nr,nc,rows,cols,inTree,frontier);
    }
    return edges;
  }

  private void addFrontier(int r,int c,int R,int C,boolean[][] in,List<int[]> F){
    int[][] D={{1,0},{-1,0},{0,1},{0,-1}};
    for(int[] d:D){
      int nr=r+d[0], nc=c+d[1];
      if(0<=nr && nr<R && 0<=nc && nc<C && !in[nr][nc]){
        F.add(new int[]{nr,nc,r,c});
      }
    }
  }
}
