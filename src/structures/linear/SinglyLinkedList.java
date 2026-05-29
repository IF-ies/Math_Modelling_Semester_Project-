package structures.linear;

import models.Package;

/**
 * Veteran Logistics — Singly Linked List (SLL)
 * Master Package Registry
 *
 * A manually implemented singly linked list that serves as the
 * permanent, append-only master log of every package that enters
 * the Veteran Kayseri system.
 *
 * Operations:
 *   addRecord(Package)  — O(1) append to tail
 *   displayLog()        — O(n) traversal and print
 *   getSize()           — O(1) size query
 *   search(String id)   — O(n) linear search by PackageID
 *
 * @author Veteran Development Team
 */
public class SinglyLinkedList {

    private static class Node {
        Package data;
        Node next;
        Node(Package data) { this.data = data; this.next = null; }
    }

    private Node head;
    private Node tail;
    private int size;

    public SinglyLinkedList() { this.head = null; this.tail = null; this.size = 0; }

    /** Appends a package to the end — O(1) via tail pointer. */
    public void addRecord(Package pkg) {
        Node newNode = new Node(pkg);
        if (head == null) { head = newNode; tail = newNode; }
        else { tail.next = newNode; tail = newNode; }
        size++;
    }

    /** Traverses and prints all records — O(n). */
    public void displayLog() {
        if (head == null) { System.out.println("    (Kayit defteri bos)"); return; }
        Node current = head;
        int index = 1;
        while (current != null) {
            System.out.printf("    %3d. %s%n", index++, current.data);
            current = current.next;
        }
    }

    /** Linear search by package ID — O(n). */
    public Package search(String packageID) {
        Node current = head;
        while (current != null) {
            if (current.data.getPackageID().equalsIgnoreCase(packageID)) return current.data;
            current = current.next;
        }
        return null;
    }

    public int     getSize()  { return size; }
    public boolean isEmpty()  { return size == 0; }
}
