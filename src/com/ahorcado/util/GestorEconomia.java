package com.ahorcado.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GestorEconomia - Maneja la moneda virtual del juego (Kali Koints / KK).
 * 
 * Singleton que controla el saldo del jugador. Permite ganar KK al
 * terminar partidas y gastarlos en la tienda pa comprar bugs, skins
 * y fondos. Persiste todo en economia.txt.
 * 
 * Las recompensas son constantes que se pueden ajustar facil
 * (victoria=50KK, derrota=5KK, bonus por dificultad, etc).
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorEconomia {

    // === RECOMPENSAS (facilmente ajustables) ===
    public static final int KK_INICIAL = 100;
    public static final int KK_VICTORIA = 50;
    public static final int KK_POR_INTENTO_SOBRANTE = 10;
    public static final int KK_DERROTA = 5;
    public static final int KK_BONUS_DIFICIL = 25;
    public static final int KK_BONUS_PESADILLA = 50;
    public static final int KK_BONUS_SIN_FALLOS = 30;
    public static final int KK_BONUS_RAPIDO = 20; // <60s

    private static final String ARCHIVO = "economia.txt";
    private static final String RUTA = System.getProperty("user.dir") + File.separator + ARCHIVO;

    private static GestorEconomia instancia;

    private int saldo;
    private int totalGanado;
    private int totalGastado;

    // Listeners para animacion en UI cuando cambia el saldo
    public interface SaldoListener {
        void onSaldoCambio(int saldoAnterior, int saldoNuevo, int delta);
    }
    private final List<WeakReference<SaldoListener>> listeners = new ArrayList<>();

    private GestorEconomia() {
        cargar();
    }

    public static synchronized GestorEconomia getInstancia() {
        if (instancia == null) instancia = new GestorEconomia();
        return instancia;
    }

    public int getSaldo() { return saldo; }
    public int getTotalGanado() { return totalGanado; }
    public int getTotalGastado() { return totalGastado; }

    public void addListener(SaldoListener l) {
        if (l == null) return;
        limpiarListeners();
        for (WeakReference<SaldoListener> ref : listeners) {
            if (ref.get() == l) return;
        }
        listeners.add(new WeakReference<>(l));
    }
    public void removeListener(SaldoListener l) {
        Iterator<WeakReference<SaldoListener>> it = listeners.iterator();
        while (it.hasNext()) {
            SaldoListener actual = it.next().get();
            if (actual == null || actual == l) it.remove();
        }
    }

    private void notificar(int anterior, int nuevo) {
        for (WeakReference<SaldoListener> ref : new ArrayList<>(listeners)) {
            SaldoListener l = ref.get();
            if (l == null) {
                listeners.remove(ref);
                continue;
            }
            try { l.onSaldoCambio(anterior, nuevo, nuevo - anterior); }
            catch (Exception ignored) {}
        }
    }

    private void limpiarListeners() {
        listeners.removeIf(ref -> ref.get() == null);
    }

    /**
     * Calcula y otorga las KK por una partida finalizada.
     * @return cantidad de KK otorgadas (positiva)
     */
    public int otorgarRecompensaPartida(boolean victoria, int intentosRestantes,
                                        int letrasFalladasPartida, int tiempoSegundos,
                                        String dificultad) {
        int kk;
        if (victoria) {
            kk = KK_VICTORIA + Math.max(0, intentosRestantes) * KK_POR_INTENTO_SOBRANTE;
            if (letrasFalladasPartida == 0) kk += KK_BONUS_SIN_FALLOS;
            if (tiempoSegundos > 0 && tiempoSegundos <= 60) kk += KK_BONUS_RAPIDO;
        } else {
            kk = KK_DERROTA;
        }
        if (dificultad != null) {
            String d = dificultad.toUpperCase();
            if (d.contains("DIFICIL")) kk += KK_BONUS_DIFICIL;
            else if (d.contains("PESADILLA")) kk += KK_BONUS_PESADILLA;
        }
        agregar(kk);
        return kk;
    }

    public void agregar(int cantidad) {
        if (cantidad <= 0) return;
        int anterior = saldo;
        saldo += cantidad;
        totalGanado += cantidad;
        guardar();
        notificar(anterior, saldo);
    }

    /**
     * Intenta gastar la cantidad indicada.
     * @return true si tenia saldo suficiente y se descontaron las KK.
     */
    public boolean gastar(int cantidad) {
        if (cantidad <= 0) return true;
        if (saldo < cantidad) return false;
        int anterior = saldo;
        saldo -= cantidad;
        totalGastado += cantidad;
        guardar();
        notificar(anterior, saldo);
        return true;
    }

    public boolean puedePermitirse(int cantidad) {
        return saldo >= cantidad;
    }

    /** Reinicio total (uso en boton "REINICIAR SISTEMA"). */
    public void reiniciar() {
        int anterior = saldo;
        saldo = KK_INICIAL;
        totalGanado = 0;
        totalGastado = 0;
        guardar();
        notificar(anterior, saldo);
    }

    private void cargar() {
        File f = new File(RUTA);
        if (!f.exists()) {
            saldo = KK_INICIAL;
            totalGanado = 0;
            totalGastado = 0;
            guardar();
            return;
        }
        boolean encontroSaldo = false;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                String[] p = linea.split("=", 2);
                if (p.length != 2) continue;
                String k = p[0].trim();
                String v = p[1].trim();
                try {
                    switch (k) {
                        case "SALDO": saldo = Integer.parseInt(v); encontroSaldo = true; break;
                        case "TOTAL_GANADO": totalGanado = Integer.parseInt(v); break;
                        case "TOTAL_GASTADO": totalGastado = Integer.parseInt(v); break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[ECONOMIA] Error al leer " + RUTA + ": " + e.getMessage());
        }
        if (!encontroSaldo) {
            saldo = KK_INICIAL;
            guardar();
        }
    }

    private void guardar() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(RUTA))) {
            w.write("SALDO=" + saldo); w.newLine();
            w.write("TOTAL_GANADO=" + totalGanado); w.newLine();
            w.write("TOTAL_GASTADO=" + totalGastado); w.newLine();
        } catch (IOException e) {
            System.err.println("[ECONOMIA] Error al escribir " + RUTA + ": " + e.getMessage());
        }
    }
}
