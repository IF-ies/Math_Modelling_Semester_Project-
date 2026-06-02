package structures.linear;

import models.Package;

/**
 * Veteran Logistics — Priority Queue (Binary Min-Heap)
 * Urgent Dispatch Queue
 *
 * A manually implemented priority queue backed by an array-based binary
 * min-heap. Packages with the LOWEST priority number are the most urgent
 * (1 = Highest, 5 = Lowest) and leave the queue first.
 *
 * Ordering rule:
 *   - Primary:  smaller priority number  → dispatched earlier
 *   - Tie-break: earlier insertion order → dispatched earlier (stable / FIFO)
 *
 * Operations:
 *   enqueue(Package)  — O(log n) insert + sift-up
 *   dequeue()         — O(log n) remove most urgent + sift-down
 *   peek()            — O(1) view most urgent without removing
 *   displayQueue()    — O(n log n) non-destructive print in priority order
 *
 * @author Veteran Development Team
 */
public class PriorityDeliveryQueue {

    /** Heap entry: the package plus its arrival sequence (for stable ties). */
    private static class Entry {
        Package pkg;
        long    seq;
        Entry(Package pkg, long seq) { this.pkg = pkg; this.seq = seq; }
    }

    private Entry[] heap;
    private int     size;
    private long    counter;   // ever-increasing arrival sequence number

    public PriorityDeliveryQueue() {
        this.heap    = new Entry[16];
        this.size    = 0;
        this.counter = 0;
    }

    /** Returns true if entry {@code a} is more urgent than entry {@code b}. */
    private boolean moreUrgent(Entry a, Entry b) {
        if (a.pkg.getPriority() != b.pkg.getPriority())
            return a.pkg.getPriority() < b.pkg.getPriority();
        return a.seq < b.seq;   // same priority → keep FIFO order
    }

    /** Inserts a package and restores the heap property — O(log n). */
    public void enqueue(Package pkg) {
        if (size == heap.length) grow();
        heap[size] = new Entry(pkg, counter++);
        siftUp(size);
        size++;
    }

    /** Removes and returns the most urgent package — O(log n). */
    public Package dequeue() {
        if (size == 0) { System.out.println("    ⚠ Priority queue is empty."); return null; }
        Package top = heap[0].pkg;
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) siftDown(0);
        return top;
    }

    /** Returns the most urgent package without removing it — O(1). */
    public Package peek() { return (size > 0) ? heap[0].pkg : null; }

    // ─── Heap maintenance ────────────────────────────────────────────
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (moreUrgent(heap[i], heap[parent])) { swap(i, parent); i = parent; }
            else break;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, best = i;
            if (left  < size && moreUrgent(heap[left],  heap[best])) best = left;
            if (right < size && moreUrgent(heap[right], heap[best])) best = right;
            if (best == i) break;
            swap(i, best);
            i = best;
        }
    }

    private void swap(int a, int b) { Entry t = heap[a]; heap[a] = heap[b]; heap[b] = t; }

    private void grow() {
        Entry[] bigger = new Entry[heap.length * 2];
        System.arraycopy(heap, 0, bigger, 0, heap.length);
        heap = bigger;
    }

    /**
     * Prints all packages in priority order (most urgent first) without
     * draining the heap. Works on a copy, so the queue stays intact.
     */
    public void displayQueue() {
        if (size == 0) { System.out.println("    (Priority queue is empty)"); return; }
        Entry[] copy = new Entry[size];
        System.arraycopy(heap, 0, copy, 0, size);
        // Selection sort on the copy using the same urgency rule.
        for (int i = 0; i < copy.length; i++) {
            int best = i;
            for (int j = i + 1; j < copy.length; j++)
                if (moreUrgent(copy[j], copy[best])) best = j;
            Entry t = copy[i]; copy[i] = copy[best]; copy[best] = t;
        }
        for (int i = 0; i < copy.length; i++)
            System.out.printf("    %3d. %s%n", i + 1, copy[i].pkg);
    }

    public int     getSize() { return size; }
    public boolean isEmpty() { return size == 0; }
}
