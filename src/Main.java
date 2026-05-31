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
 * ║        Kayseri Sehir Ici Dagitim ve Lojistik Yonetimi             ║
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
        printSectionHeader("PHASE 1 — VERI YUKLEMESI (DATA INGESTION)");
        loadMapData();
        loadPackageData();

        // ── Step 2: Interactive menu ────────────────────────────
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("  [" + COMPANY_NAME + "] Secim yapiniz ▸ ");
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
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("  ⚠ Gecersiz secim. Lutfen tekrar deneyin.");
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
     * Merkez depo: Meydan (Kayseri)
     */
    private static void loadMapData() {
        System.out.println("\n  ▶ Kayseri sehir haritasi yukleniyor: " + MAP_DATA_FILE);
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
            System.out.printf("    ✔ %d kenar, %d kosegeden olusan harita yuklendi.%n",
                    edgeCount, cityMap.getVertexCount());
        } catch (IOException e) {
            System.out.println("    ✘ Harita verisi okunamadi: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("    ✘ Gecersiz mesafe formati: " + e.getMessage());
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
        System.out.println("\n  ▶ Kayseri paket verileri yukleniyor: " + PACKAGE_DATA_FILE);
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
            System.out.printf("    ✔ %d paket basariyla yuklendi.%n", count);
        } catch (IOException e) {
            System.out.println("    ✘ Paket verisi okunamadi: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MENU ACTIONS
    // ═══════════════════════════════════════════════════════════════

    /** Option 1: Display Master Registry (SLL) */
    private static void showMasterRegistry() {
        printSectionHeader("ANA KAYIT DEFTERI (Singly Linked List)");
        System.out.println("  Toplam kayit: " + masterRegistry.getSize());
        masterRegistry.displayLog();
        printDivider();
    }

    /** Option 2: Display Intake Buffer (DLL) */
    private static void showIntakeBuffer() {
        printSectionHeader("GIRIS TAMPONU (Doubly Linked List)");
        System.out.println("  Tampondaki paket sayisi: " + intakeBuffer.getSize());
        System.out.println("\n  [Bas → Son]:");
        intakeBuffer.displayBuffer();
        System.out.println("\n  [Son → Bas] (ters):");
        intakeBuffer.displayReverse();
        printDivider();
    }

    /** Option 3: Delivery Queue operations */
    private static void processDeliveryQueue(Scanner scanner) {
        printSectionHeader("STANDART TESLIMAT KUYRUGU (FIFO Queue)");

        System.out.println("  [1] Tamponden kuyruga paket aktar");
        System.out.println("  [2] Kuyruktan paket al (dequeue)");
        System.out.println("  [3] Kuyrugu goruntule");
        System.out.print("  Secim ▸ ");
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
                System.out.printf("  ✔ %d paket tamponden kuyruga aktarildi.%n", moved);
                break;
            case "2":
                Package dequeued = deliveryQueue.dequeue();
                if (dequeued != null) {
                    System.out.println("  ✔ Kuyruktan alindi: " + dequeued);
                }
                break;
            case "3":
                System.out.println("  Kuyruk icerigi (on → arka):");
                deliveryQueue.displayQueue();
                break;
            default:
                System.out.println("  ⚠ Gecersiz secim.");
        }
        printDivider();
    }

    /** Option 4: Truck Stack operations */
    private static void processTruckLoading(Scanner scanner) {
        printSectionHeader("ARAC YUKLEME ALANI (LIFO Stack)");

        System.out.println("  [1] Kuyruktan araca paket yukle");
        System.out.println("  [2] Aractan ust paketi indir (pop)");
        System.out.println("  [3] Arac yuk durumunu goruntule");
        System.out.print("  Secim ▸ ");
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
                System.out.printf("  ✔ %d paket araca yuklendi.%n", loaded);
                break;
            case "2":
                Package popped = truckStack.pop();
                if (popped != null) {
                    System.out.println("  ✔ Aractan indirildi: " + popped);
                }
                break;
            case "3":
                System.out.println("  Arac yuk durumu:");
                truckStack.displayStack();
                break;
            default:
                System.out.println("  ⚠ Gecersiz secim.");
        }
        printDivider();
    }

    /** Option 5: Address Directory (AVL Tree) */
    private static void showAddressDirectory(Scanner scanner) {
        printSectionHeader("ADRES REHBERI (AVL Tree)");

        System.out.println("  [1] Rehberi goruntule (siralı)");
        System.out.println("  [2] Agac yapisini goruntule");
        System.out.println("  [3] Semt ara");
        System.out.println("  [4] Yeni kayit ekle");
        System.out.print("  Secim ▸ ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1":
                System.out.println("  Sirali rehber (" + addressDirectory.getNodeCount()
                        + " kayit, agac yuksekligi = " + addressDirectory.getTreeHeight() + "):");
                addressDirectory.inOrderTraversal();
                break;
            case "2":
                System.out.println("  Agac yapisi:");
                addressDirectory.printTree();
                break;
            case "3":
                System.out.print("  Semt adi giriniz ▸ ");
                String searchKey = scanner.nextLine().trim();
                String result = addressDirectory.search(searchKey);
                if (result != null) {
                    System.out.println("  ✔ Bulundu: " + searchKey + " → Musteri: " + result);
                } else {
                    System.out.println("  ✘ Semt bulunamadi: '" + searchKey + "'");
                }
                break;
            case "4":
                System.out.print("  Semt adi ▸ ");
                String neighborhood = scanner.nextLine().trim();
                System.out.print("  Musteri ID  ▸ ");
                String custID = scanner.nextLine().trim();
                addressDirectory.insert(neighborhood, custID);
                System.out.println("  ✔ Eklendi: " + neighborhood + " → " + custID);
                break;
            default:
                System.out.println("  ⚠ Gecersiz secim.");
        }
        printDivider();
    }

    /** Option 6: Display City Map */
    private static void showCityMap() {
        printSectionHeader("KAYSERI SEHIR HARITASI (Weighted Graph — Adjacency List)");
        System.out.println("  Merkez Depo: Meydan");
        System.out.println("  Kosegen sayisi: " + cityMap.getVertexCount());
        cityMap.displayGraph();
        printDivider();
    }

    /** Option 7: Find Shortest Path (Dijkstra) */
    private static void findShortestPath(Scanner scanner) {
        printSectionHeader("EN KISA YOL — Dijkstra Algoritmasi");

        System.out.println("  Mevcut konumlar:");
        String[] names = cityMap.getVertexNames();
        for (int i = 0; i < names.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, names[i]);
        }

        System.out.print("  BASLANGIC konumu giriniz ▸ ");
        String start = scanner.nextLine().trim();
        System.out.print("  BITIS konumu giriniz     ▸ ");
        String end = scanner.nextLine().trim();

        System.out.println();
        cityMap.calculateShortestPath(start, end);
        printDivider();
    }

    /** Option 8: Compute MST (Prim's) */
    private static void computeMST() {
        printSectionHeader("MINIMUM YAYILAN AGAC — Prim Algoritmasi");
        cityMap.calculateMST();
        printDivider();
    }

    /** Option 9: Full automated demo */
    private static void runFullDemo() {
        printSectionHeader("TAM SISTEM DEMONSTRASYONU");
        System.out.println("  " + COMPANY_NAME + " tum alt sistemleri calistiriliyor...\n");

        // ── 1. Master Registry (SLL) ────────────────────────────
        printSubSection("1. ANA KAYIT DEFTERI (Singly Linked List)");
        System.out.println("  Sisteme giren tum paketler:");
        masterRegistry.displayLog();

        System.out.println("\n  PKG_KYS_005 aranıyor:");
        Package found = masterRegistry.search("PKG_KYS_005");
        if (found != null) {
            System.out.println("    ✔ Bulundu: " + found);
        } else {
            System.out.println("    ✘ Bulunamadi.");
        }

        // ── 2. Intake Buffer (DLL) ─────────────────────────────
        printSubSection("2. GIRIS TAMPONU (Doubly Linked List)");

        if (intakeBuffer.isEmpty()) {
            System.out.println("  Demo icin tampon yeniden dolduruluyor...");
            intakeBuffer.insertAtTail(new Package("PKG_KYS_013", "Talas"));
            intakeBuffer.insertAtTail(new Package("PKG_KYS_014", "Belsin"));
            intakeBuffer.insertAtTail(new Package("PKG_KYS_015", "Erkilet"));
            intakeBuffer.insertAtHead(new Package("PKG_KYS_URGENT", "Meydan", 1, 0.5));
            System.out.println("  3 paket sona, 1 acil paket basa eklendi.");
        }
        System.out.println("\n  Tampon icerigi (Bas → Son):");
        intakeBuffer.displayBuffer();
        System.out.println("\n  Bastan paket kaldiriliyor:");
        Package removedHead = intakeBuffer.removeFromHead();
        if (removedHead != null) {
            System.out.println("    Kaldirildi: " + removedHead);
        }
        System.out.println("  Kaldirma sonrasi tampon:");
        intakeBuffer.displayBuffer();

        // ── 3. Delivery Queue (FIFO) ───────────────────────────
        printSubSection("3. TESLIMAT KUYRUGU (FIFO Queue)");
        int enqueued = 0;
        while (!intakeBuffer.isEmpty()) {
            Package pkg = intakeBuffer.removeFromHead();
            if (pkg != null) {
                deliveryQueue.enqueue(pkg);
                enqueued++;
            }
        }
        System.out.printf("  %d paket tamponden kuyruga aktarildi.%n", enqueued);
        System.out.println("  Kuyruk icerigi (on → arka):");
        deliveryQueue.displayQueue();

        System.out.println("\n  On paket kuyruktan aliniyor:");
        Package dq = deliveryQueue.dequeue();
        if (dq != null) {
            System.out.println("    Alindi: " + dq);
        }
        System.out.println("  Islem sonrasi kuyruk:");
        deliveryQueue.displayQueue();

        // ── 4. Truck Stack (LIFO) ──────────────────────────────
        printSubSection("4. ARAC YUKLEME (LIFO Stack)");
        int loaded = 0;
        while (!deliveryQueue.isEmpty()) {
            Package pkg = deliveryQueue.dequeue();
            if (pkg != null) {
                truckStack.push(pkg);
                loaded++;
            }
        }
        System.out.printf("  %d paket araca yuklendi.%n", loaded);
        System.out.println("  Arac yuk durumu:");
        truckStack.displayStack();

        System.out.println("\n  Teslimat noktasinda paketler indiriliyor (LIFO sirasi):");
        while (!truckStack.isEmpty()) {
            Package p = truckStack.pop();
            System.out.println("    📦 Teslim edildi: " + p);
        }

        // ── 5. Address Directory (AVL Tree) ────────────────────
        printSubSection("5. ADRES REHBERI (AVL Tree)");
        System.out.println("  Kayit sayisi: " + addressDirectory.getNodeCount()
                + " | Agac yuksekligi: " + addressDirectory.getTreeHeight());
        System.out.println("\n  In-order gezinme (alfabetik sirada semtler):");
        addressDirectory.inOrderTraversal();
        System.out.println("\n  Agac yapisi:");
        addressDirectory.printTree();

        String[] searchTerms = {"Talas", "Erkilet", "Anbar", "YokSemt"};
        System.out.println("\n  Arama demonstrasyonu:");
        for (String term : searchTerms) {
            String res = addressDirectory.search(term);
            if (res != null) {
                System.out.printf("    ✔ '%s' → Musteri: %s%n", term, res);
            } else {
                System.out.printf("    ✘ '%s' → BULUNAMADI%n", term);
            }
        }

        // ── 6. City Map (Graph) ────────────────────────────────
        printSubSection("6. KAYSERI SEHIR HARITASI (Weighted Graph)");
        System.out.println("  Komsuluk listesi gosterimi:");
        cityMap.displayGraph();

        // ── 7. Dijkstra's Algorithm ────────────────────────────
        printSubSection("7. EN KISA YOL — Dijkstra Algoritmasi");
        System.out.println("  Rota: Meydan → Mimsin");
        cityMap.calculateShortestPath("Meydan", "Mimsin");
        System.out.println();
        System.out.println("  Rota: Meydan → Anbar");
        cityMap.calculateShortestPath("Meydan", "Anbar");

        // ── 8. Prim's MST ──────────────────────────────────────
        printSubSection("8. MINIMUM YAYILAN AGAC — Prim Algoritmasi");
        cityMap.calculateMST();

        System.out.println();
        printDivider();
        System.out.println("  ✔ Tam demonstrasyon tamamlandi.");
        System.out.println("    " + COMPANY_NAME + " tum alt sistemleri calisiyor.");
        printDivider();
    }

    /** Option 10: Manuel Paket Ekleme */
    private static void addNewPackage(Scanner scanner) {
        printSectionHeader("YENI PAKET EKLEME");
        System.out.print("  Paket ID giriniz (orn. PKG_KYS_099) ▸ ");
        String pkgID = scanner.nextLine().trim();
        System.out.print("  Hedef Semt giriniz ▸ ");
        String dest = scanner.nextLine().trim();

        if (!pkgID.isEmpty() && !dest.isEmpty()) {
            Package newPkg = new Package(pkgID, dest);

            masterRegistry.addRecord(newPkg); // SLL'ye ekler
            intakeBuffer.insertAtTail(newPkg); // DLL'ye ekler
            addressDirectory.insert(dest, pkgID); // AVL Tree'ye ekler

            System.out.println("  ✔ Paket sisteme basariyla eklendi: " + newPkg);
        } else {
            System.out.println("  ⚠ Hatali veya eksik giris yapildi. Paket eklenemedi.");
        }
        printDivider();
    }

    /** Option 11: Manuel Rota Ekleme */
    private static void addNewRoute(Scanner scanner) {
        printSectionHeader("YENI ROTA (KENAR) EKLEME");
        System.out.print("  Kaynak Semt giriniz ▸ ");
        String source = scanner.nextLine().trim();
        System.out.print("  Hedef Semt giriniz ▸ ");
        String dest = scanner.nextLine().trim();
        System.out.print("  Mesafe (km) giriniz ▸ ");
        String distStr = scanner.nextLine().trim();

        try {
            int dist = Integer.parseInt(distStr);
            if (!source.isEmpty() && !dest.isEmpty() && dist > 0) {
                cityMap.addEdge(source, dest, dist); // Graf'a yeni kenar ekler
                System.out.println("  ✔ Yeni rota basariyla eklendi: " + source + " ↔ " + dest + " (" + dist + " km)");
            } else {
                System.out.println("  ⚠ Hatali veya eksik giris yapildi. Rota eklenemedi.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠ Gecersiz mesafe formati. Lutfen sadece sayi giriniz.");
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
        System.out.println("  ║        " + COMPANY_NAME + " — Kayseri Sehir Ici Lojistik         ║");
        System.out.println("  ║        Urban Logistics & Distribution System  v1.0         ║");
        System.out.println("  ║        " + TAGLINE + "              ║");
        System.out.println("  ║                                                            ║");
        System.out.println("  ║  Merkez Depo: Meydan, Kayseri                             ║");
        System.out.println("  ║  Semtler: Alpaslan, Talas, Erkilet, Belsin,               ║");
        System.out.println("  ║           Ildem, Mimsin, Anbar, Kocasinan                 ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ┌──────────────────────────────────────────────────────────────┐");
        System.out.printf( "  │              %s — Ana Menu                           │%n", COMPANY_NAME);
        System.out.println("  ├──────────────────────────────────────────────────────────────┤");
        System.out.println("  │  [1]  Ana Kayit Defteri           (Singly Linked List)      │");
        System.out.println("  │  [2]  Giris Tamponu               (Doubly Linked List)      │");
        System.out.println("  │  [3]  Teslimat Kuyrugu            (FIFO Queue)              │");
        System.out.println("  │  [4]  Arac Yukleme                (LIFO Stack)              │");
        System.out.println("  │  [5]  Adres Rehberi               (AVL Tree)                │");
        System.out.println("  │  [6]  Kayseri Sehir Haritasi      (Weighted Graph)          │");
        System.out.println("  │  [7]  En Kisa Yol                 (Dijkstra Algoritmasi)    │");
        System.out.println("  │  [8]  Minimum Yayilan Agac        (Prim Algoritmasi)        │");
        System.out.println("  │  [9]  ★ Tam Demo                  (Tum Alt Sistemler)       │");
        System.out.println("  │  [10] Yeni Paket Ekleme           (Manuel Giris)            │");
        System.out.println("  │  [11] Yeni Rota Ekleme            (Manuel Giris)            │");
        System.out.println("  │  [0]  Cikis                                                 │");
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
        System.out.println("  ║  " + COMPANY_NAME + " Lojistik Sistemini Kullandiginiz icin  ║");
        System.out.println("  ║  Tesekkur Ederiz!                                           ║");
        System.out.println("  ║  " + TAGLINE + "                          ║");
        System.out.println("  ║  © 2026 " + COMPANY_NAME + ". Tum haklari saklidir.          ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}