package com.uade.labyrinth.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/sort")
public class SortController {
  @PostMapping("/merge")
  public Map<String,Object> merge(@RequestBody Map<String,Object> req){
    @SuppressWarnings("unchecked")
    List<Number> nums = (List<Number>) req.get("nums");
    int[] a = nums.stream().mapToInt(Number::intValue).toArray();
    mergeSort(a,0,a.length-1,new int[a.length]);
    List<Integer> out = new ArrayList<>();
    for(int x:a) out.add(x);
    return Map.of("sorted", out);
  }

  private void mergeSort(int[] a,int l,int r,int[] tmp){
    if(l>=r) return;
    int m=(l+r)/2;
    mergeSort(a,l,m,tmp); mergeSort(a,m+1,r,tmp);
    int i=l,j=m+1,k=l;
    while(i<=m || j<=r){
      if(j>r || (i<=m && a[i]<=a[j])) tmp[k++]=a[i++]; else tmp[k++]=a[j++];
    }
    for(i=l;i<=r;i++) a[i]=tmp[i];
  }
}
