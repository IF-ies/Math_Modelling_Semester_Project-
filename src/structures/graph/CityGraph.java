package structures.graph;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       SwiftRoute Logistics — Weighted Graph                    ║
 * ║           City Map & Routing Engine                            ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * A manually implemented weighted, undirected graph that models the
 * city's road network.  Vertices represent neighborhoods/locations,
 * and edges represent roads with distances (weights) in kilometres.
 *
 * The graph uses an adjacency-list representation built entirely
 * from scratch — no java.util.LinkedList, HashMap, or similar
 * built-in collections for the core logic.
 *
 * Algorithms:
 *   • Dijkstra's Algorithm   — shortest path between two locations
 *   • Prim's Algorithm       — Minimum Spanning Tree (MST)
 *
 * @author SwiftRoute Development Team
 */
public class CityGraph {

    // ═══════════════════════════════════════════════════════════════
    // ─── Inner Helper Classes ─────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    /**
     * Represents a single edge in an adjacency list.
     */
    private static class EdgeNode {
        int destIndex;
        int weight;
        EdgeNode next;

        EdgeNode(int destIndex, int weight) {
            this.destIndex = destIndex;
            this.weight = weight;
            this.next = null;
        }
    }

    /**
     * Lightweight min-heap entry used by Dijkstra / Prim.
     */
    private static class HeapEntry {
        int vertexIndex;
        int key;  // distance (Dijkstra) or edge weight (Prim)

        HeapEntry(int vertexIndex, int key) {
            this.vertexIndex = vertexIndex;
            this.key = key;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ─── Fields ───────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    private static final int MAX_VERTICES = 100;

    private String[] vertexNames;       // maps index → vertex name
    private EdgeNode[] adjacencyList;   // adjacency list heads
    private int vertexCount;

    // ═══════════════════════════════════════════════════════════════
    // ─── Constructor ──────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    public CityGraph() {
        vertexNames = new String[MAX_VERTICES];
        adjacencyList = new EdgeNode[MAX_VERTICES];
        vertexCount = 0;
    }

