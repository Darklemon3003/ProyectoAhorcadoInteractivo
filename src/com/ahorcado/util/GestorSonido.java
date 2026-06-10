package com.ahorcado.util;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

/**
 * GestorSonido - Carga y reproduce los archivos de sonido del juego.
 * 
 * Maneja los clips de audio (efectos, musica de fondo, etc).
 * Si los archivos .wav no existen en /sounds/, los genera automaticamente
 * con tonos sinteticos basicos pa que el juego tenga audio aunque sea.
 * 
 * NOTA: los sonidos se generan con la API javax.sound si no existen
 * los archivos, no es la mejor calidad pero funciona.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GestorSonido {
    
    private static final int SAMPLE_RATE = 44100;
    private static final String SOUNDS_DIR = "sounds/";
    
    private static final String RUTA_ACIERTO = SOUNDS_DIR + "beep_acierto.wav";
    private static final String RUTA_ERROR = SOUNDS_DIR + "alarm_error.wav";
    private static final String RUTA_TECLEO = SOUNDS_DIR + "tecleo.wav";
    private static final String RUTA_HUM = SOUNDS_DIR + "bgm_hum.wav";
    private static final String RUTA_HEARTBEAT = SOUNDS_DIR + "bgm_heartbeat.wav";
    private static final String RUTA_CLACK = SOUNDS_DIR + "sfx_clack.wav";
    private static final String RUTA_MENU = SOUNDS_DIR + "bgm_menu.wav";
    private static final String RUTA_BGM_GAME = SOUNDS_DIR + "bgm_game.wav";
    
    private Clip clipAcierto;
    private Clip clipError;
    private Clip clipTecleo;
    private Clip clipHum;
    private Clip clipHeartbeat;
    private Clip clipClack;
    private Clip clipMenu;
    private Clip clipBgmGame;
    
    private boolean sonidosCargados = false;
    private boolean silenciado = false;
    
    public GestorSonido() {
        inicializarSonidos();
    }
    
    private void inicializarSonidos() {
        try {
            generarArchivosSiNoExisten();
            
            clipAcierto = cargarClip(RUTA_ACIERTO);
            clipError = cargarClip(RUTA_ERROR);
            clipTecleo = cargarClip(RUTA_TECLEO);
            clipHum = cargarClip(RUTA_HUM);
            clipHeartbeat = cargarClip(RUTA_HEARTBEAT);
            clipClack = cargarClip(RUTA_CLACK);
            clipMenu = cargarClip(RUTA_MENU);
            clipBgmGame = cargarClip(RUTA_BGM_GAME);
            
            sonidosCargados = (clipAcierto != null && clipError != null && clipTecleo != null);
            
            if (clipHum != null) {
                // Configurar el volumen del zumbido eléctrico
                try {
                    FloatControl gainControl = (FloatControl) clipHum.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(-15.0f); // Reducir volumen
                } catch (Exception e) {}
            }
        } catch (Exception e) {
            System.err.println("[SISTEMA]> ERR_AUDIO: " + e.getMessage());
        }
    }
    
    private void generarArchivosSiNoExisten() throws IOException {
        if (!recursoExiste(RUTA_ACIERTO)) guardarWavEnResources("beep_acierto.wav", generarTonoSintetico(880, 150, 0.3f, true));
        if (!recursoExiste(RUTA_ERROR)) guardarWavEnResources("alarm_error.wav", generarTonoSintetico(220, 400, 0.4f, false));
        if (!recursoExiste(RUTA_TECLEO)) guardarWavEnResources("tecleo.wav", generarClickMecanico());
        if (!recursoExiste(RUTA_HUM)) guardarWavEnResources("bgm_hum.wav", generarZumbidoElectrico());
        if (!recursoExiste(RUTA_HEARTBEAT)) guardarWavEnResources("bgm_heartbeat.wav", generarLatido());
        if (!recursoExiste(RUTA_CLACK)) guardarWavEnResources("sfx_clack.wav", generarClickMecanicoFuerte());
        if (!recursoExiste(RUTA_MENU)) guardarWavEnResources("bgm_menu.wav", generarMusicaMenu());
        if (!recursoExiste(RUTA_BGM_GAME)) guardarWavEnResources("bgm_game.wav", generarMusicaJuego());
    }
    
    private boolean recursoExiste(String ruta) {
        URL url = getClass().getClassLoader().getResource(ruta);
        return url != null;
    }
    
    private byte[] generarTonoSintetico(float frecuencia, int duracionMs, float volumen, boolean suave) {
        int numSamples = (int) ((duracionMs / 1000.0) * SAMPLE_RATE);
        byte[] audioData = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double valor;
            if (suave) {
                double envolvente = Math.exp(-tiempo * 8);
                valor = Math.sin(2 * Math.PI * frecuencia * tiempo) * envolvente * volumen;
            } else {
                double ciclo = (frecuencia * tiempo) % 1.0;
                valor = (ciclo < 0.5 ? 1.0 : -1.0) * volumen;
                valor *= (0.7 + 0.3 * Math.sin(2 * Math.PI * 8 * tiempo));
            }
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }
    
    private byte[] generarClickMecanico() {
        int numSamples = (int) (0.05 * SAMPLE_RATE);
        byte[] audioData = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double ruido = (Math.random() - 0.5) * 2.0;
            double resonancia = Math.sin(2 * Math.PI * 2000 * tiempo) * Math.exp(-tiempo * 40);
            double envolvente = Math.exp(-tiempo * 60);
            double valor = (ruido * 0.3 + resonancia * 0.7) * envolvente * 0.5;
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }

    private byte[] generarZumbidoElectrico() {
        int numSamples = (int) (1.0 * SAMPLE_RATE);
        byte[] audioData = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double v1 = Math.sin(2 * Math.PI * 50 * tiempo) * 0.4;
            double v2 = Math.sin(2 * Math.PI * 100 * tiempo) * 0.2;
            double v3 = Math.sin(2 * Math.PI * 150 * tiempo) * 0.1;
            double ruido = (Math.random() - 0.5) * 0.05;
            double valor = (v1 + v2 + v3 + ruido) * 0.3;
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }
    
    private byte[] generarLatido() {
        int numSamples = (int) (1.0 * SAMPLE_RATE);
        byte[] audioData = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double pulse1 = Math.exp(-Math.pow((tiempo - 0.1) / 0.02, 2)) * Math.sin(2 * Math.PI * 40 * tiempo);
            double pulse2 = Math.exp(-Math.pow((tiempo - 0.3) / 0.02, 2)) * Math.sin(2 * Math.PI * 45 * tiempo);
            double valor = (pulse1 + pulse2 * 0.7) * 0.8;
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }
    
    private byte[] generarClickMecanicoFuerte() {
        int numSamples = (int) (0.2 * SAMPLE_RATE);
        byte[] audioData = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double ruido = (Math.random() - 0.5) * 2.0;
            double resonancia = Math.sin(2 * Math.PI * 80 * tiempo) * Math.exp(-tiempo * 10);
            double envolvente = Math.exp(-tiempo * 20);
            double valor = (ruido * 0.3 + resonancia * 0.7) * envolvente * 0.9;
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }
    
    private byte[] generarMusicaMenu() {
        // Generar una melodía suave estilo hacker/retro (8 segundos de loop)
        int duracionSegundos = 8;
        int numSamples = duracionSegundos * SAMPLE_RATE;
        byte[] audioData = new byte[numSamples * 2];
        
        // Notas de una escala menor pentatónica (estilo hacker)
        double[] notas = {220.0, 261.63, 293.66, 329.63, 392.0, 440.0}; // A3, C4, D4, E4, G4, A4
        int[] patronNotas = {0, 2, 4, 2, 0, 4, 5, 4, 2, 0, 2, 4, 2, 0, 5, 4}; // Patrón de melodía
        
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double tiempoNota = tiempo % 2.0; // Ciclo de 2 segundos por frase
            int indiceNota = (int)(tiempoNota / 0.125) % patronNotas.length;
            
            double frecuenciaBase = notas[patronNotas[indiceNota]];
            
            // Oscilador principal (onda seno suave)
            double onda1 = Math.sin(2 * Math.PI * frecuenciaBase * tiempo);
            
            // Armonía (onda cuadrada suave para efecto retro)
            double onda2 = Math.signum(Math.sin(2 * Math.PI * frecuenciaBase * 0.5 * tiempo)) * 0.3;
            
            // Efecto de vibrato suave
            double vibrato = 1.0 + 0.02 * Math.sin(2 * Math.PI * 5 * tiempo);
            
            // Envolvente suave (fade in/out para cada nota)
            double posicionEnNota = (tiempoNota % 0.125) / 0.125;
            double envolvente = Math.sin(Math.PI * posicionEnNota);
            
            // Mezcla de ondas
            double valor = (onda1 * 0.7 + onda2 * 0.3) * vibrato * envolvente * 0.25;
            
            // Filtro paso bajo suave (para suavizar el sonido)
            valor = valor * 0.8;
            
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }
    
    private byte[] crearWavPCM(byte[] audioData, int canales) {
        int byteRate = SAMPLE_RATE * canales * 2;
        int totalSize = 36 + audioData.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeBytes("RIFF");
            dos.writeInt(Integer.reverseBytes(totalSize));
            dos.writeBytes("WAVE");
            dos.writeBytes("fmt ");
            dos.writeInt(Integer.reverseBytes(16));
            dos.writeShort(Short.reverseBytes((short) 1));
            dos.writeShort(Short.reverseBytes((short) canales));
            dos.writeInt(Integer.reverseBytes(SAMPLE_RATE));
            dos.writeInt(Integer.reverseBytes(byteRate));
            dos.writeShort(Short.reverseBytes((short) (canales * 2)));
            dos.writeShort(Short.reverseBytes((short) 16));
            dos.writeBytes("data");
            dos.writeInt(Integer.reverseBytes(audioData.length));
            dos.write(audioData);
            dos.close();
        } catch (IOException e) { e.printStackTrace(); }
        return baos.toByteArray();
    }
    
    private void guardarWavEnResources(String nombreArchivo, byte[] datos) {
        try {
            String rutaProyecto = System.getProperty("user.dir");
            File dirSounds = new File(rutaProyecto, "src/main/resources/sounds");
            if (!dirSounds.exists()) dirSounds.mkdirs();
            File archivo = new File(dirSounds, nombreArchivo);
            try (FileOutputStream fos = new FileOutputStream(archivo)) { fos.write(datos); }
        } catch (IOException e) {
            try {
                File temp = File.createTempFile("ahorcado_", "_" + nombreArchivo);
                try (FileOutputStream fos = new FileOutputStream(temp)) { fos.write(datos); }
            } catch (IOException ex) {}
        }
    }
    
    private Clip cargarClip(String ruta) {
        try {
            String rutaProyecto = System.getProperty("user.dir");
            File archivo = new File(rutaProyecto, "src/main/resources/" + ruta);
            if (archivo.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(archivo);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                return clip;
            }
            URL url = getClass().getClassLoader().getResource(ruta);
            if (url != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                return clip;
            }
        } catch (Exception e) {}
        return null;
    }
    
    public void reproducirAcierto() { reproducirClip(clipAcierto); }
    public void reproducirError() { reproducirClip(clipError); }
    public void reproducirTecleo() { reproducirClip(clipTecleo); }
    public void reproducirPalanca() { reproducirClip(clipClack); }
    
    public void reproducirMusicaMenu() {
        System.out.println("[DEBUG AUDIO] reproducirMusicaMenu() llamado. clipMenu: " + (clipMenu != null) + ", silenciado: " + silenciado);
        if (clipMenu != null) {
            try {
                // Detener si está corriendo
                if (clipMenu.isRunning()) {
                    clipMenu.stop();
                }
                // Reiniciar posición
                clipMenu.setFramePosition(0);
                // Reproducir en loop si no está silenciado
                if (!silenciado) {
                    clipMenu.loop(Clip.LOOP_CONTINUOUSLY);
                    System.out.println("[DEBUG AUDIO] Música de menú iniciada correctamente");
                } else {
                    System.out.println("[DEBUG AUDIO] Audio silenciado, no se reproduce música");
                }
            } catch (Exception e) {
                System.out.println("[DEBUG AUDIO] Error al reproducir música de menú: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[DEBUG AUDIO] clipMenu es null - no se puede reproducir");
        }
    }
    
    public void detenerMusicaMenu() {
        System.out.println("[DEBUG AUDIO] detenerMusicaMenu() llamado. clipMenu running: " + (clipMenu != null && clipMenu.isRunning()));
        if (clipMenu != null && clipMenu.isRunning()) {
            clipMenu.stop();
            System.out.println("[DEBUG AUDIO] Música de menú detenida");
        }
    }
    
    public void reproducirBgmGame() {
        System.out.println("[DEBUG AUDIO] reproducirBgmGame() llamado. clipBgmGame: " + (clipBgmGame != null) + ", silenciado: " + silenciado);
        if (clipBgmGame != null) {
            try {
                // Detener si está corriendo
                if (clipBgmGame.isRunning()) {
                    clipBgmGame.stop();
                }
                clipBgmGame.setFramePosition(0);
                // Volumen bajo para no opacar SFX/TTS
                try {
                    FloatControl gain = (FloatControl) clipBgmGame.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(-12.0f);
                } catch (Exception e) {
                    System.out.println("[DEBUG AUDIO] No se pudo ajustar volumen de BGM: " + e.getMessage());
                }
                // Reproducir si no está silenciado
                if (!silenciado) {
                    clipBgmGame.loop(Clip.LOOP_CONTINUOUSLY);
                    System.out.println("[DEBUG AUDIO] Música de juego iniciada correctamente");
                } else {
                    System.out.println("[DEBUG AUDIO] Audio silenciado, no se reproduce música de juego");
                }
            } catch (Exception e) {
                System.out.println("[DEBUG AUDIO] Error al reproducir música de juego: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[DEBUG AUDIO] clipBgmGame es null - no se puede reproducir");
        }
    }
    
    public void detenerBgmGame() {
        if (clipBgmGame != null && clipBgmGame.isRunning()) {
            clipBgmGame.stop();
        }
    }
    
    public boolean isBgmGameRunning() {
        return clipBgmGame != null && clipBgmGame.isRunning();
    }
    
    public void setSilenciado(boolean silenciar) {
        this.silenciado = silenciar;
        if (silenciar) {
            detenerMusicaMenu();
            detenerTodo();
        }
    }
    
    public boolean isSilenciado() {
        return silenciado;
    }
    
    public void actualizarTension(int fallos, boolean vivo) {
        if (!sonidosCargados) return;
        
        if (vivo && clipHum != null && !clipHum.isRunning()) {
            clipHum.loop(Clip.LOOP_CONTINUOUSLY);
        } else if (!vivo && clipHum != null && clipHum.isRunning()) {
            clipHum.stop();
        }
        
        if (vivo && fallos >= 5) {
            if (clipHeartbeat != null && !clipHeartbeat.isRunning()) {
                clipHeartbeat.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } else {
            if (clipHeartbeat != null && clipHeartbeat.isRunning()) {
                clipHeartbeat.stop();
            }
        }
    }
    
    public void detenerTodo() {
        if (clipHum != null) clipHum.stop();
        if (clipHeartbeat != null) clipHeartbeat.stop();
        if (clipMenu != null) clipMenu.stop();
        if (clipBgmGame != null) clipBgmGame.stop();
    }
    
    private void reproducirClip(Clip clip) {
        if (clip != null && sonidosCargados) {
            try {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            } catch (Exception e) {}
        }
    }
    
    public boolean sonidosDisponibles() { return sonidosCargados; }
    
    public void cerrar() {
        detenerTodo();
        if (clipAcierto != null) clipAcierto.close();
        if (clipError != null) clipError.close();
        if (clipTecleo != null) clipTecleo.close();
        if (clipHum != null) clipHum.close();
        if (clipHeartbeat != null) clipHeartbeat.close();
        if (clipClack != null) clipClack.close();
        if (clipMenu != null) clipMenu.close();
        if (clipBgmGame != null) clipBgmGame.close();
    }
    
    /**
     * Genera una pista de musica ambiente oscura/cyberpunk para el momento del juego.
     * 12 segundos de loop: pads bajos + arpeggio sutil + percusion minima.
     */
    private byte[] generarMusicaJuego() {
        int duracionSegundos = 12;
        int numSamples = duracionSegundos * SAMPLE_RATE;
        byte[] audioData = new byte[numSamples * 2];
        
        // Escala menor armonica: A2, C3, D3, E3, F3, G#3
        double[] notasPad = {110.0, 130.81, 146.83, 164.81};
        double[] notasArp = {220.0, 261.63, 329.63, 392.0, 329.63, 261.63};
        
        for (int i = 0; i < numSamples; i++) {
            double tiempo = i / (double) SAMPLE_RATE;
            double valor = 0;
            
            // PAD: acorde grave oscilante
            double padEnv = 0.8 + 0.2 * Math.sin(2 * Math.PI * 0.15 * tiempo);
            for (double nota : notasPad) {
                valor += Math.sin(2 * Math.PI * nota * tiempo) * 0.06 * padEnv;
            }
            
            // ARPEGGIO: notas rapidas con decay
            double tiempoArp = tiempo % 3.0; // ciclo cada 3 segundos
            int indiceArp = (int)(tiempoArp / 0.25) % notasArp.length;
            double posEnNota = (tiempoArp % 0.25) / 0.25;
            double arpEnv = Math.exp(-posEnNota * 6);
            double freqArp = notasArp[indiceArp];
            // Onda triangular suave para el arpegio
            double fase = (freqArp * tiempo) % 1.0;
            double ondaTriangular = 2.0 * Math.abs(2.0 * fase - 1.0) - 1.0;
            valor += ondaTriangular * 0.04 * arpEnv;
            
            // SUB-BASS: pulso grave
            double subCiclo = tiempo % 1.5;
            double subEnv = Math.exp(-subCiclo * 3);
            valor += Math.sin(2 * Math.PI * 55 * tiempo) * 0.1 * subEnv;
            
            // HI-HAT sintetico muy sutil (ruido filtrado)
            double hhCiclo = tiempo % 0.5;
            if (hhCiclo < 0.01) {
                valor += (Math.random() - 0.5) * 0.08 * Math.exp(-hhCiclo * 300);
            }
            
            // Atmosfera: ruido muy bajo con LFO
            double atmosLfo = 0.5 + 0.5 * Math.sin(2 * Math.PI * 0.08 * tiempo);
            valor += (Math.random() - 0.5) * 0.012 * atmosLfo;
            
            // Limitar
            valor = Math.max(-0.9, Math.min(0.9, valor));
            
            short sample = (short) (valor * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return crearWavPCM(audioData, 1);
    }
}
