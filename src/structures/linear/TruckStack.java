package structures.linear;

import models.Package;

/**
 * Veteran Logistics — Stack (LIFO)
 * Truck Loading Bay
 *
 * A manually implemented stack backed by a singly linked list.
 * The last package pushed onto the truck is the first one unloaded (LIFO).
 *
 * Operations:
 *   push(Package)      — O(1) push onto top
 *   pop()              — O(1) remove from top
 *   peek()             — O(1) view top without removing
 *   displayStack()     — O(n) full traversal (top → bottom)
 *
 * @author Veteran Development Team
 */
public class TruckStack {

    private static class Node {
        Package data;
        Node next;
        Node(Package data) { this.data = data; this.next = null; }
    }

    private Node top;
    private int size;

    public TruckStack() { this.top = null; this.size = 0; }

    /** Pushes a package onto the top — O(1). */
    public void push(Package pkg) {
        Node newNode = new Node(pkg);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /** Pops and returns the top package — O(1). */
    public Package pop() {
        if (top == null) { System.out.println("    ⚠ Truck loading bay is empty."); return null; }
        Package removed = top.data;
        top = top.next;
        size--;
        return removed;
    }

    /** Returns top package without removing — O(1). */
    public Package peek() { return (top != null) ? top.data : null; }

    /** Prints all packages top to bottom. */
    public void displayStack() {
        if (top == null) { System.out.println("    (Truck loading bay is empty)"); return; }
        Node current = top;
        int index = 1;
        System.out.println("    [TOP]");
        while (current != null) { System.out.printf("    %3d. %s%n", index++, current.data); current = current.next; }
        System.out.println("    [BOTTOM]");
    }

    public int     getSize()  { return size; }
    public boolean isEmpty()  { return size == 0; }
}