    // ═══════════════════════════════════════════════════════════════
    // ─── Vertex Management ────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns the index of a vertex by name.  If the vertex doesn't
     * exist yet, it is automatically created.
     */
    private int getOrCreateVertexIndex(String name) {
        for (int i = 0; i < vertexCount; i++) {
            if (vertexNames[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        // Not found — create a new vertex
        if (vertexCount >= MAX_VERTICES) {
            throw new RuntimeException("Maximum vertex count (" + MAX_VERTICES + ") exceeded.");
        }
        vertexNames[vertexCount] = name;
        adjacencyList[vertexCount] = null;
        return vertexCount++;
    }

    /**
     * Returns the index of a vertex by name, or -1 if not found.
     */
    private int getVertexIndex(String name) {
        for (int i = 0; i < vertexCount; i++) {
            if (vertexNames[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    // ═══════════════════════════════════════════════════════════════
    // ─── Edge Management ──────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    /**
     * Adds an undirected, weighted edge between two locations.
     *
     * @param source      source location name
     * @param destination destination location name
     * @param weight      distance in kilometres
     */
    public void addEdge(String source, String destination, int weight) {
        int srcIdx = getOrCreateVertexIndex(source);
        int dstIdx = getOrCreateVertexIndex(destination);

        // Add destination to source's adjacency list
        EdgeNode newEdge1 = new EdgeNode(dstIdx, weight);
        newEdge1.next = adjacencyList[srcIdx];
        adjacencyList[srcIdx] = newEdge1;

        // Add source to destination's adjacency list (undirected)
        EdgeNode newEdge2 = new EdgeNode(srcIdx, weight);
        newEdge2.next = adjacencyList[dstIdx];
        adjacencyList[dstIdx] = newEdge2;
    }

    /**
     * Prints the entire adjacency list representation of the graph.
     */
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

    // ═══════════════════════════════════════════════════════════════
    // ─── Dijkstra's Algorithm ─────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    /**
     * Calculates and prints the shortest path from start to end
     * using Dijkstra's algorithm.
     *
     * @param start the starting location name
     * @param end   the ending location name
     */
    public void calculateShortestPath(String start, String end) {
        int srcIdx = getVertexIndex(start);
        int endIdx = getVertexIndex(end);

        if (srcIdx == -1 || endIdx == -1) {
            System.out.println("    ⚠ One or both locations not found in the city map.");
            return;
        }

        final int INF = Integer.MAX_VALUE;
        int[] dist = new int[vertexCount];
        int[] prev = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            dist[i] = INF;
            prev[i] = -1;
            visited[i] = false;
        }
        dist[srcIdx] = 0;

        // Simple min-heap using arrays (manual priority queue)
        // We'll use a basic O(V^2) approach for clarity
        for (int count = 0; count < vertexCount; count++) {
            // Find unvisited vertex with smallest dist
            int u = -1;
            int minDist = INF;
            for (int i = 0; i < vertexCount; i++) {
                if (!visited[i] && dist[i] < minDist) {
                    minDist = dist[i];
                    u = i;
                }
            }

            if (u == -1) break;  // remaining vertices are unreachable
            visited[u] = true;

            // Relax all neighbours of u
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

        // ─── Print Result ────────────────────────────────────────
        if (dist[endIdx] == INF) {
            System.out.println("    ⚠ No path exists from " + start + " to " + end + ".");
            return;
        }

        // Reconstruct path
        String[] path = new String[vertexCount];
        int pathLen = 0;
        for (int at = endIdx; at != -1; at = prev[at]) {
            path[pathLen++] = vertexNames[at];
        }

        // Print path in correct order
        System.out.printf("    Shortest distance: %d km%n", dist[endIdx]);
        System.out.print("    Route           : ");
        for (int i = pathLen - 1; i >= 0; i--) {
            System.out.print(path[i]);
            if (i > 0) System.out.print(" → ");
        }
        System.out.println();

        // Print step-by-step distances
        System.out.println("    Step-by-step:");
        for (int i = pathLen - 1; i > 0; i--) {
            int fromIdx = getVertexIndex(path[i]);
            int toIdx = getVertexIndex(path[i - 1]);
            int edgeWeight = getEdgeWeight(fromIdx, toIdx);
            System.out.printf("      %s → %s : %d km%n", path[i], path[i - 1], edgeWeight);
        }
    }

    /**
     * Returns the weight of the edge between two vertices.
     */
    private int getEdgeWeight(int fromIdx, int toIdx) {
        EdgeNode edge = adjacencyList[fromIdx];
        while (edge != null) {
            if (edge.destIndex == toIdx) {
                return edge.weight;
            }
            edge = edge.next;
        }
        return -1;
    }

    // ═══════════════════════════════════════════════════════════════
    // ─── Prim's Algorithm (MST) ───────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    /**
     * Calculates and prints the Minimum Spanning Tree (MST) using
     * Prim's algorithm.  The MST represents the most cost-efficient
     * infrastructure network connecting all locations.
     */
    public void calculateMST() {
        if (vertexCount == 0) {
            System.out.println("    ⚠ Graph is empty — cannot compute MST.");
            return;
        }

        final int INF = Integer.MAX_VALUE;
        int[] key = new int[vertexCount];       // min weight edge to MST
        int[] parent = new int[vertexCount];    // parent in MST
        boolean[] inMST = new boolean[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            key[i] = INF;
            parent[i] = -1;
            inMST[i] = false;
        }
        key[0] = 0;  // start from vertex 0

        int totalWeight = 0;

        for (int count = 0; count < vertexCount; count++) {
            // Pick the minimum key vertex not yet in MST
            int u = -1;
            int minKey = INF;
            for (int i = 0; i < vertexCount; i++) {
                if (!inMST[i] && key[i] < minKey) {
                    minKey = key[i];
                    u = i;
                }
            }

            if (u == -1) break; // disconnected graph
            inMST[u] = true;
            totalWeight += key[u];

            // Update keys of adjacent vertices
            EdgeNode edge = adjacencyList[u];
            while (edge != null) {
                int v = edge.destIndex;
                if (!inMST[v] && edge.weight < key[v]) {
                    key[v] = edge.weight;
                    parent[v] = u;
                }
                edge = edge.next;
            }
        }

        // ─── Print MST ──────────────────────────────────────────
        System.out.println("    MST Edges (Prim's Algorithm):");
        System.out.println("    ─────────────────────────────────────────────");
        System.out.printf("    %-15s %-15s %s%n", "From", "To", "Distance (km)");
        System.out.println("    ─────────────────────────────────────────────");

        for (int i = 1; i < vertexCount; i++) {
            if (parent[i] != -1) {
                System.out.printf("    %-15s %-15s %d%n",
                        vertexNames[parent[i]], vertexNames[i], key[i]);
            }
        }
        System.out.println("    ─────────────────────────────────────────────");
        System.out.printf("    Total MST Weight: %d km%n", totalWeight);
    }

    // ═══════════════════════════════════════════════════════════════
    // ─── Utility ──────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════

    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Returns an array of all vertex names currently in the graph.
     */
    public String[] getVertexNames() {
        String[] names = new String[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            names[i] = vertexNames[i];
        }
        return names;
    }
}
