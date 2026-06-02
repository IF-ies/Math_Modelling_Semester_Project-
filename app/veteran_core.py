# -*- coding: utf-8 -*-
"""
Veteran Logistics — Cekirdek Veri Yapilari (Java projesinin birebir Python portu)

Bu modul, projenin Java siniflarinin ayni mantikla yazilmis Python karsiliklarini
icerir. Hicbir hazir koleksiyon (collections.deque, heapq vb.) cekirdek mantikta
KULLANILMAZ; tum yapilar elle, dugum (node) tabanli olarak kurulmustur.

  - SinglyLinkedList  -> Master Registry (Tekli Bagli Liste)
  - DoublyLinkedList  -> Intake Buffer  (Ciftli Bagli Liste)
  - DeliveryQueue     -> FIFO Kuyruk
  - TruckStack        -> LIFO Yigin
  - AVLTree           -> Adres Rehberi  (Kendini dengeleyen BST)
  - CityGraph         -> Sehir Haritasi (Agirlikli graf + Dijkstra + Prim)
"""

# ───────────────────────────────────────────────────────────────────────────
#  Domain Model — Package
# ───────────────────────────────────────────────────────────────────────────
class Package:
    """Kayseri lojistik sistemindeki tek bir paketi temsil eder."""
    def __init__(self, package_id, destination, priority=3, weight_kg=1.0):
        self.package_id = package_id
        self.destination = destination
        self.priority = priority        # 1 = en yuksek, 5 = en dusuk
        self.weight_kg = weight_kg

    def __str__(self):
        return "[%s] -> %s | Priority: %d | Weight: %.1f kg" % (
            self.package_id, self.destination, self.priority, self.weight_kg)


# ───────────────────────────────────────────────────────────────────────────
#  Singly Linked List — Master Registry
# ───────────────────────────────────────────────────────────────────────────
class SinglyLinkedList:
    """Sisteme giren her paketin kalici, yalnizca-ekleme yapilan kayit defteri."""
    class _Node:
        __slots__ = ("data", "next")
        def __init__(self, data):
            self.data = data
            self.next = None

    def __init__(self):
        self.head = None
        self.tail = None
        self.size = 0

    def add_record(self, pkg):
        """Sona ekler — tail isaretcisi sayesinde O(1)."""
        node = self._Node(pkg)
        if self.head is None:
            self.head = self.tail = node
        else:
            self.tail.next = node
            self.tail = node
        self.size += 1

    def remove_record(self, package_id):
        """ID'ye gore ilk eslesen kaydi cikarir — O(n). Cikarilan paketi dondurur."""
        cur, prev = self.head, None
        while cur is not None:
            if cur.data.package_id.lower() == package_id.lower():
                if prev is None:
                    self.head = cur.next
                else:
                    prev.next = cur.next
                if cur is self.tail:
                    self.tail = prev
                self.size -= 1
                return cur.data
            prev, cur = cur, cur.next
        return None

    def search(self, package_id):
        """ID'ye gore dogrusal arama — O(n)."""
        cur = self.head
        while cur is not None:
            if cur.data.package_id.lower() == package_id.lower():
                return cur.data
            cur = cur.next
        return None

    def to_list(self):
        out, cur = [], self.head
        while cur is not None:
            out.append(cur.data)
            cur = cur.next
        return out

    def is_empty(self):
        return self.size == 0


