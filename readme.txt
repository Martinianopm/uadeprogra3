# TP Algoritmos – Laberinto (Java + Spring Boot + Neo4j Aura): 
Hecho por: Martiniano Peralta - Thiago Pereita - Nicolas Ferrante

Juego web + API con algoritmos de **grafos**, **greedy**, **divide y vencerás**, **programación dinámica**, **backtracking** y **branch & bound**.

---

## URLs

- **Juego web:**  
  👉 https://bug-free-cod-r46v967pxpj63pwr5-8080.app.github.dev/

---

## Cómo correr

```bash
cd /workspaces/uadeprogra3/demo
./gradlew bootRun -x test
````

El backend levanta en el puerto **8080**.
Podés verificar la conexión a Neo4j con:

```bash
curl $URL/debug/ping
```

---

## Endpoints principales

| Tipo | Endpoint                     | Descripción                                                                      |
| ---- | ---------------------------- | -------------------------------------------------------------------------------- |
| POST | `/maze/generate`             | Genera un laberinto (`method`: `dfs`, `prim`, `kruskal`, `weighted`: true/false) |
| GET  | `/maze/{mazeId}`             | Devuelve los nodos y aristas del laberinto                                       |
| POST | `/maze/solve`                | Resuelve con `algo`: `bfs` o `dijkstra`                                          |
| POST | `/greedy/activity-selection` | Selección de actividades por método greedy                                       |
| POST | `/sort/merge`                | Ordena un arreglo por MergeSort                                                  |
| POST | `/dp/coin-change`            | Calcula el mínimo de monedas (programación dinámica)                             |
| POST | `/backtracking/nqueens`      | Resuelve N-Reinas                                                                |
| POST | `/bnb/knapsack`              | Soluciona Mochila 0/1 (Branch & Bound)                                           |

---

## Ejemplos de uso

### Generar laberinto

```bash
curl -X POST $URL/maze/generate \
  -H "Content-Type: application/json" \
  -d '{"rows":15,"cols":20,"method":"dfs","weighted":false}'
```

### Resolver (BFS)

```bash
curl -X POST $URL/maze/solve \
  -H "Content-Type: application/json" \
  -d '{"mazeId":"MZ-XXXX","start":{"r":0,"c":0},"end":{"r":14,"c":19},"algo":"bfs"}'
```

### MergeSort

```bash
curl -X POST $URL/sort/merge \
  -H "Content-Type: application/json" \
  -d '{"nums":[5,2,9,1,5,6]}'
```

### Coin Change

```bash
curl -X POST $URL/dp/coin-change \
  -H "Content-Type: application/json" \
  -d '{"coins":[1,2,5],"amount":11}'
```

### N-Queens

```bash
curl -X POST $URL/backtracking/nqueens \
  -H "Content-Type: application/json" \
  -d '{"n":8}'
```

### Knapsack (Branch & Bound)

```bash
curl -X POST $URL/bnb/knapsack \
  -H "Content-Type: application/json" \
  -d '{"capacity":10,"weights":[2,3,4,5],"values":[3,4,5,8]}'
```

---

## Cómo probar el juego

1. Abrí [la web del juego](https://bug-free-cod-r46v967pxpj63pwr5-8080.app.github.dev/).
2. Clic en **Generar (DFS/Prim/Kruskal)**.
3. Movete con las **flechas**.
4. Clic en **Resolver (BFS/Dijkstra)**.