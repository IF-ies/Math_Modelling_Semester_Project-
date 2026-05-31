package structures.linear;

import models.Package;

/**
 * Veteran Logistics — Doubly Linked List (DLL)
 * Intake Buffer
 *
 * A manually implemented doubly linked list used as the intake
 * buffer for incoming packages in the Kayseri distribution system.
 *
 * Operations:
 *   insertAtTail(Package)  — O(1) insertion at end
 *   insertAtHead(Package)  — O(1) insertion at front
 *   removeFromHead()       — O(1) removal from front
 *   removeFromTail()       — O(1) removal from end
 *   removePackage(String)  — O(n) removal by PackageID
 *   displayBuffer()        — O(n) traversal
 *
 * @author Veteran Development Team
 */
public class DoublyLinkedList {

    private static class Node {
        Package data;
        Node prev;
        Node next;
        Node(Package data) { this.data = data; this.prev = null; this.next = null; }
    }

    private Node head;
    private Node tail;
    private int size;

    public DoublyLinkedList() { this.head = null; this.tail = null; this.size = 0; }

    /** Inserts at tail — O(1) via tail pointer. */
    public void insertAtTail(Package pkg) {
        Node newNode = new Node(pkg);
        if (tail == null) { head = newNode; tail = newNode; }
        else { tail.next = newNode; newNode.prev = tail; tail = newNode; }
        size++;
    }

    /** Inserts at head — O(1) via head pointer. */
    public void insertAtHead(Package pkg) {
        Node newNode = new Node(pkg);
        if (head == null) { head = newNode; tail = newNode; }
        else { newNode.next = head; head.prev = newNode; head = newNode; }
        size++;
    }

    /** Removes and returns head — O(1). */
    public Package removeFromHead() {
        if (head == null) { System.out.println("    ⚠ Buffer is empty."); return null; }
        Package removed = head.data;
        if (head == tail) { head = null; tail = null; }
        else { head = head.next; head.prev = null; }
        size--;
        return removed;
    }

    /** Removes and returns tail — O(1) via prev pointer. */
    public Package removeFromTail() {
        if (tail == null) { System.out.println("    ⚠ Buffer is empty."); return null; }
        Package removed = tail.data;
        if (head == tail) { head = null; tail = null; }
        else { tail = tail.prev; tail.next = null; }
        size--;
        return removed;
    }

    /** Removes first node matching the given ID — O(n). */
    public Package removePackage(String packageID) {
        Node current = head;
        while (current != null) {
            if (current.data.getPackageID().equalsIgnoreCase(packageID)) {
                if (current.prev != null) current.prev.next = current.next; else head = current.next;
                if (current.next != null) current.next.prev = current.prev; else tail = current.prev;
                size--;
                return current.data;
            }
            current = current.next;
        }
        return null; // not found — caller handles messaging
    }

    /** Prints buffer head → tail. */
    public void displayBuffer() {
        if (head == null) { System.out.println("    (Buffer is empty)"); return; }
        Node current = head;
        int index = 1;
        while (current != null) { System.out.printf("    %3d. %s%n", index++, current.data); current = current.next; }
    }

    /** Prints buffer tail → head. */
    public void displayReverse() {
        if (tail == null) { System.out.println("    (Buffer is empty)"); return; }
        Node current = tail;
        int index = size;
        while (current != null) { System.out.printf("    %3d. %s%n", index--, current.data); current = current.prev; }
    }

    public int     getSize()   { return size; }
    public boolean isEmpty()   { return size == 0; }
    public Package peekHead()  { return (head != null) ? head.data : null; }
    public Package peekTail()  { return (tail != null) ? tail.data : null; }
}
