import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;

        int n = sc.nextInt();
        int m = sc.nextInt();

        Map<Integer, Map<Long, Integer>> fatores = new HashMap<>();
        Set<Long> primosGlobais = new HashSet<>();
        
        for (int i = 1; i <= n; i++) {
            long val = sc.nextLong();
            fatores.put(i, new HashMap<>());
            decomporEmPrimos(val, fatores.get(i), primosGlobais);
        }

        int[][] paresBons = new int[m][2];
        for (int k = 0; k < m; k++) {
            paresBons[k][0] = sc.nextInt();
            paresBons[k][1] = sc.nextInt();
        }

        int resultadoTotal = 0;
        int s = 0, t = n + 1;

        for (long p : primosGlobais) {
            FlowNetwork G = new FlowNetwork(n + 2);

            for (int i = 1; i <= n; i++) {
                if(!fatores.get(i).containsKey(p)) {
                    continue;
                }

                int qtd = fatores.get(i).get(p);

                if (i % 2 == 0) {
                    G.addEdge(new FlowEdge(s, i, qtd));
                    continue;
                }

                G.addEdge(new FlowEdge(i, t, qtd));
            }

            for (int k = 0; k < m; k++) {
                int u = paresBons[k][0];
                int v = paresBons[k][1];

                int impar = (u % 2 != 0) ? u : v;
                int par   = (u % 2 != 0) ? v : u;

                int capEsquerda = fatores.get(par).get(p) == null ? 0 : fatores.get(par).get(p);
                G.addEdge(new FlowEdge(par, impar, capEsquerda));
            }

            FordFulkerson maxFlow = new FordFulkerson(G, s, t);
            resultadoTotal += (int) maxFlow.value();
        }

        System.out.println(resultadoTotal);
    }

    private static void decomporEmPrimos(long val, Map<Long, Integer> mapaFatores, Set<Long> primosGlobais) {
        for (long d = 2; d * d <= val; d++) {
            if (val % d == 0) {
                int qtd = 0;
                while (val % d == 0) {
                    qtd++;
                    val /= d;
                }
                mapaFatores.put(d, qtd);
                primosGlobais.add(d);
            }
        }
        if (val > 1) {
            mapaFatores.put(val, 1);
            primosGlobais.add(val);
        }
    }
}


class FlowEdge {
    private final int v;  
    private final int w;           
    private final double capacity;  
    private double flow;           

    public FlowEdge(int v, int w, double capacity) {
        this.v = v;
        this.w = w;
        this.capacity = capacity;
        this.flow = 0.0;
    }

    public int from() { return v; }
    public int to() { return w; }
    public double capacity() { return capacity; }
    public double flow() { return flow; }

    public int other(int vertex) {
        if (vertex == v) return w;
        else if (vertex == w) return v;
        else throw new IllegalArgumentException("Vértice inválido");
    }

    public double residualCapacityTo(int vertex) {
        if (vertex == v) return flow;            
        else if (vertex == w) return capacity - flow; 
        else throw new IllegalArgumentException("Vértice inválido");
    }

    public void addResidualFlowTo(int vertex, double delta) {
        if (vertex == v) flow -= delta;
        else if (vertex == w) flow += delta;
        else throw new IllegalArgumentException("Vértice inválido");
    }
}

class FlowNetwork {
    private final int V;
    private List<FlowEdge>[] adj;

    @SuppressWarnings("unchecked")
    public FlowNetwork(int V) {
        this.V = V;
        adj = (List<FlowEdge>[]) new ArrayList[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new ArrayList<>();
        }
    }

    public int V() { return V; }

    public void addEdge(FlowEdge e) {
        int v = e.from();
        int w = e.to();
        adj[v].add(e);
        adj[w].add(e);
    }

    public Iterable<FlowEdge> adj(int v) { return adj[v]; }
}

class FordFulkerson {
    private boolean[] marked;     
    private FlowEdge[] edgeTo;    
    private double value;

    public FordFulkerson(FlowNetwork G, int s, int t) {
        value = 0.0;
        while (hasAugmentingPath(G, s, t)) {

            double bottle = Double.POSITIVE_INFINITY;
            for (int v = t; v != s; v = edgeTo[v].other(v)) {
                bottle = Math.min(bottle, edgeTo[v].residualCapacityTo(v));
            }

            for (int v = t; v != s; v = edgeTo[v].other(v)) {
                edgeTo[v].addResidualFlowTo(v, bottle);
            }

            value += bottle;
        }
    }

    public double value() { return value; }

    private boolean hasAugmentingPath(FlowNetwork G, int s, int t) {
        edgeTo = new FlowEdge[G.V()];
        marked = new boolean[G.V()];

        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        marked[s] = true;

        while (!queue.isEmpty() && !marked[t]) {
            int v = queue.remove();

            for (FlowEdge e : G.adj(v)) {
                int w = e.other(v);

                if (e.residualCapacityTo(w) > 0 && !marked[w]) {
                    edgeTo[w] = e;
                    marked[w] = true;
                    queue.add(w);
                }
            }
        }
        return marked[t];
    }
}