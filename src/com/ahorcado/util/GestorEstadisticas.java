package com.ahorcado.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * GestorEstadisticas - Guarda y carga las estadisticas del jugador.
 * 
 * Usa un patron Singleton (una sola instancia) pa que todas las clases
 * puedan acceder a las mismas estadisticas. Guarda todo en un archivo
 * stats.txt pa que se mantenga entre sesiones (persistencia de datos).
 * 
 * Estadisticas que guarda: juegos jugados/ganados, racha, XP, nivel,
 * precision de letras, logros desbloqueados, etc.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorEstadisticas {
    
    private static final String ARCHIVO_STATS = "stats.txt";
    private static final String RUTA_STATS = System.getProperty("user.dir") + java.io.File.separator + ARCHIVO_STATS;

    private static GestorEstadisticas instancia;

    public static GestorEstadisticas getInstancia() {
        if (instancia == null) {
            instancia = new GestorEstadisticas();
        }
        return instancia;
    }

    // EstadÃ­sticas
    private int juegosJugados;
    private int juegosGanados;
    private int rachaActual;
    private int rachaMaxima;
    private int experiencia;
    private int letrasCorrectas;
    private int letrasIntentadas;
    private int tiempoTotalSegundos;
    private String alias = "GUEST";
    private Set<String> logros = new LinkedHashSet<>();
    private String ultimoResumen = "";
    
    public GestorEstadisticas() {
        cargarEstadisticas();
    }
    
    /**
     * Carga las estadÃ­sticas desde el archivo.
     */
    private void cargarEstadisticas() {
        File archivo = new File(RUTA_STATS);
        if (!archivo.exists()) {
            // Inicializar valores por defecto
            juegosJugados = 0;
            juegosGanados = 0;
            rachaActual = 0;
            rachaMaxima = 0;
            guardarEstadisticas();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split("=");
                if (partes.length == 2) {
                    String clave = partes[0].trim();
                    switch (clave) {
                        case "JUEGOS_JUGADOS":
                            juegosJugados = parseEntero(partes[1]);
                            break;
                        case "JUEGOS_GANADOS":
                            juegosGanados = parseEntero(partes[1]);
                            break;
                        case "RACHA_ACTUAL":
                            rachaActual = parseEntero(partes[1]);
                            break;
                        case "RACHA_MAXIMA":
                            rachaMaxima = parseEntero(partes[1]);
                            break;
                        case "EXPERIENCIA":
                            experiencia = parseEntero(partes[1]);
                            break;
                        case "LETRAS_CORRECTAS":
                            letrasCorrectas = parseEntero(partes[1]);
                            break;
                        case "LETRAS_INTENTADAS":
                            letrasIntentadas = parseEntero(partes[1]);
                            break;
                        case "TIEMPO_TOTAL_SEGUNDOS":
                            tiempoTotalSegundos = parseEntero(partes[1]);
                            break;
                        case "ALIAS":
                            alias = partes[1].trim();
                            break;
                        case "LOGROS":
                            cargarLogros(partes[1].trim());
                            break;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            // En caso de error, inicializar en cero
            juegosJugados = 0;
            juegosGanados = 0;
            rachaActual = 0;
            rachaMaxima = 0;
        }
    }
    
    /**
     * Guarda las estadÃ­sticas actuales en el archivo.
     */
    private int parseEntero(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void cargarLogros(String datos) {
        logros.clear();
        if (datos == null || datos.isBlank()) return;
        String[] partes = datos.split(",");
        for (String logro : partes) {
            if (!logro.isBlank()) logros.add(logro.trim());
        }
    }

    public void guardarEstadisticas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RUTA_STATS))) {
            writer.write("JUEGOS_JUGADOS=" + juegosJugados);
            writer.newLine();
            writer.write("JUEGOS_GANADOS=" + juegosGanados);
            writer.newLine();
            writer.write("RACHA_ACTUAL=" + rachaActual);
            writer.newLine();
            writer.write("RACHA_MAXIMA=" + rachaMaxima);
            writer.newLine();
            writer.write("EXPERIENCIA=" + experiencia);
            writer.newLine();
            writer.write("LETRAS_CORRECTAS=" + letrasCorrectas);
            writer.newLine();
            writer.write("LETRAS_INTENTADAS=" + letrasIntentadas);
            writer.newLine();
            writer.write("TIEMPO_TOTAL_SEGUNDOS=" + tiempoTotalSegundos);
            writer.newLine();
            writer.write("ALIAS=" + alias);
            writer.newLine();
            writer.write("LOGROS=" + String.join(",", logros));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudieron guardar estadÃ­sticas: " + e.getMessage());
        }
    }
    
    /**
     * Registra una victoria.
     */
    public void registrarVictoria() {
        registrarResultado(true, 0, 0, 0, 3, "NORMAL", "GENERAL");
    }
    
    /**
     * Registra una derrota.
     */
    public void registrarDerrota() {
        registrarResultado(false, 0, 0, 0, 0, "NORMAL", "GENERAL");
    }

    public int registrarResultado(boolean victoria, int correctasPartida, int intentosPartida, int tiempoSegundos, int pistasRestantes, String dificultad, String categoria) {
        juegosJugados++;
        letrasCorrectas += Math.max(0, correctasPartida);
        letrasIntentadas += Math.max(0, intentosPartida);
        tiempoTotalSegundos += Math.max(0, tiempoSegundos);

        int xpGanada = victoria ? 100 : 20;
        xpGanada += Math.max(0, correctasPartida) * 10;
        xpGanada -= Math.max(0, intentosPartida - correctasPartida) * 5;
        xpGanada += Math.max(0, pistasRestantes) * 15;

        if ("DIFICIL".equalsIgnoreCase(dificultad)) xpGanada = (int) Math.round(xpGanada * 1.35);
        if ("PESADILLA".equalsIgnoreCase(dificultad)) xpGanada = (int) Math.round(xpGanada * 1.75);
        if (tiempoSegundos > 0 && tiempoSegundos <= 60 && victoria) xpGanada += 50;

        if (victoria) {
            juegosGanados++;
            rachaActual++;
            if (rachaActual > rachaMaxima) rachaMaxima = rachaActual;
        } else {
            rachaActual = 0;
        }

        xpGanada = Math.max(0, xpGanada);
        experiencia += xpGanada;
        evaluarLogros(victoria, correctasPartida, intentosPartida, tiempoSegundos, pistasRestantes, categoria);
        ultimoResumen = "XP +" + xpGanada + " | Nivel " + getNivel() + " | " + getRango();
        guardarEstadisticas();
        return xpGanada;
    }

    private void evaluarLogros(boolean victoria, int correctasPartida, int intentosPartida, int tiempoSegundos, int pistasRestantes, String categoria) {
        if (juegosJugados >= 1) logros.add("PRIMER_ACCESO");
        if (victoria) logros.add("PRIMERA_VICTORIA");
        if (victoria && intentosPartida == correctasPartida) logros.add("SIN_ERRORES");
        if (victoria && pistasRestantes >= 3) logros.add("SIN_AYUDA");
        if (victoria && tiempoSegundos > 0 && tiempoSegundos <= 60) logros.add("ANALISTA_RAPIDO");
        if (rachaActual >= 3) logros.add("RACHA_X3");
        if (rachaActual >= 5) logros.add("RACHA_X5");
        if (victoria && categoria != null && categoria.equalsIgnoreCase("Hardware")) logros.add("DOMINIO_HARDWARE");
        if (victoria && categoria != null && categoria.equalsIgnoreCase("Ciberseguridad")) logros.add("DOMINIO_CYBER");
    }
    
    // Getters
    public int getJuegosJugados() {
        return juegosJugados;
    }
    
    public int getJuegosGanados() {
        return juegosGanados;
    }
    
    public int getRachaActual() {
        return rachaActual;
    }
    
    public int getRachaMaxima() {
        return rachaMaxima;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public void agregarXP(int cantidad) {
        if (cantidad > 0) {
            experiencia += cantidad;
            guardarEstadisticas();
        }
    }

    public int getNivel() {
        return Math.max(1, (experiencia / 250) + 1);
    }

    public int getXpSiguienteNivel() {
        return getNivel() * 250;
    }

    public double getPrecision() {
        if (letrasIntentadas == 0) return 0.0;
        return (letrasCorrectas * 100.0) / letrasIntentadas;
    }

    public int getTiempoPromedioSegundos() {
        if (juegosJugados == 0) return 0;
        return tiempoTotalSegundos / juegosJugados;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        if (alias != null && !alias.isBlank()) this.alias = alias.trim();
    }

    public String getUltimoResumen() {
        return ultimoResumen;
    }

    public String getLogrosResumen() {
        if (logros.isEmpty()) return "SIN LOGROS";
        return String.join(" | ", logros);
    }
    
    /**
     * Calcula el porcentaje de victorias.
     */
    public double getPorcentajeVictorias() {
        if (juegosJugados == 0) return 0.0;
        return (juegosGanados * 100.0) / juegosJugados;
    }
    
    /**
     * Calcula el rango/tÃ­tulo del jugador segÃºn sus victorias.
     */
    public String getRango() {
        int nivel = getNivel();
        if (nivel >= 18) return "LEYENDA DEL SISTEMA";
        if (nivel >= 15) return "MAESTRO DECODER";
        if (nivel >= 12) return "ARQUITECTO NEXUS";
        if (nivel >= 10) return "HACKER ETICO";
        if (nivel >= 8) return "ESPECIALISTA CYBER";
        if (nivel >= 6) return "TECNICO DE RED";
        if (nivel >= 4) return "ANALISTA DE SISTEMAS";
        if (nivel >= 2) return "OPERADOR JUNIOR";
        return "RECLUTA DIGITAL";
    }
    
    /**
     * Formatea las estadÃ­sticas para mostrar en UI con iconos Unicode.
     */
    public String formatearEstadisticas() {
        return String.format(
            "[ALIAS]: %s\n[RANGO]: %s\n[NIVEL]: %d\n[XP]: %d/%d\n[JUGADOS]: %d\n[GANADOS]: %d\n[PORCENTAJE]: %.1f%%\n[PRECISION]: %.1f%%\n[RACHA ACTUAL]: %d\n[RACHA MAXIMA]: %d",
            alias, getRango(), getNivel(), experiencia, getXpSiguienteNivel(), juegosJugados, juegosGanados, getPorcentajeVictorias(), getPrecision(), rachaActual, rachaMaxima
        );
    }
    
    /**
     * Formatea las estadÃ­sticas en lÃ­neas separadas para panel con iconos.
     */
    public String[] formatearEstadisticasConIconos() {
        return new String[] {
            String.format("RANGO: %s", getRango()),
            String.format("NIVEL: %d", getNivel()),
            String.format("XP: %d/%d", experiencia, getXpSiguienteNivel()),
            String.format("JUGADOS: %d", juegosJugados),
            String.format("GANADOS: %d", juegosGanados),
            String.format("PORCENTAJE: %.1f%%", getPorcentajeVictorias()),
            String.format("PRECISION: %.1f%%", getPrecision()),
            String.format("RACHA ACTUAL: %d", rachaActual),
            String.format("RACHA MAXIMA: %d", rachaMaxima)
        };
    }
    
    /**
     * Formatea en una sola lÃ­nea compacta.
     */
    public String formatearCompacto() {
        return String.format(
            "[NIVEL: %d | XP: %d | JUGADOS: %d | GANADOS: %d | RACHA: %d]",
            getNivel(), experiencia, juegosJugados, juegosGanados, rachaActual
        );
    }
}

