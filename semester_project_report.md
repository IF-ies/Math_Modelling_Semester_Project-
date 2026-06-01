# Urban Logistics & Distribution System

## Semester Project Report

---

<div align="center">

**ABDULLAH GÜL UNIVERSITY (AGÜ)**

**Faculty of Engineering — Department of Computer Engineering**

**MATHEMATICAL MODELLING AND ALGORITHMIC THINKING-S1**

---

**Group Name: Veteran**

| # | Name Surname | Student ID |
|---|-------------|------------|
| 1 | Ahmet Uzungöl | 2211051063 |
| 2 | Sümeyra Yıldız | 2211051070 |
| 3 | Züheyr Temel | 2211051067 |
| 4 | Abdullah İnce | 2211051010 |
| 5 | İbrahim Furkan Yılmaz | 2211051013 |

**Instructor:** Cavidan Yakupoğlu Karaağaç

**Date:** May 2026

</div>

---

## Table of Contents

1. [Corporate Identity & Mission Statement](#1-corporate-identity--mission-statement)
2. [System Architecture Overview](#2-system-architecture-overview)
3. [Technical Requirements & Implementation Logic](#3-technical-requirements--implementation-logic)
   - 3.1 Domain Model — Package
   - 3.2 Master Registry — Singly Linked List
   - 3.3 Intake Buffer — Doubly Linked List
   - 3.4 Standard Delivery — Queue (FIFO)
   - 3.5 Truck Loading — Stack (LIFO)
   - 3.6 Address Directory — AVL Tree
   - 3.7 City Map & Routing — Weighted Graph
4. [Complexity Analysis](#4-complexity-analysis)
5. [File I/O & Data Ingestion](#5-file-io--data-ingestion)
6. [Conclusion](#6-conclusion)

---

## 1. Corporate Identity & Mission Statement

**Company Name:** Veteran

**Tagline:** *Delivering Smarter, Faster, Together.*

**Mission Statement:**
Veteran, Kayseri'nin metropoliten alanında son mil paket teslimatını optimize etmeyi hedefleyen bir kentsel lojistik teknoloji şirketidir. Merkez deposu Meydan semtinde konumlanan Veteran, ileri veri yapıları ve graf algoritmalarından yararlanarak her paketin en verimli rotayla teslim edilmesini sağlar. Alpaslan, Talas, Erkilet, Belsin, İldem, Mimsin, Anbar ve Kocasinan gibi Kayseri semtleri arasındaki bağlantıları modelleyen ağ yapısı; Dijkstra'nın en kısa yol algoritması, Prim'in Minimum Spanning Tree algoritması, AVL ağacı tabanlı adres rehberi ve immutable master kayıt defteri aracılığıyla yönetilmektedir.

---

## 2. System Architecture Overview

The system is composed of **eight Java classes** organized into a clean package hierarchy, strictly following Object-Oriented Programming (OOP) principles. Every core data structure is implemented **manually from scratch** — no `java.util.LinkedList`, `java.util.Stack`, `java.util.Queue`, `java.util.HashMap`, or any similar built-in collection is used for the core logic.

```
src/
├── Main.java                            // Console UI, File I/O, Demo
├── models/
│   └── Package.java                     // Domain model
└── structures/
    ├── linear/
    │   ├── SinglyLinkedList.java         // Master Registry
    │   ├── DoublyLinkedList.java         // Intake Buffer
    │   ├── DeliveryQueue.java            // FIFO Queue
    │   └── TruckStack.java              // LIFO Stack
    ├── tree/
    │   └── AVLTree.java                  // Address Directory
    └── graph/
        └── CityGraph.java                // City Map + Dijkstra + Prim
```

**Kayseri Şehir Haritası (mapData.txt):**

| Kaynak | Hedef | Mesafe (km) |
|--------|-------|-------------|
| Meydan | Alpaslan | 4 |
| Meydan | Talas | 8 |
| Meydan | Erkilet | 10 |
| Meydan | Belsin | 12 |
| Meydan | Kocasinan | 7 |
| Alpaslan | Talas | 5 |
| Alpaslan | Erkilet | 9 |
| Alpaslan | Ildem | 12 |
| Talas | Mimsin | 11 |
| Talas | Ildem | 8 |
| Belsin | Anbar | 3 |
| Belsin | Erkilet | 14 |
| Ildem | Mimsin | 6 |
| Kocasinan | Alpaslan | 6 |
| Kocasinan | Belsin | 9 |

**Data flow through the system:**

```
                ┌─────────────┐
                │ packageData │
                │    .txt     │
                └──────┬──────┘
                       │ parse
                       ▼
              ┌─────────────────┐
              │ Master Registry │──── permanent, append-only log (SLL)
              │   (SLL)         │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │  Intake Buffer  │──── temporary staging area (DLL)
              │    (DLL)        │
              └────────┬────────┘
                       │ removeFromHead()
                       ▼
              ┌─────────────────┐
              │ Delivery Queue  │──── FIFO dispatch scheduling
              │   (Queue)       │
              └────────┬────────┘
                       │ dequeue()
                       ▼
              ┌─────────────────┐
              │  Truck Stack    │──── LIFO loading onto vehicle
              │   (Stack)       │
              └────────┬────────┘
                       │ pop()
                       ▼
                  📦 Delivered
```

---

## 3. Technical Requirements & Implementation Logic

### 3.1 Domain Model — `Package.java`

The `Package` class encapsulates four attributes: `packageID` (String, e.g. `PKG_KYS_001`), `destination` (String, e.g. `Talas`), `priority` (int, 1–5), and `weightKg` (double). It provides two constructors: a full four-argument constructor and a simplified two-argument constructor that defaults `priority` to 3 and `weightKg` to 1.0.

### 3.2 Master Registry — Singly Linked List (SLL)

**Purpose:** An immutable, append-only audit log of every package that enters the Veteran system.

**Implementation details:**

The `SinglyLinkedList` class uses a private inner `Node` class containing a `Package data` field and a `Node next` pointer. The list maintains **three fields**: `head`, `tail`, and `size`.

- **`addRecord(Package pkg)`** — Creates a new `Node`, and if the list is empty (`head == null`), sets both `head` and `tail` to the new node. Otherwise, it links `tail.next` to the new node and advances the `tail` pointer. The `size` counter is incremented. Because a **dedicated `tail` pointer** is maintained, this operation avoids traversal entirely and executes in **O(1)** time.

- **`displayLog()`** — Iterates from `head` to `null` following `next` pointers, printing each package with a numbered index. Complexity: **O(n)**.

- **`search(String packageID)`** — Performs a linear scan from `head`, comparing each node's `data.getPackageID()` via `equalsIgnoreCase()`. Returns the matching `Package` or `null`. Complexity: **O(n)** worst-case.

- **`getSize()` / `isEmpty()`** — Return the pre-maintained `size` field directly. Complexity: **O(1)**.

### 3.3 Intake Buffer — Doubly Linked List (DLL)

**Purpose:** A flexible staging buffer where incoming packages are held before being dispatched to the Queue or Stack.

**Implementation details:**

The `DoublyLinkedList` class uses a private inner `Node` with three fields: `Package data`, `Node prev`, and `Node next`. The list maintains `head`, `tail`, and `size`.

- **`insertAtTail(Package pkg)`** — Direct `tail` pointer access ensures **O(1)**.

- **`insertAtHead(Package pkg)`** — Direct `head` pointer access ensures **O(1)**.

- **`removeFromHead()`** — `head` advances to `head.next` and the new head's `prev` is nulled. **O(1)**.

- **`removeFromTail()`** — `tail` retreats to `tail.prev`; the `prev` pointer enables this to be **O(1)** (unlike SLL which would require O(n)).

- **`removePackage(String packageID)`** — Linear traversal from `head`. **O(n)** worst-case.

- **`displayBuffer()` / `displayReverse()`** — Forward via `next`, backward via `prev`. Both **O(n)**.

### 3.4 Standard Delivery — Queue (FIFO)

**Purpose:** First-In, First-Out scheduling of packages for standard delivery processing.

The `DeliveryQueue` class is backed by a **singly linked list** with `front` and `rear` pointers.

- **`enqueue(Package pkg)`** — **O(1)** via the `rear` pointer.
- **`dequeue()`** — **O(1)** direct pointer manipulation.
- **`peek()`** — Returns `front.data` without mutation. **O(1)**.

### 3.5 Truck Loading — Stack (LIFO)

**Purpose:** Last-In, First-Out loading of packages onto the delivery truck.

The `TruckStack` class is backed by a **singly linked list** using only a `top` pointer.

- **`push(Package pkg)`** — New node's `next` points to current `top`. **O(1)**.
- **`pop()`** — Advances `top` to `top.next`. **O(1)**.
- **`peek()`** — Returns `top.data` without removal. **O(1)**.

### 3.6 Address Directory — AVL Tree

**Purpose:** A self-balancing binary search tree that stores Kayseri neighborhood names (keys, compared lexicographically) and associated customer IDs.

#### Height & Balance Factor

- **`height(AVLNode)`** returns `node.height` or 0 if null.
- **`updateHeight(AVLNode)`** sets `node.height = 1 + max(height(left), height(right))`.
- **`getBalanceFactor(AVLNode)`** returns `height(left) - height(right)`.

#### Rotation Logic

**Left Rotation (`rotateLeft(AVLNode x)`):**
```
      x                y
       \              / \
        y     →      x   C
       / \            \
      B   C            B
```

**Right Rotation (`rotateRight(AVLNode y)`):**
```
        y            x
       /            / \
      x     →     A   y
     / \              /
    A   B            B
```

**`balance(AVLNode node)`** handles all four cases:

| Case | Condition | Action |
|------|-----------|--------|
| **LL** | `bf > 1` and `bf(left) >= 0` | Single right rotation |
| **LR** | `bf > 1` and `bf(left) < 0` | Left-rotate left child, then right-rotate node |
| **RR** | `bf < -1` and `bf(right) <= 0` | Single left rotation |
| **RL** | `bf < -1` and `bf(right) > 0` | Right-rotate right child, then left-rotate node |

### 3.7 City Map & Routing — Weighted Graph

**Purpose:** Models Kayseri's road network as a weighted, undirected graph. Merkez depo: **Meydan**. Semtler: Alpaslan, Talas, Erkilet, Belsin, İldem, Mimsin, Anbar, Kocasinan.

**Internal representation:**
- `String[] vertexNames` — maps integer index → location name (capacity: 100).
- `EdgeNode[] adjacencyList` — array of linked-list heads.
- `int vertexCount` — tracks the number of vertices added.

#### Dijkstra's Algorithm — `calculateShortestPath(String start, String end)`

O(V²) array-scanning variant:

1. `dist[]` initialized to `Integer.MAX_VALUE`; source distance set to 0.
2. Main loop: find unvisited vertex `u` with smallest `dist[u]` — **O(V)** per iteration.
3. Relaxation: for each neighbor `v`, if `dist[u] + weight < dist[v]`, update `dist[v]` and `prev[v]`.
4. Path reconstruction via `prev[]` array.

**Total complexity: O(V²)**

#### Prim's Algorithm — `calculateMST()`

Same O(V²) structure:

1. `key[]` initialized to `Integer.MAX_VALUE`; `key[0] = 0`.
2. Main loop: find min `key[i]` not in MST — **O(V)** per iteration.
3. Update neighbor keys; accumulate `totalWeight`.

**Total complexity: O(V²)**

---

## 4. Complexity Analysis

### 4.1 Linear Data Structure Operations

| Operation | Structure | Best Case | Worst Case | Space | Justification |
|-----------|-----------|-----------|------------|-------|---------------|
| `addRecord(pkg)` | SLL | **O(1)** | **O(1)** | O(1) | Direct `tail.next` assignment; tail pointer maintained |
| `displayLog()` | SLL | **O(n)** | **O(n)** | O(1) | Full traversal from `head` to `null` |
| `search(id)` | SLL | **O(1)** | **O(n)** | O(1) | Best: match at head. Worst: match at tail or absent |
| `insertAtTail(pkg)` | DLL | **O(1)** | **O(1)** | O(1) | Direct `tail` pointer access; no traversal |
| `insertAtHead(pkg)` | DLL | **O(1)** | **O(1)** | O(1) | Direct `head` pointer access; no traversal |
| `removeFromHead()` | DLL | **O(1)** | **O(1)** | O(1) | Direct `head` pointer; `prev`/`next` relinking |
| `removeFromTail()` | DLL | **O(1)** | **O(1)** | O(1) | Direct `tail` pointer; `prev` pointer enables O(1) |
| `removePackage(id)` | DLL | **O(1)** | **O(n)** | O(1) | Best: match at head. Worst: linear scan to tail |
| `enqueue(pkg)` | Queue | **O(1)** | **O(1)** | O(1) | Direct `rear.next` assignment |
| `dequeue()` | Queue | **O(1)** | **O(1)** | O(1) | Direct `front` pointer advancement |
| `push(pkg)` | Stack | **O(1)** | **O(1)** | O(1) | Prepend to `top`; single pointer update |
| `pop()` | Stack | **O(1)** | **O(1)** | O(1) | Advance `top` to `top.next` |

### 4.2 Hierarchical Data Structure Operations

| Operation | Structure | Best Case | Worst Case | Space | Justification |
|-----------|-----------|-----------|------------|-------|---------------|
| `insert(nbhd, cid)` | AVL Tree | **O(log n)** | **O(log n)** | O(log n) | AVL self-balancing guarantees height ≤ 1.44·log₂(n). |
| `search(nbhd)` | AVL Tree | **O(1)** | **O(log n)** | O(log n) | Best: root match. Worst: leaf-level or absent. |
| `inOrderTraversal()` | AVL Tree | **O(n)** | **O(n)** | O(log n) | Visits every node exactly once. |
| `balance(node)` | AVL Tree | **O(1)** | **O(1)** | O(1) | At most 2 rotations; each O(1). |
| `rotateLeft(x)` | AVL Tree | **O(1)** | **O(1)** | O(1) | Three pointer reassignments + two `updateHeight()`. |
| `rotateRight(y)` | AVL Tree | **O(1)** | **O(1)** | O(1) | Three pointer reassignments + two `updateHeight()`. |

### 4.3 Graph Algorithm Operations

Let V = number of vertices, E = number of edges.

| Operation | Algorithm | Best Case | Worst Case | Space | Justification |
|-----------|-----------|-----------|------------|-------|---------------|
| `addEdge(s, d, w)` | — | **O(1)** | **O(V)** | O(1) | `getOrCreateVertexIndex()` scans up to V names. |
| `calculateShortestPath(s, e)` | Dijkstra | **O(V²)** | **O(V²)** | O(V) | V iterations × O(V) min-scan + O(E) relaxations. |
| `calculateMST()` | Prim | **O(V²)** | **O(V²)** | O(V) | Identical structure to Dijkstra. |
| `displayGraph()` | — | **O(V + E)** | **O(V + E)** | O(1) | Iterates every vertex and every edge. |

### 4.4 Overall Space Complexity

| Structure | Space Complexity | Note |
|-----------|-----------------|------|
| SLL (Master Registry) | **O(n)** | One `Node` per package |
| DLL (Intake Buffer) | **O(n)** | One `Node` per package |
| Queue | **O(n)** | Singly-linked nodes |
| Stack | **O(n)** | Singly-linked nodes |
| AVL Tree | **O(n)** | One `AVLNode` per unique neighborhood |
| Graph | **O(V + E)** | Vertex name array + adjacency list edge nodes |

---

## 5. File I/O & Data Ingestion

The system reads two external text files at startup using `java.io.BufferedReader` and `java.io.FileReader`:

### `mapData.txt`

**Format:** `Source Destination Distance_KM` (whitespace-delimited, one edge per line)

Merkez depo **Meydan**'dan hareketle Kayseri semtleri (Alpaslan, Talas, Erkilet, Belsin, İldem, Mimsin, Anbar, Kocasinan) arasındaki 15 kenar yüklenmektedir. Yorum satırları `#` ile başlar ve atlanır.

### `packageData.txt`

**Format:** `PackageID Destination` (whitespace-delimited, one package per line)

Format: `PKG_KYS_XXX`. Her satır okunduğunda:
1. **Master Registry**'e `addRecord(pkg)` ile eklenir (SLL)
2. **Intake Buffer**'a `insertAtTail(pkg)` ile eklenir (DLL)
3. **Address Directory**'e `insert(destination, packageID)` ile eklenir (AVL)

Demonstrasyon veri seti **12 paket** ve Kayseri'nin 8 farklı semtini kapsamaktadır.

---

## 6. Conclusion

The Veteran Urban Logistics & Distribution System demonstrates a complete, end-to-end application of fundamental and advanced data structures to solve a real-world logistics problem for the city of Kayseri. Every data structure — Singly Linked List, Doubly Linked List, Queue, Stack, AVL Tree, and Weighted Graph — was implemented manually from scratch without relying on Java's built-in collection framework.

The system achieves the following performance guarantees:

- **O(1)** amortized insertion and removal for all linear structures (SLL append, DLL head/tail operations, Queue enqueue/dequeue, Stack push/pop), enabled by the deliberate use of `head`, `tail`, `front`, `rear`, and `top` pointers.
- **O(log n)** guaranteed search and insertion in the Address Directory, thanks to the AVL tree's self-balancing rotations that maintain height within 1.44·log₂(n).
- **O(V²)** shortest-path and minimum spanning tree computations via Dijkstra's and Prim's algorithms, appropriate for Kayseri's urban road network.

---

<div align="center">

*© 2026 Veteran — Abdullah Gül University, Department of Computer Engineering*

*Mathematical Modelling and Algorithmic Thinking-S1*

</div>
