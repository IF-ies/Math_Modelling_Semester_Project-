# -*- coding: utf-8 -*-
"""
Veteran — Urban Logistics & Distribution System  (Desktop UI)

A modern, button-driven, animated tkinter application. There is NO console/cmd
look; every operation is shown with visual components (tables, cards, graph and
tree drawings) and on-screen animations.

Run:  python veteran_gui.py
"""
import tkinter as tk
from tkinter import ttk, font as tkfont

import veteran_core as core


# ═══════════════════════════════════════════════════════════════════════════
#  Color palette
# ═══════════════════════════════════════════════════════════════════════════
C = {
    "brand":      "#1F3A5F",   # deep navy (header)
    "brand_dark": "#162C49",   # sidebar
    "brand_hi":   "#274B78",   # hover
    "accent":     "#2E86DE",   # bright blue accent
    "accent_dim": "#4A90D9",
    "bg":         "#EEF2F7",   # app background
    "card":       "#FFFFFF",
    "card_brd":   "#D9E0EA",
    "text":       "#22303C",
    "muted":      "#7B8794",
    "ok":         "#27AE60",
    "ok_dim":     "#58D68D",
    "warn":       "#E67E22",
    "danger":     "#E74C3C",
    "gold":       "#F1C40F",
    "edge":       "#B7C2D0",
    "depot":      "#E74C3C",
    "node":       "#2E86DE",
    "node_txt":   "#FFFFFF",
}


# ═══════════════════════════════════════════════════════════════════════════
#  Helpers
# ═══════════════════════════════════════════════════════════════════════════
def lerp_color(c1, c2, t):
    """Linear interpolation between two hex colors (t: 0..1)."""
    a = [int(c1[i:i + 2], 16) for i in (1, 3, 5)]
    b = [int(c2[i:i + 2], 16) for i in (1, 3, 5)]
    m = [int(a[i] + (b[i] - a[i]) * t) for i in range(3)]
    return "#%02X%02X%02X" % (m[0], m[1], m[2])


def round_rect(cv, x1, y1, x2, y2, r=14, **kw):
    """Draws a rounded rectangle on a canvas; returns the polygon id."""
    pts = [x1 + r, y1, x2 - r, y1, x2, y1, x2, y1 + r, x2, y2 - r, x2, y2,
           x2 - r, y2, x1 + r, y2, x1, y2, x1, y2 - r, x1, y1 + r, x1, y1]
    return cv.create_polygon(pts, smooth=True, **kw)


# ═══════════════════════════════════════════════════════════════════════════
#  Hover button
# ═══════════════════════════════════════════════════════════════════════════
class HoverButton(tk.Label):
    """Flat, modern button with a hover effect (tk.Label based)."""
    def __init__(self, parent, text, command=None, bg=C["accent"], fg="white",
                 hover=None, font=None, padx=18, pady=10, **kw):
        super().__init__(parent, text=text, bg=bg, fg=fg, font=font,
                         padx=padx, pady=pady, cursor="hand2", **kw)
        self._bg = bg
        self._hover = hover or lerp_color(bg, "#FFFFFF", 0.16)
        self._cmd = command
        self.bind("<Enter>", lambda e: self.configure(bg=self._hover))
        self.bind("<Leave>", lambda e: self.configure(bg=self._bg))
        self.bind("<Button-1>", lambda e: self.configure(bg=lerp_color(self._bg, "#000000", 0.12)))
        self.bind("<ButtonRelease-1>", self._release)

    def _release(self, e):
        self.configure(bg=self._hover)
        if self._cmd and 0 <= e.x <= self.winfo_width() and 0 <= e.y <= self.winfo_height():
            self._cmd()

    def set_bg(self, bg):
        self._bg = bg
        self._hover = lerp_color(bg, "#FFFFFF", 0.16)
        self.configure(bg=bg)


# ═══════════════════════════════════════════════════════════════════════════
#  Sidebar navigation button
# ═══════════════════════════════════════════════════════════════════════════
class NavButton(tk.Frame):
    def __init__(self, parent, text, command, font, badge=""):
        super().__init__(parent, bg=C["brand_dark"], cursor="hand2", height=40)
        self.pack_propagate(False)
        self._command = command
        self._active = False
        self.bar = tk.Frame(self, bg=C["brand_dark"], width=4)
        self.bar.pack(side="left", fill="y")
        self.lbl = tk.Label(self, text=("  " + (badge + "  " if badge else "") + text),
                            bg=C["brand_dark"], fg="#C9D6E5", font=font, anchor="w")
        self.lbl.pack(side="left", fill="both", expand=True)
        for w in (self, self.lbl, self.bar):
            w.bind("<Enter>", self._on_enter)
            w.bind("<Leave>", self._on_leave)
            w.bind("<Button-1>", lambda e: self._command())

    def _on_enter(self, _):
        if not self._active:
            self.lbl.configure(bg=C["brand_hi"])
            self.configure(bg=C["brand_hi"])

    def _on_leave(self, _):
        if not self._active:
            self.lbl.configure(bg=C["brand_dark"])
            self.configure(bg=C["brand_dark"])

    def set_active(self, active):
        self._active = active
        if active:
            self.bar.configure(bg=C["accent"])
            self.lbl.configure(bg=C["brand_hi"], fg="white", font=self.lbl.cget("font"))
            self.configure(bg=C["brand_hi"])
        else:
            self.bar.configure(bg=C["brand_dark"])
            self.lbl.configure(bg=C["brand_dark"], fg="#C9D6E5")
            self.configure(bg=C["brand_dark"])


# ═══════════════════════════════════════════════════════════════════════════
#  Graph (city map) drawing canvas
# ═══════════════════════════════════════════════════════════════════════════
class GraphCanvas(tk.Canvas):
    def __init__(self, parent, graph, app):
        super().__init__(parent, bg="#F7FAFD", highlightthickness=1,
                         highlightbackground=C["card_brd"])
        self.graph = graph
        self.app = app
        self.mode = None          # None | 'path' | 'mst'
        self.path = []
        self.path_k = 0           # number of edges shown during animation
        self.mst_edges = []
        self.bind("<Configure>", lambda e: self.render())

    def _positions(self):
        w, h = max(self.winfo_width(), 50), max(self.winfo_height(), 50)
        mx, my = 60, 50
        pos = {}
        import math
        ring = [n for n in self.graph.vertex_names if n not in core.DISTRICT_POS]
        for i, name in enumerate(self.graph.vertex_names):
            if name in core.DISTRICT_POS:
                nx, ny = core.DISTRICT_POS[name]
            else:
                k = ring.index(name)
                ang = 2 * math.pi * k / max(len(ring), 1)
                nx, ny = 0.5 + 0.42 * math.cos(ang), 0.5 + 0.42 * math.sin(ang)
            pos[name] = (mx + nx * (w - 2 * mx), my + ny * (h - 2 * my))
        return pos

    def render(self):
        self.delete("all")
        if self.graph.vertex_count == 0:
            return
        pos = self._positions()
        path_edges = set()
        if self.mode == "path" and len(self.path) >= 2:
            for i in range(min(self.path_k, len(self.path) - 1)):
                a, b = self.path[i], self.path[i + 1]
                path_edges.add(frozenset((a, b)))
        mst_set = set(frozenset((u, v)) for (u, v, _) in self.mst_edges) if self.mode == "mst" else set()

        # Edges
        for (u, v, wgt) in self.graph.edges():
            x1, y1 = pos[u]; x2, y2 = pos[v]
            key = frozenset((u, v))
            if key in path_edges:
                col, width = C["ok"], 5
            elif key in mst_set:
                col, width = C["gold"], 5
            else:
                col, width = C["edge"], 2
            self.create_line(x1, y1, x2, y2, fill=col, width=width, capstyle="round")
            mx_, my_ = (x1 + x2) / 2, (y1 + y2) / 2
            self.create_oval(mx_ - 13, my_ - 11, mx_ + 13, my_ + 11,
                             fill="white", outline=C["card_brd"])
            self.create_text(mx_, my_, text=str(wgt), font=self.app.f_small, fill=C["muted"])

        # Nodes
        hi_nodes = set(self.path[:self.path_k + 1]) if self.mode == "path" else set()
        for name in self.graph.vertex_names:
            x, y = pos[name]
            is_depot = name.lower() == "meydan"
            r = 30 if is_depot else 26
            if name in hi_nodes:
                fill = C["ok"]
            elif is_depot:
                fill = C["depot"]
            else:
                fill = C["node"]
            self.create_oval(x - r, y - r, x + r, y + r, fill=fill, outline="white", width=3)
            self.create_text(x, y, text=name, font=self.app.f_small_b, fill="white")
            if is_depot:
                self.create_text(x, y + r + 12, text="DEPOT", font=self.app.f_tiny, fill=C["depot"])

    # --- Animations ---
    def show_base(self):
        self.mode = None
        self.path = []
        self.mst_edges = []
        self.render()

    def animate_path(self, path):
        self.mode = "path"
        self.path = path
        self.path_k = 0
        self.mst_edges = []
        self._step_path()

    def _step_path(self):
        self.render()
        if self.path_k < len(self.path) - 1:
            self.path_k += 1
            self.after(420, self._step_path)

    def show_mst(self, edges):
        self.mode = "mst"
        self.mst_edges = edges
        self.path = []
        self.render()


