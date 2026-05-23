package structures.linear;

import models.Package;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       SwiftRoute Logistics — Singly Linked List (SLL)          ║
 * ║                   Master Package Registry                      ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * A manually implemented singly linked list that serves as the
 * permanent, append-only master log of every package that enters
 * the SwiftRoute system.
 *
 * Supported operations:
 *   • addRecord(Package)  — O(1) append to tail
 *   • displayLog()        — O(n) traversal and print
 *   • getSize()           — O(1) size query
 *   • search(String id)   — O(n) linear search by PackageID
 *
 * @author SwiftRoute Development Team
 */
public class SinglyLinkedList {

    // ─── Inner Node ──────────────────────────────────────────────────

    /**
     * Node for the singly linked list.  Each node wraps a Package
     * reference and points to the next node in the chain.
     */
    private static class Node {
        Package data;
        Node next;

        Node(Package data) {
            this.data = data;
            this.next = null;
        }
    }

    // ─── Fields ──────────────────────────────────────────────────────

    private Node head;
    private Node tail;
    private int size;

    // ─── Constructor ─────────────────────────────────────────────────

    public SinglyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // ─── Core Operations ─────────────────────────────────────────────

    /**
     * Appends a package record to the end of the registry.
     * Time complexity: O(1) because we maintain a tail pointer.
     *
     * @param pkg the package to register
     */
    public void addRecord(Package pkg) {
        Node newNode = new Node(pkg);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Traverses the entire list and prints each record.
     */
    public void displayLog() {
        if (head == null) {
            System.out.println("    (Registry is empty)");
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
     * Searches for a package by its ID.
     *
     * @param packageID the ID to search for
     * @return the Package if found, null otherwise
     */
    public Package search(String packageID) {
        Node current = head;
        while (current != null) {
            if (current.data.getPackageID().equalsIgnoreCase(packageID)) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * @return the number of records in the registry
     */
    public int getSize() {
        return size;
    }

    /**
     * @return true if the registry contains no records
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
