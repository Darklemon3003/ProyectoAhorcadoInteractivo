package com.ahorcado.util;

import java.awt.Color;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * GestorSkins - Catalogo de skins/temas de color del ahorcado (Singleton).
 * 
 * Maneja las skins que cambian los colores de la horca y el cuerpo.
 * El jugador puede comprarlas con KK y equipar la que quiera.
 * Persiste en skins.txt. La skin "default_cian" siempre esta desbloqueada.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorSkins {

    private static GestorSkins instancia;
    private final List<SkinAhorcado> catalogo = new ArrayList<>();
    private String skinActivoId = "default_cian";
    private static final String ARCHIVO = "skins.txt";

    public interface SkinListener {
        void onSkinCambiado(SkinAhorcado nueva);
    }
    private final List<WeakReference<SkinListener>> listeners = new ArrayList<>();

    private GestorSkins() {
        construirCatalogo();
        cargar();
    }

    public static GestorSkins getInstancia() {
        if (instancia == null) instancia = new GestorSkins();
        return instancia;
    }

    private void construirCatalogo() {
        catalogo.add(new SkinAhorcado("default_cian", "Cian Neon",
            "Esquema clasico cyberpunk", 0,
            new Color(140, 255, 255), new Color(140, 255, 255),
            new Color(0, 255, 255, 50), new Color(0xFF, 0x55, 0x55),
            new Color(0x0A, 0x0E, 0x14), new Color(0, 255, 255, 120),
            true));

        catalogo.add(new SkinAhorcado("verde_matrix", "Matrix Green",
            "Protocolo Matrix activado", 80,
            new Color(0x00, 0xFF, 0x41), new Color(0x00, 0xFF, 0x88),
            new Color(0, 255, 65, 50), new Color(0xFF, 0x44, 0x00),
            new Color(0x05, 0x0A, 0x05), new Color(0, 255, 100, 120),
            false));

        catalogo.add(new SkinAhorcado("rojo_sangre", "Blood Circuit",
            "Circuitos de sangre digital", 120,
            new Color(0xFF, 0x33, 0x33), new Color(0xFF, 0x66, 0x44),
            new Color(255, 30, 30, 50), new Color(0xFF, 0xFF, 0x00),
            new Color(0x14, 0x05, 0x05), new Color(255, 50, 30, 120),
            false));

        catalogo.add(new SkinAhorcado("ambar_retro", "Retro Amber",
            "Terminal retro de los 80s", 100,
            new Color(0xFF, 0xAA, 0x00), new Color(0xFF, 0xCC, 0x33),
            new Color(255, 170, 0, 50), new Color(0xFF, 0x33, 0x33),
            new Color(0x0F, 0x0A, 0x02), new Color(255, 180, 0, 120),
            false));

        catalogo.add(new SkinAhorcado("rosa_glitch", "Neon Pink Glitch",
            "Estetica vaporwave", 150,
            new Color(0xFF, 0x00, 0xFF), new Color(0xFF, 0x66, 0xFF),
            new Color(255, 0, 255, 50), new Color(0x00, 0xFF, 0xFF),
            new Color(0x10, 0x05, 0x10), new Color(255, 0, 255, 120),
            false));

        catalogo.add(new SkinAhorcado("oro_elite", "Gold Elite",
            "Solo para operadores de elite", 250,
            new Color(0xFF, 0xD7, 0x00), new Color(0xFF, 0xE5, 0x55),
            new Color(255, 215, 0, 50), new Color(0xFF, 0x22, 0x22),
            new Color(0x0A, 0x0A, 0x02), new Color(255, 215, 0, 120),
            false));

        catalogo.add(new SkinAhorcado("hielo_artico", "Arctic Ice",
            "Frio como el zero-day", 180,
            new Color(0x88, 0xCC, 0xFF), new Color(0xAA, 0xDD, 0xFF),
            new Color(100, 180, 255, 50), new Color(0xFF, 0x88, 0x00),
            new Color(0x05, 0x08, 0x14), new Color(100, 200, 255, 120),
            false));

        catalogo.add(new SkinAhorcado("phantom_white", "Phantom White",
            "Invisible como un rootkit", 200,
            new Color(0xDD, 0xDD, 0xDD), new Color(0xFF, 0xFF, 0xFF),
            new Color(200, 200, 200, 40), new Color(0xFF, 0x00, 0x00),
            new Color(0x08, 0x08, 0x08), new Color(220, 220, 220, 100),
            false));
    }

    public List<SkinAhorcado> getCatalogo() { return Collections.unmodifiableList(catalogo); }

    public SkinAhorcado getSkinActivo() {
        for (SkinAhorcado s : catalogo) {
            if (s.id.equals(skinActivoId)) return s;
        }
        return catalogo.get(0);
    }

    public void setSkinActivo(String id) {
        for (SkinAhorcado s : catalogo) {
            if (s.id.equals(id) && s.desbloqueado) {
                skinActivoId = id;
                guardar();
                notificarSkinCambiado(s);
                return;
            }
        }
    }

    public boolean comprar(String id) {
        GestorEconomia eco = GestorEconomia.getInstancia();
        for (SkinAhorcado s : catalogo) {
            if (s.id.equals(id) && !s.desbloqueado) {
                if (eco.gastar(s.costoKK)) {
                    s.desbloqueado = true;
                    guardar();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void addListener(SkinListener l) {
        if (l == null) return;
        limpiarListeners();
        for (WeakReference<SkinListener> ref : listeners) {
            if (ref.get() == l) return;
        }
        listeners.add(new WeakReference<>(l));
    }
    public void removeListener(SkinListener l) {
        Iterator<WeakReference<SkinListener>> it = listeners.iterator();
        while (it.hasNext()) {
            SkinListener actual = it.next().get();
            if (actual == null || actual == l) it.remove();
        }
    }

    private void notificarSkinCambiado(SkinAhorcado skin) {
        for (WeakReference<SkinListener> ref : new ArrayList<>(listeners)) {
            SkinListener listener = ref.get();
            if (listener == null) {
                listeners.remove(ref);
                continue;
            }
            listener.onSkinCambiado(skin);
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
                    skinActivoId = linea.substring(7).trim();
                } else if (!linea.isEmpty() && !linea.startsWith("#")) {
                    desbloqueados.add(linea);
                }
            }
            for (SkinAhorcado s : catalogo) {
                if (desbloqueados.contains(s.id)) s.desbloqueado = true;
            }
        } catch (IOException ignored) {}
    }

    private void guardar() {
        File f = new File(System.getProperty("user.dir"), ARCHIVO);
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("# Skins desbloqueados");
            pw.println("ACTIVO=" + skinActivoId);
            for (SkinAhorcado s : catalogo) {
                if (s.desbloqueado) pw.println(s.id);
            }
        } catch (IOException ignored) {}
    }
}
