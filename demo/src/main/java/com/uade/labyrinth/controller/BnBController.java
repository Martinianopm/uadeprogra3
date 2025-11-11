package com.uade.labyrinth.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/bnb")
public class BnBController {

  /** Nodo para Branch & Bound del 0/1 Knapsack */
  static class Node {
    int level;          // índice del último ítem decidido
    int value;          // valor acumulado
    int weight;         // peso acumulado
    double bound;       // cota superior
    BitSet take;        // qué ítems tomamos
    Node(int n){ this.take = new BitSet(n); }
  }

  /** Calcula la cota superior (bound) usando fraccionamiento greedy */
  private double computeBound(Node u, int W, int[] w, int[] v, Integer[] idx, int n){
    if (u.weight > W) return 0.0;
    double val = u.value;
    int wt = u.weight;
    int i = u.level + 1;

    // agregar ítems completos mientras entren
    while (i < n && wt + w[idx[i]] <= W) {
      wt += w[idx[i]];
      val += v[idx[i]];
      i++;
    }
    // agregar fracción del siguiente (relación v/w)
    if (i < n) {
      val += (double)(W - wt) * v[idx[i]] / w[idx[i]];
    }
    return val;
  }

  /** Input: {"capacity":10,"weights":[2,3,4,5],"values":[3,4,5,8]} */
  @PostMapping("/knapsack")
  public Map<String,Object> knapsack(@RequestBody Map<String,Object> req){
    int W = ((Number) req.get("capacity")).intValue();
    @SuppressWarnings("unchecked")
    List<Number> Lw = (List<Number>) req.get("weights");
    @SuppressWarnings("unchecked")
    List<Number> Lv = (List<Number>) req.get("values");

    int n = Lw.size();
    int[] w = Lw.stream().mapToInt(Number::intValue).toArray();
    int[] v = Lv.stream().mapToInt(Number::intValue).toArray();

    // ordenar índices por ratio valor/peso descendente
    Integer[] idx = new Integer[n];
    for (int i=0;i<n;i++) idx[i]=i;
    Arrays.sort(idx, Comparator.comparingDouble(i -> -1.0 * v[i] / w[i]));

    // PQ max-heap por bound
    PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(x -> -x.bound));

    Node best = new Node(n);
    best.value = 0;

    Node root = new Node(n);
    root.level = -1; root.value = 0; root.weight = 0;
    root.bound = computeBound(root, W, w, v, idx, n);
    pq.add(root);

    while (!pq.isEmpty()){
      Node u = pq.poll();

      if (u.bound <= best.value) continue;     // poda por cota
      if (u.level == n-1) continue;            // sin más ítems

      int nextLevel = u.level + 1;
      int it = idx[nextLevel];

      // 1) Tomar el ítem
      Node take = new Node(n);
      take.level  = nextLevel;
      take.weight = u.weight + w[it];
      take.value  = u.value + v[it];
      take.take   = (BitSet) u.take.clone();
      take.take.set(it);
      take.bound  = computeBound(take, W, w, v, idx, n);

      if (take.weight <= W && take.value > best.value) best = take;
      if (take.bound > best.value) pq.add(take);

      // 2) No tomar el ítem
      Node skip = new Node(n);
      skip.level  = nextLevel;
      skip.weight = u.weight;
      skip.value  = u.value;
      skip.take   = (BitSet) u.take.clone();
      skip.bound  = computeBound(skip, W, w, v, idx, n);

      if (skip.bound > best.value) pq.add(skip);
    }

    List<Integer> chosen = new ArrayList<>();
    int tw=0, tv=0;
    for (int i=0;i<n;i++){
      if (best.take.get(i)) {
        chosen.add(i);
        tw += w[i];
        tv += v[i];
      }
    }

    return Map.of(
      "bestValue", tv,
      "weight", tw,
      "chosenIndexes", chosen
    );
  }
}
