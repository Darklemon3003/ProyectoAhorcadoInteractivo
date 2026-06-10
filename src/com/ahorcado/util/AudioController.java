package com.ahorcado.util;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.List;

/**
 * AudioController - Controlador de audio del juego (Singleton).
 * 
 * Maneja todos los sonidos del juego de forma centralizada:
 * efectos de sonido, musica de fondo y la voz sintetica.
 * Usa un ArrayList pa llevar registro de los clips activos
 * y poder detenerlos todos cuando se cierra el juego.
 * 
 * NOTA: si no hay archivos de sonido en /sounds/ no pasa nada,
 * el juego funciona sin audio tambien.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class AudioController {
    
    private static AudioController instancia;
    private GestorSonido gestorSonido;
    private MotorVoz motorVoz;
    private List<Clip> clipsActivos;
    private boolean audioGlobalActivo;
    private boolean bgmGameActivo; // Canal BGM independiente
    private Clip clipTecleoActual; // Para cancelar sonido de teclado previo
    
    private AudioController() {
        this.gestorSonido = new GestorSonido();
        this.motorVoz = new MotorVoz();
        this.clipsActivos = new ArrayList<>();
        this.audioGlobalActivo = true;
    }
    
    /**
     * Obtiene la instancia única del AudioController (Singleton).
     */
    public static AudioController getInstance() {
        if (instancia == null) {
            instancia = new AudioController();
        }
        return instancia;
    }
    
    /**
     * Registra un clip activo para seguimiento.
     */
    public void registrarClip(Clip clip) {
        if (clip != null && !clipsActivos.contains(clip)) {
            clipsActivos.add(clip);
        }
    }
    
    /**
     * [CRÍTICO] Detiene TODOS los sonidos activos sin excepción.
     * Usa stop() + flush() sin close() para permitir reutilización de clips.
     * También detiene la voz sintetizada.
     */
    public void stopAllSounds() {
        // 1. Detener sonido de teclado actual
        if (clipTecleoActual != null) {
            try {
                if (clipTecleoActual.isRunning()) {
                    clipTecleoActual.stop();
                }
                clipTecleoActual.flush();
            } catch (Exception e) {
                System.err.println("[AudioController] Error al detener clip teclado: " + e.getMessage());
            }
            clipTecleoActual = null;
        }
        
        // 2. Detener todos los clips registrados (sin cerrarlos)
        for (Clip clip : clipsActivos) {
            if (clip != null) {
                try {
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.flush();
                } catch (Exception e) {
                    System.err.println("[AudioController] Error al detener clip: " + e.getMessage());
                }
            }
        }
        clipsActivos.clear();
        
        // 3. Detener clips del GestorSonido (sin cerrar/destruir)
        if (gestorSonido != null) {
            gestorSonido.detenerTodo();
        }
        
        // 4. Detener voz sintetizada
        if (motorVoz != null) {
            motorVoz.setSilenciado(true);
        }
        
        // 5. Marcar audio como inactivo
        audioGlobalActivo = false;
    }
    
    /**
     * Reactiva el audio después de un stopAllSounds().
     */
    public void reactivarAudio() {
        audioGlobalActivo = true;
        if (motorVoz != null) {
            motorVoz.setSilenciado(false);
        }
        // Restaurar GestorSonido: quitar silenciado y recrear si fue destruido
        if (gestorSonido == null || !gestorSonido.sonidosDisponibles()) {
            gestorSonido = new GestorSonido();
        }
        gestorSonido.setSilenciado(false);
    }
    
    /**
     * Verifica si el audio está globalmente activo.
     */
    public boolean isAudioGlobalActivo() {
        return audioGlobalActivo;
    }
    
    // === Delegación a GestorSonido ===
    
    public void reproducirAcierto() {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            gestorSonido.reproducirAcierto();
        }
    }
    
    public void reproducirError() {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            gestorSonido.reproducirError();
        }
    }
    
    public void reproducirTecleo() {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            // Cancelar sonido de teclado previo si existe
            if (clipTecleoActual != null && clipTecleoActual.isRunning()) {
                clipTecleoActual.stop();
                clipTecleoActual.flush();
            }
            gestorSonido.reproducirTecleo();
        }
    }
    
    public void reproducirPalanca() {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            gestorSonido.reproducirPalanca();
        }
    }
    
    public void reproducirMusicaMenu() {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            gestorSonido.reproducirMusicaMenu();
        }
    }
    
    public void detenerMusicaMenu() {
        if (gestorSonido != null) {
            gestorSonido.detenerMusicaMenu();
        }
    }
    
    /**
     * Inicia la musica de fondo exclusiva del juego (canal BGM independiente).
     * No interfiere con SFX ni TTS - opera en su propio Clip.
     */
    public void reproducirBgmGame() {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            gestorSonido.reproducirBgmGame();
            bgmGameActivo = true;
        }
    }
    
    /**
     * Detiene la musica de fondo del juego.
     */
    public void detenerBgmGame() {
        if (gestorSonido != null) {
            gestorSonido.detenerBgmGame();
        }
        bgmGameActivo = false;
    }
    
    /**
     * Consulta si el BGM de juego esta activo.
     */
    public boolean isBgmGameActivo() {
        return bgmGameActivo && gestorSonido != null && gestorSonido.isBgmGameRunning();
    }
    
    public void actualizarTension(int fallos, boolean vivo) {
        if (!audioGlobalActivo) return;
        if (gestorSonido != null) {
            gestorSonido.actualizarTension(fallos, vivo);
        }
    }
    
    public boolean sonidosDisponibles() {
        return gestorSonido != null && gestorSonido.sonidosDisponibles();
    }
    
    // === Delegación a MotorVoz ===
    
    public void hablarFrase(String frase) {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarFrase(frase);
        }
    }
    
    public void hablarExito() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarExito();
        }
    }
    
    public void hablarError() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarError();
        }
    }
    
    public void hablarVictoria() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarVictoria();
        }
    }
    
    public void hablarDerrota() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarDerrota();
        }
    }
    
    public void hablarReinicio() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarReinicio();
        }
    }
    
    public void hablarPistaCategoria(String categoria) {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarPistaCategoria(categoria);
        }
    }
    
    public void hablarPistaLetra(char letra) {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarPistaLetra(letra);
        }
    }
    
    public void hablarPistaDescripcion() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarPistaDescripcion();
        }
    }
    
    public void hablarClick() {
        if (!audioGlobalActivo) return;
        if (motorVoz != null) {
            motorVoz.hablarClick();
        }
    }
    
    public void setSilenciado(boolean silenciar) {
        if (motorVoz != null) {
            motorVoz.setSilenciado(silenciar);
        }
        if (gestorSonido != null) {
            gestorSonido.setSilenciado(silenciar);
        }
        if (silenciar) {
            audioGlobalActivo = false;
        } else {
            audioGlobalActivo = true;
        }
    }
    
    public boolean isSilenciado() {
        return motorVoz != null && motorVoz.isSilenciado();
    }
    
    /**
     * Silencia SOLO el MotorVoz (TTS). No afecta SFX ni música.
     */
    public void setVozSilenciada(boolean silenciar) {
        if (motorVoz != null) {
            motorVoz.setSilenciado(silenciar);
        }
    }
    
    /**
     * Verifica si SOLO la voz (TTS) está silenciada.
     */
    public boolean isVozSilenciada() {
        return motorVoz != null && motorVoz.isSilenciado();
    }
    
    /**
     * Limpieza completa al cerrar la aplicación.
     * Cierra permanentemente todos los clips y destruye el singleton.
     */
    public void cerrar() {
        stopAllSounds();
        // Cerrar permanentemente clips del GestorSonido
        if (gestorSonido != null) {
            gestorSonido.cerrar();
            gestorSonido = null;
        }
        instancia = null;
    }
}
