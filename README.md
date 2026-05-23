# Veteran - Urban Logistics & Distribution System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Data Structures](https://img.shields.io/badge/Data_Structures-000000?style=for-the-badge)
![Algorithms](https://img.shields.io/badge/Algorithms-4B0082?style=for-the-badge)

**Veteran** is an urban logistics technology simulation built in Java. Its mission is to optimize last-mile package delivery across metropolitan areas by leveraging advanced data structures and graph algorithms.

This project was developed as a semester project for the **Mathematical Modelling and Algorithmic Thinking** course at **Abdullah Gül University (AGÜ)**.

## 🚀 Features & Data Structures

What makes this project special is that **all data structures are implemented manually from scratch** (no `java.util.*` collections are used for the core logic). 

* **Master Registry (Singly Linked List - SLL):** An immutable, append-only audit log of every package that enters the system.
* **Intake Buffer (Doubly Linked List - DLL):** A flexible staging buffer where incoming packages are held before dispatch.
* **Standard Delivery (FIFO Queue):** First-In, First-Out scheduling for standard delivery processing.
* **Truck Loading (LIFO Stack):** Last-In, First-Out loading mechanism so that the last package loaded is the first one unloaded.
* **Address Directory (AVL Tree):** A self-balancing binary search tree mapping neighborhoods to customer IDs with `O(log n)` search performance.
* **City Map & Routing (Weighted Graph):** Models the city's road network using an adjacency-list representation.
  * **Shortest Path (Dijkstra's Algorithm):** Finds the fastest delivery route between locations.
  * **Infrastructure Optimization (Prim's Algorithm):** Computes the Minimum Spanning Tree (MST) for the most cost-efficient road network.

## 📁 Project Structure

```text
Math_Modelling_Semester_Project/
├── data/
│   ├── mapData.txt              # City road network (Source, Dest, Distance)
│   └── packageData.txt          # Package manifest
├── src/
│   ├── Main.java                # Console UI & Execution
│   ├── models/
│   │   └── Package.java
│   ├── structures/
│   │   ├── linear/              # SLL, DLL, Queue, Stack
│   │   ├── tree/                # AVL Tree
│   │   └── graph/               # Graph, Dijkstra, Prim
├── baslat.bat                   # Windows execution script
└── semester_project_report.md   # Comprehensive Academic Report
```

## 🛠️ How to Run

### Windows (Easiest Way)
Simply double-click the **`baslat.bat`** file in the root directory. It will compile all the Java files and launch the interactive Console UI automatically.

### Manual Compilation
```bash
# Compile the project
javac -d out src/models/Package.java src/structures/linear/*.java src/structures/tree/*.java src/structures/graph/*.java src/Main.java

# Run the program
java -cp out Main
```

## 📜 Demonstration
The system includes a **Full Demo Mode (Option 9)** in the main menu which automatically ingests data from `.txt` files and sequentially executes operations across all data structures to demonstrate data flow and algorithmic correctness.

## 👥 Authors
* **İbrahim Furkan Yılmaz** (Group: Veteran)
* Abdullah Gül University - Computer Engineering Department
