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
 * ║        Kayseri Urban Delivery & Logistics Management              ║
 * ║        Mission: Optimize urban package delivery using              ║
 * ║                 advanced data structures & graph algorithms.       ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * Main entry point — console-based interactive UI.
 *
 * @author Veteran Development Team
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
    private static final SinglyLinkedList masterRegistry   = new SinglyLinkedList();
    private static final DoublyLinkedList intakeBuffer     = new DoublyLinkedList();
    private static final DeliveryQueue    deliveryQueue    = new DeliveryQueue();
    private static final TruckStack       truckStack       = new TruckStack();
    private static final AVLTree          addressDirectory = new AVLTree();
    private static final CityGraph        cityMap          = new CityGraph();

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
            System.out.print("  [" + COMPANY_NAME + "] Make a choice ▸ ");
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
                case "10":
                    addNewPackage(scanner);
                    break;
                case "11":
                    addNewRoute(scanner);
                    break;
                case "12":
                    deletePackage(scanner);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("  ⚠ Invalid choice. Please try again.");
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
     * Central depot: Meydan (Kayseri)
     */
    private static void loadMapData() {
        System.out.println("\n  ▶ Loading Kayseri city map: " + MAP_DATA_FILE);
        int edgeCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(MAP_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String source      = parts[0];
                    String destination = parts[1];
                    int distance       = Integer.parseInt(parts[2]);
                    cityMap.addEdge(source, destination, distance);
                    edgeCount++;
                }
            }
            System.out.printf("    ✔ Map loaded: %d edges, %d vertices.%n",
                    edgeCount, cityMap.getVertexCount());
        } catch (IOException e) {
            System.out.println("    ✘ Could not read map data: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("    ✘ Invalid distance format: " + e.getMessage());
        }
    }

    /**
     * Reads packageData.txt and:
     * 1) Registers each package in the Master Registry (SLL)
     * 2) Adds each package to the Intake Buffer (DLL)
     * 3) Inserts each destination into the Address Directory (AVL)
     *
     * Expected format per line: PackageID Destination
     */
    private static void loadPackageData() {
        System.out.println("\n  ▶ Loading Kayseri package data: " + PACKAGE_DATA_FILE);
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PACKAGE_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String packageID   = parts[0];
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
            System.out.printf("    ✔ %d packages loaded successfully.%n", count);
        } catch (IOException e) {
            System.out.println("    ✘ Could not read package data: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MENU ACTIONS
    // ═══════════════════════════════════════════════════════════════

    /** Option 1: Display Master Registry (SLL) */
    private static void showMasterRegistry() {
        printSectionHeader("MASTER REGISTRY (Singly Linked List)");
        System.out.println("  Total records: " + masterRegistry.getSize());
        masterRegistry.displayLog();
        printDivider();
    }

    /** Option 2: Display Intake Buffer (DLL) */
    private static void showIntakeBuffer() {
        printSectionHeader("INTAKE BUFFER (Doubly Linked List)");
        System.out.println("  Packages in buffer: " + intakeBuffer.getSize());
        System.out.println("\n  [Head → Tail]:");
        intakeBuffer.displayBuffer();
        System.out.println("\n  [Tail → Head] (reverse):");
        intakeBuffer.displayReverse();
        printDivider();
    }

    /** Option 3: Delivery Queue operations */
    private static void processDeliveryQueue(Scanner scanner) {
        printSectionHeader("STANDARD DELIVERY QUEUE (FIFO Queue)");

        System.out.println("  [1] Move packages from buffer to queue");
        System.out.println("  [2] Dequeue a package");
        System.out.println("  [3] Display queue");
        System.out.print("  Choice ▸ ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1":
                int moved = 0;
                while (!intakeBuffer.isEmpty()) {
                    Package pkg = intakeBuffer.removeFromHead();
                    if (pkg != null) {
                        deliveryQueue.enqueue(pkg);
                        moved++;
                    }
                }
                System.out.printf("  ✔ %d packages moved from buffer to queue.%n", moved);
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
                System.out.println("  ⚠ Invalid choice.");
        }
        printDivider();
    }

    /** Option 4: Truck Stack operations */
    private static void processTruckLoading(Scanner scanner) {
        printSectionHeader("TRUCK LOADING BAY (LIFO Stack)");

        System.out.println("  [1] Load packages from queue onto truck");
        System.out.println("  [2] Unload top package (pop)");
        System.out.println("  [3] Display truck load");
        System.out.print("  Choice ▸ ");
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
                System.out.printf("  ✔ %d packages loaded onto truck.%n", loaded);
                break;
            case "2":
                Package popped = truckStack.pop();
                if (popped != null) {
                    System.out.println("  ✔ Unloaded: " + popped);
                }
                break;
            case "3":
                System.out.println("  Truck load:");
                truckStack.displayStack();
                break;
            default:
                System.out.println("  ⚠ Invalid choice.");
        }
        printDivider();
    }

    /** Option 5: Address Directory (AVL Tree) */
    private static void showAddressDirectory(Scanner scanner) {
        printSectionHeader("ADDRESS DIRECTORY (AVL Tree)");

        System.out.println("  [1] Display directory (sorted)");
        System.out.println("  [2] Display tree structure");
        System.out.println("  [3] Search district");
        System.out.println("  [4] Add new record");
        System.out.print("  Choice ▸ ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1":
                System.out.println("  Sorted directory (" + addressDirectory.getNodeCount()
                        + " records, tree height = " + addressDirectory.getTreeHeight() + "):");
                addressDirectory.inOrderTraversal();
                break;
            case "2":
                System.out.println("  Tree structure:");
                addressDirectory.printTree();
                break;
            case "3":
                System.out.print("  Enter district name ▸ ");
                String searchKey = scanner.nextLine().trim();
                String result = addressDirectory.search(searchKey);
                if (result != null) {
                    System.out.println("  ✔ Found: " + searchKey + " → Customer: " + result);
                } else {
                    System.out.println("  ✘ District not found: '" + searchKey + "'");
                }
                break;
            case "4":
                System.out.print("  District name ▸ ");
                String neighborhood = scanner.nextLine().trim();
                System.out.print("  Customer ID  ▸ ");
                String custID = scanner.nextLine().trim();
                addressDirectory.insert(neighborhood, custID);
                System.out.println("  ✔ Added: " + neighborhood + " → " + custID);
                break;
            default:
                System.out.println("  ⚠ Invalid choice.");
        }
        printDivider();
    }

    /** Option 6: Display City Map */
    private static void showCityMap() {
        printSectionHeader("KAYSERI CITY MAP (Weighted Graph — Adjacency List)");
        System.out.println("  Central Depot: Meydan");
        System.out.println("  Vertex count: " + cityMap.getVertexCount());
        cityMap.displayGraph();
        printDivider();
    }

    /** Option 7: Find Shortest Path (Dijkstra) */
    private static void findShortestPath(Scanner scanner) {
        printSectionHeader("SHORTEST PATH — Dijkstra's Algorithm");

        System.out.println("  Available locations:");
        String[] names = cityMap.getVertexNames();
        for (int i = 0; i < names.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, names[i]);
        }

        System.out.print("  Enter START location ▸ ");
        String start = scanner.nextLine().trim();
        System.out.print("  Enter END location   ▸ ");
        String end = scanner.nextLine().trim();

        System.out.println();
        cityMap.calculateShortestPath(start, end);
        printDivider();
    }

    /** Option 8: Compute MST (Prim's) */
    private static void computeMST() {
        printSectionHeader("MINIMUM SPANNING TREE — Prim's Algorithm");
        cityMap.calculateMST();
        printDivider();
    }

    /** Option 9: Full automated demo */
    private static void runFullDemo() {
        printSectionHeader("FULL SYSTEM DEMONSTRATION");
        System.out.println("  Running all " + COMPANY_NAME + " subsystems...\n");

        // ── 1. Master Registry (SLL) ────────────────────────────
        printSubSection("1. MASTER REGISTRY (Singly Linked List)");
        System.out.println("  All packages entering the system:");
        masterRegistry.displayLog();

        System.out.println("\n  Searching for PKG_KYS_005:");
        Package found = masterRegistry.search("PKG_KYS_005");
        if (found != null) {
            System.out.println("    ✔ Found: " + found);
        } else {
            System.out.println("    ✘ Not found.");
        }

        // ── 2. Intake Buffer (DLL) ─────────────────────────────
        printSubSection("2. INTAKE BUFFER (Doubly Linked List)");

        if (intakeBuffer.isEmpty()) {
            System.out.println("  Refilling buffer for demo...");
            intakeBuffer.insertAtTail(new Package("PKG_KYS_013", "Talas"));
            intakeBuffer.insertAtTail(new Package("PKG_KYS_014", "Belsin"));
            intakeBuffer.insertAtTail(new Package("PKG_KYS_015", "Erkilet"));
            intakeBuffer.insertAtHead(new Package("PKG_KYS_URGENT", "Meydan", 1, 0.5));
            System.out.println("  3 packages added to tail, 1 urgent package to head.");
        }
        System.out.println("\n  Buffer contents (Head → Tail):");
        intakeBuffer.displayBuffer();
        System.out.println("\n  Removing package from head:");
        Package removedHead = intakeBuffer.removeFromHead();
        if (removedHead != null) {
            System.out.println("    Removed: " + removedHead);
        }
        System.out.println("  Buffer after removal:");
        intakeBuffer.displayBuffer();

        // ── 3. Delivery Queue (FIFO) ───────────────────────────
        printSubSection("3. DELIVERY QUEUE (FIFO Queue)");
        int enqueued = 0;
        while (!intakeBuffer.isEmpty()) {
            Package pkg = intakeBuffer.removeFromHead();
            if (pkg != null) {
                deliveryQueue.enqueue(pkg);
                enqueued++;
            }
        }
        System.out.printf("  %d packages moved from buffer to queue.%n", enqueued);
        System.out.println("  Queue contents (front → rear):");
        deliveryQueue.displayQueue();

        System.out.println("\n  Dequeuing front package:");
        Package dq = deliveryQueue.dequeue();
        if (dq != null) {
            System.out.println("    Dequeued: " + dq);
        }
        System.out.println("  Queue after operation:");
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
        System.out.printf("  %d packages loaded onto truck.%n", loaded);
        System.out.println("  Truck load:");
        truckStack.displayStack();

        System.out.println("\n  Unloading packages at delivery point (LIFO order):");
        while (!truckStack.isEmpty()) {
            Package p = truckStack.pop();
            System.out.println("    📦 Delivered: " + p);
        }

        // ── 5. Address Directory (AVL Tree) ────────────────────
        printSubSection("5. ADDRESS DIRECTORY (AVL Tree)");
        System.out.println("  Record count: " + addressDirectory.getNodeCount()
                + " | Tree height: " + addressDirectory.getTreeHeight());
        System.out.println("\n  In-order traversal (districts alphabetically):");
        addressDirectory.inOrderTraversal();
        System.out.println("\n  Tree structure:");
        addressDirectory.printTree();

        String[] searchTerms = {"Talas", "Erkilet", "Anbar", "NoSuchDistrict"};
        System.out.println("\n  Search demonstration:");
        for (String term : searchTerms) {
            String res = addressDirectory.search(term);
            if (res != null) {
                System.out.printf("    ✔ '%s' → Customer: %s%n", term, res);
            } else {
                System.out.printf("    ✘ '%s' → NOT FOUND%n", term);
            }
        }

        // ── 6. City Map (Graph) ────────────────────────────────
        printSubSection("6. KAYSERI CITY MAP (Weighted Graph)");
        System.out.println("  Adjacency list view:");
        cityMap.displayGraph();

        // ── 7. Dijkstra's Algorithm ────────────────────────────
        printSubSection("7. SHORTEST PATH — Dijkstra's Algorithm");
        System.out.println("  Route: Meydan → Mimsin");
        cityMap.calculateShortestPath("Meydan", "Mimsin");
        System.out.println();
        System.out.println("  Route: Meydan → Anbar");
        cityMap.calculateShortestPath("Meydan", "Anbar");

        // ── 8. Prim's MST ──────────────────────────────────────
        printSubSection("8. MINIMUM SPANNING TREE — Prim's Algorithm");
        cityMap.calculateMST();

        System.out.println();
        printDivider();
        System.out.println("  ✔ Full demonstration complete.");
        System.out.println("    All " + COMPANY_NAME + " subsystems operational.");
        printDivider();
    }

    /** Option 10: Add a package manually */
    private static void addNewPackage(Scanner scanner) {
        printSectionHeader("ADD NEW PACKAGE");
        System.out.print("  Enter Package ID (e.g. PKG_KYS_099) ▸ ");
        String pkgID = scanner.nextLine().trim();
        System.out.print("  Enter Destination District ▸ ");
        String dest = scanner.nextLine().trim();

        if (!pkgID.isEmpty() && !dest.isEmpty()) {
            Package newPkg = new Package(pkgID, dest);

            masterRegistry.addRecord(newPkg);      // add to SLL
            intakeBuffer.insertAtTail(newPkg);     // add to DLL
            addressDirectory.insert(dest, pkgID);  // add to AVL Tree

            System.out.println("  ✔ Package added to system successfully: " + newPkg);
        } else {
            System.out.println("  ⚠ Invalid or missing input. Package not added.");
        }
        printDivider();
    }

    /** Option 11: Add a route (edge) manually */
    private static void addNewRoute(Scanner scanner) {
        printSectionHeader("ADD NEW ROUTE (EDGE)");
        System.out.print("  Enter Source District ▸ ");
        String source = scanner.nextLine().trim();
        System.out.print("  Enter Target District ▸ ");
        String dest = scanner.nextLine().trim();
        System.out.print("  Enter Distance (km) ▸ ");
        String distStr = scanner.nextLine().trim();

        try {
            int dist = Integer.parseInt(distStr);
            if (!source.isEmpty() && !dest.isEmpty() && dist > 0) {
                cityMap.addEdge(source, dest, dist); // add new edge to graph
                System.out.println("  ✔ New route added successfully: " + source + " ↔ " + dest + " (" + dist + " km)");
            } else {
                System.out.println("  ⚠ Invalid or missing input. Route not added.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠ Invalid distance format. Please enter numbers only.");
        }
        printDivider();
    }

    /** Option 12: Delete a package manually — removes it from the Master Registry (SLL) and Intake Buffer (DLL). */
    private static void deletePackage(Scanner scanner) {
        printSectionHeader("DELETE PACKAGE");
        System.out.print("  Enter Package ID to delete ▸ ");
        String pkgID = scanner.nextLine().trim();

        if (pkgID.isEmpty()) {
            System.out.println("  ⚠ Empty input. Deletion cancelled.");
            printDivider();
            return;
        }

        Package fromRegistry = masterRegistry.removeRecord(pkgID); // remove from SLL
        Package fromBuffer   = intakeBuffer.removePackage(pkgID);  // remove from DLL

        if (fromRegistry == null && fromBuffer == null) {
            System.out.println("  ✘ Package not found: '" + pkgID + "'");
        } else {
            Package shown = (fromRegistry != null) ? fromRegistry : fromBuffer;
            System.out.println("  ✔ Package deleted from system: " + shown);
            System.out.println("    - Master Registry (SLL): "
                    + (fromRegistry != null ? "removed" : "not present"));
            System.out.println("    - Intake Buffer (DLL):   "
                    + (fromBuffer != null ? "removed" : "not in buffer"));
        }
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
        System.out.println("  ║        " + COMPANY_NAME + " — Kayseri Urban Logistics            ║");
        System.out.println("  ║        Urban Logistics & Distribution System  v1.0         ║");
        System.out.println("  ║        " + TAGLINE + "              ║");
        System.out.println("  ║                                                            ║");
        System.out.println("  ║  Central Depot: Meydan, Kayseri                           ║");
        System.out.println("  ║  Districts: Alpaslan, Talas, Erkilet, Belsin,             ║");
        System.out.println("  ║             Ildem, Mimsin, Anbar, Kocasinan               ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ┌──────────────────────────────────────────────────────────────┐");
        System.out.printf( "  │              %s — Main Menu                          │%n", COMPANY_NAME);
        System.out.println("  ├──────────────────────────────────────────────────────────────┤");
        System.out.println("  │  [1]  Master Registry             (Singly Linked List)      │");
        System.out.println("  │  [2]  Intake Buffer               (Doubly Linked List)      │");
        System.out.println("  │  [3]  Delivery Queue              (FIFO Queue)              │");
        System.out.println("  │  [4]  Truck Loading               (LIFO Stack)              │");
        System.out.println("  │  [5]  Address Directory           (AVL Tree)                │");
        System.out.println("  │  [6]  Kayseri City Map            (Weighted Graph)          │");
        System.out.println("  │  [7]  Shortest Path               (Dijkstra's Algorithm)    │");
        System.out.println("  │  [8]  Minimum Spanning Tree       (Prim's Algorithm)        │");
        System.out.println("  │  [9]  ★ Full Demo                 (All Subsystems)          │");
        System.out.println("  │  [10] Add New Package             (Manual Entry)            │");
        System.out.println("  │  [11] Add New Route               (Manual Entry)            │");
        System.out.println("  │  [12] Delete Package              (Manual Entry)            │");
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
        System.out.println("  ║  Thank you for using the                                    ║");
        System.out.println("  ║  " + COMPANY_NAME + " Logistics System!                                ║");
        System.out.println("  ║  " + TAGLINE + "                          ║");
        System.out.println("  ║  © 2026 " + COMPANY_NAME + ". All rights reserved.                   ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