# ───────────────────────────────────────────────────────────────────────────
#  Doubly Linked List — Intake Buffer
# ───────────────────────────────────────────────────────────────────────────
class DoublyLinkedList:
    """Paketlerin kuyruga/yigina gonderilmeden once tutuldugu esnek tampon."""
    class _Node:
        __slots__ = ("data", "prev", "next")
        def __init__(self, data):
            self.data = data
            self.prev = None
            self.next = None

    def __init__(self):
        self.head = None
        self.tail = None
        self.size = 0

    def insert_at_tail(self, pkg):
        node = self._Node(pkg)
        if self.tail is None:
            self.head = self.tail = node
        else:
            self.tail.next = node
            node.prev = self.tail
            self.tail = node
        self.size += 1

    def insert_at_head(self, pkg):
        node = self._Node(pkg)
        if self.head is None:
            self.head = self.tail = node
        else:
            node.next = self.head
            self.head.prev = node
            self.head = node
        self.size += 1

    def remove_from_head(self):
        if self.head is None:
            return None
        removed = self.head.data
        if self.head is self.tail:
            self.head = self.tail = None
        else:
            self.head = self.head.next
            self.head.prev = None
        self.size -= 1
        return removed

    def remove_from_tail(self):
        if self.tail is None:
            return None
        removed = self.tail.data
        if self.head is self.tail:
            self.head = self.tail = None
        else:
            self.tail = self.tail.prev
            self.tail.next = None
        self.size -= 1
        return removed

    def remove_package(self, package_id):
        cur = self.head
        while cur is not None:
            if cur.data.package_id.lower() == package_id.lower():
                if cur.prev is not None:
                    cur.prev.next = cur.next
                else:
                    self.head = cur.next
                if cur.next is not None:
                    cur.next.prev = cur.prev
                else:
                    self.tail = cur.prev
                self.size -= 1
                return cur.data
            cur = cur.next
        return None

    def to_list(self):
        out, cur = [], self.head
        while cur is not None:
            out.append(cur.data)
            cur = cur.next
        return out

    def is_empty(self):
        return self.size == 0


# ───────────────────────────────────────────────────────────────────────────
#  Queue (FIFO) — Delivery Queue
# ───────────────────────────────────────────────────────────────────────────
class DeliveryQueue:
    """Ilk giren ilk cikar (FIFO) prensibiyle calisan teslimat kuyrugu."""
    class _Node:
        __slots__ = ("data", "next")
        def __init__(self, data):
            self.data = data
            self.next = None

    def __init__(self):
        self.front = None
        self.rear = None
        self.size = 0

    def enqueue(self, pkg):
        node = self._Node(pkg)
        if self.rear is None:
            self.front = self.rear = node
        else:
            self.rear.next = node
            self.rear = node
        self.size += 1

    def dequeue(self):
        if self.front is None:
            return None
        removed = self.front.data
        self.front = self.front.next
        if self.front is None:
            self.rear = None
        self.size -= 1
        return removed

    def peek(self):
        return self.front.data if self.front else None

    def to_list(self):
        out, cur = [], self.front
        while cur is not None:
            out.append(cur.data)
            cur = cur.next
        return out

    def is_empty(self):
        return self.size == 0


# ───────────────────────────────────────────────────────────────────────────
#  Stack (LIFO) — Truck Loading Bay
# ───────────────────────────────────────────────────────────────────────────
class TruckStack:
    """Son giren ilk cikar (LIFO) prensibiyle calisan kamyon yukleme yigini."""
    class _Node:
        __slots__ = ("data", "next")
        def __init__(self, data):
            self.data = data
            self.next = None

    def __init__(self):
        self.top = None
        self.size = 0

    def push(self, pkg):
        node = self._Node(pkg)
        node.next = self.top
        self.top = node
        self.size += 1

    def pop(self):
        if self.top is None:
            return None
        removed = self.top.data
        self.top = self.top.next
        self.size -= 1
        return removed

    def peek(self):
        return self.top.data if self.top else None

    def to_list(self):
        """Tepe -> dip sirasinda dondurur."""
        out, cur = [], self.top
        while cur is not None:
            out.append(cur.data)
            cur = cur.next
        return out

    def is_empty(self):
        return self.size == 0


