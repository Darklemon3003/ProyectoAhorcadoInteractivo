package com.ahorcado.util;

import java.io.IOException;

/**
 * MotorVoz - Sistema de texto a voz (TTS) usando PowerShell de Windows.
 * 
 * Usa la API System.Speech de Windows pa leer en voz alta los mensajes
 * del juego. Tiene sistema de prioridades: la voz del usuario (alta)
 * interrumpe la voz automatica (baja).
 * 
 * NOTA: solo funciona en Windows porque usa PowerShell. En otros
 * sistemas operativos simplemente no habla pero no da error.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class MotorVoz {

    public enum Prioridad {
        BAJA,   // Frases Deep-Thought automáticas
        ALTA    // Acciones del usuario: letras, pistas, reinicio
    }

    private boolean silenciado = false;
    private Process procesoVozActual = null;

    /**
     * Activa o desactiva el silencio global.
     * Mata cualquier proceso de voz en ejecucion.
     */
    public void setSilenciado(boolean silenciar) {
        this.silenciado = silenciar;
        if (silenciar) {
            detenerVozActual();
        }
    }

    public boolean isSilenciado() {
        return silenciado;
    }

    /**
     * Detiene el proceso de voz actual si existe.
     */
    private synchronized void detenerVozActual() {
        if (procesoVozActual != null && procesoVozActual.isAlive()) {
            procesoVozActual.destroyForcibly();
            procesoVozActual = null;
        }
    }

    /**
     * Habla un texto usando PowerShell + System.Speech.
     * Si la prioridad es ALTA, interrumpe cualquier voz BAJA en curso.
     */
    private void hablar(String texto, Prioridad prioridad) {
        if (silenciado || texto == null || texto.isEmpty()) return;

        // Si hay voz en curso
        if (procesoVozActual != null && procesoVozActual.isAlive()) {
            if (prioridad == Prioridad.ALTA) {
                // Prioridad alta interrumpe cualquier voz
                detenerVozActual();
            } else {
                // Prioridad baja no interrumpe
                return;
            }
        }

        // Escapar comillas simples para PowerShell
        String textoEscapado = texto.replace("'", "''");

        String comando = String.format(
            "Add-Type -AssemblyName System.Speech; " +
            "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
            "$synth.Speak('%s'); " +
            "$synth.Dispose();",
            textoEscapado
        );

        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", comando);
        pb.inheritIO().redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD);

        try {
            procesoVozActual = pb.start();
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo iniciar sintetizador de voz: " + e.getMessage());
        }
    }

    /**
     * Narra una frase del panel Deep-Thought (prioridad BAJA - puede ser interrumpida).
     */
    public void hablarFrase(String frase) {
        hablar(frase, Prioridad.BAJA);
    }

    /**
     * Mensaje de exito (prioridad ALTA).
     */
    public void hablarExito() {
        hablar("Segmento autorizado", Prioridad.ALTA);
    }

    /**
     * Mensaje de error (prioridad ALTA).
     */
    public void hablarError() {
        hablar("Error de integridad detectado", Prioridad.ALTA);
    }

    /**
     * Mensaje de victoria (prioridad ALTA).
     */
    public void hablarVictoria() {
        hablar("Protocolo completado. Acceso concedido", Prioridad.ALTA);
    }

    /**
     * Mensaje de derrota (prioridad ALTA).
     */
    public void hablarDerrota() {
        hablar("Conexion perdida. Sistema bloqueado", Prioridad.ALTA);
    }

    /**
     * Mensaje de reinicio (prioridad ALTA).
     */
    public void hablarReinicio() {
        hablar("Reiniciando protocolos de seguridad. Sistema listo para nueva secuencia", Prioridad.ALTA);
    }

    /**
     * Narra la categoria de una pista (prioridad ALTA).
     */
    public void hablarPistaCategoria(String categoria) {
        hablar("Extrayendo datos. Categoria encontrada: " + categoria, Prioridad.ALTA);
    }

    /**
     * Narra una letra revelada en pista (prioridad ALTA).
     */
    public void hablarPistaLetra(char letra) {
        hablar("Extrayendo datos. Letra identificada: " + letra, Prioridad.ALTA);
    }

    /**
     * Narra descripcion de pista (prioridad ALTA).
     */
    public void hablarPistaDescripcion() {
        hablar("Extrayendo datos. Descargando informacion de la base de datos", Prioridad.ALTA);
    }

    /**
     * Sonido de click breve (usado para teclas).
     */
    public void hablarClick() {
        // No narrar clicks para no saturar
    }
}
