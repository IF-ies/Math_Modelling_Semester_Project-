package structures.tree;

/**
 * Veteran Logistics — AVL Tree
 * Address Directory (Self-Balancing BST)
 *
 * A manually implemented AVL tree for the Kayseri address directory.
 * Each node stores a neighborhood name (key) and an associated customer ID.
 *
 * Guarantees O(log n) search and insert via rotations that maintain
 * balance factor in {-1, 0, +1} at every node.
 *
 * Operations:
 *   insert(neighborhood, customerID)  — balanced insertion
 *   search(neighborhood)              — O(log n) lookup
 *   inOrderTraversal()                — sorted display
 *   balance(), rotateLeft(), rotateRight() — internal rebalancing
 *
 * @author Veteran Development Team
 */
public class AVLTree {

    private static class AVLNode {
        String neighborhood;
        String customerID;
        AVLNode left, right;
        int height;

        AVLNode(String neighborhood, String customerID) {
            this.neighborhood = neighborhood;
            this.customerID   = customerID;
            this.left = this.right = null;
            this.height = 1;
        }
    }

    private AVLNode root;
    private int nodeCount;

    public AVLTree() { this.root = null; this.nodeCount = 0; }

    // ─── Height & Balance ────────────────────────────────────────────
    private int height(AVLNode node)          { return (node == null) ? 0 : node.height; }
    private void updateHeight(AVLNode node)   { node.height = 1 + Math.max(height(node.left), height(node.right)); }
    private int getBalanceFactor(AVLNode node){ return (node == null) ? 0 : height(node.left) - height(node.right); }

    // ─── Rotations ───────────────────────────────────────────────────

    /**
     * Left rotation:
     *      x                y
     *       \              / \
     *        y     →      x   C
     *       / \            \
     *      B   C            B
     */
    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode B = y.left;
        y.left  = x;
        x.right = B;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    /**
     * Right rotation:
     *        y            x
     *       /            / \
     *      x     →     A   y
     *     / \              /
     *    A   B            B
     */
    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode B = x.right;
        x.right = y;
        y.left  = B;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    /**
     * Rebalances using all four cases:
     *   LL → right rotation
     *   RR → left rotation
     *   LR → left-rotate left child, then right-rotate node
     *   RL → right-rotate right child, then left-rotate node
     */
    private AVLNode balance(AVLNode node) {
        int bf = getBalanceFactor(node);
        if (bf > 1) {
            if (getBalanceFactor(node.left) < 0) node.left = rotateLeft(node.left); // LR
            return rotateRight(node);
        }
        if (bf < -1) {
            if (getBalanceFactor(node.right) > 0) node.right = rotateRight(node.right); // RL
            return rotateLeft(node);
        }
        return node;
    }

    // ─── Insert ──────────────────────────────────────────────────────

    public void insert(String neighborhood, String customerID) {
        root = insertRec(root, neighborhood, customerID);
        nodeCount++;
    }

    private AVLNode insertRec(AVLNode node, String neighborhood, String customerID) {
        if (node == null) return new AVLNode(neighborhood, customerID);
        int cmp = neighborhood.compareToIgnoreCase(node.neighborhood);
        if      (cmp < 0) node.left  = insertRec(node.left,  neighborhood, customerID);
        else if (cmp > 0) node.right = insertRec(node.right, neighborhood, customerID);
        else { node.customerID = customerID; nodeCount--; return node; } // duplicate: update
        updateHeight(node);
        return balance(node);
    }

    // ─── Search ──────────────────────────────────────────────────────

    public String search(String neighborhood) {
        AVLNode result = searchRec(root, neighborhood);
        return (result != null) ? result.customerID : null;
    }

    private AVLNode searchRec(AVLNode node, String neighborhood) {
        if (node == null) return null;
        int cmp = neighborhood.compareToIgnoreCase(node.neighborhood);
        if      (cmp < 0) return searchRec(node.left,  neighborhood);
        else if (cmp > 0) return searchRec(node.right, neighborhood);
        else              return node;
    }

    // ─── Traversals ──────────────────────────────────────────────────

    public void inOrderTraversal() {
        if (root == null) { System.out.println("    (Address directory is empty)"); return; }
        inOrderRec(root);
    }

    private void inOrderRec(AVLNode node) {
        if (node == null) return;
        inOrderRec(node.left);
        System.out.printf("    %-20s → Customer: %-20s (h=%d)%n",
                node.neighborhood, node.customerID, node.height);
        inOrderRec(node.right);
    }

    public void printTree() {
        if (root == null) { System.out.println("    (Address directory is empty)"); return; }
        printTreeRec(root, "", true);
    }

    private void printTreeRec(AVLNode node, String prefix, boolean isTail) {
        if (node == null) return;
        System.out.println(prefix + (isTail ? "    └── " : "    ├── ")
                + node.neighborhood + " [" + node.customerID + "] (h=" + node.height + ")");
        String childPrefix = prefix + (isTail ? "        " : "    │   ");
        if (node.left != null || node.right != null) {
            printTreeRec(node.left,  childPrefix, node.right == null);
            if (node.right != null) printTreeRec(node.right, childPrefix, true);
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────
    public int     getNodeCount()  { return nodeCount; }
    public boolean isEmpty()       { return root == null; }
    public int     getTreeHeight() { return height(root); }
}
