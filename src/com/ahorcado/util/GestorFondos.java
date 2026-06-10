package com.ahorcado.util;

import java.awt.Color;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * GestorFondos - Catalogo de fondos/wallpapers del juego (Singleton).
 * 
 * Maneja los 15 fondos disponibles, cuales estan desbloqueados,
 * cual esta activo y la compra de nuevos fondos con KK.
 * Persiste en fondos.txt. El fondo "default_void" siempre viene gratis.
 * 
 * Tiene un listener (FondoListener) pa notificar a la interfaz
 * cuando el jugador cambia de fondo y se actualice en tiempo real.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorFondos {

    private static GestorFondos instancia;
    private final List<FondoJuego> catalogo = new ArrayList<>();
    private String fondoActivoId = "default_void";
    private static final String ARCHIVO = "fondos.txt";

    public interface FondoListener {
        void onFondoCambiado(FondoJuego nuevo);
    }
    private final List<WeakReference<FondoListener>> listeners = new ArrayList<>();

    private GestorFondos() {
        construirCatalogo();
        cargar();
    }

    public static synchronized GestorFondos getInstancia() {
        if (instancia == null) instancia = new GestorFondos();
        return instancia;
    }

    private void construirCatalogo() {
        // === COMUNES (gratuitos o baratos) ===
        catalogo.add(new FondoJuego("default_void", "Void Terminal",
            "El vacio digital por defecto. Minimalista y oscuro.",
            0, "grid",
            new Color(0x00, 0x44, 0x55), new Color(0x00, 0x88, 0x99), new Color(0x00, 0xFF, 0xFF),
            FondoJuego.Rareza.COMUN, true));

        catalogo.add(new FondoJuego("binary_rain", "Binary Downpour",
            "Lluvia de ceros y unos. El codigo fuente de la realidad.",
            60, "binary",
            new Color(0x00, 0xCC, 0x44), new Color(0x00, 0xFF, 0x88), new Color(0x88, 0xFF, 0xAA),
            FondoJuego.Rareza.COMUN, false));

        catalogo.add(new FondoJuego("dark_terminal", "Ghost Terminal",
            "Comandos fantasma ejecutandose en la sombra.",
            75, "terminal",
            new Color(0x00, 0xFF, 0x41), new Color(0x00, 0xCC, 0x33), new Color(0x88, 0xFF, 0x88),
            FondoJuego.Rareza.COMUN, false));

        // === RAROS ===
        catalogo.add(new FondoJuego("neon_circuit", "Neon Circuit",
            "Circuitos PCB con pulsos electricos viajando por trazas neon.",
            120, "circuit",
            new Color(0x00, 0xFF, 0xFF), new Color(0x00, 0xCC, 0xFF), new Color(0xFF, 0x00, 0xFF),
            FondoJuego.Rareza.RARO, false));

        catalogo.add(new FondoJuego("matrix_cascade", "Matrix Cascade",
            "La cascada verde original. Ve la verdad detras del codigo.",
            130, "matrix",
            new Color(0x00, 0xFF, 0x41), new Color(0x88, 0xFF, 0x88), new Color(0xFF, 0xFF, 0xFF),
            FondoJuego.Rareza.RARO, false));

        catalogo.add(new FondoJuego("digital_rain", "Acid Rain",
            "Lluvia acida digital. Cada gota corroe los firewalls.",
            110, "rain",
            new Color(0x88, 0x00, 0xFF), new Color(0xAA, 0x44, 0xFF), new Color(0xFF, 0x00, 0xFF),
            FondoJuego.Rareza.RARO, false));

        catalogo.add(new FondoJuego("hex_grid", "Honeycomb Matrix",
            "Red hexagonal con pulsos sincronizados de datos.",
            140, "hex",
            new Color(0xFF, 0xAA, 0x00), new Color(0xFF, 0xCC, 0x33), new Color(0xFF, 0xFF, 0x00),
            FondoJuego.Rareza.RARO, false));

        catalogo.add(new FondoJuego("wave_rider", "Wave Rider",
            "Ondas sinusoidales de frecuencias interceptadas.",
            100, "waves",
            new Color(0x00, 0xBB, 0xFF), new Color(0x00, 0xFF, 0xCC), new Color(0x00, 0xFF, 0xFF),
            FondoJuego.Rareza.RARO, false));

        // === EPICOS ===
        catalogo.add(new FondoJuego("dedsec_ops", "DedSec Operations",
            "Centro de operaciones DedSec. Hackea el sistema desde dentro.",
            220, "dedsec",
            new Color(0xFF, 0x00, 0x88), new Color(0xFF, 0x55, 0xAA), new Color(0xFF, 0xFF, 0xFF),
            FondoJuego.Rareza.EPICO, false));

        catalogo.add(new FondoJuego("particle_net", "Neural Network",
            "Red neuronal con nodos interconectados y sinapsis brillantes.",
            200, "particles",
            new Color(0x00, 0xFF, 0xFF), new Color(0xFF, 0x00, 0xFF), new Color(0x00, 0xFF, 0x88),
            FondoJuego.Rareza.EPICO, false));

        catalogo.add(new FondoJuego("inferno", "Inferno Core",
            "El nucleo ardiente del servidor. Solo los valientes entran.",
            250, "fire",
            new Color(0xFF, 0x44, 0x00), new Color(0xFF, 0xAA, 0x00), new Color(0xFF, 0xFF, 0x00),
            FondoJuego.Rareza.EPICO, false));

        catalogo.add(new FondoJuego("glitch_storm", "Glitch Storm",
            "Tormenta de interferencia. La realidad se fragmenta.",
            230, "glitch",
            new Color(0xFF, 0x00, 0xFF), new Color(0x00, 0xFF, 0xFF), new Color(0xFF, 0xFF, 0x00),
            FondoJuego.Rareza.EPICO, false));

        // === LEGENDARIOS ===
        catalogo.add(new FondoJuego("vaporwave_sunset", "Vaporwave Sunset",
            "Atardecer retrowave. Nostalgia del futuro que nunca fue.",
            350, "vaporwave",
            new Color(0xFF, 0x00, 0xFF), new Color(0x00, 0xFF, 0xFF), new Color(0xFF, 0x88, 0x00),
            FondoJuego.Rareza.LEGENDARIO, false));

        catalogo.add(new FondoJuego("cosmic_void", "Cosmic Void",
            "El vacio cosmico. Nebulosas y estrellas en el abismo infinito.",
            400, "galaxy",
            new Color(0x55, 0x00, 0xFF), new Color(0xFF, 0x00, 0x88), new Color(0xFF, 0xFF, 0xFF),
            FondoJuego.Rareza.LEGENDARIO, false));

        catalogo.add(new FondoJuego("skull_domain", "Skull Domain",
            "El dominio de los craneos digitales. Solo para leyendas.",
            450, "skull",
            new Color(0xFF, 0x00, 0x44), new Color(0xFF, 0xFF, 0xFF), new Color(0xFF, 0xD7, 0x00),
            FondoJuego.Rareza.LEGENDARIO, false));
    }

    public List<FondoJuego> getCatalogo() {
        return Collections.unmodifiableList(catalogo);
    }

    public FondoJuego getFondoActivo() {
        for (FondoJuego f : catalogo) {
            if (f.id.equals(fondoActivoId)) return f;
        }
        return catalogo.get(0);
    }

    public void setFondoActivo(String id) {
        for (FondoJuego f : catalogo) {
            if (f.id.equals(id) && f.desbloqueado) {
                fondoActivoId = id;
                guardar();
                notificarFondoCambiado(f);
                return;
            }
        }
    }

    public boolean comprar(String id) {
        GestorEconomia eco = GestorEconomia.getInstancia();
        for (FondoJuego f : catalogo) {
            if (f.id.equals(id) && !f.desbloqueado) {
                if (eco.gastar(f.costoKK)) {
                    f.desbloqueado = true;
                    guardar();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void addListener(FondoListener l) {
        if (l == null) return;
        limpiarListeners();
        for (WeakReference<FondoListener> ref : listeners) {
            if (ref.get() == l) return;
        }
        listeners.add(new WeakReference<>(l));
    }

    public void removeListener(FondoListener l) {
        Iterator<WeakReference<FondoListener>> it = listeners.iterator();
        while (it.hasNext()) {
            FondoListener actual = it.next().get();
            if (actual == null || actual == l) it.remove();
        }
    }

    private void notificarFondoCambiado(FondoJuego fondo) {
        for (WeakReference<FondoListener> ref : new ArrayList<>(listeners)) {
            FondoListener listener = ref.get();
            if (listener == null) {
                listeners.remove(ref);
                continue;
            }
            listener.onFondoCambiado(fondo);
        }
    }

    private void limpiarListeners() {
        listeners.removeIf(ref -> ref.get() == null);
    }

    private void cargar() {
        File f = new File(System.getProperty("user.dir"), ARCHIVO);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            Set<String> desbloqueados = new HashSet<>();
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("ACTIVO=")) {
                    fondoActivoId = linea.substring(7).trim();
                } else if (!linea.isEmpty() && !linea.startsWith("#")) {
                    desbloqueados.add(linea);
                }
            }
            for (FondoJuego fj : catalogo) {
                if (desbloqueados.contains(fj.id)) fj.desbloqueado = true;
            }
        } catch (IOException ignored) {}
    }

    private void guardar() {
        File f = new File(System.getProperty("user.dir"), ARCHIVO);
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("# Fondos desbloqueados");
            pw.println("ACTIVO=" + fondoActivoId);
            for (FondoJuego fj : catalogo) {
                if (fj.desbloqueado) pw.println(fj.id);
            }
        } catch (IOException ignored) {}
    }
}
