package structures.linear;

import models.Package;

/**
 * Veteran Logistics — Queue (FIFO)
 * Standard Delivery Queue
 *
 * A manually implemented queue backed by a singly linked list.
 * Packages are enqueued at the rear and dequeued from the front (FIFO).
 *
 * Operations:
 *   enqueue(Package)  — O(1) add to rear
 *   dequeue()         — O(1) remove from front
 *   peek()            — O(1) view front without removing
 *   displayQueue()    — O(n) full traversal
 *
 * @author Veteran Development Team
 */
public class DeliveryQueue {

    private static class Node {
        Package data;
        Node next;
        Node(Package data) { this.data = data; this.next = null; }
    }

    private Node front;
    private Node rear;
    private int size;

    public DeliveryQueue() { this.front = null; this.rear = null; this.size = 0; }

    /** Adds a package to the rear — O(1) via rear pointer. */
    public void enqueue(Package pkg) {
        Node newNode = new Node(pkg);
        if (rear == null) { front = newNode; rear = newNode; }
        else { rear.next = newNode; rear = newNode; }
        size++;
    }

    /** Removes and returns the front package — O(1). */
    public Package dequeue() {
        if (front == null) { System.out.println("    ⚠ Kuyruk bos."); return null; }
        Package removed = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return removed;
    }

    /** Returns the front package without removing — O(1). */
    public Package peek() { return (front != null) ? front.data : null; }

    /** Prints all packages front to rear. */
    public void displayQueue() {
        if (front == null) { System.out.println("    (Kuyruk bos)"); return; }
        Node current = front;
        int index = 1;
        while (current != null) { System.out.printf("    %3d. %s%n", index++, current.data); current = current.next; }
    }

    public int     getSize()  { return size; }
    public boolean isEmpty()  { return size == 0; }
}
