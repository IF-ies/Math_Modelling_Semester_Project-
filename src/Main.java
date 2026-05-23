import models.Package;
import structures.linear.SinglyLinkedList;
import structures.linear.DoublyLinkedList;
import structures.linear.DeliveryQueue;
import structures.linear.TruckStack;
import structures.tree.AVLTree;
import structures.graph.CityGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║                                                                    ║
 * ║            ███████╗██╗    ██╗██╗███████╗████████╗                   ║
 * ║            ██╔════╝██║    ██║██║██╔════╝╚══██╔══╝                   ║
 * ║            ███████╗██║ █╗ ██║██║█████╗     ██║                      ║
 * ║            ╚════██║██║███╗██║██║██╔══╝     ██║                      ║
 * ║            ███████║╚███╔███╔╝██║██║        ██║                      ║
 * ║            ╚══════╝ ╚══╝╚══╝ ╚═╝╚═╝        ╚═╝                      ║
 * ║                                                                    ║
 * ║            ██████╗  ██████╗ ██╗   ██╗████████╗███████╗             ║
 * ║            ██╔══██╗██╔═══██╗██║   ██║╚══██╔══╝██╔════╝             ║
 * ║            ██████╔╝██║   ██║██║   ██║   ██║   █████╗               ║
 * ║            ██╔══██╗██║   ██║██║   ██║   ██║   ██╔══╝               ║
 * ║            ██║  ██║╚██████╔╝╚██████╔╝   ██║   ███████╗             ║
 * ║            ╚═╝  ╚═╝ ╚═════╝  ╚═════╝    ╚═╝   ╚══════╝             ║
 * ║                                                                    ║
 * ║        Urban Logistics & Distribution System  v1.0                 ║
 * ║        Mission: Optimize urban package delivery using              ║
 * ║                 advanced data structures & graph algorithms.       ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * Main entry point — console-based interactive UI.
 *
 * @author SwiftRoute Development Team
 */
public class Main {

    // ═══════════════════════════════════════════════════════════════
    //  Company Branding
    // ═══════════════════════════════════════════════════════════════
    private static final String COMPANY_NAME = "Veteran";
    private static final String TAGLINE      = "Delivering Smarter, Faster, Together.";

    // ═══════════════════════════════════════════════════════════════
    //  Data Structures
    // ═══════════════════════════════════════════════════════════════
    private static final SinglyLinkedList masterRegistry  = new SinglyLinkedList();
    private static final DoublyLinkedList intakeBuffer    = new DoublyLinkedList();
    private static final DeliveryQueue    deliveryQueue   = new DeliveryQueue();
    private static final TruckStack       truckStack      = new TruckStack();
    private static final AVLTree          addressDirectory = new AVLTree();
    private static final CityGraph        cityMap         = new CityGraph();

    // ═══════════════════════════════════════════════════════════════
    //  File Paths
    // ═══════════════════════════════════════════════════════════════
    private static final String MAP_DATA_FILE     = "data/mapData.txt";
    private static final String PACKAGE_DATA_FILE = "data/packageData.txt";

    // ═══════════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        printBanner();

        // ── Step 1: Load external data ──────────────────────────
        System.out.println();
        printSectionHeader("PHASE 1 — DATA INGESTION");
        loadMapData();
        loadPackageData();

