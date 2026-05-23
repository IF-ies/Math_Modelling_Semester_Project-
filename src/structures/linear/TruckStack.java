package structures.linear;

import models.Package;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       SwiftRoute Logistics — Stack (LIFO)                      ║
 * ║                   Truck Loading Bay                            ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * A manually implemented stack backed by a singly linked list.
 * Used for truck loading: the last package pushed onto the truck
 * is the first one unloaded at the destination (Last-In, First-Out).
 *
 * Supported operations:
 *   • push(Package)      — O(1) push onto top
 *   • pop()              — O(1) remove from top
 *   • peek()             — O(1) view top without removing
 *   • displayStack()     — O(n) full traversal (top → bottom)
 *
 * @author SwiftRoute Development Team
 */
public class TruckStack {

    // ─── Inner Node ──────────────────────────────────────────────────

    private static class Node {
        Package data;
        Node next;

        Node(Package data) {
            this.data = data;
            this.next = null;
        }
    }

    // ─── Fields ──────────────────────────────────────────────────────

    private Node top;
    private int size;

    // ─── Constructor ─────────────────────────────────────────────────

    public TruckStack() {
        this.top = null;
        this.size = 0;
    }

    // ─── Core Operations ─────────────────────────────────────────────

    /**
     * Pushes a package onto the top of the stack — O(1).
     *
     * @param pkg the package to load
     */
    public void push(Package pkg) {
        Node newNode = new Node(pkg);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * Pops and returns the package from the top — O(1).
     *
     * @return the popped package, or null if stack is empty
     */
    public Package pop() {
        if (top == null) {
            System.out.println("    ⚠ Truck stack is empty — nothing to pop.");
            return null;
        }
        Package removed = top.data;
        top = top.next;
        size--;
        return removed;
    }

    /**
     * Returns (but does not remove) the package at the top.
     *
     * @return the top package, or null if empty
     */
    public Package peek() {
        return (top != null) ? top.data : null;
    }

    /**
     * Prints every package in the stack from top to bottom.
     */
    public void displayStack() {
        if (top == null) {
            System.out.println("    (Stack is empty)");
            return;
        }
        Node current = top;
        int index = 1;
        System.out.println("    [TOP]");
        while (current != null) {
            System.out.printf("    %3d. %s%n", index++, current.data);
            current = current.next;
        }
        System.out.println("    [BOTTOM]");
    }

    // ─── Utility ─────────────────────────────────────────────────────

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
