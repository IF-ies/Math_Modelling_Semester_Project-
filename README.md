# Veteran - Urban Logistics & Distribution System
## Kayseri Şehir İçi Dağıtım ve Lojistik Yönetimi

**Grup:** Veteran | **Üniversite:** Abdullah Gül University (AGÜ)

### Grup Üyeleri
| # | İsim | Öğrenci No |
|---|------|------------|
| 1 | Ahmet Uzungöl | 2211051063 |
| 2 | Sümeyra Yıldız | 2211051070 |
| 3 | Züheyr Temel | 2211051067 |
| 4 | Abdullah İnce | 2211051010 |
| 5 | İbrahim Furkan Yılmaz | 2211051013 |

### Merkez Depo: Meydan, Kayseri
**Semtler:** Alpaslan, Talas, Erkilet, Belsin, İldem, Mimsin, Anbar, Kocasinan

### Uygulanan Veri Yapıları
- **SLL** — Ana Kayıt Defteri (Master Registry)
- **DLL** — Giriş Tamponu (Intake Buffer)
- **Queue (FIFO)** — Teslimat Kuyruğu
- **Stack (LIFO)** — Araç Yükleme
- **AVL Tree** — Adres Rehberi
- **Weighted Graph** — Kayseri Şehir Haritası + Dijkstra + Prim

### Derleme & Çalıştırma
```bash
javac -d out src/models/Package.java src/structures/linear/*.java src/structures/tree/*.java src/structures/graph/*.java src/Main.java
java -cp out Main
```
