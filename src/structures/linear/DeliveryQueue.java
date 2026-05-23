package structures.linear;

import models.Package;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       SwiftRoute Logistics — Queue (FIFO)                      ║
 * ║                 Standard Delivery Queue                        ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * A manually implemented queue backed by a singly linked list.
 * Used for standard delivery processing: packages are enqueued at
 * the rear and dequeued from the front (First-In, First-Out).
 *
 * Supported operations:
 *   • enqueue(Package)  — O(1) add to rear
 *   • dequeue()         — O(1) remove from front
 *   • peek()            — O(1) view front without removing
 *   • displayQueue()    — O(n) full traversal
 *
 * @author SwiftRoute Development Team
 */
public class DeliveryQueue {

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

    private Node front;
    private Node rear;
    private int size;

    // ─── Constructor ─────────────────────────────────────────────────

    public DeliveryQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    // ─── Core Operations ─────────────────────────────────────────────

    /**
     * Adds a package to the rear of the queue — O(1).
     *
     * @param pkg the package to enqueue
     */
    public void enqueue(Package pkg) {
        Node newNode = new Node(pkg);
        if (rear == null) {          // queue is empty
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the package at the front — O(1).
     *
     * @return the dequeued package, or null if queue is empty
     */
    public Package dequeue() {
        if (front == null) {
            System.out.println("    ⚠ Delivery queue is empty — nothing to dequeue.");
            return null;
        }
        Package removed = front.data;
        front = front.next;
        if (front == null) {
            rear = null;             // queue became empty
        }
        size--;
        return removed;
    }

    /**
     * Returns (but does not remove) the package at the front.
     *
     * @return the front package, or null if empty
     */
    public Package peek() {
        return (front != null) ? front.data : null;
    }

    /**
     * Prints every package currently in the queue, front to rear.
     */
    public void displayQueue() {
        if (front == null) {
            System.out.println("    (Queue is empty)");
            return;
        }
        Node current = front;
        int index = 1;
        while (current != null) {
            System.out.printf("    %3d. %s%n", index++, current.data);
            current = current.next;
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