# ───────────────────────────────────────────────────────────────────────────
#  Priority Queue (Binary Min-Heap) — Priority Dispatch
# ───────────────────────────────────────────────────────────────────────────
class PriorityDeliveryQueue:
    """Acil sevkiyat kuyrugu — elle yazilmis ikili min-heap (heapq KULLANILMAZ).

    Dusuk priority sayisi = daha acil (1 = en yuksek .. 5 = en dusuk).
    Esit oncelikte: once gelen once cikar (kararli / FIFO).
    Dahili dizi olarak ham bir liste kullanilir; sift-up/sift-down elle yazilmistir.
    """
    def __init__(self):
        self._heap = []        # (priority, seq, pkg) uclulerinin dizisi
        self._counter = 0      # gelis sirasi (kararli esitlik bozma)

    @property
    def size(self):
        return len(self._heap)

    def is_empty(self):
        return len(self._heap) == 0

    @staticmethod
    def _more_urgent(a, b):
        """a, b = (priority, seq, pkg). a daha mi acil?"""
        if a[0] != b[0]:
            return a[0] < b[0]      # kucuk oncelik sayisi daha acil
        return a[1] < b[1]          # ayni oncelik: once gelen oncelikli

    def enqueue(self, pkg):
        """Ekler ve heap ozelligini geri kurar — O(log n)."""
        self._heap.append((pkg.priority, self._counter, pkg))
        self._counter += 1
        self._sift_up(len(self._heap) - 1)

    def dequeue(self):
        """En acil paketi cikarir ve dondurur — O(log n)."""
        h = self._heap
        if not h:
            return None
        top = h[0]
        last = h.pop()
        if h:
            h[0] = last
            self._sift_down(0)
        return top[2]

    def peek(self):
        """En acil paketi cikarmadan dondurur — O(1)."""
        return self._heap[0][2] if self._heap else None

    def _sift_up(self, i):
        h = self._heap
        while i > 0:
            parent = (i - 1) // 2
            if self._more_urgent(h[i], h[parent]):
                h[i], h[parent] = h[parent], h[i]
                i = parent
            else:
                break

    def _sift_down(self, i):
        h = self._heap
        n = len(h)
        while True:
            left, right, best = 2 * i + 1, 2 * i + 2, i
            if left < n and self._more_urgent(h[left], h[best]):
                best = left
            if right < n and self._more_urgent(h[right], h[best]):
                best = right
            if best == i:
                break
            h[i], h[best] = h[best], h[i]
            i = best

    def to_list(self):
        """Kuyrugu BOZMADAN, oncelik sirasinda (en acil once) paketleri dondurur."""
        copy = list(self._heap)
        for i in range(len(copy)):           # kararli secim siralamasi
            best = i
            for j in range(i + 1, len(copy)):
                if self._more_urgent(copy[j], copy[best]):
                    best = j
            copy[i], copy[best] = copy[best], copy[i]
        return [e[2] for e in copy]


# ───────────────────────────────────────────────────────────────────────────
#  AVL Tree — Address Directory
# ───────────────────────────────────────────────────────────────────────────
class AVLNode:
    __slots__ = ("neighborhood", "customer_id", "left", "right", "height")
    def __init__(self, neighborhood, customer_id):
        self.neighborhood = neighborhood
        self.customer_id = customer_id
        self.left = None
        self.right = None
        self.height = 1


