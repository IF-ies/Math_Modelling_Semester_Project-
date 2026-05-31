package structures.graph;

/**
 * Veteran Logistics — Weighted Graph
 * Kayseri City Map & Routing Engine
 *
 * A manually implemented weighted, undirected graph that models the
 * Kayseri road network. Vertices = neighborhoods, edges = roads (km).
 * Adjacency-list representation — no built-in Java collections used.
 *
 * Algorithms:
 *   Dijkstra's  — shortest path between two locations
 *   Prim's      — Minimum Spanning Tree (MST)
 *
 * Central Depot (Hub): Meydan
 *
 * @author Veteran Development Team
 */
public class CityGraph {

    // ─── Inner Classes ───────────────────────────────────────────────
    private static class EdgeNode {
        int destIndex;
        int weight;
        EdgeNode next;
        EdgeNode(int destIndex, int weight) { this.destIndex = destIndex; this.weight = weight; this.next = null; }
    }

    // ─── Fields ──────────────────────────────────────────────────────
    private static final int MAX_VERTICES = 100;
    private String[]   vertexNames;
    private EdgeNode[] adjacencyList;
    private int        vertexCount;

    public CityGraph() {
        vertexNames    = new String[MAX_VERTICES];
        adjacencyList  = new EdgeNode[MAX_VERTICES];
        vertexCount    = 0;
    }

    // ─── Vertex Management ───────────────────────────────────────────
    private int getOrCreateVertexIndex(String name) {
        for (int i = 0; i < vertexCount; i++) {
            if (vertexNames[i].equalsIgnoreCase(name)) return i;
        }
        if (vertexCount >= MAX_VERTICES) throw new RuntimeException("Max vertex count exceeded.");
        vertexNames[vertexCount]   = name;
        adjacencyList[vertexCount] = null;
        return vertexCount++;
    }