# ═══════════════════════════════════════════════════════════════════════════
#  AVL tree drawing canvas
# ═══════════════════════════════════════════════════════════════════════════
class AVLCanvas(tk.Canvas):
    def __init__(self, parent, app):
        super().__init__(parent, bg="#F7FAFD", highlightthickness=1,
                         highlightbackground=C["card_brd"])
        self.app = app
        self.tree = None
        self.highlight = set()
        self.bind("<Configure>", lambda e: self.render())

    def set_tree(self, tree):
        self.tree = tree
        self.highlight = set()
        self.render()

    def _layout(self):
        """Node positions via in-order x assignment."""
        pos, counter = {}, [0]

        def rec(node, depth):
            if node is None:
                return
            rec(node.left, depth + 1)
            pos[node] = (counter[0], depth)
            counter[0] += 1
            rec(node.right, depth + 1)

        if self.tree:
            rec(self.tree.root, 0)
        return pos, counter[0]

    def render(self):
        self.delete("all")
        if not self.tree or self.tree.root is None:
            self.create_text(self.winfo_width() / 2, self.winfo_height() / 2,
                             text="(Address directory is empty)", fill=C["muted"], font=self.app.f_body)
            return
        pos, n = self._layout()
        w, h = max(self.winfo_width(), 50), max(self.winfo_height(), 50)
        gap_x = (w - 80) / max(n - 1, 1)
        gap_y = 86
        scr = {}
        for node, (cx, depth) in pos.items():
            scr[node] = (40 + cx * gap_x, 46 + depth * gap_y)

        def draw_edges(node):
            if node is None:
                return
            x, y = scr[node]
            for child in (node.left, node.right):
                if child is not None:
                    cx, cy = scr[child]
                    self.create_line(x, y, cx, cy, fill=C["edge"], width=2)
                    draw_edges(child)
        draw_edges(self.tree.root)

        def draw_nodes(node):
            if node is None:
                return
            x, y = scr[node]
            r = 26
            fill = C["ok"] if node in self.highlight else C["accent"]
            self.create_oval(x - r, y - r, x + r, y + r, fill=fill, outline="white", width=3)
            self.create_text(x, y - 4, text=node.neighborhood[:8], font=self.app.f_small_b, fill="white")
            self.create_text(x, y + 9, text="h=%d" % node.height, font=self.app.f_tiny, fill="#DCE7F5")
            draw_nodes(node.left)
            draw_nodes(node.right)
        draw_nodes(self.tree.root)

    def animate_search(self, name, on_done=None):
        path, found = self.tree.search_path(name)
        self.highlight = set()
        self.render()

        def step(i):
            if i < len(path):
                self.highlight.add(path[i])
                self.render()
                self.after(380, lambda: step(i + 1))
            elif on_done:
                on_done(found)
        step(0)


