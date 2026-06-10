package com.ahorcado.util;

import java.awt.Color;

/**
 * Bug - Modelo de un "bug/glitch" coleccionable del juego.
 * 
 * Cada bug tiene un id, nombre, descripcion, costo en KK (Kali Koints),
 * colores tematicos y una rareza (COMUN, RARO, EPICO, LEGENDARIO).
 * Los bugs aparecen como popups durante el juego cuando estan desbloqueados.
 * 
 * Tiene una maquina de estados de animacion (AnimationStateMachine) pa
 * controlar los gestos del bug (IDLE, LAUGH, GROWL, etc). Esto seria
 * como un mini patron State que vimos en clase.
 * 
 * Se usa encapsulamiento: el campo desbloqueado es privado con getter/setter.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class Bug {

    public enum Rareza {
        COMUN("COMUN", new Color(0x00FF88)),
        RARO("RARO", new Color(0x00FFFF)),
        EPICO("EPICO", new Color(0xFF55AA)),
        LEGENDARIO("LEGENDARIO", new Color(0xFFD700));

        public final String etiqueta;
        public final Color color;
        Rareza(String etiqueta, Color color) {
            this.etiqueta = etiqueta;
            this.color = color;
        }
    }

    /**
     * Estados de gesto que un bug animado puede exhibir.
     * Cada gesto tiene una duracion base en milisegundos.
     */
    public enum BugGesture {
        IDLE(0),                 // Inactivo con parpadeo ciclico (loop infinito)
        LAUGH(900),              // Risa de hacker al acertar
        GROWL(700),              // Gruñido/Enojo al fallar
        ATTACK_LETTER(1200),     // Mordida/corrupcion de letra - la funcionalidad "brutal"
        VICTORY_DANCE(2000);     // Celebracion de fin de partida

        public final int duracionMs;
        BugGesture(int duracionMs) { this.duracionMs = duracionMs; }
    }

    /**
     * Maquina de estados de animacion ligera.
     * Controla gesto actual, progreso (0.0-1.0), transiciones y retorno a IDLE.
     * Thread-safe para uso desde Timer de Swing.
     */
    public static class AnimationStateMachine {
        private BugGesture gestoActual = BugGesture.IDLE;
        private long inicioGesto = System.currentTimeMillis();
        private int tickIdle = 0; // contador global para parpadeo IDLE

        /** Dispara un gesto. Si el bug esta bloqueado, solo permite IDLE. */
        public synchronized void disparar(BugGesture gesto, boolean desbloqueado) {
            if (gesto == null) return;
            // Solo bugs desbloqueados acceden a gestos premium
            if (!desbloqueado && gesto != BugGesture.IDLE) return;
            this.gestoActual = gesto;
            this.inicioGesto = System.currentTimeMillis();
        }

        /** Avanza el tick global (llamar cada frame del timer). */
        public synchronized void tick() {
            tickIdle++;
            // Auto-retorno a IDLE cuando el gesto temporal termina
            if (gestoActual != BugGesture.IDLE && gestoActual.duracionMs > 0) {
                long elapsed = System.currentTimeMillis() - inicioGesto;
                if (elapsed >= gestoActual.duracionMs) {
                    gestoActual = BugGesture.IDLE;
                }
            }
        }

        /** Progreso del gesto actual (0.0 = inicio, 1.0 = fin). Para IDLE siempre retorna 0. */
        public synchronized float getProgreso() {
            if (gestoActual == BugGesture.IDLE || gestoActual.duracionMs <= 0) return 0f;
            long elapsed = System.currentTimeMillis() - inicioGesto;
            return Math.min(1.0f, (float) elapsed / gestoActual.duracionMs);
        }

        public synchronized BugGesture getGestoActual() { return gestoActual; }
        public synchronized int getTickIdle() { return tickIdle; }

        /** Reinicia a IDLE inmediatamente. */
        public synchronized void reset() {
            gestoActual = BugGesture.IDLE;
            tickIdle = 0;
        }
    }

    public final String id;
    public final String nombre;
    public final String descripcion;
    public final int costoKK;
    public final Color colorPrincipal;
    public final Color colorAccento;
    public final Rareza rareza;
    public final String accesorio; // identificador iconografico para la fabrica de avatares
    public final String etiquetaCorta; // texto que aparece dentro del popup
    private boolean desbloqueado;

    // Maquina de estados de animacion - una por instancia de Bug
    private final AnimationStateMachine animState = new AnimationStateMachine();

    public Bug(String id, String nombre, String descripcion, int costoKK,
               Color colorPrincipal, Color colorAccento, Rareza rareza,
               String accesorio, String etiquetaCorta, boolean desbloqueadoPorDefecto) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoKK = costoKK;
        this.colorPrincipal = colorPrincipal;
        this.colorAccento = colorAccento;
        this.rareza = rareza;
        this.accesorio = accesorio;
        this.etiquetaCorta = etiquetaCorta;
        this.desbloqueado = desbloqueadoPorDefecto;
    }

    public boolean isDesbloqueado() { return desbloqueado; }
    public void setDesbloqueado(boolean v) { this.desbloqueado = v; }

    /** Acceso a la maquina de animacion. */
    public AnimationStateMachine getAnimState() { return animState; }

    /** Atajo: dispara un gesto respetando el estado de desbloqueo. */
    public void dispararGesto(BugGesture gesto) {
        animState.disparar(gesto, desbloqueado);
    }
}
