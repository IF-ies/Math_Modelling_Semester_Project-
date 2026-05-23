package structures.tree;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       SwiftRoute Logistics — AVL Tree                          ║
 * ║              Address Directory (Self-Balancing BST)            ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * A manually implemented AVL (Adelson-Velsky and Landis) tree that
 * serves as the address directory.  Each node stores a neighborhood
 * name (the key) along with the associated customer ID.
 *
 * The tree guarantees O(log n) search, insert, and delete by
 * maintaining a balance factor of {-1, 0, +1} at every node through
 * single and double rotations.
 *
 * Supported operations:
 *   • insert(neighborhood, customerID)  — balanced insertion
 *   • search(neighborhood)              — O(log n) lookup
 *   • inOrderTraversal()                — sorted display
 *   • balance(), rotateLeft(), rotateRight() — internal rebalancing
 *
 * @author SwiftRoute Development Team
 */
public class AVLTree {

    // ─── Inner Node ──────────────────────────────────────────────────

    /**
     * AVL tree node storing a neighborhood name, a customer ID,
     * child pointers, and the node's height for balance-factor
     * computation.
     */
    private static class AVLNode {
        String neighborhood;
        String customerID;
        AVLNode left;
        AVLNode right;
        int height;

        AVLNode(String neighborhood, String customerID) {
            this.neighborhood = neighborhood;
            this.customerID = customerID;
            this.left = null;
            this.right = null;
            this.height = 1; // new node is a leaf
        }
    }

    // ─── Fields ──────────────────────────────────────────────────────

    private AVLNode root;
    private int nodeCount;

    // ─── Constructor ─────────────────────────────────────────────────

    public AVLTree() {
        this.root = null;
        this.nodeCount = 0;
    }

    // ─── Height Helpers ──────────────────────────────────────────────

    /**
     * Returns the height of a node (0 for null).
     */
    private int height(AVLNode node) {
        return (node == null) ? 0 : node.height;
    }

    /**
     * Recalculates and sets the height of a node based on its children.
     */
    private void updateHeight(AVLNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    /**
     * Returns the balance factor of a node.
     * Positive → left-heavy, Negative → right-heavy.
     */
    private int getBalanceFactor(AVLNode node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    // ─── Rotations ───────────────────────────────────────────────────

    /**
     * Performs a left rotation around the given node.
     *
     *      x                y
     *       \              / \
     *        y     →      x   C
     *       / \            \
     *      B   C            B
     *
     * @param x the node to rotate around
     * @return the new subtree root (y)
     */
    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode B = y.left;

        // Perform rotation
        y.left = x;
        x.right = B;

        // Update heights (x first because it is now lower)
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * Performs a right rotation around the given node.
     *
     *        y            x
     *       /            / \
     *      x     →     A   y
     *     / \              /
     *    A   B            B
     *
     * @param y the node to rotate around
     * @return the new subtree root (x)
     */
    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode B = x.right;

        // Perform rotation
        x.right = y;
        y.left = B;

        // Update heights
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    // ─── Balance ─────────────────────────────────────────────────────

    /**
     * Rebalances the subtree rooted at the given node if needed.
     * Handles all four imbalance cases:
     *   LL → single right rotation
     *   RR → single left rotation
     *   LR → left-right double rotation
     *   RL → right-left double rotation
     *
     * @param node the subtree root to balance
     * @return the new subtree root after rebalancing
     */
    private AVLNode balance(AVLNode node) {
        int bf = getBalanceFactor(node);

        // Left-heavy
        if (bf > 1) {
            if (getBalanceFactor(node.left) < 0) {
                // LR case: left-rotate the left child first
                node.left = rotateLeft(node.left);
            }
            // LL case (or LR after pre-rotation)
            return rotateRight(node);
        }

        // Right-heavy
        if (bf < -1) {
            if (getBalanceFactor(node.right) > 0) {
                // RL case: right-rotate the right child first
                node.right = rotateRight(node.right);
            }
            // RR case (or RL after pre-rotation)
            return rotateLeft(node);
        }

        return node; // already balanced
    }

    // ─── Insert ──────────────────────────────────────────────────────

    /**
     * Public API: inserts a neighborhood–customerID pair into the tree.
     *
     * @param neighborhood the neighborhood name (used as the key)
     * @param customerID   the customer identifier
     */
    public void insert(String neighborhood, String customerID) {
        root = insertRec(root, neighborhood, customerID);
        nodeCount++;
    }

    /**
     * Recursive insertion followed by rebalancing on the way up.
     */
    private AVLNode insertRec(AVLNode node, String neighborhood, String customerID) {
        // Base case: empty spot found
        if (node == null) {
            return new AVLNode(neighborhood, customerID);
        }

        int cmp = neighborhood.compareToIgnoreCase(node.neighborhood);

        if (cmp < 0) {
            node.left = insertRec(node.left, neighborhood, customerID);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, neighborhood, customerID);
        } else {
            // Duplicate neighborhood — update customer ID
            node.customerID = customerID;
            nodeCount--; // offset the increment in the public method
            return node;
        }

        // Update height of this ancestor node
        updateHeight(node);

        // Rebalance
        return balance(node);
    }

    // ─── Search ──────────────────────────────────────────────────────

    /**
     * Searches for a neighborhood in the tree.
     *
     * @param neighborhood the neighborhood to look up
     * @return the customer ID if found, or null
     */
    public String search(String neighborhood) {
        AVLNode result = searchRec(root, neighborhood);
        return (result != null) ? result.customerID : null;
    }

    private AVLNode searchRec(AVLNode node, String neighborhood) {
        if (node == null) {
            return null;
        }
        int cmp = neighborhood.compareToIgnoreCase(node.neighborhood);
        if (cmp < 0) {
            return searchRec(node.left, neighborhood);
        } else if (cmp > 0) {
            return searchRec(node.right, neighborhood);
        } else {
            return node;
        }
    }

    // ─── Traversals ──────────────────────────────────────────────────

    /**
     * Prints all entries in alphabetical order (in-order traversal).
     */
    public void inOrderTraversal() {
        if (root == null) {
            System.out.println("    (Address directory is empty)");
            return;
        }
        inOrderRec(root);
    }

    private void inOrderRec(AVLNode node) {
        if (node == null) return;
        inOrderRec(node.left);
        System.out.printf("    %-20s → Customer: %s  (h=%d)%n",
                node.neighborhood, node.customerID, node.height);
        inOrderRec(node.right);
    }

    /**
     * Prints a visual representation of the tree structure.
     */
    public void printTree() {
        if (root == null) {
            System.out.println("    (Address directory is empty)");
            return;
        }
        printTreeRec(root, "", true);
    }

    private void printTreeRec(AVLNode node, String prefix, boolean isTail) {
        if (node == null) return;

        System.out.println(prefix + (isTail ? "    └── " : "    ├── ")
                + node.neighborhood + " [" + node.customerID + "] (h=" + node.height + ")");

        String childPrefix = prefix + (isTail ? "        " : "    │   ");

        if (node.left != null || node.right != null) {
            printTreeRec(node.left, childPrefix, node.right == null);
            if (node.right != null) {
                printTreeRec(node.right, childPrefix, true);
            }
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────

    public int getNodeCount() {
        return nodeCount;
    }

    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Returns the height of the entire tree.
     */
    public int getTreeHeight() {
        return height(root);
    }
}