    private int getVertexIndex(String name) {
        for (int i = 0; i < vertexCount; i++) {
            if (vertexNames[i].equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    // ─── Edge Management ─────────────────────────────────────────────

    /**
     * Adds an undirected, weighted edge between two Kayseri locations.
     *
     * @param source      source location name
     * @param destination destination location name
     * @param weight      distance in kilometres
     */
    public void addEdge(String source, String destination, int weight) {
        int srcIdx = getOrCreateVertexIndex(source);
        int dstIdx = getOrCreateVertexIndex(destination);
        EdgeNode e1 = new EdgeNode(dstIdx, weight); e1.next = adjacencyList[srcIdx]; adjacencyList[srcIdx] = e1;
        EdgeNode e2 = new EdgeNode(srcIdx, weight); e2.next = adjacencyList[dstIdx]; adjacencyList[dstIdx] = e2;
    }

    /** Prints the adjacency list of the entire graph. */
    public void displayGraph() {
        for (int i = 0; i < vertexCount; i++) {
            System.out.printf("    %-15s →", vertexNames[i]);
            EdgeNode edge = adjacencyList[i];
            boolean first = true;
            while (edge != null) {
                if (!first) System.out.print(",");
                System.out.printf(" %s(%d km)", vertexNames[edge.destIndex], edge.weight);
                first = false;
                edge = edge.next;
            }
            System.out.println();
        }
    }

    // ─── Dijkstra's Algorithm ────────────────────────────────────────

    /**
     * Calculates and prints the shortest path using Dijkstra's algorithm.
     * Time complexity: O(V^2) — array-scanning variant.
     *
     * @param start starting location
     * @param end   ending location
     */
    public void calculateShortestPath(String start, String end) {
        int srcIdx = getVertexIndex(start);
        int endIdx = getVertexIndex(end);
        if (srcIdx == -1 || endIdx == -1) {
            System.out.println("    ⚠ Location not found on map: " + start + " or " + end);
            return;
        }

        final int INF = Integer.MAX_VALUE;
        int[]     dist    = new int[vertexCount];
        int[]     prev    = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];

        for (int i = 0; i < vertexCount; i++) { dist[i] = INF; prev[i] = -1; visited[i] = false; }
        dist[srcIdx] = 0;

        for (int count = 0; count < vertexCount; count++) {
            // Find unvisited vertex with smallest dist — O(V)
            int u = -1, minDist = INF;
            for (int i = 0; i < vertexCount; i++) {
                if (!visited[i] && dist[i] < minDist) { minDist = dist[i]; u = i; }
            }
            if (u == -1) break;
            visited[u] = true;

            // Relax neighbours
            EdgeNode edge = adjacencyList[u];
            while (edge != null) {
                int v = edge.destIndex;
                if (!visited[v] && dist[u] + edge.weight < dist[v]) {
                    dist[v] = dist[u] + edge.weight;
                    prev[v] = u;
                }
                edge = edge.next;
            }
        }

        if (dist[endIdx] == INF) {
            System.out.println("    ⚠ " + start + " and " + end + " have no connecting path.");
            return;
        }

        // Reconstruct path
        String[] path = new String[vertexCount];
        int pathLen = 0;
        for (int at = endIdx; at != -1; at = prev[at]) path[pathLen++] = vertexNames[at];

        System.out.printf("    Shortest distance : %d km%n", dist[endIdx]);
        System.out.print("    Route             : ");
        for (int i = pathLen - 1; i >= 0; i--) {
            System.out.print(path[i]);
            if (i > 0) System.out.print(" → ");
        }
        System.out.println();

        System.out.println("    Step by step:");
        for (int i = pathLen - 1; i > 0; i--) {
            int fromIdx = getVertexIndex(path[i]);
            int toIdx   = getVertexIndex(path[i - 1]);
            System.out.printf("      %s → %s : %d km%n", path[i], path[i - 1], getEdgeWeight(fromIdx, toIdx));
        }
    }

    private int getEdgeWeight(int fromIdx, int toIdx) {
        EdgeNode edge = adjacencyList[fromIdx];
        while (edge != null) { if (edge.destIndex == toIdx) return edge.weight; edge = edge.next; }
        return -1;
    }

    // ─── Prim's Algorithm (MST) ──────────────────────────────────────

    /**
     * Calculates and prints the Minimum Spanning Tree using Prim's algorithm.
     * Time complexity: O(V^2) — array-scanning variant.
     */
    public void calculateMST() {
        if (vertexCount == 0) { System.out.println("    ⚠ Graph is empty — MST cannot be computed."); return; }

        final int INF = Integer.MAX_VALUE;
        int[]     key    = new int[vertexCount];
        int[]     parent = new int[vertexCount];
        boolean[] inMST  = new boolean[vertexCount];

        for (int i = 0; i < vertexCount; i++) { key[i] = INF; parent[i] = -1; inMST[i] = false; }
        key[0] = 0;

        int totalWeight = 0;

        for (int count = 0; count < vertexCount; count++) {
            // Find min key vertex not in MST — O(V)
            int u = -1, minKey = INF;
            for (int i = 0; i < vertexCount; i++) {
                if (!inMST[i] && key[i] < minKey) { minKey = key[i]; u = i; }
            }
            if (u == -1) break;
            inMST[u] = true;
            totalWeight += key[u];

            EdgeNode edge = adjacencyList[u];
            while (edge != null) {
                int v = edge.destIndex;
                if (!inMST[v] && edge.weight < key[v]) { key[v] = edge.weight; parent[v] = u; }
                edge = edge.next;
            }
        }

        System.out.println("    MST Edges (Prim's Algorithm) — Kayseri Infrastructure Network:");
        System.out.println("    ─────────────────────────────────────────────────────");
        System.out.printf("    %-15s %-15s %s%n", "Source", "Target", "Distance (km)");
        System.out.println("    ─────────────────────────────────────────────────────");
        for (int i = 1; i < vertexCount; i++) {
            if (parent[i] != -1)
                System.out.printf("    %-15s %-15s %d%n", vertexNames[parent[i]], vertexNames[i], key[i]);
        }
        System.out.println("    ─────────────────────────────────────────────────────");
        System.out.printf("    Total MST Weight: %d km%n", totalWeight);
    }

    // ─── Utility ─────────────────────────────────────────────────────
    public int      getVertexCount() { return vertexCount; }
    public String[] getVertexNames() {
        String[] names = new String[vertexCount];
        for (int i = 0; i < vertexCount; i++) names[i] = vertexNames[i];
        return names;
    }
}
