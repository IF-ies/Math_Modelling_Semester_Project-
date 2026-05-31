# Veteran - Urban Logistics & Distribution System
## Kayseri Urban Delivery & Logistics Management

**Group:** Veteran | **University:** Abdullah Gül University (AGÜ)

### Group Members
| # | Name | Student No |
|---|------|------------|
| 1 | Ahmet Uzungöl | 2211051063 |
| 2 | Sümeyra Yıldız | 2211051070 |
| 3 | Züheyr Temel | 2211051067 |
| 4 | Abdullah İnce | 2211051010 |
| 5 | İbrahim Furkan Yılmaz | 2211051013 |

### Central Depot: Meydan, Kayseri
**Districts:** Alpaslan, Talas, Erkilet, Belsin, İldem, Mimsin, Anbar, Kocasinan

### Implemented Data Structures
- **SLL** — Master Registry
- **DLL** — Intake Buffer
- **Queue (FIFO)** — Delivery Queue
- **Stack (LIFO)** — Truck Loading
- **AVL Tree** — Address Directory
- **Weighted Graph** — Kayseri City Map + Dijkstra + Prim

### Build & Run
```bash
javac -d out src/models/Package.java src/structures/linear/*.java src/structures/tree/*.java src/structures/graph/*.java src/Main.java
java -cp out Main
```

Or double-click **`baslat.bat`** for the GUI. The GUI compiles the current
source on every launch and runs it, so it always reflects the latest code
(a JDK must be installed, or `JAVA_HOME` set).
