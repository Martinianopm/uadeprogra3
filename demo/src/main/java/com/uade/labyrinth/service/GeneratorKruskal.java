package com.uade.labyrinth.service;

import java.util.*;

public class GeneratorKruskal {
  private static class DSU{
    int[] p, r;
    DSU(int n){ p=new int[n]; r=new int[n]; for(int i=0;i<n;i++) p[i]=i; }
    int f(int x){ return p[x]==x?x:(p[x]=f(p[x])); }
    boolean u(int a,int b){
      a=f(a); b=f(b); if(a==b) return false;
      if(r[a]<r[b]){int t=a;a=b;b=t;}
      p[b]=a; if(r[a]==r[b]) r[a]++; return true;
    }
  }

  /** Kruskal en grilla con pesos aleatorios sobre aristas entre vecinos. */
  public List<int[]> generateEdges(int rows, int cols){
    int N = rows*cols;
    DSU dsu = new DSU(N);
    List<int[]> all = new ArrayList<>(); // [w,r1,c1,r2,c2]
    Random rng = new Random();

    int[][] D={{1,0},{0,1}}; // solo abajo y derecha para no duplicar
    for(int r=0;r<rows;r++){
      for(int c=0;c<cols;c++){
        for(int[] d:D){
          int nr=r+d[0], nc=c+d[1];
          if(nr<rows && nc<cols){
            all.add(new int[]{rng.nextInt(1000), r,c,nr,nc});
          }
        }
      }
    }
    all.sort(Comparator.comparingInt(a->a[0]));

    List<int[]> mst = new ArrayList<>();
    for(int[] e: all){
      int a = e[1]*cols + e[2];
      int b = e[3]*cols + e[4];
      if(dsu.u(a,b)){
        mst.add(new int[]{e[1],e[2],e[3],e[4]});
      }
    }
    return mst;
  }
}