        // ── Step 2: Interactive menu ────────────────────────────
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("  [" + COMPANY_NAME + "] Select option ▸ ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showMasterRegistry();
                    break;
                case "2":
                    showIntakeBuffer();
                    break;
                case "3":
                    processDeliveryQueue(scanner);
                    break;
                case "4":
                    processTruckLoading(scanner);
                    break;
                case "5":
                    showAddressDirectory(scanner);
                    break;
                case "6":
                    showCityMap();
                    break;
                case "7":
                    findShortestPath(scanner);
                    break;
                case "8":
                    computeMST();
                    break;
                case "9":
                    runFullDemo();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("  ⚠ Invalid option. Please try again.");
            }
        }

        printFooter();
        scanner.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  FILE I/O
    // ═══════════════════════════════════════════════════════════════

    /**
     * Reads mapData.txt and populates the CityGraph.
     * Expected format per line: Source Destination Distance_KM
     */
    private static void loadMapData() {
        System.out.println("\n  ▶ Loading city map from: " + MAP_DATA_FILE);
        int edgeCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(MAP_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String source = parts[0];
                    String destination = parts[1];
                    int distance = Integer.parseInt(parts[2]);
                    cityMap.addEdge(source, destination, distance);
                    edgeCount++;
                }
            }
            System.out.printf("    ✔ Successfully loaded %d edges, %d vertices.%n",
                    edgeCount, cityMap.getVertexCount());
        } catch (IOException e) {
            System.out.println("    ✘ Error reading map data: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("    ✘ Invalid distance format in map data: " + e.getMessage());
        }
    }

    /**
     * Reads packageData.txt and:
     *   1) Registers each package in the Master Registry (SLL)
     *   2) Adds each package to the Intake Buffer (DLL)
     *   3) Inserts each destination into the Address Directory (AVL)
     *
     * Expected format per line: PackageID Destination
     */
    private static void loadPackageData() {
        System.out.println("\n  ▶ Loading packages from: " + PACKAGE_DATA_FILE);
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PACKAGE_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String packageID = parts[0];
                    String destination = parts[1];
                    Package pkg = new Package(packageID, destination);

                    // Register in Master Registry (SLL)
                    masterRegistry.addRecord(pkg);

                    // Add to Intake Buffer (DLL)
                    intakeBuffer.insertAtTail(pkg);

                    // Insert into Address Directory (AVL Tree)
                    addressDirectory.insert(destination, packageID);

                    count++;
                }
            }
            System.out.printf("    ✔ Successfully loaded %d packages.%n", count);
        } catch (IOException e) {
            System.out.println("    ✘ Error reading package data: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MENU ACTIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Option 1: Display Master Registry (SLL)
     */
    private static void showMasterRegistry() {
        printSectionHeader("MASTER REGISTRY (Singly Linked List)");
        System.out.println("  Total records: " + masterRegistry.getSize());
        masterRegistry.displayLog();
        printDivider();
    }

    /**
     * Option 2: Display Intake Buffer (DLL)
     */
    private static void showIntakeBuffer() {
        printSectionHeader("INTAKE BUFFER (Doubly Linked List)");
        System.out.println("  Packages in buffer: " + intakeBuffer.getSize());
        System.out.println("\n  [Head → Tail]:");
        intakeBuffer.displayBuffer();
        System.out.println("\n  [Tail → Head] (reverse):");
        intakeBuffer.displayReverse();
        printDivider();
    }

    /**
     * Option 3: Delivery Queue operations
     */
    private static void processDeliveryQueue(Scanner scanner) {
        printSectionHeader("STANDARD DELIVERY QUEUE (FIFO)");

        System.out.println("  [1] Enqueue packages from buffer");
        System.out.println("  [2] Dequeue next package");
        System.out.println("  [3] Display queue");
        System.out.print("  Select ▸ ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1":
                // Move packages from intake buffer to delivery queue
                int moved = 0;
                while (!intakeBuffer.isEmpty()) {
                    Package pkg = intakeBuffer.removeFromHead();
                    if (pkg != null) {
                        deliveryQueue.enqueue(pkg);
                        moved++;
                    }
                }
                System.out.printf("  ✔ Enqueued %d packages from buffer to delivery queue.%n", moved);
                break;
            case "2":
                Package dequeued = deliveryQueue.dequeue();
                if (dequeued != null) {
                    System.out.println("  ✔ Dequeued: " + dequeued);
                }
                break;
            case "3":
                System.out.println("  Queue contents (front → rear):");
                deliveryQueue.displayQueue();
                break;
            default:
                System.out.println("  ⚠ Invalid sub-option.");
        }
        printDivider();
    }

    /**
     * Option 4: Truck Stack operations
     */
    private static void processTruckLoading(Scanner scanner) {
        printSectionHeader("TRUCK LOADING BAY (LIFO Stack)");

        System.out.println("  [1] Load packages from queue onto truck");
        System.out.println("  [2] Unload top package from truck");
        System.out.println("  [3] Display truck stack");
        System.out.print("  Select ▸ ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1":
                int loaded = 0;
                while (!deliveryQueue.isEmpty()) {
                    Package pkg = deliveryQueue.dequeue();
                    if (pkg != null) {
                        truckStack.push(pkg);
                        loaded++;
                    }
                }
                System.out.printf("  ✔ Loaded %d packages onto the truck.%n", loaded);
                break;
            case "2":
                Package popped = truckStack.pop();
                if (popped != null) {
                    System.out.println("  ✔ Unloaded: " + popped);
                }
                break;
            case "3":
                System.out.println("  Truck stack contents:");
                truckStack.displayStack();
                break;
            default:
                System.out.println("  ⚠ Invalid sub-option.");
        }
        printDivider();
    }

    /**
     * Option 5: Address Directory (AVL Tree)
     */
    private static void showAddressDirectory(Scanner scanner) {
        printSectionHeader("ADDRESS DIRECTORY (AVL Tree)");

        System.out.println("  [1] Display directory (in-order)");
        System.out.println("  [2] Display tree structure");
        System.out.println("  [3] Search for a neighborhood");
        System.out.println("  [4] Insert a new entry");
        System.out.print("  Select ▸ ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1":
                System.out.println("  Sorted directory (" + addressDirectory.getNodeCount()
                        + " entries, tree height = " + addressDirectory.getTreeHeight() + "):");
                addressDirectory.inOrderTraversal();
                break;
            case "2":
                System.out.println("  Tree structure:");
                addressDirectory.printTree();
                break;
            case "3":
                System.out.print("  Enter neighborhood name ▸ ");
                String searchKey = scanner.nextLine().trim();
                String result = addressDirectory.search(searchKey);
                if (result != null) {
                    System.out.println("  ✔ Found: " + searchKey + " → Customer: " + result);
                } else {
                    System.out.println("  ✘ Neighborhood '" + searchKey + "' not found.");
                }
                break;
            case "4":
                System.out.print("  Enter neighborhood ▸ ");
                String neighborhood = scanner.nextLine().trim();
                System.out.print("  Enter customer ID  ▸ ");
                String custID = scanner.nextLine().trim();
                addressDirectory.insert(neighborhood, custID);
                System.out.println("  ✔ Inserted: " + neighborhood + " → " + custID);
                break;
            default:
                System.out.println("  ⚠ Invalid sub-option.");
        }
        printDivider();
    }

    /**
     * Option 6: Display City Map (Graph adjacency list)
     */
    private static void showCityMap() {
        printSectionHeader("CITY MAP (Weighted Graph — Adjacency List)");
        System.out.println("  Vertices: " + cityMap.getVertexCount());
        cityMap.displayGraph();
        printDivider();
    }

    /**
     * Option 7: Find Shortest Path (Dijkstra)
     */
    private static void findShortestPath(Scanner scanner) {
        printSectionHeader("SHORTEST PATH — Dijkstra's Algorithm");

        System.out.println("  Available locations:");
        String[] names = cityMap.getVertexNames();
        for (int i = 0; i < names.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, names[i]);
        }

        System.out.print("  Enter START location ▸ ");
        String start = scanner.nextLine().trim();
        System.out.print("  Enter END   location ▸ ");
        String end = scanner.nextLine().trim();

        System.out.println();
        cityMap.calculateShortestPath(start, end);
        printDivider();
    }

    /**
     * Option 8: Compute MST (Prim's Algorithm)
     */
    private static void computeMST() {
        printSectionHeader("MINIMUM SPANNING TREE — Prim's Algorithm");
        cityMap.calculateMST();
        printDivider();
    }

    /**
     * Option 9: Full automated demo showcasing all structures
     */
    private static void runFullDemo() {
        printSectionHeader("FULL SYSTEM DEMONSTRATION");
        System.out.println("  Running end-to-end demo of all " + COMPANY_NAME + " subsystems...\n");

        // ── 1. Master Registry (SLL) ────────────────────────────
        printSubSection("1. MASTER REGISTRY (Singly Linked List)");
        System.out.println("  All packages ever received:");
        masterRegistry.displayLog();

        // Search demo
        System.out.println("\n  Searching for PKG-005:");
        Package found = masterRegistry.search("PKG-005");
        if (found != null) {
            System.out.println("    ✔ Found: " + found);
        } else {
            System.out.println("    ✘ Not found.");
        }

        // ── 2. Intake Buffer (DLL) ─────────────────────────────
        printSubSection("2. INTAKE BUFFER (Doubly Linked List)");

        // Re-populate buffer for demo if empty
        if (intakeBuffer.isEmpty()) {
            System.out.println("  Re-populating buffer for demonstration...");
            intakeBuffer.insertAtTail(new Package("DEMO-001", "Kadikoy"));
            intakeBuffer.insertAtTail(new Package("DEMO-002", "Besiktas"));
            intakeBuffer.insertAtTail(new Package("DEMO-003", "Uskudar"));
            intakeBuffer.insertAtHead(new Package("DEMO-URGENT", "Fatih", 1, 0.5));
            System.out.println("  Inserted 3 at tail + 1 urgent at head.");
        }
        System.out.println("\n  Buffer contents (Head → Tail):");
        intakeBuffer.displayBuffer();
        System.out.println("\n  Removing from head:");
        Package removedHead = intakeBuffer.removeFromHead();
        if (removedHead != null) {
            System.out.println("    Removed: " + removedHead);
        }
        System.out.println("  Buffer after removal:");
        intakeBuffer.displayBuffer();

        // ── 3. Delivery Queue (FIFO) ───────────────────────────
        printSubSection("3. DELIVERY QUEUE (FIFO Queue)");
        // Move buffer → queue
        int enqueued = 0;
        while (!intakeBuffer.isEmpty()) {
            Package pkg = intakeBuffer.removeFromHead();
            if (pkg != null) {
                deliveryQueue.enqueue(pkg);
                enqueued++;
            }
        }
        System.out.printf("  Moved %d packages from buffer → queue.%n", enqueued);
        System.out.println("  Queue contents (front → rear):");
        deliveryQueue.displayQueue();

        System.out.println("\n  Dequeueing front package:");
        Package dq = deliveryQueue.dequeue();
        if (dq != null) {
            System.out.println("    Dequeued: " + dq);
        }
        System.out.println("  Queue after dequeue:");
        deliveryQueue.displayQueue();

        // ── 4. Truck Stack (LIFO) ──────────────────────────────
        printSubSection("4. TRUCK LOADING (LIFO Stack)");
        int loaded = 0;
        while (!deliveryQueue.isEmpty()) {
            Package pkg = deliveryQueue.dequeue();
            if (pkg != null) {
                truckStack.push(pkg);
                loaded++;
            }
        }
        System.out.printf("  Loaded %d packages onto truck.%n", loaded);
        System.out.println("  Truck stack:");
        truckStack.displayStack();

        System.out.println("\n  Unloading at destination (LIFO order):");
        while (!truckStack.isEmpty()) {
            Package p = truckStack.pop();
            System.out.println("    📦 Delivered: " + p);
        }

        // ── 5. Address Directory (AVL Tree) ────────────────────
        printSubSection("5. ADDRESS DIRECTORY (AVL Tree)");
        System.out.println("  Entries: " + addressDirectory.getNodeCount()
                + " | Tree height: " + addressDirectory.getTreeHeight());
        System.out.println("\n  In-order traversal (sorted by neighborhood):");
        addressDirectory.inOrderTraversal();
        System.out.println("\n  Tree structure:");
        addressDirectory.printTree();

        // Search demo
        String[] searchTerms = {"Besiktas", "Fatih", "NonExistent"};
        System.out.println("\n  Search demonstrations:");
        for (String term : searchTerms) {
            String res = addressDirectory.search(term);
            if (res != null) {
                System.out.printf("    ✔ '%s' → Customer: %s%n", term, res);
            } else {
                System.out.printf("    ✘ '%s' → NOT FOUND%n", term);
            }
        }

        // ── 6. City Map (Graph) ────────────────────────────────
        printSubSection("6. CITY MAP (Weighted Graph)");
        System.out.println("  Adjacency list representation:");
        cityMap.displayGraph();

        // ── 7. Dijkstra's Algorithm ────────────────────────────
        printSubSection("7. SHORTEST PATH — Dijkstra's Algorithm");
        System.out.println("  Route: Warehouse → Bakirkoy");
        cityMap.calculateShortestPath("Warehouse", "Bakirkoy");
        System.out.println();
        System.out.println("  Route: Uskudar → Beyoglu");
        cityMap.calculateShortestPath("Uskudar", "Beyoglu");

        // ── 8. Prim's MST ──────────────────────────────────────
        printSubSection("8. MINIMUM SPANNING TREE — Prim's Algorithm");
        cityMap.calculateMST();

        // ── Done ────────────────────────────────────────────────
        System.out.println();
        printDivider();
        System.out.println("  ✔ Full demonstration complete.");
        System.out.println("    All " + COMPANY_NAME + " subsystems operational.");
        printDivider();
    }

    // ═══════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════════

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                            ║");
        System.out.println("  ║            ░██████╗░██╗░░░░░░░██╗░██╗███████╗████████╗      ║");
        System.out.println("  ║            ██╔════╝░██║░░██╗░░██║░██║██╔════╝╚══██╔══╝      ║");
        System.out.println("  ║            ╚█████╗░░╚██╗████╗██╔╝░██║█████╗░░░░██║░░░      ║");
        System.out.println("  ║            ░╚═══██╗░░████╔═████║░░██║██╔══╝░░░░██║░░░      ║");
        System.out.println("  ║            ██████╔╝░░╚██╔╝░╚██╔╝░░██║██║░░░░░░░██║░░░      ║");
        System.out.println("  ║            ╚═════╝░░░░╚═╝░░░╚═╝░░░╚═╝╚═╝░░░░░░░╚═╝░░░      ║");
        System.out.println("  ║                                                            ║");
        System.out.println("  ║            ██████╗░░█████╗░██╗░░░██╗████████╗███████╗       ║");
        System.out.println("  ║            ██╔══██╗██╔══██╗██║░░░██║╚══██╔══╝██╔════╝       ║");
        System.out.println("  ║            ██████╔╝██║░░██║██║░░░██║░░░██║░░░█████╗░        ║");
        System.out.println("  ║            ██╔══██╗██║░░██║██║░░░██║░░░██║░░░██╔══╝░        ║");
        System.out.println("  ║            ██║░░██║╚█████╔╝╚██████╔╝░░░██║░░░███████╗       ║");
        System.out.println("  ║            ╚═╝░░╚═╝░╚════╝░░╚═════╝░░░░╚═╝░░░╚══════╝       ║");
        System.out.println("  ║                                                            ║");
        System.out.println("  ║        " + COMPANY_NAME + "                                    ║");
        System.out.println("  ║        Urban Logistics & Distribution System  v1.0         ║");
        System.out.println("  ║        " + TAGLINE + "              ║");
        System.out.println("  ║                                                            ║");
        System.out.println("  ║  Mission: Optimize urban package delivery using advanced   ║");
        System.out.println("  ║           data structures and graph algorithms.            ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ┌──────────────────────────────────────────────────────────────┐");
        System.out.println("  │              " + COMPANY_NAME + " — Main Menu                 │");
        System.out.println("  ├──────────────────────────────────────────────────────────────┤");
        System.out.println("  │  [1]  View Master Registry        (Singly Linked List)      │");
        System.out.println("  │  [2]  View Intake Buffer          (Doubly Linked List)      │");
        System.out.println("  │  [3]  Delivery Queue Operations   (FIFO Queue)              │");
        System.out.println("  │  [4]  Truck Loading Operations    (LIFO Stack)              │");
        System.out.println("  │  [5]  Address Directory            (AVL Tree)               │");
        System.out.println("  │  [6]  View City Map               (Weighted Graph)          │");
        System.out.println("  │  [7]  Find Shortest Path          (Dijkstra's Algorithm)    │");
        System.out.println("  │  [8]  Compute MST                 (Prim's Algorithm)        │");
        System.out.println("  │  [9]  ★ Run Full Demo             (All Subsystems)          │");
        System.out.println("  │  [0]  Exit                                                  │");
        System.out.println("  └──────────────────────────────────────────────────────────────┘");
    }

    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════════════════");
        System.out.printf("   %s — %s%n", COMPANY_NAME, title);
        System.out.println("  ══════════════════════════════════════════════════════════════");
    }

    private static void printSubSection(String title) {
        System.out.println();
        System.out.println("  ──────────────────────────────────────────────────────────────");
        System.out.println("   " + title);
        System.out.println("  ──────────────────────────────────────────────────────────────");
    }

    private static void printDivider() {
        System.out.println("  ──────────────────────────────────────────────────────────────");
    }

    private static void printFooter() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║  Thank you for using " + COMPANY_NAME + "!                    ║");
        System.out.println("  ║  " + TAGLINE + "                          ║");
        System.out.println("  ║  © 2026 " + COMPANY_NAME + ". All rights reserved.           ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