# ═══════════════════════════════════════════════════════════════════════════
#  Main application
# ═══════════════════════════════════════════════════════════════════════════
class VeteranApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Veteran — Urban Logistics & Distribution System")
        self.geometry("1180x740")
        self.minsize(1040, 660)
        self.configure(bg=C["bg"])

        self._init_fonts()
        self._init_style()
        self._init_data()

        self.nav_buttons = {}
        self.current = None

        self._build_header()
        self._build_body()
        self._build_status()

        self.nav("dashboard")

    # ── Fonts ────────────────────────────────────────────────
    def _init_fonts(self):
        fam = "Segoe UI"
        self.f_title  = tkfont.Font(family=fam, size=18, weight="bold")
        self.f_h1     = tkfont.Font(family=fam, size=20, weight="bold")
        self.f_h2     = tkfont.Font(family=fam, size=14, weight="bold")
        self.f_body   = tkfont.Font(family=fam, size=11)
        self.f_body_b = tkfont.Font(family=fam, size=11, weight="bold")
        self.f_btn    = tkfont.Font(family=fam, size=10, weight="bold")
        self.f_nav    = tkfont.Font(family=fam, size=11)
        self.f_small  = tkfont.Font(family=fam, size=9)
        self.f_small_b = tkfont.Font(family=fam, size=9, weight="bold")
        self.f_tiny   = tkfont.Font(family=fam, size=7)
        self.f_stat   = tkfont.Font(family=fam, size=26, weight="bold")

    def _init_style(self):
        st = ttk.Style(self)
        try:
            st.theme_use("clam")
        except tk.TclError:
            pass
        st.configure("Treeview",
                     background="white", fieldbackground="white",
                     foreground=C["text"], rowheight=30,
                     font=self.f_body, bordercolor=C["card_brd"])
        st.configure("Treeview.Heading",
                     background=C["brand"], foreground="white",
                     font=self.f_body_b, relief="flat", padding=6)
        st.map("Treeview.Heading", background=[("active", C["brand_hi"])])
        st.map("Treeview", background=[("selected", C["accent"])],
               foreground=[("selected", "white")])

    # ── Data ─────────────────────────────────────────────────
    def _init_data(self):
        self.registry = core.SinglyLinkedList()
        self.buffer = core.DoublyLinkedList()
        self.queue = core.DeliveryQueue()
        self.stack = core.TruckStack()
        self.pq = core.PriorityDeliveryQueue()
        self.avl = core.AVLTree()
        self.graph = core.CityGraph()
        for (s, d, w) in core.SEED_EDGES:
            self.graph.add_edge(s, d, w)
        for i, (pid, dest, pri) in enumerate(core.SEED_PACKAGES, start=1):
            pkg = core.Package(pid, dest, pri)
            self.registry.add_record(pkg)
            self.buffer.insert_at_tail(pkg)
            self.pq.enqueue(pkg)
            self.avl.insert(dest, "CUST_%03d" % i)

    # ── Header ───────────────────────────────────────────────
    def _build_header(self):
        head = tk.Frame(self, bg=C["brand"], height=72)
        head.pack(side="top", fill="x")
        head.pack_propagate(False)
        tk.Label(head, text="📦", bg=C["brand"], fg="white",
                 font=tkfont.Font(family="Segoe UI Emoji", size=24)).pack(side="left", padx=(18, 6))
        box = tk.Frame(head, bg=C["brand"])
        box.pack(side="left", pady=12)
        tk.Label(box, text="VETERAN", bg=C["brand"], fg="white",
                 font=self.f_title).pack(side="top", anchor="w")
        tk.Label(box, text="Urban Logistics & Distribution System  •  Delivering Smarter, Faster, Together.",
                 bg=C["brand"], fg="#AEC4DE", font=self.f_small).pack(side="top", anchor="w")
        tk.Label(head, text="Central Depot: Meydan, Kayseri", bg=C["brand"], fg="#AEC4DE",
                 font=self.f_body).pack(side="right", padx=20)

    # ── Body: sidebar + content ──────────────────────────────
    def _build_body(self):
        body = tk.Frame(self, bg=C["bg"])
        body.pack(side="top", fill="both", expand=True)

        side = tk.Frame(body, bg=C["brand_dark"], width=248)
        side.pack(side="left", fill="y")
        side.pack_propagate(False)

        items = [
            ("dashboard", "Dashboard", "⌂"),
            ("registry",  "Master Registry (SLL)", "①"),
            ("buffer",    "Intake Buffer (DLL)", "②"),
            ("queue",     "Delivery Queue (FIFO)", "③"),
            ("priority",  "Priority Dispatch (Heap)", "★"),
            ("stack",     "Truck Loading (LIFO)", "④"),
            ("avl",       "Address Directory (AVL)", "⑤"),
            ("graph",     "City Map (Graph)", "⑥"),
            ("dijkstra",  "Shortest Path (Dijkstra)", "⑦"),
            ("mst",       "Min. Spanning Tree (Prim)", "⑧"),
            ("demo",      "Full Demo", "⑨"),
            ("add_pkg",   "Add Package", "⑩"),
            ("add_route", "Add Route", "⑪"),
            ("del_pkg",   "Delete Package", "⑫"),
        ]
        for key, text, badge in items:
            btn = NavButton(side, text, lambda k=key: self.nav(k), self.f_nav, badge)
            btn.pack(side="top", fill="x", padx=0, pady=1)
            self.nav_buttons[key] = btn

        tk.Frame(side, bg=C["brand_dark"]).pack(side="top", fill="both", expand=True)
        tk.Label(side, text="© 2026 Veteran • AGU", bg=C["brand_dark"], fg="#5A6B80",
                 font=self.f_small).pack(side="bottom", pady=10)

        self.content = tk.Frame(body, bg=C["bg"])
        self.content.pack(side="left", fill="both", expand=True)

    def _build_status(self):
        self.status = tk.Frame(self, bg="#DCE3EC", height=30)
        self.status.pack(side="bottom", fill="x")
        self.status.pack_propagate(False)
        self.status_dot = tk.Label(self.status, text="●", bg="#DCE3EC", fg=C["ok"], font=self.f_body)
        self.status_dot.pack(side="left", padx=(12, 4))
        self.status_lbl = tk.Label(self.status, text="Ready.", bg="#DCE3EC", fg=C["text"], font=self.f_body)
        self.status_lbl.pack(side="left")

    def toast(self, msg, kind="info"):
        col = {"info": C["accent"], "ok": C["ok"], "warn": C["warn"], "danger": C["danger"]}.get(kind, C["accent"])
        self.status_dot.configure(fg=col)
        self.status_lbl.configure(text=msg)
        # short highlight animation
        self.status.configure(bg=lerp_color("#DCE3EC", col, 0.25))
        for w in (self.status_dot, self.status_lbl):
            w.configure(bg=self.status.cget("bg"))
        self.after(550, self._toast_reset)

    def _toast_reset(self):
        self.status.configure(bg="#DCE3EC")
        self.status_dot.configure(bg="#DCE3EC")
        self.status_lbl.configure(bg="#DCE3EC")

    # ── Routing ──────────────────────────────────────────────
    def nav(self, key):
        for k, b in self.nav_buttons.items():
            b.set_active(k == key)
        self.current = key
        for w in self.content.winfo_children():
            w.destroy()
        getattr(self, "view_" + key)()

    # ── Shared UI pieces ─────────────────────────────────────
    def _page(self, title, subtitle=""):
        wrap = tk.Frame(self.content, bg=C["bg"])
        wrap.pack(fill="both", expand=True, padx=24, pady=20)
        tk.Label(wrap, text=title, bg=C["bg"], fg=C["brand"], font=self.f_h1).pack(anchor="w")
        if subtitle:
            tk.Label(wrap, text=subtitle, bg=C["bg"], fg=C["muted"], font=self.f_body).pack(anchor="w", pady=(2, 0))
        sep = tk.Frame(wrap, bg=C["accent"], height=3, width=70)
        sep.pack(anchor="w", pady=(8, 14))
        return wrap

    def _card(self, parent, **kw):
        outer = tk.Frame(parent, bg=C["card_brd"])
        inner = tk.Frame(outer, bg=C["card"], **kw)
        inner.pack(fill="both", expand=True, padx=1, pady=1)
        return outer, inner

    def _toolbar(self, parent):
        bar = tk.Frame(parent, bg=C["bg"])
        bar.pack(fill="x", pady=(0, 12))
        return bar

    def _btn(self, parent, text, cmd, color=None):
        b = HoverButton(parent, text, command=cmd, bg=color or C["accent"],
                        font=self.f_btn)
        b.pack(side="left", padx=(0, 10))
        return b

    # ═══════════════════════════════════════════════════════
    #  DASHBOARD
    # ═══════════════════════════════════════════════════════
    def view_dashboard(self):
        wrap = self._page("Dashboard", "Live status of the Veteran logistics system")

        stats = [
            ("Registered", self.registry.size, C["accent"]),
            ("In Buffer", self.buffer.size, C["warn"]),
            ("In Queue", self.queue.size, "#8E44AD"),
            ("Priority Q", self.pq.size, "#D35400"),
            ("On Truck", self.stack.size, C["ok"]),
            ("AVL Records", self.avl.node_count, "#16A085"),
            ("Map Nodes", self.graph.vertex_count, C["depot"]),
        ]
        row = tk.Frame(wrap, bg=C["bg"])
        row.pack(fill="x")
        for i, (label, val, col) in enumerate(stats):
            outer, card = self._card(row)
            outer.grid(row=0, column=i, padx=(0 if i == 0 else 10, 0), sticky="nsew")
            row.grid_columnconfigure(i, weight=1)
            tk.Frame(card, bg=col, height=4).pack(fill="x")
            tk.Label(card, text=str(val), bg=C["card"], fg=col, font=self.f_stat).pack(pady=(14, 0))
            tk.Label(card, text=label, bg=C["card"], fg=C["muted"], font=self.f_body).pack(pady=(0, 14))

        # Info + quick access
        mid = tk.Frame(wrap, bg=C["bg"])
        mid.pack(fill="both", expand=True, pady=(18, 0))

        outer, info = self._card(mid)
        outer.pack(side="left", fill="both", expand=True)
        tk.Label(info, text="What is this system?", bg=C["card"], fg=C["brand"], font=self.f_h2).pack(anchor="w", padx=18, pady=(16, 4))
        txt = ("Veteran is an urban logistics system that manages Kayseri's last-mile package delivery "
               "using hand-written data structures and graph algorithms.\n\n"
               "A package's journey:\n"
               "   Master Registry (SLL)  →  Intake Buffer (DLL)  →  Delivery Queue (FIFO)\n"
               "   →  Truck Loading (LIFO)  →  Delivered\n\n"
               "Addresses are kept in an AVL tree and city roads in a weighted graph; the shortest "
               "path is computed with Dijkstra and the infrastructure network with Prim's algorithm.")
        tk.Label(info, text=txt, bg=C["card"], fg=C["text"], font=self.f_body,
                 justify="left", anchor="w").pack(anchor="w", padx=18, pady=(0, 16))

        outer2, quick = self._card(mid, width=300)
        outer2.pack(side="left", fill="y", padx=(16, 0))
        quick.pack_propagate(False)
        tk.Label(quick, text="Quick Access", bg=C["card"], fg=C["brand"], font=self.f_h2).pack(anchor="w", padx=18, pady=(16, 10))
        for text, key, col in [("▶  Run Full Demo", "demo", C["ok"]),
                               ("⑦  Shortest Path", "dijkstra", C["accent"]),
                               ("⑤  Address Directory", "avl", "#16A085"),
                               ("⑩  Add New Package", "add_pkg", C["warn"])]:
            b = HoverButton(quick, text, command=lambda k=key: self.nav(k),
                            bg=col, font=self.f_btn, anchor="w")
            b.configure(anchor="w")
            b.pack(fill="x", padx=18, pady=5)

    # ═══════════════════════════════════════════════════════
    #  ① MASTER REGISTRY (SLL)
    # ═══════════════════════════════════════════════════════
    def view_registry(self):
        wrap = self._page("① Master Registry", "Singly Linked List (SLL) — permanent, append-only ledger")
        bar = self._toolbar(wrap)
        tk.Label(bar, text="Search Package ID:", bg=C["bg"], fg=C["text"], font=self.f_body).pack(side="left", padx=(0, 6))
        ent = tk.Entry(bar, font=self.f_body, width=20, relief="flat",
                       highlightthickness=1, highlightbackground=C["card_brd"], highlightcolor=C["accent"])
        ent.pack(side="left", ipady=4, padx=(0, 10))
        self._btn(bar, "🔍  Search", lambda: self._registry_search(ent.get(), tv))
        self._btn(bar, "↻  Refresh", lambda: self._fill_registry(tv), C["muted"])
        tk.Label(bar, text="Total: %d records" % self.registry.size, bg=C["bg"],
                 fg=C["muted"], font=self.f_body_b).pack(side="right")

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        cols = ("no", "id", "dest", "pri", "w")
        tv = ttk.Treeview(card, columns=cols, show="headings")
        for c, t, w in [("no", "#", 50), ("id", "Package ID", 160), ("dest", "Destination", 160),
                        ("pri", "Priority", 90), ("w", "Weight (kg)", 110)]:
            tv.heading(c, text=t)
            tv.column(c, width=w, anchor="center" if c != "id" else "w")
        tv.tag_configure("hit", background=C["ok_dim"], foreground="white")
        tv.tag_configure("odd", background="#F4F8FC")
        tv.pack(fill="both", expand=True, padx=2, pady=2)
        self._fill_registry(tv)

    def _fill_registry(self, tv):
        tv.delete(*tv.get_children())
        for i, p in enumerate(self.registry.to_list(), start=1):
            tag = "odd" if i % 2 else "even"
            tv.insert("", "end", iid=p.package_id,
                      values=(i, p.package_id, p.destination, p.priority, "%.1f" % p.weight_kg),
                      tags=(tag,))

    def _registry_search(self, key, tv):
        key = key.strip()
        if not key:
            return
        self._fill_registry(tv)
        found = self.registry.search(key)
        if found:
            tv.item(found.package_id, tags=("hit",))
            tv.see(found.package_id)
            tv.selection_set(found.package_id)
            self.toast("Found: %s → %s" % (found.package_id, found.destination), "ok")
        else:
            self.toast("Package not found: '%s'" % key, "warn")

    # ═══════════════════════════════════════════════════════
    #  ② INTAKE BUFFER (DLL)
    # ═══════════════════════════════════════════════════════
    def view_buffer(self):
        wrap = self._page("② Intake Buffer", "Doubly Linked List (DLL) — head/tail insert & remove in O(1)")
        bar = self._toolbar(wrap)
        self._btn(bar, "＋ Insert Head", lambda: self._buf_insert(True))
        self._btn(bar, "Insert Tail ＋", lambda: self._buf_insert(False))
        self._btn(bar, "Remove Head ⟶", lambda: self._buf_remove(True), C["warn"])
        self._btn(bar, "⟵ Remove Tail", lambda: self._buf_remove(False), C["warn"])
        self.buf_count = tk.Label(bar, text="", bg=C["bg"], fg=C["muted"], font=self.f_body_b)
        self.buf_count.pack(side="right")

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        cols = ("no", "id", "dest", "pos")
        self.buf_tv = ttk.Treeview(card, columns=cols, show="headings")
        for c, t, w in [("no", "#", 60), ("id", "Package ID", 200), ("dest", "Destination", 200), ("pos", "Position", 140)]:
            self.buf_tv.heading(c, text=t)
            self.buf_tv.column(c, width=w, anchor="center" if c != "id" else "w")
        self.buf_tv.tag_configure("flash", background=C["ok_dim"], foreground="white")
        self.buf_tv.tag_configure("odd", background="#F4F8FC")
        self.buf_tv.pack(fill="both", expand=True, padx=2, pady=2)
        self._fill_buffer()

    def _fill_buffer(self, flash_id=None):
        self.buf_tv.delete(*self.buf_tv.get_children())
        items = self.buffer.to_list()
        n = len(items)
        for i, p in enumerate(items, start=1):
            pos = "HEAD" if i == 1 else ("TAIL" if i == n else "")
            tag = "flash" if p.package_id == flash_id else ("odd" if i % 2 else "even")
            self.buf_tv.insert("", "end", iid=p.package_id,
                               values=(i, p.package_id, p.destination, pos), tags=(tag,))
        self.buf_count.configure(text="Buffer: %d packages" % self.buffer.size)
        if flash_id:
            self.after(700, lambda: self._unflash(self.buf_tv, flash_id))

    def _unflash(self, tv, iid):
        if tv.exists(iid):
            idx = tv.index(iid)
            tv.item(iid, tags=("odd" if (idx + 1) % 2 else "even",))

    _new_counter = [900]

    def _next_pkg(self):
        self._new_counter[0] += 1
        return core.Package("PKG_KYS_%03d" % self._new_counter[0],
                            ["Talas", "Belsin", "Erkilet", "Ildem", "Anbar", "Mimsin"][self._new_counter[0] % 6])

    def _buf_insert(self, at_head):
        p = self._next_pkg()
        if at_head:
            self.buffer.insert_at_head(p)
        else:
            self.buffer.insert_at_tail(p)
        self._fill_buffer(flash_id=p.package_id)
        self.toast("Inserted (%s): %s" % ("head" if at_head else "tail", p.package_id), "ok")

    def _buf_remove(self, from_head):
        p = self.buffer.remove_from_head() if from_head else self.buffer.remove_from_tail()
        if p:
            self._fill_buffer()
            self.toast("Removed (%s): %s" % ("head" if from_head else "tail", p.package_id), "warn")
        else:
            self.toast("Buffer is empty.", "warn")

    # ═══════════════════════════════════════════════════════
    #  ③ DELIVERY QUEUE (FIFO)
    # ═══════════════════════════════════════════════════════
    def view_queue(self):
        wrap = self._page("③ Delivery Queue", "FIFO Queue — first in, first out")
        bar = self._toolbar(wrap)
        self._btn(bar, "Move Buffer → Queue", self._queue_fill_from_buffer, C["accent"])
        self._btn(bar, "Dequeue (from front) ⟶", self._queue_dequeue, C["warn"])
        self._btn(bar, "↻ Reset Demo", self._queue_reset, C["muted"])

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        self.q_canvas = tk.Canvas(card, bg="#F7FAFD", highlightthickness=0)
        self.q_canvas.pack(fill="both", expand=True, padx=2, pady=2)
        self.q_canvas.bind("<Configure>", lambda e: self._draw_lane(self.q_canvas, self.queue.to_list(), "FRONT", "REAR"))
        self._draw_lane(self.q_canvas, self.queue.to_list(), "FRONT", "REAR")

    def _draw_lane(self, cv, items, head_lbl, tail_lbl, flash_idx=None, vertical=False):
        cv.delete("all")
        if not items:
            cv.create_text(cv.winfo_width() / 2, cv.winfo_height() / 2,
                           text="(Empty)", fill=C["muted"], font=self.f_h2)
            return
        n = len(items)
        if not vertical:
            cw, gap, x0, y = 150, 18, 40, cv.winfo_height() / 2 - 35
            for i, p in enumerate(items):
                x = x0 + i * (cw + gap)
                col = C["ok"] if i == flash_idx else C["accent"]
                round_rect(cv, x, y, x + cw, y + 70, r=12, fill=col, outline="")
                cv.create_text(x + cw / 2, y + 22, text=p.package_id, fill="white", font=self.f_small_b)
                cv.create_text(x + cw / 2, y + 44, text="→ " + p.destination, fill="#E8F0FB", font=self.f_small)
                if i == 0:
                    cv.create_text(x + cw / 2, y - 14, text=head_lbl, fill=C["depot"], font=self.f_small_b)
                if i == n - 1 and n > 1:
                    cv.create_text(x + cw / 2, y + 84, text=tail_lbl, fill=C["muted"], font=self.f_small_b)
                if i < n - 1:
                    ax = x + cw + gap / 2
                    cv.create_text(ax, y + 35, text="›", fill=C["muted"], font=self.f_h1)
        else:
            ch, gap, y0, x = 52, 12, 30, cv.winfo_width() / 2 - 110
            for i, p in enumerate(items):
                y = y0 + i * (ch + gap)
                col = C["ok"] if i == flash_idx else C["accent"]
                round_rect(cv, x, y, x + 220, y + ch, r=10, fill=col, outline="")
                cv.create_text(x + 16, y + ch / 2, text=p.package_id, fill="white", font=self.f_small_b, anchor="w")
                cv.create_text(x + 220 - 16, y + ch / 2, text="→ " + p.destination, fill="#E8F0FB", font=self.f_small, anchor="e")
                if i == 0:
                    cv.create_text(x - 16, y + ch / 2, text=head_lbl, fill=C["depot"], font=self.f_small_b, anchor="e")
                if i == n - 1 and n > 1:
                    cv.create_text(x + 220 + 16, y + ch / 2, text=tail_lbl, fill=C["muted"], font=self.f_small_b, anchor="w")

    def _queue_fill_from_buffer(self):
        moved = 0
        while not self.buffer.is_empty():
            self.queue.enqueue(self.buffer.remove_from_head())
            moved += 1
        self._draw_lane(self.q_canvas, self.queue.to_list(), "FRONT", "REAR", flash_idx=self.queue.size - 1)
        self.toast("%d packages moved from buffer to queue." % moved, "ok" if moved else "warn")

    def _queue_dequeue(self):
        p = self.queue.dequeue()
        if p:
            self._draw_lane(self.q_canvas, self.queue.to_list(), "FRONT", "REAR", flash_idx=0)
            self.toast("Dequeued: %s sent to delivery." % p.package_id, "warn")
        else:
            self.toast("Queue is empty. Move from buffer first.", "warn")

    def _queue_reset(self):
        self.buffer = core.DoublyLinkedList()
        for pid, dest, pri in core.SEED_PACKAGES:
            self.buffer.insert_at_tail(core.Package(pid, dest, pri))
        self.queue = core.DeliveryQueue()
        self._draw_lane(self.q_canvas, [], "FRONT", "REAR")
        self.toast("Demo reset: 12 packages loaded into buffer.", "info")

    # ═══════════════════════════════════════════════════════
    #  ★ PRIORITY DISPATCH (Min-Heap)
    # ═══════════════════════════════════════════════════════
    def view_priority(self):
        wrap = self._page("★ Priority Dispatch",
                          "Priority Queue — hand-written binary Min-Heap  (1 = most urgent … 5 = lowest)")
        bar = self._toolbar(wrap)
        self._btn(bar, "Dispatch Most Urgent ⟶", self._pq_dispatch_one, C["accent"])
        self._btn(bar, "🚚 Dispatch All (by priority)", self._pq_dispatch_all, C["ok"])
        self._btn(bar, "↻ Reset", self._pq_reset, C["muted"])
        self.pq_count = tk.Label(bar, text="", bg=C["bg"], fg=C["muted"], font=self.f_body_b)
        self.pq_count.pack(side="right")

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        cols = ("rank", "id", "dest", "pri")
        self.pq_tv = ttk.Treeview(card, columns=cols, show="headings")
        for c, t, w in [("rank", "Order", 80), ("id", "Package ID", 200),
                        ("dest", "Destination", 200), ("pri", "Priority", 120)]:
            self.pq_tv.heading(c, text=t)
            self.pq_tv.column(c, width=w, anchor="center" if c != "id" else "w")
        # oncelik renk tonlari (1 = en acil)
        for pr, bg in [(1, "#F9D7D2"), (2, "#FCE3CC"), (3, "#D6EAF8"), (4, "#EAEDED"), (5, "#F4F6F7")]:
            self.pq_tv.tag_configure("p%d" % pr, background=bg, foreground=C["text"])
        self.pq_tv.tag_configure("flash", background=C["ok_dim"], foreground="white")
        self.pq_tv.pack(fill="both", expand=True, padx=2, pady=2)
        self._fill_priority()

    _PRI_LABEL = {1: "1 — Highest", 2: "2 — High", 3: "3 — Normal", 4: "4 — Low", 5: "5 — Lowest"}

    def _fill_priority(self, flash_id=None):
        self.pq_tv.delete(*self.pq_tv.get_children())
        for i, p in enumerate(self.pq.to_list(), start=1):
            tag = "flash" if p.package_id == flash_id else ("p%d" % p.priority if 1 <= p.priority <= 5 else "")
            self.pq_tv.insert("", "end", iid=p.package_id,
                              values=(i, p.package_id, p.destination,
                                      self._PRI_LABEL.get(p.priority, str(p.priority))),
                              tags=(tag,))
        self.pq_count.configure(text="In heap: %d packages" % self.pq.size)

    def _pq_dispatch_one(self):
        p = self.pq.dequeue()
        if p:
            self._fill_priority()
            self.toast("🚚 Dispatched (priority %d): %s → %s" % (p.priority, p.package_id, p.destination), "ok")
        else:
            self.toast("Priority queue is empty. Press Reset to reload.", "warn")

    def _pq_dispatch_all(self):
        if self.pq.is_empty():
            self.toast("Priority queue is empty. Press Reset to reload.", "warn")
            return
        order = []
        while not self.pq.is_empty():
            order.append(self.pq.dequeue())
        self._fill_priority()
        head = ", ".join("%s(P%d)" % (p.package_id, p.priority) for p in order[:4])
        self.toast("Dispatched all %d packages by priority: %s …" % (len(order), head), "ok")

    def _pq_reset(self):
        self.pq = core.PriorityDeliveryQueue()
        for pid, dest, pri in core.SEED_PACKAGES:
            self.pq.enqueue(core.Package(pid, dest, pri))
        self._fill_priority()
        self.toast("Priority queue reloaded with %d packages." % self.pq.size, "info")

    # ═══════════════════════════════════════════════════════
    #  ④ TRUCK STACK (LIFO)
    # ═══════════════════════════════════════════════════════
    def view_stack(self):
        wrap = self._page("④ Truck Loading", "LIFO Stack — last in, first out")
        bar = self._toolbar(wrap)
        self._btn(bar, "Load from Queue (Push)", self._stack_load_from_queue, C["accent"])
        self._btn(bar, "Pop (from top) ⟶", self._stack_pop, C["warn"])
        tk.Label(bar, text="Top = last loaded = first delivered", bg=C["bg"],
                 fg=C["muted"], font=self.f_small).pack(side="right")

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        self.s_canvas = tk.Canvas(card, bg="#F7FAFD", highlightthickness=0)
        self.s_canvas.pack(fill="both", expand=True, padx=2, pady=2)
        self.s_canvas.bind("<Configure>", lambda e: self._draw_lane(self.s_canvas, self.stack.to_list(), "TOP", "BOTTOM", vertical=True))
        self._draw_lane(self.s_canvas, self.stack.to_list(), "TOP", "BOTTOM", vertical=True)

    def _stack_load_from_queue(self):
        if self.queue.is_empty():
            # if the queue is empty, auto-feed from the buffer
            while not self.buffer.is_empty():
                self.queue.enqueue(self.buffer.remove_from_head())
        loaded = 0
        while not self.queue.is_empty():
            self.stack.push(self.queue.dequeue())
            loaded += 1
        self._draw_lane(self.s_canvas, self.stack.to_list(), "TOP", "BOTTOM", flash_idx=0, vertical=True)
        self.toast("%d packages loaded onto truck." % loaded, "ok" if loaded else "warn")

    def _stack_pop(self):
        p = self.stack.pop()
        if p:
            self._draw_lane(self.s_canvas, self.stack.to_list(), "TOP", "BOTTOM", flash_idx=0, vertical=True)
            self.toast("📦 Delivered (pop): %s" % p.package_id, "warn")
        else:
            self.toast("Truck is empty. Load from queue first.", "warn")

    # ═══════════════════════════════════════════════════════
    #  ⑤ ADDRESS DIRECTORY (AVL)
    # ═══════════════════════════════════════════════════════
    def view_avl(self):
        wrap = self._page("⑤ Address Directory", "AVL Tree — self-balancing BST, search O(log n)")
        bar = self._toolbar(wrap)
        tk.Label(bar, text="District:", bg=C["bg"], fg=C["text"], font=self.f_body).pack(side="left", padx=(0, 6))
        ent = tk.Entry(bar, font=self.f_body, width=16, relief="flat",
                       highlightthickness=1, highlightbackground=C["card_brd"], highlightcolor=C["accent"])
        ent.pack(side="left", ipady=4, padx=(0, 8))
        self._btn(bar, "🔍 Search (animated)", lambda: self._avl_search(ent.get()))
        self._btn(bar, "＋ New Record", lambda: self._avl_add(ent.get()), C["ok"])
        tk.Label(bar, text="Nodes: %d  •  Height: %d" % (self.avl.node_count, self.avl.tree_height()),
                 bg=C["bg"], fg=C["muted"], font=self.f_body_b).pack(side="right")

        split = tk.Frame(wrap, bg=C["bg"])
        split.pack(fill="both", expand=True)
        outer, card = self._card(split)
        outer.pack(side="left", fill="both", expand=True)
        self.avl_canvas = AVLCanvas(card, self)
        self.avl_canvas.pack(fill="both", expand=True, padx=2, pady=2)
        self.avl_canvas.set_tree(self.avl)

        outer2, side = self._card(split, width=280)
        outer2.pack(side="left", fill="y", padx=(14, 0))
        side.pack_propagate(False)
        tk.Label(side, text="Sorted List", bg=C["card"], fg=C["brand"], font=self.f_h2).pack(anchor="w", padx=14, pady=(12, 6))
        lst = ttk.Treeview(side, columns=("s", "c"), show="headings", height=12)
        lst.heading("s", text="District"); lst.heading("c", text="Customer")
        lst.column("s", width=120); lst.column("c", width=120)
        lst.pack(fill="both", expand=True, padx=10, pady=(0, 12))
        for (name, cid, h) in self.avl.in_order():
            lst.insert("", "end", values=(name, cid))

    def _avl_search(self, name):
        name = name.strip()
        if not name:
            return
        self.toast("Searching: %s ..." % name, "info")
        self.avl_canvas.animate_search(name, on_done=lambda found: self.toast(
            ("Found: %s → %s" % (name, found.customer_id)) if found else ("Not found: %s" % name),
            "ok" if found else "warn"))

    def _avl_add(self, name):
        name = name.strip()
        if not name:
            self.toast("Type a district name first.", "warn")
            return
        cid = "CUST_%03d" % (self.avl.node_count + 1)
        self.avl.insert(name, cid)
        self.nav("avl")
        self.toast("Inserted/updated in AVL: %s → %s" % (name, cid), "ok")

    # ═══════════════════════════════════════════════════════
    #  ⑥ CITY MAP (GRAPH)
    # ═══════════════════════════════════════════════════════
    def view_graph(self):
        wrap = self._page("⑥ City Map", "Weighted, undirected graph — Kayseri road network (adjacency list)")
        info = tk.Label(wrap, text="Red node = Central Depot (Meydan). Numbers on edges = distance (km).",
                        bg=C["bg"], fg=C["muted"], font=self.f_body)
        info.pack(anchor="w", pady=(0, 10))
        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        gc = GraphCanvas(card, self.graph, self)
        gc.pack(fill="both", expand=True, padx=2, pady=2)
        gc.show_base()

    # ═══════════════════════════════════════════════════════
    #  ⑦ DIJKSTRA
    # ═══════════════════════════════════════════════════════
    def view_dijkstra(self):
        wrap = self._page("⑦ Shortest Path", "Dijkstra's Algorithm — shortest route between two districts")
        bar = self._toolbar(wrap)
        names = list(self.graph.vertex_names)
        tk.Label(bar, text="Start:", bg=C["bg"], fg=C["text"], font=self.f_body).pack(side="left")
        start = ttk.Combobox(bar, values=names, font=self.f_body, width=14, state="readonly")
        start.set("Meydan"); start.pack(side="left", padx=(6, 14))
        tk.Label(bar, text="End:", bg=C["bg"], fg=C["text"], font=self.f_body).pack(side="left")
        end = ttk.Combobox(bar, values=names, font=self.f_body, width=14, state="readonly")
        end.set("Mimsin"); end.pack(side="left", padx=(6, 14))
        self._btn(bar, "▶ Compute", lambda: self._run_dijkstra(start.get(), end.get()))

        split = tk.Frame(wrap, bg=C["bg"])
        split.pack(fill="both", expand=True)
        outer, card = self._card(split)
        outer.pack(side="left", fill="both", expand=True)
        self.dij_canvas = GraphCanvas(card, self.graph, self)
        self.dij_canvas.pack(fill="both", expand=True, padx=2, pady=2)
        self.dij_canvas.show_base()

        outer2, panel = self._card(split, width=300)
        outer2.pack(side="left", fill="y", padx=(14, 0))
        panel.pack_propagate(False)
        tk.Label(panel, text="Result", bg=C["card"], fg=C["brand"], font=self.f_h2).pack(anchor="w", padx=16, pady=(14, 8))
        self.dij_result = tk.Label(panel, text="Select Start/End and click 'Compute'.",
                                   bg=C["card"], fg=C["text"], font=self.f_body, justify="left", wraplength=260, anchor="nw")
        self.dij_result.pack(fill="both", expand=True, anchor="nw", padx=16, pady=(0, 14))

    def _run_dijkstra(self, start, end):
        if start == end:
            self.toast("Start and end are the same.", "warn")
            return
        res = self.graph.dijkstra(start, end)
        if not res:
            self.dij_result.configure(text="No path found between %s and %s." % (start, end))
            self.toast("No path.", "warn")
            self.dij_canvas.show_base()
            return
        self.dij_canvas.animate_path(res["path"])
        steps = "\n".join("   %s → %s : %d km" % (a, b, w) for (a, b, w) in res["steps"])
        self.dij_result.configure(text="Total distance:  %d km\n\nRoute:\n   %s\n\nSteps:\n%s"
                                  % (res["distance"], "  →  ".join(res["path"]), steps))
        self.toast("Shortest path: %d km (%d steps)" % (res["distance"], len(res["steps"])), "ok")

    # ═══════════════════════════════════════════════════════
    #  ⑧ PRIM MST
    # ═══════════════════════════════════════════════════════
    def view_mst(self):
        wrap = self._page("⑧ Minimum Spanning Tree", "Prim's Algorithm — connects all districts with minimum total road")
        bar = self._toolbar(wrap)
        self._btn(bar, "▶ Compute MST", self._run_mst, C["ok"])
        self._btn(bar, "↻ Clear", lambda: self.mst_canvas.show_base(), C["muted"])

        split = tk.Frame(wrap, bg=C["bg"])
        split.pack(fill="both", expand=True)
        outer, card = self._card(split)
        outer.pack(side="left", fill="both", expand=True)
        self.mst_canvas = GraphCanvas(card, self.graph, self)
        self.mst_canvas.pack(fill="both", expand=True, padx=2, pady=2)
        self.mst_canvas.show_base()

        outer2, panel = self._card(split, width=300)
        outer2.pack(side="left", fill="y", padx=(14, 0))
        panel.pack_propagate(False)
        tk.Label(panel, text="MST Edges", bg=C["card"], fg=C["brand"], font=self.f_h2).pack(anchor="w", padx=16, pady=(14, 8))
        self.mst_result = tk.Label(panel, text="Click 'Compute'.\nGold edges show the MST.",
                                   bg=C["card"], fg=C["text"], font=self.f_body, justify="left", wraplength=260, anchor="nw")
        self.mst_result.pack(fill="both", expand=True, anchor="nw", padx=16, pady=(0, 14))

    def _run_mst(self):
        res = self.graph.prim()
        if not res:
            return
        self.mst_canvas.show_mst(res["edges"])
        lines = "\n".join("   %s — %s : %d km" % (u, v, w) for (u, v, w) in res["edges"])
        self.mst_result.configure(text="%s\n\nTotal network length:\n   %d km" % (lines, res["total"]))
        self.toast("MST computed — total %d km" % res["total"], "ok")

    # ═══════════════════════════════════════════════════════
    #  ⑨ FULL DEMO
    # ═══════════════════════════════════════════════════════
    def view_demo(self):
        wrap = self._page("⑨ Full Demo", "Step-by-step, animated walkthrough of all subsystems")
        bar = self._toolbar(wrap)
        self.demo_btn = self._btn(bar, "▶ Start Demo", self._start_demo, C["ok"])

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        self.demo_log = tk.Text(card, bg="#0E2236", fg="#CFE3F5", font=tkfont.Font(family="Consolas", size=11),
                                relief="flat", padx=16, pady=14, state="disabled", wrap="word")
        self.demo_log.pack(fill="both", expand=True, padx=2, pady=2)
        self.demo_log.tag_configure("h", foreground=C["gold"], font=tkfont.Font(family="Consolas", size=12, weight="bold"))
        self.demo_log.tag_configure("ok", foreground="#7FE7A4")
        self.demo_log.tag_configure("dim", foreground="#7F94AB")

    def _demo_say(self, text, tag=None):
        self.demo_log.configure(state="normal")
        self.demo_log.insert("end", text + "\n", (tag,) if tag else ())
        self.demo_log.see("end")
        self.demo_log.configure(state="disabled")

    def _start_demo(self):
        self.demo_log.configure(state="normal"); self.demo_log.delete("1.0", "end"); self.demo_log.configure(state="disabled")
        # clean, isolated structures for the demo
        reg = core.SinglyLinkedList(); buf = core.DoublyLinkedList()
        q = core.DeliveryQueue(); stk = core.TruckStack()
        for pid, dest, pri in core.SEED_PACKAGES:
            p = core.Package(pid, dest, pri); reg.add_record(p); buf.insert_at_tail(p)
        steps = []
        steps.append(("h", "1) MASTER REGISTRY (SLL)"))
        steps.append(("dim", "   %d packages written to the permanent ledger." % reg.size))
        steps.append(("dim", "   Search PKG_KYS_005 → %s" % reg.search("PKG_KYS_005")))
        steps.append(("h", "2) INTAKE BUFFER (DLL)"))
        steps.append(("dim", "   %d packages in buffer (head: %s, tail: %s)." %
                      (buf.size, buf.head.data.package_id, buf.tail.data.package_id)))
        steps.append(("h", "3) DELIVERY QUEUE (FIFO)"))
        moved = 0
        while not buf.is_empty():
            q.enqueue(buf.remove_from_head()); moved += 1
        steps.append(("dim", "   %d packages moved from buffer to queue." % moved))
        steps.append(("dim", "   Dequeue → %s sent to delivery." % q.dequeue().package_id))
        steps.append(("h", "4) TRUCK LOADING (LIFO)"))
        loaded = 0
        while not q.is_empty():
            stk.push(q.dequeue()); loaded += 1
        steps.append(("dim", "   %d packages loaded onto truck (top: %s)." % (loaded, stk.peek().package_id)))
        order = []
        while not stk.is_empty():
            order.append(stk.pop().package_id)
        steps.append(("dim", "   Delivery order (LIFO): " + ", ".join(order[:5]) + " ..."))
        steps.append(("h", "5) ADDRESS DIRECTORY (AVL)"))
        steps.append(("dim", "   %d districts registered, tree height %d." % (self.avl.node_count, self.avl.tree_height())))
        steps.append(("h", "6) CITY MAP (GRAPH)"))
        steps.append(("dim", "   %d districts, %d roads." % (self.graph.vertex_count, len(self.graph.edges()))))
        steps.append(("h", "7) SHORTEST PATH (DIJKSTRA)"))
        d = self.graph.dijkstra("Meydan", "Mimsin")
        steps.append(("dim", "   Meydan → Mimsin : %d km  (%s)" % (d["distance"], " → ".join(d["path"]))))
        steps.append(("h", "8) MIN. SPANNING TREE (PRIM)"))
        m = self.graph.prim()
        steps.append(("dim", "   Total infrastructure network: %d km (%d edges)." % (m["total"], len(m["edges"]))))
        steps.append(("h", "9) PRIORITY DISPATCH (MIN-HEAP)"))
        pq = core.PriorityDeliveryQueue()
        for pid, dest, pri in core.SEED_PACKAGES:
            pq.enqueue(core.Package(pid, dest, pri))
        urgency = ["%s(P%d)" % (p.package_id, p.priority) for p in pq.to_list()]
        steps.append(("dim", "   Dispatch order by urgency: " + ", ".join(urgency[:6]) + " …"))
        steps.append(("dim", "   Most urgent: %s" % pq.peek().package_id))
        steps.append(("ok", "✔ Full demo complete — all subsystems operational."))

        # play with animation
        self._demo_play(steps, 0)

    def _demo_play(self, steps, i):
        if i >= len(steps):
            self.toast("Demo complete.", "ok")
            return
        tag, text = steps[i]
        self._demo_say(text, tag)
        self.after(520 if tag == "h" else 360, lambda: self._demo_play(steps, i + 1))

    # ═══════════════════════════════════════════════════════
    #  ⑩ ADD PACKAGE
    # ═══════════════════════════════════════════════════════
    def view_add_pkg(self):
        wrap = self._page("⑩ Add Package", "New package; added to SLL + DLL + AVL at once")
        outer, card = self._card(wrap)
        outer.pack(fill="x")
        form = tk.Frame(card, bg=C["card"]); form.pack(padx=26, pady=22, anchor="w")
        e_id = self._field(form, "Package ID", 0, "PKG_KYS_099")
        e_dest = self._field(form, "Destination", 1, "Talas")
        e_pri = self._field(form, "Priority (1-5)", 2, "3")
        e_w = self._field(form, "Weight (kg)", 3, "1.5")
        HoverButton(form, "＋  Add to System", bg=C["ok"], font=self.f_btn,
                    command=lambda: self._do_add_pkg(e_id, e_dest, e_pri, e_w)).grid(row=4, column=1, sticky="w", pady=(14, 0))

        outer2, prev = self._card(wrap)
        outer2.pack(fill="both", expand=True, pady=(16, 0))
        tk.Label(prev, text="Registry (current state)", bg=C["card"], fg=C["brand"], font=self.f_h2).pack(anchor="w", padx=14, pady=(12, 6))
        self.add_tv = ttk.Treeview(prev, columns=("id", "dest", "pri", "w"), show="headings", height=8)
        for c, t, w in [("id", "Package ID", 180), ("dest", "Destination", 160), ("pri", "Priority", 90), ("w", "Weight", 100)]:
            self.add_tv.heading(c, text=t); self.add_tv.column(c, width=w, anchor="center" if c != "id" else "w")
        self.add_tv.tag_configure("flash", background=C["ok_dim"], foreground="white")
        self.add_tv.pack(fill="both", expand=True, padx=12, pady=(0, 12))
        self._refresh_add_tv()

    def _field(self, form, label, row, default=""):
        tk.Label(form, text=label + ":", bg=C["card"], fg=C["text"], font=self.f_body, width=14, anchor="e").grid(row=row, column=0, padx=(0, 12), pady=6, sticky="e")
        e = tk.Entry(form, font=self.f_body, width=26, relief="flat",
                     highlightthickness=1, highlightbackground=C["card_brd"], highlightcolor=C["accent"])
        e.insert(0, default)
        e.grid(row=row, column=1, pady=6, sticky="w", ipady=4)
        return e

    def _refresh_add_tv(self, flash_id=None):
        self.add_tv.delete(*self.add_tv.get_children())
        for p in self.registry.to_list():
            tag = "flash" if p.package_id == flash_id else ()
            self.add_tv.insert("", "end", iid=p.package_id,
                               values=(p.package_id, p.destination, p.priority, "%.1f" % p.weight_kg), tags=tag)
        if flash_id and self.add_tv.exists(flash_id):
            self.add_tv.see(flash_id)
            self.after(900, lambda: self.add_tv.exists(flash_id) and self.add_tv.item(flash_id, tags=()))

    def _do_add_pkg(self, e_id, e_dest, e_pri, e_w):
        pid, dest = e_id.get().strip(), e_dest.get().strip()
        if not pid or not dest:
            self.toast("Package ID and Destination are required.", "warn")
            return
        try:
            pri = max(1, min(5, int(e_pri.get())))
        except ValueError:
            pri = 3
        try:
            w = float(e_w.get())
        except ValueError:
            w = 1.0
        if self.registry.search(pid):
            self.toast("This ID is already registered: %s" % pid, "warn")
            return
        p = core.Package(pid, dest, pri, w)
        self.registry.add_record(p)
        self.buffer.insert_at_tail(p)
        self.avl.insert(dest, "CUST_%03d" % (self.avl.node_count + 1))
        self._refresh_add_tv(flash_id=pid)
        self.toast("Added: %s → %s (SLL + DLL + AVL)" % (pid, dest), "ok")

    # ═══════════════════════════════════════════════════════
    #  ⑪ ADD ROUTE
    # ═══════════════════════════════════════════════════════
    def view_add_route(self):
        wrap = self._page("⑪ Add Route", "Adds a new edge (road) to the city map — the graph updates")
        outer, card = self._card(wrap)
        outer.pack(fill="x")
        form = tk.Frame(card, bg=C["card"]); form.pack(padx=26, pady=22, anchor="w")
        e_s = self._field(form, "Source", 0, "Meydan")
        e_d = self._field(form, "Destination", 1, "New_District")
        e_km = self._field(form, "Distance (km)", 2, "5")
        HoverButton(form, "＋  Add Route", bg=C["ok"], font=self.f_btn,
                    command=lambda: self._do_add_route(e_s, e_d, e_km)).grid(row=3, column=1, sticky="w", pady=(14, 0))

        outer2, card2 = self._card(wrap)
        outer2.pack(fill="both", expand=True, pady=(16, 0))
        self.route_canvas = GraphCanvas(card2, self.graph, self)
        self.route_canvas.pack(fill="both", expand=True, padx=2, pady=2)
        self.route_canvas.show_base()

    def _do_add_route(self, e_s, e_d, e_km):
        s, d = e_s.get().strip(), e_d.get().strip()
        if not s or not d:
            self.toast("Source and Destination are required.", "warn")
            return
        try:
            km = int(e_km.get())
            if km <= 0:
                raise ValueError
        except ValueError:
            self.toast("Distance must be a positive integer.", "warn")
            return
        self.graph.add_edge(s, d, km)
        self.route_canvas.graph = self.graph
        self.route_canvas.show_base()
        self.toast("Route added: %s ↔ %s (%d km)" % (s, d, km), "ok")

    # ═══════════════════════════════════════════════════════
    #  ⑫ DELETE PACKAGE
    # ═══════════════════════════════════════════════════════
    def view_del_pkg(self):
        wrap = self._page("⑫ Delete Package", "Removes the package from the Registry (SLL) and Intake Buffer (DLL)")
        bar = self._toolbar(wrap)
        tk.Label(bar, text="Package ID to delete:", bg=C["bg"], fg=C["text"], font=self.f_body).pack(side="left", padx=(0, 6))
        ent = tk.Entry(bar, font=self.f_body, width=20, relief="flat",
                       highlightthickness=1, highlightbackground=C["card_brd"], highlightcolor=C["accent"])
        ent.pack(side="left", ipady=4, padx=(0, 10))
        self._btn(bar, "🗑  Delete", lambda: self._do_del(ent.get()), C["danger"])

        outer, card = self._card(wrap)
        outer.pack(fill="both", expand=True)
        self.del_tv = ttk.Treeview(card, columns=("id", "dest", "pri"), show="headings")
        for c, t, w in [("id", "Package ID", 200), ("dest", "Destination", 180), ("pri", "Priority", 100)]:
            self.del_tv.heading(c, text=t); self.del_tv.column(c, width=w, anchor="center" if c != "id" else "w")
        self.del_tv.tag_configure("odd", background="#F4F8FC")
        self.del_tv.pack(fill="both", expand=True, padx=2, pady=2)
        self.del_tv.bind("<<TreeviewSelect>>", lambda e: self._sel_to_entry(ent))
        self._fill_del_tv()

    def _sel_to_entry(self, ent):
        sel = self.del_tv.selection()
        if sel:
            ent.delete(0, "end"); ent.insert(0, sel[0])

    def _fill_del_tv(self):
        self.del_tv.delete(*self.del_tv.get_children())
        for i, p in enumerate(self.registry.to_list(), start=1):
            self.del_tv.insert("", "end", iid=p.package_id,
                               values=(p.package_id, p.destination, p.priority),
                               tags=("odd" if i % 2 else "even",))

    def _do_del(self, pid):
        pid = pid.strip()
        if not pid:
            self.toast("Enter a Package ID.", "warn")
            return
        r = self.registry.remove_record(pid)
        b = self.buffer.remove_package(pid)
        if r is None and b is None:
            self.toast("Package not found: %s" % pid, "warn")
        else:
            self._fill_del_tv()
            self.toast("Deleted: %s  (SLL: %s, DLL: %s)" % (
                pid, "yes" if r else "no", "yes" if b else "no"), "danger")


def main():
    app = VeteranApp()
    app.mainloop()


if __name__ == "__main__":
    main()
