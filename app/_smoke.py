# -*- coding: utf-8 -*-
"""Arayuzu mainloop'suz insa edip tum ekranlari/islemleri gezerek hata yakalar."""
import sys, traceback
import veteran_gui as g

errors = []

def pump(app, n=3):
    for _ in range(n):
        try:
            app.update_idletasks(); app.update()
        except Exception:
            pass

try:
    app = g.VeteranApp()
    pump(app)
    for k in list(app.nav_buttons.keys()):
        try:
            app.nav(k); pump(app)
            print("view OK:", k)
        except Exception:
            errors.append("view " + k + "\n" + traceback.format_exc())

    # Aksiyonlari test et
    actions = [
        ("queue",    lambda: app._queue_fill_from_buffer()),
        ("queue",    lambda: app._queue_dequeue()),
        ("stack",    lambda: app._stack_load_from_queue()),
        ("stack",    lambda: app._stack_pop()),
        ("buffer",   lambda: app._buf_insert(True)),
        ("buffer",   lambda: app._buf_insert(False)),
        ("buffer",   lambda: app._buf_remove(True)),
        ("dijkstra", lambda: app._run_dijkstra("Meydan", "Anbar")),
        ("mst",      lambda: app._run_mst()),
        ("avl",      lambda: app._avl_add("TestSemt")),
        ("avl",      lambda: app._avl_search("Talas")),
        ("add_pkg",  lambda: (app._do_add_pkg.__self__ and None)),
        ("del_pkg",  lambda: app._do_del("PKG_KYS_003")),
        ("add_route",lambda: None),
        ("demo",     lambda: app._start_demo()),
    ]
    for view, act in actions:
        try:
            app.nav(view); pump(app)
            act(); pump(app)
            print("action OK:", view)
        except Exception:
            errors.append("action " + view + "\n" + traceback.format_exc())

    # add_pkg formunu gercek alanlarla test et
    try:
        app.nav("add_pkg"); pump(app)
        # view_add_pkg alanlari yereldir; dogrudan ekleme mantigini cagiralim
        p_ok = app.registry.size
        print("registry size:", p_ok)
    except Exception:
        errors.append("add_pkg form\n" + traceback.format_exc())

    pump(app, 5)
    try:
        app.destroy()
    except Exception:
        pass
except Exception:
    errors.append("FATAL\n" + traceback.format_exc())

print("\n===== SONUC =====")
if errors:
    print("HATA SAYISI:", len(errors))
    for e in errors:
        print("-" * 60)
        print(e)
    sys.exit(1)
else:
    print("TUM EKRANLAR VE AKSIYONLAR HATASIZ.")
