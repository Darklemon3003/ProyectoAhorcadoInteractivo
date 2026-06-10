package com.ahorcado.util;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GestorMisiones - Sistema de misiones diarias y desafios permanentes.
 * 
 * Singleton que maneja dos tipos de objetivos:
 * - Misiones diarias: se resetean cada dia nuevo (6 misiones)
 * - Desafios permanentes: retos a largo plazo en 4 tiers (BRONCE/PLATA/ORO/PLATINO)
 * 
 * Las misiones dan XP al completarse. Persiste todo en misiones.txt
 * pa que el progreso se guarde entre sesiones.
 * 
 * Usa ArrayLists pa guardar las listas de misiones y desafios,
 * y un listener pa notificar a la interfaz cuando se actualiza algo.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorMisiones {

    private static GestorMisiones instancia;
    private static final String ARCHIVO = System.getProperty("user.dir") + File.separator + "misiones.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // === MISIONES DIARIAS ===
    private LocalDate fechaUltimoReset;
    private List<MisionDiaria> misionesDiarias;

    // === DESAFIOS PERMANENTES ===
    private List<Desafio> desafios;

    // Listener para notificar cambios
    public interface MisionesListener {
        void onMisionActualizada();
    }
    private List<MisionesListener> listeners = new ArrayList<>();

    // === MODELO DE MISION DIARIA ===
    public static class MisionDiaria {
        public final String id;
        public final String nombre;
        public final String descripcion;
        public final int meta;
        public final int xpRecompensa;
        public int progreso;
        public boolean completada;
        public boolean recompensaReclamada;

        public MisionDiaria(String id, String nombre, String descripcion, int meta, int xpRecompensa) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.meta = meta;
            this.xpRecompensa = xpRecompensa;
            this.progreso = 0;
            this.completada = false;
            this.recompensaReclamada = false;
        }

        public float getProgresoPorcentaje() {
            return Math.min(1.0f, (float) progreso / meta);
        }
    }

    // === MODELO DE DESAFIO PERMANENTE ===
    public static class Desafio {
        public final String id;
        public final String nombre;
        public final String descripcion;
        public final int meta;
        public final int xpRecompensa;
        public final String dificultad; // BRONCE, PLATA, ORO, PLATINO
        public int progreso;
        public boolean completado;
        public boolean recompensaReclamada;

        public Desafio(String id, String nombre, String descripcion, int meta, int xpRecompensa, String dificultad) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.meta = meta;
            this.xpRecompensa = xpRecompensa;
            this.dificultad = dificultad;
            this.progreso = 0;
            this.completado = false;
            this.recompensaReclamada = false;
        }

        public float getProgresoPorcentaje() {
            return Math.min(1.0f, (float) progreso / meta);
        }
    }

    private GestorMisiones() {
        misionesDiarias = new ArrayList<>();
        desafios = new ArrayList<>();
        cargar();
        verificarResetDiario();
    }

    public static synchronized GestorMisiones getInstancia() {
        if (instancia == null) {
            instancia = new GestorMisiones();
        }
        return instancia;
    }

    public void addListener(MisionesListener l) {
        if (l != null && !listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(MisionesListener l) {
        listeners.remove(l);
    }

    private void notificarCambio() {
        for (MisionesListener l : listeners) l.onMisionActualizada();
    }

    // ===================== MISIONES DIARIAS =====================

    private void generarMisionesDiarias() {
        misionesDiarias.clear();
        misionesDiarias.add(new MisionDiaria("d_ganar3", "Victorias del dia", "Gana 3 partidas hoy", 3, 80));
        misionesDiarias.add(new MisionDiaria("d_racha2", "Racha imparable", "Consigue una racha de 2 victorias seguidas", 2, 60));
        misionesDiarias.add(new MisionDiaria("d_jugar5", "Operador activo", "Juega 5 partidas hoy", 5, 50));
        misionesDiarias.add(new MisionDiaria("d_perfecta", "Sin fallos", "Gana una partida sin ningun error", 1, 120));
        misionesDiarias.add(new MisionDiaria("d_rapida", "Descifrado veloz", "Gana una partida en menos de 60 segundos", 1, 100));
        misionesDiarias.add(new MisionDiaria("d_dificil", "Alta encriptacion", "Gana una partida en dificultad DIFICIL o PESADILLA", 1, 90));
        fechaUltimoReset = LocalDate.now();
    }

    private void verificarResetDiario() {
        LocalDate hoy = LocalDate.now();
        if (fechaUltimoReset == null || !fechaUltimoReset.equals(hoy)) {
            generarMisionesDiarias();
            guardar();
        }
    }

    // ===================== DESAFIOS PERMANENTES =====================

    private void generarDesafios() {
        desafios.clear();
        // BRONCE - Accesibles pero requieren esfuerzo
        desafios.add(new Desafio("c_wins10", "Hacker Novato", "Acumula 10 victorias totales", 10, 150, "BRONCE"));
        desafios.add(new Desafio("c_racha3", "Triple Amenaza", "Consigue una racha de 3 victorias seguidas", 3, 120, "BRONCE"));
        desafios.add(new Desafio("c_jugar20", "Veterano de campo", "Juega 20 partidas en total", 20, 100, "BRONCE"));
        desafios.add(new Desafio("c_categorias3", "Multidisciplinar", "Gana partidas en 3 categorias distintas", 3, 130, "BRONCE"));

        // PLATA - Requieren dedicacion
        desafios.add(new Desafio("c_wins25", "Operador Experto", "Acumula 25 victorias totales", 25, 250, "PLATA"));
        desafios.add(new Desafio("c_racha5", "Imparable", "Consigue una racha de 5 victorias seguidas", 5, 300, "PLATA"));
        desafios.add(new Desafio("c_perfectas5", "Precision Quirurgica", "Gana 5 partidas sin ningun error", 5, 280, "PLATA"));
        desafios.add(new Desafio("c_rapidas5", "Rayo de datos", "Gana 5 partidas en menos de 60 segundos", 5, 260, "PLATA"));
        desafios.add(new Desafio("c_dificil5", "Criptanalista", "Gana 5 partidas en DIFICIL o superior", 5, 300, "PLATA"));

        // ORO - Muy retadores
        desafios.add(new Desafio("c_wins50", "Maestro Decoder", "Acumula 50 victorias totales", 50, 500, "ORO"));
        desafios.add(new Desafio("c_racha10", "Leyenda viva", "Consigue una racha de 10 victorias seguidas", 10, 600, "ORO"));
        desafios.add(new Desafio("c_pesadilla5", "Pesadilla superada", "Gana 5 partidas en PESADILLA", 5, 550, "ORO"));
        desafios.add(new Desafio("c_perfectas10", "Inmaculado", "Gana 10 partidas sin ningun error", 10, 500, "ORO"));
        desafios.add(new Desafio("c_nivel10", "Elite Digital", "Alcanza el nivel 10", 10, 400, "ORO"));

        // PLATINO - Para los mas dedicados
        desafios.add(new Desafio("c_wins100", "Centuria Cyber", "Acumula 100 victorias totales", 100, 1000, "PLATINO"));
        desafios.add(new Desafio("c_racha15", "Dios del codigo", "Consigue una racha de 15 victorias seguidas", 15, 1200, "PLATINO"));
        desafios.add(new Desafio("c_nivel15", "Arquitecto Nexus", "Alcanza el nivel 15", 15, 800, "PLATINO"));
        desafios.add(new Desafio("c_jugar100", "Centenar operativo", "Juega 100 partidas en total", 100, 700, "PLATINO"));
    }

    // ===================== PROGRESO =====================

    /**
     * Llamar despues de cada partida para actualizar progreso de misiones y desafios.
     */
    public void registrarPartida(boolean victoria, boolean perfecta, boolean rapida, 
                                  String dificultad, int rachaActual, int victoriasTotal, 
                                  int partidasTotal, int nivel, String categoria) {
        verificarResetDiario();

        // === MISIONES DIARIAS ===
        for (MisionDiaria m : misionesDiarias) {
            if (m.completada) continue;
            switch (m.id) {
                case "d_ganar3":
                    if (victoria) m.progreso++;
                    break;
                case "d_racha2":
                    m.progreso = Math.max(m.progreso, Math.min(rachaActual, m.meta));
                    break;
                case "d_jugar5":
                    m.progreso++;
                    break;
                case "d_perfecta":
                    if (victoria && perfecta) m.progreso++;
                    break;
                case "d_rapida":
                    if (victoria && rapida) m.progreso++;
                    break;
                case "d_dificil":
                    if (victoria && ("DIFICIL".equalsIgnoreCase(dificultad) || "PESADILLA".equalsIgnoreCase(dificultad)))
                        m.progreso++;
                    break;
            }
            if (m.progreso >= m.meta) m.completada = true;
        }

        // === DESAFIOS PERMANENTES ===
        for (Desafio d : desafios) {
            if (d.completado) continue;
            switch (d.id) {
                case "c_wins10": case "c_wins25": case "c_wins50": case "c_wins100":
                    d.progreso = victoriasTotal;
                    break;
                case "c_racha3": case "c_racha5": case "c_racha10": case "c_racha15":
                    d.progreso = Math.max(d.progreso, rachaActual);
                    break;
                case "c_jugar20": case "c_jugar100":
                    d.progreso = partidasTotal;
                    break;
                case "c_perfectas5": case "c_perfectas10":
                    if (victoria && perfecta) d.progreso++;
                    break;
                case "c_rapidas5":
                    if (victoria && rapida) d.progreso++;
                    break;
                case "c_dificil5":
                    if (victoria && ("DIFICIL".equalsIgnoreCase(dificultad) || "PESADILLA".equalsIgnoreCase(dificultad)))
                        d.progreso++;
                    break;
                case "c_pesadilla5":
                    if (victoria && "PESADILLA".equalsIgnoreCase(dificultad))
                        d.progreso++;
                    break;
                case "c_nivel10": case "c_nivel15":
                    d.progreso = nivel;
                    break;
                case "c_categorias3":
                    if (victoria) d.progreso = Math.min(d.progreso + 1, d.meta); // Simplified tracking
                    break;
            }
            if (d.progreso >= d.meta) d.completado = true;
        }

        guardar();
        notificarCambio();
    }

    /**
     * Reclamar recompensa XP de una mision diaria completada.
     * @return XP otorgada, 0 si ya fue reclamada o no completada
     */
    public int reclamarRecompensaDiaria(String misionId) {
        for (MisionDiaria m : misionesDiarias) {
            if (m.id.equals(misionId) && m.completada && !m.recompensaReclamada) {
                m.recompensaReclamada = true;
                guardar();
                notificarCambio();
                return m.xpRecompensa;
            }
        }
        return 0;
    }

    /**
     * Reclamar recompensa XP de un desafio permanente completado.
     * @return XP otorgada, 0 si ya fue reclamada o no completada
     */
    public int reclamarRecompensaDesafio(String desafioId) {
        for (Desafio d : desafios) {
            if (d.id.equals(desafioId) && d.completado && !d.recompensaReclamada) {
                d.recompensaReclamada = true;
                guardar();
                notificarCambio();
                return d.xpRecompensa;
            }
        }
        return 0;
    }

    // ===================== GETTERS =====================

    public List<MisionDiaria> getMisionesDiarias() {
        verificarResetDiario();
        return misionesDiarias;
    }

    public List<Desafio> getDesafios() {
        return desafios;
    }

    public int getMisionesDiariasCompletadas() {
        int c = 0;
        for (MisionDiaria m : misionesDiarias) if (m.completada) c++;
        return c;
    }

    public int getDesafiosCompletados() {
        int c = 0;
        for (Desafio d : desafios) if (d.completado) c++;
        return c;
    }

    public int getTotalDesafios() {
        return desafios.size();
    }

    public int getXpPendienteDiarias() {
        int xp = 0;
        for (MisionDiaria m : misionesDiarias) {
            if (m.completada && !m.recompensaReclamada) xp += m.xpRecompensa;
        }
        return xp;
    }

    public int getXpPendienteDesafios() {
        int xp = 0;
        for (Desafio d : desafios) {
            if (d.completado && !d.recompensaReclamada) xp += d.xpRecompensa;
        }
        return xp;
    }

    // ===================== PERSISTENCIA =====================

    private void guardar() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(ARCHIVO))) {
            w.write("FECHA_RESET=" + (fechaUltimoReset != null ? fechaUltimoReset.format(DATE_FMT) : ""));
            w.newLine();

            // Misiones diarias
            for (MisionDiaria m : misionesDiarias) {
                w.write("MD|" + m.id + "|" + m.progreso + "|" + m.completada + "|" + m.recompensaReclamada);
                w.newLine();
            }

            // Desafios
            for (Desafio d : desafios) {
                w.write("DS|" + d.id + "|" + d.progreso + "|" + d.completado + "|" + d.recompensaReclamada);
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("[GestorMisiones] Error guardando: " + e.getMessage());
        }
    }

    private void cargar() {
        File f = new File(ARCHIVO);
        if (!f.exists()) {
            generarMisionesDiarias();
            generarDesafios();
            guardar();
            return;
        }

        // Generar primero las estructuras para tener los datos base
        generarMisionesDiarias();
        generarDesafios();

        Map<String, int[]> datosDiarias = new LinkedHashMap<>();
        Map<String, int[]> datosDesafios = new LinkedHashMap<>();

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                if (linea.startsWith("FECHA_RESET=")) {
                    String fecha = linea.substring("FECHA_RESET=".length()).trim();
                    if (!fecha.isEmpty()) {
                        try {
                            fechaUltimoReset = LocalDate.parse(fecha, DATE_FMT);
                        } catch (Exception e) {
                            fechaUltimoReset = null;
                        }
                    }
                } else if (linea.startsWith("MD|")) {
                    String[] p = linea.split("\\|");
                    if (p.length >= 5) {
                        datosDiarias.put(p[1], new int[]{
                            Integer.parseInt(p[2]),
                            Boolean.parseBoolean(p[3]) ? 1 : 0,
                            Boolean.parseBoolean(p[4]) ? 1 : 0
                        });
                    }
                } else if (linea.startsWith("DS|")) {
                    String[] p = linea.split("\\|");
                    if (p.length >= 5) {
                        datosDesafios.put(p[1], new int[]{
                            Integer.parseInt(p[2]),
                            Boolean.parseBoolean(p[3]) ? 1 : 0,
                            Boolean.parseBoolean(p[4]) ? 1 : 0
                        });
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[GestorMisiones] Error cargando: " + e.getMessage());
        }

        // Aplicar datos cargados solo si la fecha coincide (diarias no resetear)
        LocalDate hoy = LocalDate.now();
        if (fechaUltimoReset != null && fechaUltimoReset.equals(hoy)) {
            for (MisionDiaria m : misionesDiarias) {
                int[] datos = datosDiarias.get(m.id);
                if (datos != null) {
                    m.progreso = datos[0];
                    m.completada = datos[1] == 1;
                    m.recompensaReclamada = datos[2] == 1;
                }
            }
        } else {
            // Nuevo dia, resetear diarias
            generarMisionesDiarias();
        }

        // Desafios siempre se conservan
        for (Desafio d : desafios) {
            int[] datos = datosDesafios.get(d.id);
            if (datos != null) {
                d.progreso = datos[0];
                d.completado = datos[1] == 1;
                d.recompensaReclamada = datos[2] == 1;
            }
        }
    }
}
