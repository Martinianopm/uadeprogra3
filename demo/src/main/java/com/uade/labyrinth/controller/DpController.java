package com.uade.labyrinth.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/dp")
public class DpController {
  /** Input: {"coins":[1,2,5], "amount": 11} */
  @PostMapping("/coin-change")
  public Map<String,Object> coinChange(@RequestBody Map<String,Object> req){
    @SuppressWarnings("unchecked")
    List<Number> Lc = (List<Number>) req.get("coins");
    int[] coins = Lc.stream().mapToInt(Number::intValue).toArray();
    int amount = ((Number)req.get("amount")).intValue();

    int INF = 1_000_000;
    int[] dp = new int[amount+1];
    int[] last = new int[amount+1];
    Arrays.fill(dp, INF); dp[0]=0; Arrays.fill(last,-1);

    for(int c: coins){
      for(int s=c; s<=amount; s++){
        if(dp[s-c]+1 < dp[s]){ dp[s]=dp[s-c]+1; last[s]=c; }
      }
    }
    List<Integer> comb = new ArrayList<>();
    if(dp[amount] < INF){
      for(int s=amount; s>0; s-=last[s]) comb.add(last[s]);
    }
    return Map.of("minCoins", dp[amount] < INF ? dp[amount] : -1, "combination", comb);
  }
}