class AVLTree:
    """Semt adlarini saklayan, kendini dengeleyen ikili arama agaci (O(log n))."""
    def __init__(self):
        self.root = None
        self.node_count = 0

    # --- Yukseklik & denge ---
    @staticmethod
    def _h(node):
        return node.height if node else 0

    def _update_height(self, node):
        node.height = 1 + max(self._h(node.left), self._h(node.right))

    def _balance_factor(self, node):
        return 0 if node is None else self._h(node.left) - self._h(node.right)

    # --- Donmeler (rotations) ---
    def _rotate_left(self, x):
        y = x.right
        b = y.left
        y.left = x
        x.right = b
        self._update_height(x)
        self._update_height(y)
        return y

    def _rotate_right(self, y):
        x = y.left
        b = x.right
        x.right = y
        y.left = b
        self._update_height(y)
        self._update_height(x)
        return x

    def _balance(self, node):
        bf = self._balance_factor(node)
        if bf > 1:  # sol agir
            if self._balance_factor(node.left) < 0:      # LR
                node.left = self._rotate_left(node.left)
            return self._rotate_right(node)              # LL
        if bf < -1:  # sag agir
            if self._balance_factor(node.right) > 0:     # RL
                node.right = self._rotate_right(node.right)
            return self._rotate_left(node)               # RR
        return node

    # --- Ekleme ---
    def insert(self, neighborhood, customer_id):
        self.node_count += 1
        self.root = self._insert(self.root, neighborhood, customer_id)

    def _insert(self, node, neighborhood, customer_id):
        if node is None:
            return AVLNode(neighborhood, customer_id)
        cmp = _casecmp(neighborhood, node.neighborhood)
        if cmp < 0:
            node.left = self._insert(node.left, neighborhood, customer_id)
        elif cmp > 0:
            node.right = self._insert(node.right, neighborhood, customer_id)
        else:
            node.customer_id = customer_id   # ayni anahtar: guncelle
            self.node_count -= 1
            return node
        self._update_height(node)
        return self._balance(node)

    # --- Arama ---
    def search(self, neighborhood):
        node = self.root
        while node is not None:
            cmp = _casecmp(neighborhood, node.neighborhood)
            if cmp < 0:
                node = node.left
            elif cmp > 0:
                node = node.right
            else:
                return node.customer_id
        return None

    def search_path(self, neighborhood):
        """Kokten hedefe gezilen dugumlerin listesi (animasyon icin)."""
        path, node = [], self.root
        while node is not None:
            path.append(node)
            cmp = _casecmp(neighborhood, node.neighborhood)
            if cmp == 0:
                return path, node
            node = node.left if cmp < 0 else node.right
        return path, None

    def in_order(self):
        """(semt, musteri, yukseklik) uclulerini alfabetik sirada dondurur."""
        out = []
        self._in_order(self.root, out)
        return out

    def _in_order(self, node, out):
        if node is None:
            return
        self._in_order(node.left, out)
        out.append((node.neighborhood, node.customer_id, node.height))
        self._in_order(node.right, out)

    def tree_height(self):
        return self._h(self.root)


def _casecmp(a, b):
    a, b = a.lower(), b.lower()
    return -1 if a < b else (1 if a > b else 0)


# ───────────────────────────────────────────────────────────────────────────
#  Weighted Graph — City Map (Dijkstra + Prim)
# ───────────────────────────────────────────────────────────────────────────
class CityGraph:
    """Kayseri yol agini yonsuz, agirlikli graf olarak modelleyen yapi."""
    def __init__(self):
        self.vertex_names = []          # indeks -> isim
        self.adj = []                   # indeks -> [(komsu_indeks, agirlik), ...]

    # --- Dugum yonetimi ---
    def _get_or_create(self, name):
        idx = self._index(name)
        if idx != -1:
            return idx
        self.vertex_names.append(name)
        self.adj.append([])
        return len(self.vertex_names) - 1

    def _index(self, name):
        for i, n in enumerate(self.vertex_names):
            if n.lower() == name.lower():
                return i
        return -1

    @property
    def vertex_count(self):
        return len(self.vertex_names)

    def add_edge(self, source, destination, weight):
        si = self._get_or_create(source)
        di = self._get_or_create(destination)
        self.adj[si].append((di, weight))
        self.adj[di].append((si, weight))

    def edges(self):
        """Tekrarsiz (isim_u, isim_v, agirlik) kenar listesi."""
        seen, out = set(), []
        for u in range(self.vertex_count):
            for (v, w) in self.adj[u]:
                key = (min(u, v), max(u, v))
                if key in seen:
                    continue
                seen.add(key)
                out.append((self.vertex_names[u], self.vertex_names[v], w))
        return out

    def neighbors(self, name):
        idx = self._index(name)
        if idx == -1:
            return []
        return [(self.vertex_names[v], w) for (v, w) in self.adj[idx]]

    def _edge_weight(self, fi, ti):
        for (v, w) in self.adj[fi]:
            if v == ti:
                return w
        return -1

    # --- Dijkstra (O(V^2)) ---
    def dijkstra(self, start, end):
        si, ei = self._index(start), self._index(end)
        if si == -1 or ei == -1:
            return None
        n = self.vertex_count
        INF = float("inf")
        dist = [INF] * n
        prev = [-1] * n
        visited = [False] * n
        dist[si] = 0
        for _ in range(n):
            u, best = -1, INF
            for i in range(n):
                if not visited[i] and dist[i] < best:
                    best, u = dist[i], i
            if u == -1:
                break
            visited[u] = True
            for (v, w) in self.adj[u]:
                if not visited[v] and dist[u] + w < dist[v]:
                    dist[v] = dist[u] + w
                    prev[v] = u
        if dist[ei] == INF:
            return None
        path, at = [], ei
        while at != -1:
            path.append(self.vertex_names[at])
            at = prev[at]
        path.reverse()
        steps = []
        for i in range(len(path) - 1):
            fi, ti = self._index(path[i]), self._index(path[i + 1])
            steps.append((path[i], path[i + 1], self._edge_weight(fi, ti)))
        return {"distance": dist[ei], "path": path, "steps": steps}

    # --- Prim MST (O(V^2)) ---
    def prim(self):
        n = self.vertex_count
        if n == 0:
            return None
        INF = float("inf")
        key = [INF] * n
        parent = [-1] * n
        in_mst = [False] * n
        key[0] = 0
        total = 0
        for _ in range(n):
            u, best = -1, INF
            for i in range(n):
                if not in_mst[i] and key[i] < best:
                    best, u = key[i], i
            if u == -1:
                break
            in_mst[u] = True
            total += 0 if key[u] == INF else key[u]
            for (v, w) in self.adj[u]:
                if not in_mst[v] and w < key[v]:
                    key[v] = w
                    parent[v] = u
        mst = []
        for i in range(1, n):
            if parent[i] != -1:
                mst.append((self.vertex_names[parent[i]], self.vertex_names[i], key[i]))
        return {"edges": mst, "total": total}


