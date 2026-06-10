# Array and Operations — Codeforces 498C

**Problema:** Array and Operations

**Link:** <https://codeforces.com/problemset/problem/498/C>

**Grupo D — Trabalho Prático 3**

**Disciplina:** Resolução de Problemas com Grafos

## Integrantes

- Bianca Oriá
- Guilherme Garcia
- Pedro Angelus

---

## Linguagem

Java (JDK 8 ou superior)

---

## Como executar

### Pré-requisitos

- JDK 8 ou superior instalado

### Compilação

```bash
javac src/Main.java
```

### Execução

**Linux/macOS:**
```bash
java -cp src Main < dados/entrada_do_problema.txt
```

**Windows (cmd):**
```bash
java -cp src Main < dados\entrada_do_problema.txt
```

**Windows (PowerShell):**
```powershell
Get-Content dados\entrada_do_problema.txt | java -cp src Main
```

---

## Descrição do Problema

Dado um array de `n` inteiros e `m` pares de índices `(i, j)`, onde `i` é ímpar e `j` é par, deve-se construir um array de `n` números primos (não necessariamente distintos). A operação permitida é: para cada par `(i, j)` fornecido, o MDC entre `a[i]` e `a[j]` pode substituir um primo em ambas as posições (ou seja, pode-se dividir `a[i]` e `a[j]` por um mesmo primo `p` comum a ambos). O objetivo é **maximizar a soma dos expoentes** de todos os fatores primos "extraídos" via essas operações.

Em outras palavras: para cada primo `p` e cada par compatível `(i, j)`, pode-se transferir unidades de `p` de índices pares para índices ímpares, respeitando quantas vezes `p` divide cada valor. A resposta é o máximo total de transferências viáveis somado sobre todos os primos.

---

## Modelagem como Rede de Fluxo

O problema é resolvido **independentemente para cada primo `p`** que aparece na fatoração de algum elemento do array.

### Vértices

| Vértice | Significado |
|---|---|
| `s` (fonte, índice 0) | Origem do fluxo |
| `j` (índice par do array) | Representa um elemento de índice par |
| `i` (índice ímpar do array) | Representa um elemento de índice ímpar |
| `t` (sorvedouro, índice `n+1`) | Destino do fluxo |

### Arestas e Capacidades

Para cada primo `p`:

1. **`s → j`** (para todo índice par `j` que contém `p`): capacidade = expoente de `p` em `a[j]`, ou seja, quantas vezes `p` divide `a[j]`. Representa o "estoque" disponível de `p` no lado par.

2. **`j → i`** (para cada par `(i, j)` fornecido, onde `i` é ímpar e `j` é par): capacidade = expoente de `p` em `a[j]`. Representa a permissão de transferir `p` daquele índice par para o ímpar.

3. **`i → t`** (para todo índice ímpar `i` que contém `p`): capacidade = expoente de `p` em `a[i]`. Representa o "limite de absorção" do lado ímpar.

### Origem e Sorvedouro

- **Origem `s`**: injeta o fluxo disponível de cada primo `p` nos índices pares. Uma unidade de fluxo representa uma ocorrência de `p` passível de ser "aproveitada".
- **Sorvedouro `t`**: coleta o fluxo absorvido pelos índices ímpares. O valor máximo de fluxo que chega em `t` representa o total de fatores `p` que podem ser efetivamente emparelhados entre um índice par e um ímpar via algum dos pares permitidos.

### Justificativa das Capacidades

- A aresta `s → j` com capacidade = expoente de `p` em `a[j]` garante que não se use mais do que o disponível no elemento par.
- A aresta `i → t` com capacidade = expoente de `p` em `a[i]` garante que não se absorva mais do que o disponível no elemento ímpar.
- A aresta `j → i` representa a permissão concedida pelo enunciado: só se pode emparelhar `j` e `i` se esse par foi explicitamente fornecido. A capacidade limitada pelo expoente em `j` evita que `a[j]` "forneça" mais do que tem.

### Resposta Final

O resultado total é a **soma dos fluxos máximos** calculados, um para cada primo `p` presente na fatoração dos elementos do array.

---

## Algoritmo Utilizado

Foi utilizado o **Ford-Fulkerson com BFS (Edmonds-Karp)**.

A busca em largura (BFS) garante que cada caminho aumentante encontrado seja o mais curto em número de arestas, o que torna o número de iterações limitado por `O(V · E)` e o algoritmo mais previsível em termos de desempenho do que Ford-Fulkerson com DFS pura.

### Grafo Residual

A cada iteração do algoritmo, após encontrar um caminho aumentante de `s` a `t`:

1. Calcula-se o **gargalo** (mínima capacidade residual ao longo do caminho).
2. **Atualiza-se o fluxo**: para cada aresta no caminho, o fluxo é incrementado no sentido direto e a capacidade residual da aresta reversa é aumentada na mesma quantidade.

As **arestas reversas** permitem "desfazer" escolhas anteriores: se um caminho já escolhido se mostrar subótimo, o algoritmo pode redirecionar o fluxo por outra rota usando essas arestas de retorno. Sem elas, o algoritmo poderia ficar preso em soluções locais.

O algoritmo termina quando não existe mais caminho de `s` a `t` no grafo residual.

---

## Conversão do Fluxo na Resposta

Para cada primo `p`, o valor do fluxo máximo de `s` a `t` representa exatamente quantos fatores `p` podem ser "transferidos" de algum índice par para algum índice ímpar via pares permitidos. A soma de todos esses fluxos, sobre todos os primos, é a resposta do problema.

Não há necessidade de reconstruir os caminhos nem de calcular o corte mínimo explicitamente: apenas o **valor do fluxo máximo** já é a resposta para cada primo.

---

## Análise de Complexidade

Seja `P` o número de primos distintos na fatoração de todos os elementos, `n` o tamanho do array e `m` o número de pares.

- **Fatoração:** `O(n · sqrt(max_val))` para fatorar todos os elementos.
- **Por primo `p`:** a rede tem `O(n)` vértices e `O(n + m)` arestas. Edmonds-Karp roda em `O(V · E²)`, mas na prática as capacidades são pequenas (expoentes de primos em valores até ~10⁹ são no máximo ~30), o que limita o número de iterações.
- **Total:** `O(P · (n + m)² · n)` no pior caso teórico, porém eficiente na prática dado o tamanho das entradas do problema (`n, m ≤ 1000`).

**Memória:** estrutura dominante é a lista de adjacência do grafo residual, com `O(n + m)` arestas por primo.

---

## Casos Especiais

- **Elemento igual a 1:** não contribui com nenhum fator primo; não gera nenhuma aresta na rede.
- **Primo presente apenas em índices do mesmo lado (só pares ou só ímpares):** não há aresta de `j` para `i` com aquele primo; fluxo máximo é zero.
- **Pares onde um dos índices não contém o primo `p`:** a aresta `j → i` não é criada para esse primo, pois não há transferência possível.
- **Índice sem nenhum primo em comum com seus pares permitidos:** permanece isolado da rede para todos os primos.
- **Fluxo máximo zero:** ocorre normalmente quando não há par compatível para o primo em questão; não afeta a soma final.

---

## Evidência de Submissão Aceita

![Accepted](evidencias/image.png)

PDF completo da submissão disponível em: [`evidencias/Submission 377694246 - Codeforces.pdf`](evidencias/Status%20-%20Codeforces.pdf)