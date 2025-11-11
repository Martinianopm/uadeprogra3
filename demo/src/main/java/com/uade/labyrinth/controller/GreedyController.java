package com.uade.labyrinth.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/greedy")
public class GreedyController {
  /** Input: {"activities":[{"s":1,"f":3},{"s":2,"f":5}, ...]} */
  @PostMapping("/activity-selection")
  public Map<String,Object> select(@RequestBody Map<String,Object> req){
    @SuppressWarnings("unchecked")
    List<Map<String,Number>> acts = (List<Map<String,Number>>) req.get("activities");
    List<int[]> arr = new ArrayList<>();
    for (int i=0;i<acts.size();i++){
      int s = acts.get(i).get("s").intValue();
      int f = acts.get(i).get("f").intValue();
      arr.add(new int[]{s,f,i});
    }
    arr.sort(Comparator.comparingInt(a->a[1])); // por fin
    List<Integer> pick = new ArrayList<>();
    int lastEnd = Integer.MIN_VALUE;
    for (int[] a: arr){
      if (a[0] >= lastEnd){ pick.add(a[2]); lastEnd = a[1]; }
    }
    return Map.of("chosenIndexes", pick, "count", pick.size());
  }
}