# ───────────────────────────────────────────────────────────────────────────
#  Tohum (seed) verisi — exe icine gomulu; harici dosya gerekmez
# ───────────────────────────────────────────────────────────────────────────
SEED_EDGES = [
    ("Meydan", "Alpaslan", 4),
    ("Meydan", "Talas", 8),
    ("Meydan", "Erkilet", 10),
    ("Meydan", "Belsin", 12),
    ("Alpaslan", "Talas", 5),
    ("Alpaslan", "Erkilet", 9),
    ("Alpaslan", "Ildem", 12),
    ("Talas", "Mimsin", 11),
    ("Belsin", "Anbar", 3),
    ("Belsin", "Erkilet", 14),
    ("Ildem", "Mimsin", 6),
    ("Meydan", "Kocasinan", 7),
    ("Kocasinan", "Alpaslan", 6),
    ("Kocasinan", "Belsin", 9),
    ("Talas", "Ildem", 8),
]

# (PackageID, Destination, Priority)  — priority: 1 = en acil .. 5 = en dusuk
# data/packageData.txt ile birebir ayni degerler
SEED_PACKAGES = [
    ("PKG_KYS_001", "Talas",     2),
    ("PKG_KYS_002", "Belsin",    1),
    ("PKG_KYS_003", "Ildem",     3),
    ("PKG_KYS_004", "Anbar",     5),
    ("PKG_KYS_005", "Erkilet",   1),
    ("PKG_KYS_006", "Mimsin",    4),
    ("PKG_KYS_007", "Alpaslan",  2),
    ("PKG_KYS_008", "Talas",     3),
    ("PKG_KYS_009", "Belsin",    5),
    ("PKG_KYS_010", "Kocasinan", 1),
    ("PKG_KYS_011", "Ildem",     4),
    ("PKG_KYS_012", "Erkilet",   2),
]

# Sehir haritasi gorseli icin sabit dugum konumlari (0..1 normalize)
DISTRICT_POS = {
    "Meydan":    (0.50, 0.52),
    "Kocasinan": (0.46, 0.20),
    "Alpaslan":  (0.28, 0.34),
    "Talas":     (0.72, 0.30),
    "Ildem":     (0.16, 0.60),
    "Erkilet":   (0.34, 0.80),
    "Belsin":    (0.74, 0.62),
    "Anbar":     (0.88, 0.82),
    "Mimsin":    (0.46, 0.86),
}
