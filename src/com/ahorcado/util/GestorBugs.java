package com.ahorcado.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * GestorBugs - Catalogo de bugs coleccionables del juego (Singleton).
 * 
 * Maneja el catalogo completo de 12 bugs, cuales estan desbloqueados,
 * la compra de nuevos bugs con KK, y la seleccion aleatoria pa los popups.
 * Persiste los desbloqueos en bugs.txt.
 * 
 * El bug "default_glitch" siempre viene desbloqueado de fabrica.
 * Usa un LinkedHashMap pa mantener el orden de insercion del catalogo.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorBugs {

    private static final String ARCHIVO = "bugs.txt";
    private static final String RUTA = System.getProperty("user.dir") + File.separator + ARCHIVO;

    private static GestorBugs instancia;

    private final Map<String, Bug> catalogo = new LinkedHashMap<>();
    private final Random rnd = new Random();
    private final java.util.Set<String> bugsActivos = new java.util.HashSet<>(); // Bugs activos (puede haber múltiples)

    public interface InventarioListener {
        void onBugDesbloqueado(Bug bug);
    }
    private final List<WeakReference<InventarioListener>> listeners = new ArrayList<>();

    private GestorBugs() {
        construirCatalogo();
        bugsActivos.add("default_glitch"); // Default siempre activo
        cargarDesbloqueos();
    }
    
    /**
     * Obtiene el conjunto de bugs activos.
     */
    public java.util.Set<String> getBugsActivos() {
        return new java.util.HashSet<>(bugsActivos);
    }
    
    /**
     * Verifica si un bug específico está activo.
     */
    public boolean isActive(String id) {
        return bugsActivos.contains(id);
    }
    
    /**
     * Alterna el estado activo de un bug (toggle).
     * Si está activo, lo desactiva. Si no está activo, lo activa.
     */
    public void toggleBugActivo(String id) {
        Bug bug = catalogo.get(id);
        if (bug != null && bug.isDesbloqueado()) {
            if (bugsActivos.contains(id)) {
                bugsActivos.remove(id);
            } else {
                bugsActivos.add(id);
            }
            guardarDesbloqueos(); // Persiste el cambio
        }
    }

    public static synchronized GestorBugs getInstancia() {
        if (instancia == null) instancia = new GestorBugs();
        return instancia;
    }

    public void addListener(InventarioListener l) {
        if (l == null) return;
        limpiarListeners();
        for (WeakReference<InventarioListener> ref : listeners) {
            if (ref.get() == l) return;
        }
        listeners.add(new WeakReference<>(l));
    }
    public void removeListener(InventarioListener l) {
        Iterator<WeakReference<InventarioListener>> it = listeners.iterator();
        while (it.hasNext()) {
            InventarioListener actual = it.next().get();
            if (actual == null || actual == l) it.remove();
        }
    }

    private void construirCatalogo() {
        // id, nombre, descripcion, costo, colorPrincipal, colorAccento, rareza, accesorio, etiqueta, desbloqueadoPorDefecto
        registrar(new Bug("default_glitch", "Glitch Phantom",
                "El bug base del sistema. Siempre acecha la pantalla.",
                0, new Color(0x00FFFF), new Color(0xFF00FF),
                Bug.Rareza.COMUN, "static", "GLITCH_FOUND", true));

        registrar(new Bug("spider_brain", "Cerebro Arana",
                "Inteligencia parasita con ocho patas que reescribe procesos en caliente.",
                150, new Color(0x88FF00), new Color(0xFF00AA),
                Bug.Rareza.RARO, "brain_spider", "0xBRAIN_HIJACK", false));

        registrar(new Bug("grim_reaper", "Grim Reaper",
                "Demonio del kernel. Termina procesos sin previo aviso.",
                250, new Color(0xCCCCCC), new Color(0x00FF66),
                Bug.Rareza.EPICO, "reaper", "EXIT(0xDEAD)", false));

        registrar(new Bug("ded_hacker", "DedSec_Ghost",
                "Activista digital encapuchado. Hackea por la causa.",
                200, new Color(0x00FF88), new Color(0xFF8800),
                Bug.Rareza.RARO, "hoodie", "WE_ARE_DEDSEC", false));

        registrar(new Bug("top_hat", "Mr. Glitch",
                "Caballero corrupto del protocolo TCP. Sombrero glitcheado.",
                180, new Color(0x55FFAA), new Color(0xFF55AA),
                Bug.Rareza.RARO, "tophat", "FORMAL_BREACH", false));

        registrar(new Bug("vr_demon", "VR Demon",
                "Habita las sesiones VR. Inyecta payloads en stereo.",
                300, new Color(0xFF00AA), new Color(0x00FFFF),
                Bug.Rareza.EPICO, "vr", "VR_OVERRIDE", false));

        registrar(new Bug("dynamite", "DynaBug",
                "Cartucho de dinamita logica. Explota la pila de llamadas.",
                220, new Color(0xFF4444), new Color(0xFFD700),
                Bug.Rareza.RARO, "dynamite", "BOOM!_OVERFLOW", false));

        registrar(new Bug("octopus", "Octo Spy",
                "Tentaculos en cada puerto. Sniffer multicanal.",
                280, new Color(0xAA00FF), new Color(0x00FFFF),
                Bug.Rareza.EPICO, "octopus", "PORT_SCAN_8x", false));

        registrar(new Bug("devil_core", "Demon Core",
                "Reactor demoniaco. Sobrecalienta la CPU del enemigo.",
                350, new Color(0xFF2222), new Color(0xFFAA00),
                Bug.Rareza.EPICO, "devil", "CRIT_TEMP_666", false));

        registrar(new Bug("alien_orb", "Xeno Bug",
                "Codigo extraterrestre indescifrable. Solo el observa.",
                400, new Color(0x00FF44), new Color(0xFF00FF),
                Bug.Rareza.LEGENDARIO, "alien", "X3N0_PROBE", false));

        registrar(new Bug("crown_skull", "Bone Cruncher",
                "Rey craneo de las rutinas zombi. Devora memoria.",
                230, new Color(0xFFFFFF), new Color(0xFF0066),
                Bug.Rareza.RARO, "skullking", "ROYAL_ROOT", false));

        registrar(new Bug("zero_day", "Zero Day",
                "El santo grial. Exploit que aun no existe en CVE.",
                500, new Color(0xFFD700), new Color(0xFF00FF),
                Bug.Rareza.LEGENDARIO, "exploit", "0DAY_LIVE", false));
    }

    private void registrar(Bug b) { catalogo.put(b.id, b); }

    public List<Bug> getCatalogo() {
        return new ArrayList<>(catalogo.values());
    }

    public Bug get(String id) { return catalogo.get(id); }

    public List<Bug> getDesbloqueados() {
        List<Bug> r = new ArrayList<>();
        for (Bug b : catalogo.values()) if (b.isDesbloqueado()) r.add(b);
        return r;
    }

    /**
     * Selecciona aleatoriamente un bug entre los desbloqueados.
     * Devuelve null si no hay ninguno (no deberia ocurrir).
     */
    public Bug aleatorioDesbloqueado() {
        List<Bug> ds = getDesbloqueados();
        if (ds.isEmpty()) return catalogo.get("default_glitch");
        return ds.get(rnd.nextInt(ds.size()));
    }
    
    /**
     * Selecciona aleatoriamente un bug entre los activos.
     * Devuelve default_glitch si no hay ninguno activo.
     */
    public Bug aleatorioActivo() {
        List<Bug> activos = new ArrayList<>();
        for (String id : bugsActivos) {
            Bug b = catalogo.get(id);
            if (b != null) {
                activos.add(b);
            }
        }
        if (activos.isEmpty()) return catalogo.get("default_glitch");
        return activos.get(rnd.nextInt(activos.size()));
    }

    /**
     * Intenta comprar y desbloquear un bug.
     * @return ResultadoCompra con info para la UI.
     */
    public ResultadoCompra comprar(String id) {
        Bug b = catalogo.get(id);
        if (b == null) return new ResultadoCompra(false, "BUG_NOT_FOUND");
        if (b.isDesbloqueado()) return new ResultadoCompra(false, "YA_ADQUIRIDO");
        GestorEconomia eco = GestorEconomia.getInstancia();
        if (!eco.puedePermitirse(b.costoKK))
            return new ResultadoCompra(false, "FONDOS_INSUFICIENTES");
        if (!eco.gastar(b.costoKK))
            return new ResultadoCompra(false, "FONDOS_INSUFICIENTES");
        b.setDesbloqueado(true);
        guardarDesbloqueos();
        for (WeakReference<InventarioListener> ref : new ArrayList<>(listeners)) {
            InventarioListener l = ref.get();
            if (l == null) {
                listeners.remove(ref);
                continue;
            }
            try { l.onBugDesbloqueado(b); } catch (Exception ignored) {}
        }
        return new ResultadoCompra(true, "OK");
    }

    private void limpiarListeners() {
        listeners.removeIf(ref -> ref.get() == null);
    }

    /** Reinicia inventario: solo default_glitch desbloqueado. */
    public void reiniciar() {
        for (Bug b : catalogo.values()) b.setDesbloqueado("default_glitch".equals(b.id));
        guardarDesbloqueos();
    }

    private void cargarDesbloqueos() {
        File f = new File(RUTA);
        if (!f.exists()) { guardarDesbloqueos(); return; }
        Set<String> ids = new LinkedHashSet<>();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                String t = linea.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                // Leer bugs activos
                if (t.startsWith("ACTIVOS:")) {
                    String activosStr = t.substring(8).trim();
                    for (String id : activosStr.split(",")) {
                        id = id.trim();
                        if (catalogo.containsKey(id)) {
                            bugsActivos.add(id);
                        }
                    }
                    continue;
                }
                Collections.addAll(ids, t.split(","));
            }
        } catch (IOException e) {
            System.err.println("[BUGS] Error al cargar " + RUTA + ": " + e.getMessage());
        }
        ids.add("default_glitch");
        for (String raw : ids) {
            String id = raw.trim();
            Bug b = catalogo.get(id);
            if (b != null) b.setDesbloqueado(true);
        }
    }

    private void guardarDesbloqueos() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(RUTA))) {
            w.write("# Bugs desbloqueados y bugs activos");
            w.newLine();
            String activosStr = String.join(",", bugsActivos);
            w.write("ACTIVOS:" + activosStr);
            w.newLine();
            for (Bug b : catalogo.values()) {
                if (b.isDesbloqueado()) {
                    w.write(b.id);
                    w.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("[BUGS] Error al guardar " + RUTA + ": " + e.getMessage());
        }
    }

    public static class ResultadoCompra {
        public final boolean exito;
        public final String codigo;
        public ResultadoCompra(boolean e, String c) { this.exito = e; this.codigo = c; }
    }
}
