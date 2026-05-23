package structures.linear;

import models.Package;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       SwiftRoute Logistics — Doubly Linked List (DLL)          ║
 * ║                    Intake Buffer                               ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * A manually implemented doubly linked list used as the intake
 * buffer where incoming packages are temporarily held before
 * being dispatched to either the Queue or the Stack.
 *
 * Supported operations:
 *   • insertAtTail(Package)  — O(1) insertion at end
 *   • insertAtHead(Package)  — O(1) insertion at front
 *   • removeFromHead()       — O(1) removal from front
 *   • removeFromTail()       — O(1) removal from end
 *   • removePackage(String)  — O(n) removal by PackageID
 *   • displayBuffer()        — O(n) traversal
 *
 * @author SwiftRoute Development Team
 */
public class DoublyLinkedList {

    // ─── Inner Node ──────────────────────────────────────────────────

    /**
     * Each node holds a Package and has pointers to both
     * the previous and next nodes in the list.
     */
    private static class Node {
        Package data;
        Node prev;
        Node next;

        Node(Package data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }

    // ─── Fields ──────────────────────────────────────────────────────

    private Node head;
    private Node tail;
    private int size;

    // ─── Constructor ─────────────────────────────────────────────────

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // ─── Core Operations ─────────────────────────────────────────────

    /**
     * Inserts a package at the tail of the buffer — O(1).
     *
     * @param pkg the package to insert
     */
    public void insertAtTail(Package pkg) {
        Node newNode = new Node(pkg);
        if (tail == null) {          // list is empty
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    /**
     * Inserts a package at the head of the buffer — O(1).
     *
     * @param pkg the package to insert
     */
    public void insertAtHead(Package pkg) {
        Node newNode = new Node(pkg);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the package at the head — O(1).
     *
     * @return the removed package, or null if buffer is empty
     */
    public Package removeFromHead() {
        if (head == null) {
            System.out.println("    ⚠ Buffer is empty — nothing to remove.");
            return null;
        }
        Package removed = head.data;
        if (head == tail) {          // single element
            head = null;
            tail = null;
        } else {
            head = head.next;
            head.prev = null;
        }
        size--;
        return removed;
    }

    /**
     * Removes and returns the package at the tail — O(1).
     *
     * @return the removed package, or null if buffer is empty
     */
    public Package removeFromTail() {
        if (tail == null) {
            System.out.println("    ⚠ Buffer is empty — nothing to remove.");
            return null;
        }
        Package removed = tail.data;
        if (head == tail) {
            head = null;
            tail = null;
        } else {
            tail = tail.prev;
            tail.next = null;
        }
        size--;
        return removed;
    }

    /**
     * Removes the first package that matches the given ID — O(n).
     *
     * @param packageID the ID of the package to remove
     * @return the removed package, or null if not found
     */
    public Package removePackage(String packageID) {
        Node current = head;
        while (current != null) {
            if (current.data.getPackageID().equalsIgnoreCase(packageID)) {
                // Unlink the node
                if (current.prev != null) {
                    current.prev.next = current.next;
                } else {
                    head = current.next;  // removing head
                }
                if (current.next != null) {
                    current.next.prev = current.prev;
                } else {
                    tail = current.prev;  // removing tail
                }
                size--;
                return current.data;
            }
            current = current.next;
        }
        System.out.println("    ⚠ Package " + packageID + " not found in buffer.");
        return null;
    }

    /**
     * Prints the entire buffer from head to tail.
     */
    public void displayBuffer() {
        if (head == null) {
            System.out.println("    (Buffer is empty)");
            return;
        }
        Node current = head;
        int index = 1;
        while (current != null) {
            System.out.printf("    %3d. %s%n", index++, current.data);
            current = current.next;
        }
    }

    /**
     * Prints the buffer in reverse order (tail → head).
     */
    public void displayReverse() {
        if (tail == null) {
            System.out.println("    (Buffer is empty)");
            return;
        }
        Node current = tail;
        int index = size;
        while (current != null) {
            System.out.printf("    %3d. %s%n", index--, current.data);
            current = current.prev;
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Peeks at the head package without removing it.
     *
     * @return the head package, or null if empty
     */
    public Package peekHead() {
        return (head != null) ? head.data : null;
    }

    /**
     * Peeks at the tail package without removing it.
     *
     * @return the tail package, or null if empty
     */
    public Package peekTail() {
        return (tail != null) ? tail.data : null;
    }
}
