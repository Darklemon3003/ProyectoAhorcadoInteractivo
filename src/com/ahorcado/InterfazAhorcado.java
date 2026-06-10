package com.ahorcado;

import com.ahorcado.util.GestorEstadisticas;
import com.ahorcado.util.AudioController;
import com.ahorcado.util.GestorEconomia;
import com.ahorcado.util.GestorBugs;
import com.ahorcado.util.GestorFondos;
import com.ahorcado.util.GestorMisiones;
import com.ahorcado.util.FondoJuego;
import com.ahorcado.util.FondoFactory;
import com.ahorcado.util.Bug;
import com.ahorcado.util.BugAvatarFactory;
import java.awt.image.BufferedImage;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * InterfazAhorcado - Ventana principal del juego del Ahorcado.
 * 
 * Esta clase EXTIENDE JFrame (HERENCIA) y es donde se arma toda la interfaz
 * grafica del juego: el teclado virtual, el panel del ahorcado, la consola
 * de mensajes, las estadisticas, el sistema de economia, etc.
 * 
 * Le puse un hilo aqui pa que no se trabe el swing, perdon profe por el desorden
 * pero es que son muchos componentes y ActionListeners que manejar.
 * 
 * NOTA: no tocar la parte de los KeyBindings porque se rompe la interfaz
 * y no se por que, lo deje asi y funciona bien.
 * 
 * Usa varios patrones:
 * - Herencia: extiende JFrame
 * - Eventos: ActionListener en botones, KeyBindings pa el teclado
 * - Hilos: Timer de Swing pa animaciones y temporizadores
 * - Singleton: pa acceder a los gestores (estadisticas, economia, etc)
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Proyecto final POO
 */
public class InterfazAhorcado extends JFrame {

    private static final long serialVersionUID = 1L;

    // === Colores de la interfaz (tematica cyberpunk/hacker) ===
    public static Color FONDO_NEGRO = new Color(0x0A, 0x0E, 0x14);
    public static Color CIAN_NEON = new Color(0x00, 0xFF, 0xFF);
    public static Color CIAN_TENUE = new Color(0x00, 0x88, 0x99, 150);
    public static Color CIAN_GLOW = new Color(0x00, 0xFF, 0xFF, 40);
    public static Color ROJO_ALERTA = new Color(0xFF, 0x44, 0x44);
    public static Color ROJO_OSCURO = new Color(0x44, 0x00, 0x00);
    public static final Color VERDE_SISTEMA = new Color(0x00, 0xFF, 0x88);
    public static final Color VERDE_OSCURO = new Color(0x00, 0x66, 0x33);
    public static final Color GRIS_INACTIVO = new Color(0x3A, 0x3A, 0x42);
    public static final Color AMBAR = new Color(0xFF, 0x88, 0x00);
    public static final Color ROSA_NEON = new Color(0xFF, 0x55, 0xAA); // Para valores de estadÃ­sticas
    public static final Color MAGENTA_NEON = new Color(0xFF, 0x00, 0x88); // Para iconos

    // componentes principales del juego
    private LogicaJuego logica;
    private BancoPalabras bancoPalabras;
    private PanelSujeto panelSujeto;
    private Map<Character, BotonTeclaCyber> mapaTeclas = new HashMap<>();
    
    // componentes de la interfaz grafica
    private JTextPane consolaTransacciones;
    private JTextPane areaDeepThought;
    private JTextArea areaSystemData;
    private JLabel etiquetaTiempo;
    private JLabel etiquetaIntegrity;
    private JLabel etiquetaMute;
    private PanelCajasPalabra panelCajasPalabra;
    private BotonMute botonMute;
    
    // Timers (hilos de Swing pa animaciones y temporizadores)
    private Timer temporizadorSesion;
    private Timer temporizadorFrases;
    private Timer temporizadorSystemData;
    private Timer timerLogoAnimacion; // Hilo animacion logo Kali (breathing/float)
    private int segundosTranscurridos = 0;
    private boolean partidaTerminada = false;
    
    // callback pa volver al menu cuando termina la partida
    private Runnable callbackVolverAlMenu;
    
    // estilos de la consola de mensajes
    private Style estiloRoot;
    private Style estiloSistema;
    private Style estiloConsola;
    private Style estiloExito;
    private Style estiloError;
    private Style estiloInfo;
    private Style estiloFrase;
    
    // audio y estadisticas del juego
    private AudioController audioController;
    private GestorEstadisticas gestorEstadisticas;
    private Random random = new Random();
    
    // flags pa controlar los 3 canales de audio
    private boolean audioTriviaHabilitado = false;
    private boolean audioOperativoHabilitado = false;
    private boolean audioEfectosHabilitado = false;
    
    // panel de letras fallidas y estadisticas
    private JLabel etiquetaLetrasFallidas;
    private JLabel labelStatsJugados;
    private JLabel labelStatsGanados;
    private JLabel labelStatsPorcentaje;
    private JLabel labelStatsRachaActual;
    private JLabel labelStatsRachaMaxima;
    private JLabel labelStatsRango;
    private JLabel labelStatsNivel;
    private JLabel labelStatsXP;
    private JLabel labelStatsPrecision;
    // sistema de economia virtual y coleccionables
    private GestorEconomia gestorEconomia;
    private GestorBugs gestorBugs;
    private JLabel labelKKSaldo;
    private JLabel labelKKDelta; // pequena animacion +N / -N
    private GestorEconomia.SaldoListener saldoListener;
    private int ultimaKKGanada = 0;
    private String dificultadActual = "NORMAL";
    private String aliasOperador = "GUEST";
    // Sistema de fondos/wallpapers
    private GestorFondos gestorFondos;
    private PanelFondoAnimado panelFondo;
    private GestorFondos.FondoListener fondoListener;
    private int letrasCorrectasPartida = 0;
    private int letrasIntentadasPartida = 0;
    private int ultimaXPGanada = 0;

    // Efecto de tipeo consola
    private java.util.Queue<java.util.Map.Entry<String, Style>> colaMensajes = new java.util.LinkedList<>();
    private Timer timerTipeo;
    private String mensajeActual = null;
    private Style estiloActual = null;
    
    // Overlay cyberpunk (glitch popups DedSec-style)
    private PanelCyberOverlay panelCyberOverlay;
    private Timer overlayTimer;

    public InterfazAhorcado(LogicaJuego logica, BancoPalabras banco, boolean trivias, boolean feedback, boolean sfx, String alias) {
        this(logica, banco, trivias, feedback, sfx, alias, "NORMAL");
    }

    public InterfazAhorcado(LogicaJuego logica, BancoPalabras banco, boolean trivias, boolean feedback, boolean sfx, String alias, String dificultad) {
        this.logica = logica;
        this.bancoPalabras = banco != null ? banco : new BancoPalabras();
        
        this.audioEfectosHabilitado = sfx;
        this.audioOperativoHabilitado = feedback;
        this.audioTriviaHabilitado = trivias;
        this.aliasOperador = (alias == null || alias.isBlank()) ? "GUEST" : alias.trim();
        this.dificultadActual = (dificultad == null || dificultad.isBlank()) ? "NORMAL" : dificultad.trim().toUpperCase();
        
        setTitle("AHORCADO CYBER-DECODER [NEXUS-7] - OP: " + this.aliasOperador.toUpperCase());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        
        // WindowListener para asegurar limpieza completa al cerrar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (audioController != null) {
                    audioController.detenerBgmGame();
                    audioController.stopAllSounds();
                    audioController.reactivarAudio();
                }
                // Si hay callback, volver al menu; sino salir
                if (callbackVolverAlMenu != null) {
                    callbackVolverAlMenu.run();
                } else {
                    dispose();
                    System.exit(0);
                }
            }
        });
        
        getContentPane().setBackground(FONDO_NEGRO);
        getContentPane().setLayout(new BorderLayout(0, 0));

        this.audioController = AudioController.getInstance();
        this.gestorEstadisticas = GestorEstadisticas.getInstancia();
        this.gestorEstadisticas.setAlias(this.aliasOperador);
        this.gestorEstadisticas.guardarEstadisticas();
        this.gestorEconomia = GestorEconomia.getInstancia();
        this.gestorBugs = GestorBugs.getInstancia();
        this.gestorFondos = GestorFondos.getInstancia();
        
        inicializarComponentes();
        configurarMenuPausa();
        configurarKeyBindings(); // KeyBindings en lugar de KeyListener
        
        pack();
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
        
        actualizarVista();
        escribirEnConsola("[ROOT]> Terminal Nexus-7 v8.0 iniciada", estiloRoot);
        escribirEnConsola("[KERNEL]> Cargando modulos de descifrado...", estiloSistema);
        escribirEnConsola("[NET]> Conexion cifrada establecida: AES-256-GCM", estiloSistema);
        escribirEnConsola("[AUTH]> Operador " + aliasOperador.toUpperCase() + " autenticado | Nivel: " + (gestorEstadisticas != null ? gestorEstadisticas.getNivel() : 1) + " | Rango: " + (gestorEstadisticas != null ? gestorEstadisticas.getRango() : "RECLUTA"), estiloExito);
        escribirEnConsola("[FIREWALL]> Reglas de acceso actualizadas [" + dificultadActual + "]", estiloSistema);
        escribirEnConsola("[SISTEMA]> Protocolo de Voz: [Trivia:" + (audioTriviaHabilitado ? "ON" : "OFF") + 
            "] [Operativo:" + (audioOperativoHabilitado ? "ON" : "OFF") + 
            "] [Efectos:" + (audioEfectosHabilitado ? "ON" : "OFF") + "]", estiloSistema);
        escribirEnConsola("[SCAN]> Hash de sesion: 0x" + Long.toHexString(System.currentTimeMillis()).toUpperCase(), estiloConsola);
        escribirEnConsola("[CONSOLA]> Deep-Thought Analyzer activo. Esperando input...", estiloConsola);
        
        // === SINCRONIZACION AUDIO ===
        // Reactivar audio global para la nueva partida (pudo quedar desactivado por stopAllSounds)
        audioController.reactivarAudio();
        // Sincronizar boton de mute de trivia con estado global
        if (botonMute != null) {
            botonMute.setMuted(!audioTriviaHabilitado);
        }
        
        // Detener musica de menu (transicion limpia) y lanzar BGM de juego
        audioController.detenerMusicaMenu();
        audioController.reproducirBgmGame();
        
        // Primera frase con voz
        agregarFraseDeepThought();
    }
    
    /**
     * Establece el callback para volver al menu principal.
     * Si se establece, se usara en lugar de crear una nueva PantallaArranque.
     */
    public void setOnVolverAlMenu(Runnable callback) {
        this.callbackVolverAlMenu = callback;
    }
    
    /**
     * Obtiene el estado del canal de feedback/operativo.
     */
    public boolean isAudioOperativoHabilitado() {
        return audioOperativoHabilitado;
    }
    
    /**
     * Establece el estado del canal de feedback/operativo.
     */
    public void setAudioOperativoHabilitado(boolean habilitado) {
        this.audioOperativoHabilitado = habilitado;
        actualizarBannerAudio();
    }
    
    private JDialog dialogoPausa;

    private void configurarMenuPausa() {
        if (dialogoPausa != null) dialogoPausa.dispose();
        dialogoPausa = new JDialog(this, "PAUSA", true);
        dialogoPausa.setUndecorated(true);
        dialogoPausa.setSize(400, 450);
        dialogoPausa.setLocationRelativeTo(this);
        
        JPanel panelPausa = new JPanel();
        panelPausa.setLayout(new BoxLayout(panelPausa, BoxLayout.Y_AXIS));
        panelPausa.setBackground(Color.BLACK);
        panelPausa.setBorder(new LineBorder(new Color(0x00, 0xFF, 0x00), 2));
        
        JLabel tituloPausa = new JLabel("[ SISTEMA EN PAUSA ]");
        tituloPausa.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        tituloPausa.setForeground(new Color(0x00, 0xFF, 0x00));
        tituloPausa.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton btnReanudar = crearBotonPausa("REANUDAR CONEXION", new Color(0x00, 0xFF, 0x00));
        btnReanudar.addActionListener(e -> setPausado(false));
        
        // Panel de opciones de audio con 3 canales
        JPanel panelAudio = new JPanel();
        panelAudio.setLayout(new BoxLayout(panelAudio, BoxLayout.Y_AXIS));
        panelAudio.setBackground(Color.BLACK);
        panelAudio.setBorder(new LineBorder(AMBAR, 1));
        panelAudio.setMaximumSize(new Dimension(350, 120));
        
        JLabel tituloAudio = new JLabel("[ CONFIGURACION DE AUDIO ]");
        tituloAudio.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        tituloAudio.setForeground(AMBAR);
        tituloAudio.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelAudio.add(tituloAudio);
        panelAudio.add(Box.createVerticalStrut(10));
        
        // NOTA: Canal Trivia se controla desde el banner [PROTOCOLO DE VOZ] in-game
        
        JCheckBox cbOperativo = new JCheckBox("Canal Operativo/Feedback", audioOperativoHabilitado);
        cbOperativo.setForeground(CIAN_NEON);
        cbOperativo.setBackground(Color.BLACK);
        cbOperativo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        cbOperativo.setFocusPainted(false);
        cbOperativo.setAlignmentX(Component.CENTER_ALIGNMENT);
        cbOperativo.addActionListener(e -> {
            audioOperativoHabilitado = cbOperativo.isSelected();
            actualizarBannerAudio();
        });
        panelAudio.add(cbOperativo);
        panelAudio.add(Box.createVerticalStrut(5));
        
        JCheckBox cbEfectos = new JCheckBox("Canal Efectos/SFX", audioEfectosHabilitado);
        cbEfectos.setForeground(CIAN_NEON);
        cbEfectos.setBackground(Color.BLACK);
        cbEfectos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        cbEfectos.setFocusPainted(false);
        cbEfectos.setAlignmentX(Component.CENTER_ALIGNMENT);
        cbEfectos.addActionListener(e -> {
            audioEfectosHabilitado = cbEfectos.isSelected();
            actualizarBannerAudio();
        });
        panelAudio.add(cbEfectos);
        
        JButton btnDesconectar = crearBotonPausa("VOLVER AL MENU PRINCIPAL", ROJO_ALERTA);
        btnDesconectar.addActionListener(e -> {
            dialogoPausa.dispose();
            if (audioController != null) {
                audioController.detenerBgmGame();
                audioController.stopAllSounds();
                audioController.reactivarAudio();
            }
            // Usar callback si existe, sino crear nueva PantallaArranque
            if (callbackVolverAlMenu != null) {
                callbackVolverAlMenu.run();
            } else {
                dispose();
                new GlitchScreen().setVisible(true);
            }
        });
        
        panelPausa.add(Box.createVerticalGlue());
        panelPausa.add(tituloPausa);
        panelPausa.add(Box.createVerticalStrut(30));
        panelPausa.add(btnReanudar);
        panelPausa.add(Box.createVerticalStrut(20));
        panelPausa.add(panelAudio);
        panelPausa.add(Box.createVerticalStrut(20));
        panelPausa.add(btnDesconectar);
        panelPausa.add(Box.createVerticalGlue());
        
        dialogoPausa.getContentPane().add(panelPausa);
    }
    
    private void setPausado(boolean pausado) {
        if (partidaTerminada) return;
        if (pausado) {
            if (temporizadorSesion != null) temporizadorSesion.stop();
            // Reconstruir menu de pausa para reflejar estado actual de audio
            configurarMenuPausa();
            // Stop all sounds when paused
            if (audioController != null) {
                audioController.stopAllSounds();
            }
            // [CRÃTICO] Actualizar banner para reflejar estado de audio
            actualizarBannerAudio();
            dialogoPausa.setVisible(true);
        } else {
            dialogoPausa.setVisible(false);
            if (temporizadorSesion != null) temporizadorSesion.start();
            // Resume sounds when unpaused (only if audio was enabled)
            if (audioController != null) {
                audioController.reactivarAudio();
            }
            // Resume background effects if they were enabled
            if (audioEfectosHabilitado && audioController != null) {
                audioController.reproducirTecleo();
            }
            // [CRÃTICO] Actualizar banner para reflejar estado de audio
            actualizarBannerAudio();
        }
    }
    
    private JButton crearBotonPausa(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setForeground(color);
        btn.setBackground(new Color(0x05, 0x1A, 0x1E));
        btn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(color, 2));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(350, 45));
        return btn;
    }

    public InterfazAhorcado(LogicaJuego logica) {
        this(logica, new BancoPalabras(), true, true, true, "GUEST");
    }

    @Override
    public void dispose() {
        // Detener todos los timers para evitar leaks de hilos
        if (temporizadorSesion != null)    { temporizadorSesion.stop();    temporizadorSesion = null; }
        if (temporizadorFrases != null)    { temporizadorFrases.stop();    temporizadorFrases = null; }
        if (temporizadorSystemData != null){ temporizadorSystemData.stop(); temporizadorSystemData = null; }
        if (timerTipeo != null)            { timerTipeo.stop();            timerTipeo = null; }
        if (timerLogoAnimacion != null)    { timerLogoAnimacion.stop();    timerLogoAnimacion = null; }
        if (overlayTimer != null)          { overlayTimer.stop();          overlayTimer = null; }
        if (panelCyberOverlay != null)     { panelCyberOverlay.detener(); panelCyberOverlay = null; }
        if (panelFondo != null)            { panelFondo.detener(); panelFondo = null; }
        if (gestorFondos != null && fondoListener != null) {
            gestorFondos.removeListener(fondoListener);
            fondoListener = null;
        }
        // Detener timer de animacion del sujeto
        if (panelSujeto != null)           { panelSujeto.detenerTimer(); panelSujeto = null; }
        // Remover listener del singleton GestorEconomia
        if (gestorEconomia != null && saldoListener != null) {
            gestorEconomia.removeListener(saldoListener);
            saldoListener = null;
        }
        // Detener audio (incluido BGM de juego)
        if (audioController != null) {
            audioController.detenerBgmGame();
            audioController.stopAllSounds();
        }
        if (dialogoPausa != null) {
            dialogoPausa.dispose();
            dialogoPausa = null;
        }
        super.dispose();
    }

    private void inicializarComponentes() {
        JLayeredPane capaPrincipal = new JLayeredPane();
        capaPrincipal.setPreferredSize(new Dimension(1200, 800));
        capaPrincipal.setOpaque(false);
        
        JPanel contenedorBase = new JPanel(new BorderLayout(0, 0));
        contenedorBase.setBounds(0, 0, 1200, 800);
        contenedorBase.setBackground(FONDO_NEGRO);
        
        contenedorBase.add(crearPanelSuperior(), BorderLayout.NORTH);
        contenedorBase.add(crearPanelDeepThought(), BorderLayout.WEST); // Nuevo panel
        contenedorBase.add(crearPanelSystemData(), BorderLayout.EAST);
        contenedorBase.add(crearPanelCentral(), BorderLayout.CENTER);
        
        PanelScanlines capaScanlines = new PanelScanlines();
        capaScanlines.setBounds(0, 0, 1200, 800);
        capaScanlines.setOpaque(false);
        
        // Overlay cyberpunk con glitch popups estilo DedSec
        panelCyberOverlay = new PanelCyberOverlay();
        panelCyberOverlay.setBounds(0, 0, 1200, 800);
        panelCyberOverlay.setOpaque(false);
        panelCyberOverlay.setVisible(true);
        panelCyberOverlay.setEnabled(false);
        
        // Capa de fondo animado (detras de todo)
        panelFondo = new PanelFondoAnimado();
        panelFondo.setBounds(0, 0, 1200, 800);
        panelFondo.setOpaque(true);

        // Hacer el contenedorBase semi-transparente para que se vea el fondo
        contenedorBase.setOpaque(false);
        contenedorBase.setBackground(new Color(0, 0, 0, 0));

        capaPrincipal.add(panelFondo, JLayeredPane.FRAME_CONTENT_LAYER);
        capaPrincipal.add(contenedorBase, JLayeredPane.DEFAULT_LAYER);
        capaPrincipal.add(capaScanlines, JLayeredPane.PALETTE_LAYER);
        capaPrincipal.add(panelCyberOverlay, JLayeredPane.DRAG_LAYER);
        capaPrincipal.moveToFront(panelCyberOverlay);

        // Listener para cambio de fondo desde la tienda
        fondoListener = nuevo -> {
            if (panelFondo != null) panelFondo.repaint();
        };
        gestorFondos.addListener(fondoListener);

        // Redimensionar todas las capas cuando el frame cambia de tamano
        final PanelScanlines scanRef = capaScanlines;
        capaPrincipal.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = capaPrincipal.getWidth();
                int h = capaPrincipal.getHeight();
                panelFondo.setBounds(0, 0, w, h);
                contenedorBase.setBounds(0, 0, w, h);
                scanRef.setBounds(0, 0, w, h);
                panelCyberOverlay.setBounds(0, 0, w, h);
                panelCyberOverlay.revalidate();
                panelCyberOverlay.repaint();
                capaPrincipal.moveToFront(panelCyberOverlay);
            }
        });

        // Listener global: capturar posicion del mouse para ocultar bugs individualmente
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof java.awt.event.MouseEvent) {
                java.awt.event.MouseEvent me = (java.awt.event.MouseEvent) event;
                if (me.getID() == java.awt.event.MouseEvent.MOUSE_MOVED ||
                    me.getID() == java.awt.event.MouseEvent.MOUSE_DRAGGED) {
                    // Solo capturar si el evento proviene de esta ventana
                    if (javax.swing.SwingUtilities.getWindowAncestor(me.getComponent()) == InterfazAhorcado.this) {
                        if (panelCyberOverlay != null) {
                            // Convertir coordenadas del componente a coordenadas del overlay
                            java.awt.Point compLoc = me.getComponent().getLocationOnScreen();
                            java.awt.Point overlayLoc = panelCyberOverlay.getLocationOnScreen();
                            int relX = compLoc.x + me.getX() - overlayLoc.x;
                            int relY = compLoc.y + me.getY() - overlayLoc.y;
                            panelCyberOverlay.actualizarPosicionMouse(relX, relY);
                        }
                    }
                }
            }
        }, java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK);

        getContentPane().add(capaPrincipal, BorderLayout.CENTER);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        // Panel izquierdo con LOGO KALI ANIMADO + OPERADOR_ACTIVO
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.X_AXIS));
        panelIzquierdo.setOpaque(false);
        
        // Logo Kali Linux animado (breathing + float)
        PanelKaliLogoAnimado panelLogo = new PanelKaliLogoAnimado();
        panelLogo.setPreferredSize(new Dimension(52, 52));
        panelLogo.setMaximumSize(new Dimension(52, 52));
        panelIzquierdo.add(panelLogo);
        panelIzquierdo.add(Box.createHorizontalStrut(10));
        
        // Columna con operador
        JPanel colOperador = new JPanel();
        colOperador.setLayout(new BoxLayout(colOperador, BoxLayout.Y_AXIS));
        colOperador.setOpaque(false);
        
        JLabel etiquetaSistema = new JLabel("[ KALI LINUX SYSTEM ]");
        etiquetaSistema.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        etiquetaSistema.setForeground(new Color(0x5B, 0x2A, 0x86));
        etiquetaSistema.setAlignmentX(Component.LEFT_ALIGNMENT);
        colOperador.add(etiquetaSistema);
        colOperador.add(Box.createVerticalStrut(2));
        
        String nombreOperador = this.getTitle().substring(this.getTitle().lastIndexOf("OP: ") + 4);
        JLabel etiquetaOperador = new JLabel("OPERADOR_ACTIVO: " + nombreOperador);
        etiquetaOperador.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        etiquetaOperador.setForeground(new Color(0x00, 0xFF, 0x88));
        etiquetaOperador.setAlignmentX(Component.LEFT_ALIGNMENT);
        colOperador.add(etiquetaOperador);
        
        panelIzquierdo.add(colOperador);
        panel.add(panelIzquierdo, BorderLayout.WEST);
        
        // Banner de ciberseguridad con Root
        JPanel panelBanner = new JPanel(new BorderLayout());
        panelBanner.setOpaque(false);
        
        JLabel etiquetaRoot = new JLabel("ROOT", SwingConstants.CENTER);
        etiquetaRoot.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        etiquetaRoot.setForeground(new Color(0x00, 0xFF, 0x88));
        etiquetaRoot.setBorder(new EmptyBorder(5, 0, 5, 0));
        panelBanner.add(etiquetaRoot, BorderLayout.NORTH);
        
        JLabel etiquetaSubBanner = new JLabel("[ SYSTEM ACCESS GRANTED ]", SwingConstants.CENTER);
        etiquetaSubBanner.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        etiquetaSubBanner.setForeground(new Color(0x00, 0x88, 0xFF));
        etiquetaSubBanner.setBorder(new EmptyBorder(0, 0, 5, 0));
        panelBanner.add(etiquetaSubBanner, BorderLayout.CENTER);
        
        panel.add(panelBanner, BorderLayout.CENTER);
        
        // Panel de informacion derecha
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);
        
        // Saldo Kali Koints (esquina superior)
        labelKKSaldo = new JLabel("\u00A4 KK " + gestorEconomia.getSaldo());
        labelKKSaldo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        labelKKSaldo.setForeground(new Color(0xFF, 0xD7, 0x00));
        labelKKSaldo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panelInfo.add(labelKKSaldo);

        labelKKDelta = new JLabel("");
        labelKKDelta.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        labelKKDelta.setForeground(VERDE_SISTEMA);
        labelKKDelta.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panelInfo.add(labelKKDelta);
        
        // Boton de pausa junto a Kali Koints
        BotonCyber botonPausa = new BotonCyber("PAUSA", AMBAR);
        botonPausa.setAlignmentX(Component.RIGHT_ALIGNMENT);
        botonPausa.setMaximumSize(new Dimension(90, 30));
        botonPausa.addActionListener(e -> setPausado(true));
        panelInfo.add(botonPausa);
        panelInfo.add(Box.createVerticalStrut(3));

        // Registrar listener de saldo
        saldoListener = (anterior, nuevo, delta) -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                labelKKSaldo.setText("\u00A4 KK " + nuevo);
                if (delta != 0) {
                    String txt = (delta > 0 ? "+" : "") + delta;
                    labelKKDelta.setText(txt);
                    labelKKDelta.setForeground(delta > 0 ? VERDE_SISTEMA : ROJO_ALERTA);
                    Timer fadeTimer = new Timer(80, null);
                    final int[] step = {0};
                    fadeTimer.addActionListener(ev -> {
                        step[0]++;
                        if (step[0] > 20) {
                            labelKKDelta.setText("");
                            fadeTimer.stop();
                        } else if (step[0] > 14) {
                            labelKKDelta.setForeground(new Color(
                                labelKKDelta.getForeground().getRed(),
                                labelKKDelta.getForeground().getGreen(),
                                labelKKDelta.getForeground().getBlue(),
                                Math.max(0, 255 - (step[0] - 14) * 42)));
                        }
                    });
                    fadeTimer.start();
                }
            });
        };
        gestorEconomia.addListener(saldoListener);

        etiquetaIntegrity = new JLabel("[INTEGRIDAD: 100%]");
        etiquetaIntegrity.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        etiquetaIntegrity.setForeground(CIAN_TENUE);
        etiquetaIntegrity.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panelInfo.add(etiquetaIntegrity);
        
        etiquetaTiempo = new JLabel("[TIEMPO: 00:00]");
        etiquetaTiempo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        etiquetaTiempo.setForeground(CIAN_NEON);
        etiquetaTiempo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panelInfo.add(Box.createVerticalStrut(4));
        panelInfo.add(etiquetaTiempo);
        
        // NOTA: Banner de audio eliminado - se controla desde PROTOCOLO DE VOZ
        
        panel.add(panelInfo, BorderLayout.EAST);
        
        temporizadorSesion = new Timer(1000, e -> actualizarTemporizador());
        temporizadorSesion.start();
        
        return panel;
    }

    /**
     * Panel DEEP-THOUGHT ANALYZER - Reemplaza el panel de codigo fuente.
     * Muestra frases con voz sintetica.
     */
    private JPanel crearPanelDeepThought() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CIAN_TENUE, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Header con control de mute
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CIAN_TENUE));
        
        JLabel titulo = new JLabel("[PROTOCOLO DE VOZ]", SwingConstants.LEFT);
        titulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        titulo.setForeground(CIAN_NEON);
        headerPanel.add(titulo, BorderLayout.WEST);
        
        // Boton Mute/Unmute
        botonMute = new BotonMute();
        botonMute.addActionListener(e -> toggleMute());
        headerPanel.add(botonMute, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Area de frases Deep-Thought
        areaDeepThought = new JTextPane();
        areaDeepThought.setEditable(false);
        areaDeepThought.setOpaque(false);
        areaDeepThought.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        areaDeepThought.setBorder(new EmptyBorder(6, 6, 6, 6));
        
        inicializarEstiloFrases();
        
        JScrollPane scroll = new JScrollPane(areaDeepThought);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Timer para rotacion de frases (cada 12 segundos)
        temporizadorFrases = new Timer(12000, e -> agregarFraseDeepThought());
        temporizadorFrases.start();
        
        return panel;
    }

    private void inicializarEstiloFrases() {
        StyleContext ctx = StyleContext.getDefaultStyleContext();
        
        estiloFrase = ctx.addStyle("frase", null);
        StyleConstants.setForeground(estiloFrase, new Color(0x00, 0xDD, 0xEE));
        StyleConstants.setFontFamily(estiloFrase, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloFrase, 10);
    }

    private void agregarFraseDeepThought() {
        String frase = BaseFrases.obtenerFraseAleatoria();
        
        try {
            StyledDocument doc = areaDeepThought.getStyledDocument();
            
            // Limpiar si hay demasiado contenido
            if (doc.getLength() > 1000) {
                doc.remove(0, doc.getLength() / 2);
            }
            
            if (doc.getLength() > 0) {
                doc.insertString(doc.getLength(), "\n\n", estiloFrase);
            }
            
            // Prefijo de categoria
            String prefijo = "> ";
            doc.insertString(doc.getLength(), prefijo + frase, estiloFrase);
            areaDeepThought.setCaretPosition(doc.getLength());
            
            // Hablar la frase con voz sintetica (Canal Trivia/Humor)
            if (audioTriviaHabilitado) audioController.hablarFrase(frase);
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void toggleMute() {
        // Aislado: Solo controla el MotorVoz (TTS) y la flag de trivia.
        // NO afecta BGM, SFX ni feedback operativo.
        boolean nuevoEstado = !audioController.isVozSilenciada();
        audioController.setVozSilenciada(nuevoEstado);
        audioTriviaHabilitado = !nuevoEstado; // sincronizar flag local
        
        botonMute.setMuted(nuevoEstado);
        actualizarBannerAudio();
        
        escribirEnConsola("[SISTEMA]> Protocolo de Voz " + (nuevoEstado ? "desactivado" : "activado"), estiloSistema);
    }
    
    /**
     * [CRÃTICO] Actualiza el banner de audio para reflejar el estado real.
     * Debe llamarse siempre que se detenga/reactive el audio.
     */
    private void actualizarBannerAudio() {
        // No-op: banner de audio eliminado, se controla desde PROTOCOLO DE VOZ
    }

    private JPanel crearPanelSystemData() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(300, Short.MAX_VALUE));
        panel.setBorder(new EmptyBorder(4, 12, 10, 12));
        
        // === PANEL SYSTEM DATA ===
        JPanel panelSysData = new JPanel(new BorderLayout(0, 4));
        panelSysData.setOpaque(false);
        panelSysData.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x00, 0x66, 0x77), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel tituloSys = new JLabel("[ DATOS DEL SISTEMA ]", SwingConstants.LEFT);
        tituloSys.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        tituloSys.setForeground(CIAN_NEON);
        tituloSys.setBorder(new EmptyBorder(0, 0, 4, 0));
        panelSysData.add(tituloSys, BorderLayout.NORTH);
        
        areaSystemData = new JTextArea();
        areaSystemData.setEditable(false);
        areaSystemData.setOpaque(false);
        areaSystemData.setForeground(new Color(0x00, 0xBB, 0xCC));
        areaSystemData.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        areaSystemData.setBorder(null);
        
        JScrollPane scrollSys = new JScrollPane(areaSystemData);
        scrollSys.setBorder(null);
        scrollSys.setOpaque(false);
        scrollSys.getViewport().setOpaque(false);
        scrollSys.setPreferredSize(new Dimension(0, 100));
        panelSysData.add(scrollSys, BorderLayout.CENTER);
        panel.add(panelSysData, BorderLayout.NORTH);

        // === CONTENEDOR INFERIOR: Log + Estadisticas + Reinicio ===
        JPanel panelInferiorDerecho = new JPanel();
        panelInferiorDerecho.setLayout(new BoxLayout(panelInferiorDerecho, BoxLayout.Y_AXIS));
        panelInferiorDerecho.setOpaque(false);
        panelInferiorDerecho.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        
        // === PANEL REGISTRO / LOG ===
        JPanel panelTransLog = new JPanel(new BorderLayout(0, 4));
        panelTransLog.setOpaque(false);
        panelTransLog.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x00, 0x66, 0x77), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panelTransLog.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        
        JLabel tituloLog = new JLabel("[ REGISTRO DE ACCIONES ]", SwingConstants.LEFT);
        tituloLog.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        tituloLog.setForeground(CIAN_NEON);
        tituloLog.setBorder(new EmptyBorder(0, 0, 4, 0));
        panelTransLog.add(tituloLog, BorderLayout.NORTH);
        
        consolaTransacciones = new JTextPane();
        consolaTransacciones.setEditable(false);
        consolaTransacciones.setOpaque(false);
        consolaTransacciones.setForeground(CIAN_NEON);
        consolaTransacciones.setCaretColor(CIAN_NEON);
        consolaTransacciones.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollLog = new JScrollPane(consolaTransacciones);
        scrollLog.setBorder(null);
        scrollLog.setOpaque(false);
        scrollLog.getViewport().setOpaque(false);
        scrollLog.setPreferredSize(new Dimension(0, 300));
        scrollLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollLog.setMinimumSize(new Dimension(0, 200));
        scrollLog.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        panelTransLog.add(scrollLog, BorderLayout.CENTER);
        
        panelInferiorDerecho.add(panelTransLog);
        panelInferiorDerecho.add(Box.createVerticalStrut(10));

        // === PANEL ESTADISTICAS ===
        JPanel panelEstadisticas = new JPanel(new BorderLayout(0, 6));
        panelEstadisticas.setOpaque(false);
        panelEstadisticas.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xCC, 0x77, 0x00), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel tituloStats = new JLabel("[ ESTADISTICAS / USER DATA ]", SwingConstants.LEFT);
        tituloStats.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        tituloStats.setForeground(AMBAR);
        tituloStats.setBorder(new EmptyBorder(0, 0, 6, 0));
        panelEstadisticas.add(tituloStats, BorderLayout.NORTH);
        
        JPanel panelStatsIconos = new JPanel(new GridLayout(9, 1, 0, 2));
        panelStatsIconos.setBackground(FONDO_NEGRO);
        
        labelStatsJugados = crearLabelStat("\uD83C\uDFAE", "JUGADOS", "0");
        labelStatsGanados = crearLabelStat("\uD83C\uDFC6", "GANADOS", "0");
        labelStatsPorcentaje = crearLabelStat("\uD83D\uDCC8", "PORCENTAJE", "0%");
        labelStatsRachaActual = crearLabelStat("\uD83D\uDD25", "RACHA ACTUAL", "0");
        labelStatsRachaMaxima = crearLabelStat("\u2B06", "RACHA MAXIMA", "0");
        labelStatsRango = crearLabelStat("\u2694", "RANGO CLASIFICACION", "RECLUTA DIGITAL");
        labelStatsNivel = crearLabelStat("LV", "NIVEL", "1");
        labelStatsXP = crearLabelStat("XP", "EXPERIENCIA", "0/250");
        labelStatsPrecision = crearLabelStat("%", "PRECISION", "0%");
        
        panelStatsIconos.add(labelStatsRango);
        panelStatsIconos.add(labelStatsNivel);
        panelStatsIconos.add(labelStatsXP);
        panelStatsIconos.add(labelStatsJugados);
        panelStatsIconos.add(labelStatsGanados);
        panelStatsIconos.add(labelStatsPorcentaje);
        panelStatsIconos.add(labelStatsPrecision);
        panelStatsIconos.add(labelStatsRachaActual);
        panelStatsIconos.add(labelStatsRachaMaxima);
        
        panelEstadisticas.add(panelStatsIconos, BorderLayout.CENTER);
        
        JScrollPane scrollStats = new JScrollPane(panelEstadisticas);
        scrollStats.setBorder(null);
        scrollStats.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollStats.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollStats.setPreferredSize(new Dimension(0, 220));
        scrollStats.setMinimumSize(new Dimension(0, 180));
        scrollStats.getViewport().setBackground(FONDO_NEGRO);
        panelInferiorDerecho.add(scrollStats);
        panelInferiorDerecho.add(Box.createVerticalStrut(5));
        
        panel.add(panelInferiorDerecho, BorderLayout.CENTER);
        
        inicializarEstilosConsola();
        
        temporizadorSystemData = new Timer(2000, e -> actualizarSystemData());
        temporizadorSystemData.start();
        actualizarSystemData();
        
        return panel;
    }

    private void actualizarSystemData() {
        if (partidaTerminada) return;
        
        int memUsada = 6800 + random.nextInt(200);
        int integrity = Math.max(0, 100 - (logica != null ? (int)(logica.obtenerIntentosFallidos() * 14.28) : 0));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Variables del Sistema:\n");
        sb.append(String.format("  Raices:                 FG%04X\n", random.nextInt(65536)));
        sb.append(String.format("  Uso de Memoria:         %d.%02dMB\n", memUsada / 100, memUsada % 100));
        sb.append(String.format("  Integridad del Nucleo:  %d%%\n", integrity));
        sb.append(String.format("  Hilos Activos:          %d\n", 8 + random.nextInt(5)));
        sb.append(String.format("  Aciertos Cache:         %d\n", 1024 + random.nextInt(512)));
        sb.append(String.format("  Latencia de Red:        %dms\n", 12 + random.nextInt(20)));
        sb.append("  Estado Memoria:         En Linea");
        
        areaSystemData.setText(sb.toString());
        etiquetaIntegrity.setText(String.format("[INTEGRIDAD: %d%%]", integrity));
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panelSujeto = new PanelSujeto();
        panelSujeto.setPreferredSize(new Dimension(400, 520));
        panelSujeto.establecerLogica(this.logica);
        panelSujeto.setOnDerrotaCallback(() -> {
             Timer delay = new Timer(1000, e -> verificarFinJuego());
             delay.setRepeats(false);
             delay.start();
        });
        panel.add(panelSujeto, BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setOpaque(false);
        
        panelCajasPalabra = new PanelCajasPalabra();
        panelCajasPalabra.setPreferredSize(new Dimension(400, 60));
        panelCajasPalabra.setMaximumSize(new Dimension(600, 60));
        panelInferior.add(panelCajasPalabra);
        panelInferior.add(Box.createVerticalStrut(8));
        
        JLabel labelTask = new JLabel(">> Tarea actual: Identificar siguiente caracter en la secuencia");
        labelTask.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        labelTask.setForeground(CIAN_TENUE);
        labelTask.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelInferior.add(labelTask);
        panelInferior.add(Box.createVerticalStrut(6));

        // Letras fallidas
        etiquetaLetrasFallidas = new JLabel("LETRAS FALLIDAS: []");
        etiquetaLetrasFallidas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        etiquetaLetrasFallidas.setForeground(new Color(0xFF, 0x44, 0x44, 180));
        etiquetaLetrasFallidas.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelInferior.add(etiquetaLetrasFallidas);
        panelInferior.add(Box.createVerticalStrut(10));
        
        panelInferior.add(crearPanelTecladoCyber());
        panelInferior.add(Box.createVerticalStrut(10));
        panelInferior.add(crearPanelControles());
        
        panel.add(panelInferior, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JPanel panelHints = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        panelHints.setOpaque(false);
        
        BotonCyber botonHintCat = new BotonCyber("[PISTA: CATEGORIA]", CIAN_NEON);
        botonHintCat.addActionListener(e -> ejecutarPista(new PistaCategoria()));
        panelHints.add(botonHintCat);

        BotonCyber botonHintLet = new BotonCyber("[PISTA: LETRA]", CIAN_NEON);
        botonHintLet.addActionListener(e -> ejecutarPista(new PistaLetraAleatoria(this.logica)));
        panelHints.add(botonHintLet);

        BotonCyber botonHintDesc = new BotonCyber("[PISTA: DESCRIPCION]", CIAN_NEON);
        botonHintDesc.addActionListener(e -> ejecutarPista(new PistaTexto()));
        panelHints.add(botonHintDesc);
        
        panel.add(panelHints);
        
        return panel;
    }

    private JPanel crearPanelTecladoCyber() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setOpaque(false);
        
        JLabel titulo = new JLabel("MATRIZ DECODIFICADORA", SwingConstants.CENTER);
        titulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        titulo.setForeground(CIAN_NEON);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedor.add(titulo);
        contenedor.add(Box.createVerticalStrut(8));
        
        String[] filas = {"QWERTYUIOP", "ASDFGHJKL\u00D1", "ZXCVBNM"};
        for (String fila : filas) {
            JPanel filaPanel = new JPanel();
            filaPanel.setLayout(new BoxLayout(filaPanel, BoxLayout.X_AXIS));
            filaPanel.setOpaque(false);
            
            for (int i = 0; i < fila.length(); i++) {
                char letra = Character.toUpperCase(fila.charAt(i));
                char letraMinus = Character.toLowerCase(letra);
                BotonTeclaCyber boton = new BotonTeclaCyber(String.valueOf(letra));
                mapaTeclas.put(letraMinus, boton);
                filaPanel.add(boton);
                if (i < fila.length() - 1) filaPanel.add(Box.createHorizontalStrut(5));
            }
            filaPanel.add(Box.createHorizontalGlue());
            contenedor.add(filaPanel);
            contenedor.add(Box.createVerticalStrut(5));
        }
        return contenedor;
    }

    /**
     * KEYBINDINGS - Blindaje del teclado fisico.
     * WHEN_IN_FOCUSED_WINDOW garantiza captura global.
     * Sincronizado con doClick() de botones virtuales.
     */
    private void configurarKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        // Mapear todas las letras A-Z y \u00D1
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00D1";
        for (char letra : letras.toCharArray()) {
            final char letraMinus = Character.toLowerCase(letra);
            KeyStroke keyStroke = KeyStroke.getKeyStroke(letra, 0);
            String actionName = "key_" + letraMinus;
            
            inputMap.put(keyStroke, actionName);
            actionMap.put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    procesarTeclaFisica(letraMinus);
                }
            });
        }
        
        // Escape key para Pausa
        KeyStroke escapeKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        inputMap.put(escapeKey, "pause_action");
        actionMap.put("pause_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialogoPausa != null) {
                    setPausado(!dialogoPausa.isVisible());
                }
            }
        });
        
        setFocusable(true);
    }

    private void procesarTeclaFisica(char letra) {
        if (partidaTerminada) return;
        
        BotonTeclaCyber boton = mapaTeclas.get(letra);
        if (boton != null && boton.isEnabled()) {
            // Sincronizar con doClick() para mantener coherencia visual
            boton.doClick();
            audioController.hablarClick();
        }
    }

    /**
     * Procesa la letra tanto de teclado fisico como virtual.
     */
    public void procesarLetra(char letra) {
        if (partidaTerminada) return;
        if (logica.obtenerIntentosFallidos() >= 7 || logica.esGanador()) return;

        BotonTeclaCyber boton = mapaTeclas.get(letra);
        if (boton != null && !boton.isEnabled()) return;

        if (boton != null) boton.setEnabled(false);

        boolean acierto = logica.procesarIntento(letra);
        letrasIntentadasPartida++;
        if (acierto) letrasCorrectasPartida++;
        
        if (audioController != null && audioEfectosHabilitado) {
            audioController.actualizarTension(logica.obtenerIntentosFallidos(), !logica.esPerdedor() && !logica.esGanador());
        }

        if (boton != null) {
            if (acierto) {
                boton.aplicarEstilo(VERDE_SISTEMA);
                escribirEnConsola("[ROOT]> OK_CHAR: Segmento '" + 
                    Character.toUpperCase(letra) + "' verificado", estiloExito);
                if (audioOperativoHabilitado) audioController.hablarExito();
                if (audioEfectosHabilitado && audioController != null) 
                    audioController.reproducirAcierto();
                // >>> BUG GESTURE: LAUGH al acertar
                if (panelCyberOverlay != null) panelCyberOverlay.onAcierto();
            } else {
                boton.aplicarEstilo(ROJO_ALERTA);
                escribirEnConsola("[SISTEMA]> ERR_AUTH: Fallo - Intentos restantes: " + 
                    logica.getIntentosRestantes(), estiloError);
                if (audioOperativoHabilitado) audioController.hablarError();
                if (audioEfectosHabilitado && audioController != null) 
                    audioController.reproducirError();
                // >>> BUG GESTURE: GROWL al fallar
                if (panelCyberOverlay != null) panelCyberOverlay.onFallo();
                // >>> BUG GESTURE: ATTACK_LETTER si salud critica (<=2 intentos)
                if (logica.getIntentosRestantes() <= 2 && !logica.esPerdedor()) {
                    dispararAtaqueMordida();
                }
            }
        }

        actualizarVista();
        verificarFinJuego();
    }

    /** Busca una caja de letra oculta ('_') y dispara el ataque mordida del bug sobre ella. */
    private void dispararAtaqueMordida() {
        if (panelCyberOverlay == null || panelCajasPalabra == null) return;
        // Buscar componentes hijos que representen letras ocultas ('_')
        java.util.List<java.awt.Component> ocultas = new java.util.ArrayList<>();
        String progreso = logica.obtenerProgreso();
        String[] letras = progreso.split(" ");
        java.awt.Component[] hijos = panelCajasPalabra.getComponents();
        int idx = 0;
        for (String l : letras) {
            if (l.isEmpty()) continue;
            if (idx < hijos.length && l.equals("_")) {
                ocultas.add(hijos[idx]);
            }
            idx++; // saltar el strut que va intercalado
            idx++; // cada letra + strut
        }
        if (ocultas.isEmpty()) return;
        // Elegir una caja aleatoria
        java.awt.Component caja = ocultas.get(new Random().nextInt(ocultas.size()));
        // Convertir posicion a coordenadas del overlay (screen -> overlay)
        try {
            java.awt.Point cajaScreen = caja.getLocationOnScreen();
            java.awt.Point overlayScreen = panelCyberOverlay.getLocationOnScreen();
            int relX = cajaScreen.x - overlayScreen.x;
            int relY = cajaScreen.y - overlayScreen.y;
            panelCyberOverlay.onAtaqueMordida(relX, relY);
        } catch (Exception ignored) {
            // Si los componentes no estan visibles aun, ignorar
        }
    }

    private void ejecutarPista(IPista pista) {
        if (partidaTerminada || logica.obtenerPistasRestantes() <= 0) return;

        // Primero narrar "Extrayendo datos..." (Canal Operativo)
        if (audioOperativoHabilitado) audioController.hablarPistaDescripcion();

        String resultado = logica.solicitarPista(pista);
        escribirEnConsola("[CONSOLA]> PISTA_REQ: " + resultado, estiloConsola);

        // Extraer y narrar informacion especifica segun tipo de pista
        if (pista instanceof PistaLetraAleatoria) {
            // Extraer la letra del formato "PISTA: LETRA | Segmento autorizado 'X'"
            int idx = resultado.indexOf("'");
            if (idx >= 0 && idx + 2 < resultado.length()) {
                char letraRevelada = resultado.charAt(idx + 1);
                if (audioOperativoHabilitado) audioController.hablarPistaLetra(letraRevelada);
            }
        } else if (pista instanceof PistaCategoria) {
            // Extraer la categoria del resultado
            String categoria = logica.obtenerPalabraActual().getCategoria();
            if (audioOperativoHabilitado) audioController.hablarPistaCategoria(categoria);
        } else if (pista instanceof PistaTexto) {
            // Extraer la descripcion del formato "PISTA: DESCRIPCION | ..."
            int idx = resultado.indexOf("|");
            if (idx >= 0 && idx + 2 < resultado.length()) {
                String descripcion = resultado.substring(idx + 2);
                // Narrar descripcion completa
                if (audioOperativoHabilitado) audioController.hablarFrase("Extrayendo datos. Descripcion encontrada: " + descripcion);
            }
        }

        actualizarVista();
    }

    private void reiniciarSistema() {
        if (temporizadorSesion != null) {
            temporizadorSesion.stop();
            segundosTranscurridos = 0;
            etiquetaTiempo.setText("[TIEMPO: 00:00]");
            temporizadorSesion.start();
        }
        
        if (panelFinJuego != null) {
            panelFinJuego.setVisible(false);
        }
        
        // Stop all sounds before restarting to prevent audio overlap
        if (audioController != null) {
            audioController.detenerBgmGame();
            audioController.stopAllSounds();
            audioController.reactivarAudio();
        }
        
        // Activar las 3 capas de audio + BGM despues de limpiar
        activarCapasAudioInicio();
        
        partidaTerminada = false;
        letrasCorrectasPartida = 0;
        letrasIntentadasPartida = 0;
        logica = new LogicaJuego(bancoPalabras.obtenerPalabraAlAzar());
        aplicarDificultad(logica);
        panelSujeto.establecerLogica(logica);
        
        if (audioController != null && audioEfectosHabilitado) {
            audioController.actualizarTension(0, true);
        }
        
        // Reset teclado visual y logico
        for (BotonTeclaCyber b : mapaTeclas.values()) {
            b.setEnabled(true);
            b.aplicarEstilo(CIAN_NEON);
        }
        
        consolaTransacciones.setText(null);
        
        escribirEnConsola("[ROOT]> Sistema reiniciado", estiloRoot);
        escribirEnConsola("[SISTEMA]> Nueva palabra cargada", estiloSistema);
        escribirEnConsola("[CONSOLA]> Esperando input", estiloConsola);
        
        actualizarVista();
        
        // Recargar KeyBindings y forzar foco para teclado fisico
        configurarKeyBindings();
        this.requestFocusInWindow();
        this.getRootPane().requestFocusInWindow();
    }
    
    /**
     * Activa las 3 capas de audio base al iniciar una nueva partida.
     * Antes de relanzarlas, detiene instancias previas y limpia.
     */
    private void activarCapasAudioInicio() {
        // 1. Detener solo voz previa sin afectar BGM ni estado global
        audioController.setVozSilenciada(true);
        audioController.setVozSilenciada(false);
        
        // 2. CAPA TRIVIA / HUMOR: narracion de inicio
        if (audioTriviaHabilitado) {
            audioController.hablarFrase("Protocolo de extraccion de datos iniciado. Sistema Nexus listo.");
        }
        
        // 3. CAPA OPERATIVA / VOZ DE SISTEMA: narrar estado del sistema
        if (audioOperativoHabilitado) {
            audioController.hablarReinicio();
        }
        
        // 4. CAPA EFECTOS / NUCLEO: sonido ambiente de sistema
        if (audioEfectosHabilitado && audioController != null) {
            audioController.reproducirTecleo();
        }
        
        // 5. CAPA BGM: relanzar musica de fondo del juego (canal independiente)
        if (audioEfectosHabilitado && audioController != null) {
            if (!audioController.isBgmGameActivo()) {
                audioController.reproducirBgmGame();
            }
        }
    }
    
    /**
     * Reinicia el sistema con limpieza completa de logs.
     * Incluye mensaje de voz de confirmaciÃ³n.
     */
    private void reiniciarSistemaConLimpieza() {
        // Limpiar logs de consola antes de reiniciar
        consolaTransacciones.setText(null);
        
        // Reiniciar sistema normalmente
        reiniciarSistema();
        
        // Mensaje adicional de confirmaciÃ³n
        escribirEnConsola("[SISTEMA]> Protocolo de reinicio completado. Sistema listo para decodificacion", estiloSistema);
    }

    private void aplicarDificultad(LogicaJuego motor) {
        if (motor == null) return;
        if ("DIFICIL".equalsIgnoreCase(dificultadActual)) {
            motor.setPistasRestantes(0);
            motor.setMaxIntentos(6);
        } else if ("PESADILLA".equalsIgnoreCase(dificultadActual)) {
            motor.setPistasRestantes(0);
            motor.setMaxIntentos(5);
        }
    }

    private int registrarResultadoAvanzado(boolean victoria) {
        if (gestorEstadisticas == null || logica == null) return 0;
        int xp = gestorEstadisticas.registrarResultado(
            victoria,
            letrasCorrectasPartida,
            letrasIntentadasPartida,
            segundosTranscurridos,
            logica.obtenerPistasRestantes(),
            dificultadActual,
            logica.obtenerPalabraActual().getCategoria()
        );
        // Registrar en sistema de misiones
        try {
            boolean perfecta = victoria && (letrasIntentadasPartida == letrasCorrectasPartida);
            boolean rapida = victoria && (segundosTranscurridos > 0 && segundosTranscurridos <= 60);
            GestorMisiones.getInstancia().registrarPartida(
                victoria, perfecta, rapida,
                dificultadActual,
                gestorEstadisticas.getRachaActual(),
                gestorEstadisticas.getJuegosGanados(),
                gestorEstadisticas.getJuegosJugados(),
                gestorEstadisticas.getNivel(),
                logica.obtenerPalabraActual().getCategoria()
            );
        } catch (Exception e) {
            System.err.println("[MISIONES] Error registrando partida: " + e.getMessage());
        }
        return xp;
    }

    private void verificarFinJuego() {
        if (logica.esGanador()) {
            partidaTerminada = true;
            if (temporizadorSesion != null) temporizadorSesion.stop();
            escribirEnConsola("[ROOT]> PROTOCOLO COMPLETADO - ACCESO CONCEDIDO", estiloExito);
            if (audioOperativoHabilitado) audioController.hablarVictoria();
            // >>> BUG GESTURE: VICTORY_DANCE al ganar
            if (panelCyberOverlay != null) panelCyberOverlay.onVictoria();
            if (audioEfectosHabilitado && audioController != null) 
                audioController.reproducirAcierto();
            // Detener BGM y sonidos al ganar - reactivar flag global para proxima partida
            audioController.detenerBgmGame();
            audioController.stopAllSounds();
            audioController.reactivarAudio();
            actualizarBannerAudio();
            // Registrar estadistica de victoria
            if (gestorEstadisticas != null) {
                ultimaXPGanada = registrarResultadoAvanzado(true);
                actualizarEstadisticasUI();
            }
            // Otorgar Kali Koints
            ultimaKKGanada = gestorEconomia.otorgarRecompensaPartida(
                true,
                logica.getIntentosRestantes(),
                logica.obtenerIntentosFallidos(),
                segundosTranscurridos,
                dificultadActual);
            escribirEnConsola("[ECONOMIA]> RECOMPENSA: +" + ultimaKKGanada + " Kali Koints", estiloExito);
            mostrarDialogoFin(true);
        } else if (logica.esPerdedor() && panelSujeto.isPalancaJalada() && !partidaTerminada) {
            partidaTerminada = true;
            if (temporizadorSesion != null) temporizadorSesion.stop();
            escribirEnConsola("[SISTEMA]> CRITICAL FAILURE - CONEXION PERDIDA", estiloError);
            if (audioOperativoHabilitado) audioController.hablarDerrota();
            // Detener BGM y sonidos al perder - reactivar flag global para proxima partida
            audioController.detenerBgmGame();
            audioController.stopAllSounds();
            audioController.reactivarAudio();
            if (audioEfectosHabilitado && audioController != null) {
                audioController.reproducirPalanca();
            }
            // [CRÃTICO] Actualizar banner para reflejar estado de audio
            actualizarBannerAudio();
            realizarScreenShake();
            // Registrar estadistica de derrota
            if (gestorEstadisticas != null) {
                ultimaXPGanada = registrarResultadoAvanzado(false);
                actualizarEstadisticasUI();
            }
            // Otorgar Kali Koints (consuelo)
            ultimaKKGanada = gestorEconomia.otorgarRecompensaPartida(
                false, 0, logica.obtenerIntentosFallidos(),
                segundosTranscurridos, dificultadActual);
            escribirEnConsola("[ECONOMIA]> CONSUELO: +" + ultimaKKGanada + " Kali Koints", estiloInfo);
            
            // Retrasar el diÃ¡logo 2.5 segundos para ver la animaciÃ³n de caÃ­da
            Timer delayTimer = new Timer(2500, e -> {
                mostrarDialogoFin(false);
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
    }
    
    private void realizarScreenShake() {
        Timer shakeTimer = new Timer(40, null);
        final int[] shakes = {0};
        final Point original = getLocation();
        shakeTimer.addActionListener(e -> {
            if (shakes[0] < 6) {
                // Sacudida suave: máximo 4px de desplazamiento, se atenúa progresivamente
                float intensidad = 1.0f - (shakes[0] / 6.0f);
                int dx = (int)((Math.random() * 8 - 4) * intensidad);
                int dy = (int)((Math.random() * 6 - 3) * intensidad);
                setLocation(original.x + dx, original.y + dy);
                shakes[0]++;
            } else {
                setLocation(original);
                shakeTimer.stop();
            }
        });
        shakeTimer.start();
    }
    
    private JPanel panelFinJuego;

    private void mostrarDialogoFin(boolean victoria) {
        panelFinJuego = new JPanel(new BorderLayout());
        panelFinJuego.setOpaque(false); // Transparente para no ocultar la animaciÃ³n del androide
        
        String titulo = victoria ? "[ ACCESO CONCEDIDO ]" : "[ SISTEMA BLOQUEADO ]";
        String mensaje = victoria ? 
            "DATOS DESENCRIPTADOS EXITOSAMENTE" : 
            "INTENTOS AGOTADOS - CONEXION CERRADA";
            
        Color colorBase = victoria ? VERDE_SISTEMA : ROJO_ALERTA;

        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBackground(new Color(5, 10, 15, 235)); // Fondo oscuro semi-transparente
        banner.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(colorBase, 2),
            new EmptyBorder(40, 50, 40, 50)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
        lblTitulo.setForeground(colorBase);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblMsg = new JLabel(mensaje);
        lblMsg.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        lblMsg.setForeground(new Color(0xCC, 0xCC, 0xCC));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblPalabra = new JLabel("TARGET: " + logica.obtenerPalabraActual().getTexto().toUpperCase());
        lblPalabra.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        lblPalabra.setForeground(colorBase);
        lblPalabra.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea resumenPartida = new JTextArea();
        resumenPartida.setEditable(false);
        resumenPartida.setOpaque(false);
        resumenPartida.setForeground(new Color(0xCC, 0xCC, 0xCC));
        resumenPartida.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        resumenPartida.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumenPartida.setText(String.format(
            "OPERADOR: %s\nDIFICULTAD: %s\nTIEMPO: %02d:%02d\nACIERTOS: %d/%d\nPISTAS RESTANTES: %d\nXP GANADA: +%d\nKALI KOINTS: +%d KK (SALDO: %d)\nNIVEL: %d | RANGO: %s\nLOGROS: %s",
            aliasOperador.toUpperCase(),
            dificultadActual,
            segundosTranscurridos / 60,
            segundosTranscurridos % 60,
            letrasCorrectasPartida,
            letrasIntentadasPartida,
            logica.obtenerPistasRestantes(),
            ultimaXPGanada,
            ultimaKKGanada,
            gestorEconomia != null ? gestorEconomia.getSaldo() : 0,
            gestorEstadisticas != null ? gestorEstadisticas.getNivel() : 1,
            gestorEstadisticas != null ? gestorEstadisticas.getRango() : "RECLUTA DIGITAL",
            gestorEstadisticas != null ? gestorEstadisticas.getLogrosResumen() : "SIN LOGROS"
        ));
        
        JButton btnReiniciar = crearBotonPausa("NUEVA EXTRACCION", colorBase);
        btnReiniciar.addActionListener(e -> {
            panelFinJuego.setVisible(false);
            reiniciarSistemaConLimpieza();
        });
        
        JButton btnSalir = crearBotonPausa("ABANDONAR SESION", AMBAR);
        btnSalir.addActionListener(e -> {
            if (audioController != null) {
                audioController.detenerBgmGame();
                audioController.stopAllSounds();
                audioController.reactivarAudio();
            }
            // Usar callback si existe, sino crear nueva PantallaArranque
            if (callbackVolverAlMenu != null) {
                callbackVolverAlMenu.run();
            } else {
                dispose();
                new GlitchScreen().setVisible(true);
            }
        });
        
        banner.add(Box.createVerticalGlue());
        banner.add(lblTitulo);
        banner.add(Box.createVerticalStrut(20));
        banner.add(lblMsg);
        banner.add(Box.createVerticalStrut(30));
        banner.add(lblPalabra);
        banner.add(Box.createVerticalStrut(20));
        banner.add(resumenPartida);
        banner.add(Box.createVerticalStrut(35));
        banner.add(btnReiniciar);
        banner.add(Box.createVerticalStrut(15));
        banner.add(btnSalir);
        banner.add(Box.createVerticalGlue());
        
        // Contenedor lateral derecho
        JPanel rightWrapper = new JPanel(new java.awt.GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(banner);
        
        panelFinJuego.add(rightWrapper, BorderLayout.EAST);
        panelFinJuego.setBorder(new EmptyBorder(0, 0, 0, 80)); // Margen derecho
        
        setGlassPane(panelFinJuego);
        panelFinJuego.setVisible(true);
    }

    public void actualizarVista() {
        if (logica == null) return;
        panelCajasPalabra.actualizarPalabra(logica);
        panelSujeto.repaint();
        actualizarSystemData();
        actualizarLetrasFallidas();
        actualizarEstadisticasUI();
    }

    private void actualizarLetrasFallidas() {
        if (etiquetaLetrasFallidas == null || logica == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("LETRAS FALLIDAS: [");
        boolean primero = true;
        for (char letra : logica.obtenerLetrasFallidas()) {
            if (!primero) sb.append(", ");
            sb.append(Character.toUpperCase(letra));
            primero = false;
        }
        sb.append("]");
        etiquetaLetrasFallidas.setText(sb.toString());
    }

    private void actualizarEstadisticasUI() {
        if (gestorEstadisticas == null) return;
        if (labelStatsRango != null) {
            labelStatsRango.setText(String.format("%s RANGO: %s", "\u2694", gestorEstadisticas.getRango()));
        }
        if (labelStatsJugados != null) {
            labelStatsJugados.setText(String.format("%s JUGADOS: %d", "\uD83C\uDFAE", gestorEstadisticas.getJuegosJugados()));
        }
        if (labelStatsGanados != null) {
            labelStatsGanados.setText(String.format("%s GANADOS: %d", "\uD83C\uDFC6", gestorEstadisticas.getJuegosGanados()));
        }
        if (labelStatsPorcentaje != null) {
            labelStatsPorcentaje.setText(String.format("%s PORCENTAJE: %.1f%%", "\uD83D\uDCC8", gestorEstadisticas.getPorcentajeVictorias()));
        }
        if (labelStatsNivel != null) {
            labelStatsNivel.setText(String.format("LV NIVEL: %d", gestorEstadisticas.getNivel()));
        }
        if (labelStatsXP != null) {
            labelStatsXP.setText(String.format("XP EXPERIENCIA: %d/%d", gestorEstadisticas.getExperiencia(), gestorEstadisticas.getXpSiguienteNivel()));
        }
        if (labelStatsPrecision != null) {
            labelStatsPrecision.setText(String.format("%% PRECISION: %.1f%%", gestorEstadisticas.getPrecision()));
        }
        if (labelStatsRachaActual != null) {
            labelStatsRachaActual.setText(String.format("%s RACHA ACTUAL: %d", "\uD83D\uDD25", gestorEstadisticas.getRachaActual()));
        }
        if (labelStatsRachaMaxima != null) {
            labelStatsRachaMaxima.setText(String.format("%s RACHA MAXIMA: %d", "\u2B06", gestorEstadisticas.getRachaMaxima()));
        }
    }
    
    /**
     * Crea un JLabel para estadÃ­stica con icono Unicode.
     * Etiqueta en cian, valor en rosa neÃ³n.
     */
    private JLabel crearLabelStat(String icono, String etiqueta, String valorInicial) {
        JLabel label = new JLabel(icono + " " + etiqueta + ": " + valorInicial);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        label.setForeground(CIAN_NEON); // Etiqueta en cian
        return label;
    }

    private void inicializarEstilosConsola() {
        StyleContext ctx = StyleContext.getDefaultStyleContext();
        
        estiloRoot = ctx.addStyle("root", null);
        StyleConstants.setForeground(estiloRoot, new Color(0x88, 0xCC, 0xFF));
        StyleConstants.setFontFamily(estiloRoot, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloRoot, 10);
        StyleConstants.setBold(estiloRoot, true);

        estiloSistema = ctx.addStyle("sistema", null);
        StyleConstants.setForeground(estiloSistema, CIAN_NEON);
        StyleConstants.setFontFamily(estiloSistema, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloSistema, 10);

        estiloConsola = ctx.addStyle("consola", null);
        StyleConstants.setForeground(estiloConsola, new Color(0x00, 0xCC, 0xDD));
        StyleConstants.setFontFamily(estiloConsola, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloConsola, 10);

        estiloExito = ctx.addStyle("exito", null);
        StyleConstants.setForeground(estiloExito, VERDE_SISTEMA);
        StyleConstants.setFontFamily(estiloExito, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloExito, 10);
        StyleConstants.setBold(estiloExito, true);

        estiloError = ctx.addStyle("error", null);
        StyleConstants.setForeground(estiloError, ROJO_ALERTA);
        StyleConstants.setFontFamily(estiloError, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloError, 10);
        StyleConstants.setBold(estiloError, true);

        estiloInfo = ctx.addStyle("info", null);
        StyleConstants.setForeground(estiloInfo, new Color(0xAA, 0xAA, 0xAA));
        StyleConstants.setFontFamily(estiloInfo, Font.MONOSPACED);
        StyleConstants.setFontSize(estiloInfo, 10);
    }

    private void escribirEnConsola(String texto, Style estilo) {
        colaMensajes.add(new java.util.AbstractMap.SimpleEntry<>(texto, estilo != null ? estilo : estiloSistema));
        if (timerTipeo == null || !timerTipeo.isRunning()) {
            iniciarTipeo();
        }
    }

    private void iniciarTipeo() {
        if (timerTipeo == null) {
            timerTipeo = new Timer(15, e -> {
                if (mensajeActual == null) {
                    if (colaMensajes.isEmpty()) {
                        timerTipeo.stop();
                        return;
                    }
                    java.util.Map.Entry<String, Style> entrada = colaMensajes.poll();
                    mensajeActual = entrada.getKey();
                    estiloActual = entrada.getValue();
                    try {
                        StyledDocument doc = consolaTransacciones.getStyledDocument();
                        if (doc.getLength() > 0) {
                            doc.insertString(doc.getLength(), "\n", estiloActual);
                        }
                        doc.insertString(doc.getLength(), mensajeActual, estiloActual);
                        consolaTransacciones.setCaretPosition(doc.getLength());
                    } catch (BadLocationException ex) { /* ignorar */ }
                    mensajeActual = null;
                    estiloActual = null;
                }
            });
        }
        if (!timerTipeo.isRunning()) {
            timerTipeo.start();
        }
    }

    private void actualizarTemporizador() {
        if (partidaTerminada) return;
        segundosTranscurridos++;
        int minutos = segundosTranscurridos / 60;
        int segundos = segundosTranscurridos % 60;
        etiquetaTiempo.setText(String.format("[TIEMPO: %02d:%02d]", minutos, segundos));
    }



    // ========== CLASES INTERNAS ==========

    class BotonTeclaCyber extends JButton {
        private static final long serialVersionUID = 1L;
        private Color colorActual = CIAN_NEON;
        private char letra;
        private boolean hovered = false;
        
        public BotonTeclaCyber(String texto) {
            super(texto);
            this.letra = Character.toLowerCase(texto.charAt(0));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(38, 38));
            setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            setForeground(CIAN_NEON);
            setBackground(FONDO_NEGRO);
            
            addActionListener(e -> procesarLetra(letra));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    hovered = true;
                    repaint();
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void aplicarEstilo(Color color) {
            this.colorActual = color;
            setForeground(color);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fondo de hover renderizado manualmente (sin mutar setOpaque)
            if (hovered && isEnabled()) {
                g2.setColor(new Color(0x15, 0x30, 0x35));
                g2.fillRoundRect(2, 2, getWidth()-5, getHeight()-5, 4, 4);
            }
            
            g2.setColor(colorActual);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 4, 4);
            
            g2.setColor(isEnabled() ? colorActual : GRIS_INACTIVO);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            
            g2.dispose();
        }
    }

    class BotonCyber extends JButton {
        private static final long serialVersionUID = 1L;
        private Color colorBorde;
        private boolean hovered = false;

        public BotonCyber(String texto, Color colorTexto) {
            super(texto);
            this.colorBorde = colorTexto;
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(colorTexto);
            setBackground(FONDO_NEGRO);
            setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
            setPreferredSize(new Dimension(texto.length() > 10 ? 140 : 90, 32));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    hovered = true;
                    repaint();
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fondo de hover renderizado manualmente (sin mutar setOpaque)
            if (hovered && isEnabled()) {
                g2.setColor(new Color(0x15, 0x30, 0x35));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 6, 6);
            }
            
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 6, 6);
            
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            
            g2.dispose();
        }
    }

    /**
     * Boton Mute/Unmute con estados visuales.
     */
    class BotonMute extends JButton {
        private static final long serialVersionUID = 1L;
        private boolean muted = false;
        
        public BotonMute() {
            super("[SOUND: ON]");
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
            setForeground(VERDE_SISTEMA);
            setPreferredSize(new Dimension(80, 24));
        }
        
        public void setMuted(boolean m) {
            this.muted = m;
            setText(muted ? "[SOUND: OFF]" : "[SOUND: ON]");
            setForeground(muted ? ROJO_ALERTA : VERDE_SISTEMA);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fondo segun estado
            g2.setColor(muted ? new Color(0x33, 0x00, 0x00) : new Color(0x00, 0x33, 0x00));
            g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 4, 4);
            
            // Borde
            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 4, 4);
            
            // Texto
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            
            g2.dispose();
        }
    }

    class PanelCajasPalabra extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public PanelCajasPalabra() {
            setOpaque(false);
            setBackground(FONDO_NEGRO);
        }

        public void actualizarPalabra(LogicaJuego logica) {
            removeAll();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            String progreso = logica.obtenerProgreso();
            String[] letras = progreso.split(" ");
            
            for (String letra : letras) {
                if (letra.isEmpty()) {
                    continue;
                }
                JPanel caja = crearCajaLetra(letra.charAt(0));
                add(caja);
                add(Box.createHorizontalStrut(6));
            }
            
            add(Box.createHorizontalGlue());
            revalidate();
            repaint();
        }

        private JPanel crearCajaLetra(char c) {
            JPanel caja = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = getWidth();
                    int h = getHeight();
                    
                    g2.setColor(CIAN_NEON);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(2, 2, w-5, h-5, 4, 4);
                    
                    if (c != '_') {
                        g2.setColor(CIAN_NEON);
                        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
                        FontMetrics fm = g2.getFontMetrics();
                        int tx = (w - fm.stringWidth(String.valueOf(c))) / 2;
                        int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(String.valueOf(c), tx, ty);
                    }
                    
                    g2.dispose();
                }
            };
            caja.setPreferredSize(new Dimension(32, 40));
            caja.setMaximumSize(new Dimension(32, 40));
            caja.setOpaque(false);
            return caja;
        }
    }

    class PanelScanlines extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public PanelScanlines() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            
            int h = getHeight();
            int w = getWidth();
            
            g2.setColor(new Color(0x00, 0x44, 0x55, 8));
            for (int i = 0; i < h; i += 3) {
                g2.drawLine(0, i, w, i);
            }
            
            g2.setColor(new Color(0x00, 0x33, 0x44, 15));
            for (int i = 0; i < h; i += 6) {
                g2.drawLine(0, i, w, i);
            }
            
            g2.dispose();
        }
    }

    class PanelTituloBloom extends JPanel {
        private static final long serialVersionUID = 1L;
        private String tituloTexto;

        public PanelTituloBloom(String titulo) {
            this.tituloTexto = titulo;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            Font fuente = new Font(Font.SANS_SERIF, Font.BOLD, 28);
            g2.setFont(fuente);

            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(tituloTexto, fuente, frc);

            float anchoTexto = (float) layout.getBounds().getWidth();
            float x = (w - anchoTexto) / 2f;
            float y = 38f;

            AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
            Shape outline = layout.getOutline(transform);

            g2.setColor(new Color(0x00, 0xFF, 0xFF, 30));
            g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(outline);

            g2.setColor(new Color(0x00, 0xDD, 0xEE, 80));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(outline);

            g2.setColor(CIAN_NEON);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(outline);

            g2.setColor(new Color(0xE0, 0xFF, 0xFF));
            g2.fill(outline);

            g2.dispose();
        }
    }

    // === PANEL CYBER OVERLAY: Glitch popups animados estilo DedSec/Watch Dogs ===
    // Bugs redondos y dinamicos con gestos reactivos al gameplay.
    class PanelCyberOverlay extends JPanel {
        private static final long serialVersionUID = 1L;
        private Random rnd = new Random();
        private java.util.List<GlitchPopup> popups = new java.util.ArrayList<>();
        private java.util.List<GlitchFragment> fragments = new java.util.ArrayList<>();
        private int tick = 0;
        private long lastPopupTime = 0;
        private long lastFragmentTime = 0;

        // === BUG ACTIVO ANIMADO: el bug residente que reacciona al gameplay ===
        private Bug bugResidente = null;
        private int bugResX = -1, bugResY = -1; // posicion del bug residente
        private boolean bugResVisible = false;
        private int bugResSize = 90; // tamano de renderizado

        // === ATAQUE MORDIDA: coordenadas target de la caja de letra ===
        private int attackTargetX = -1, attackTargetY = -1;
        private boolean attackActive = false;
        private long attackStartTime = 0;

        // === SCREEN SHAKE LIGERO EN GROWL ===
        private int shakeOffsetX = 0, shakeOffsetY = 0;

        // === OCULTAR BUGS INDIVIDUALMENTE CON MOUSE ===
        private Point mousePosition = null;

        /** Actualiza la posicion del mouse relativa al overlay */
        public void actualizarPosicionMouse(int x, int y) {
            mousePosition = new Point(x, y);
        }

        /** Verifica si el mouse esta sobre un rectangulo dado */
        private boolean mouseSobreArea(int x, int y, int w, int h) {
            if (mousePosition == null) return false;
            return mousePosition.x >= x && mousePosition.x < x + w &&
                   mousePosition.y >= y && mousePosition.y < y + h;
        }

        private final String[] MENSAJES_HACK = {
            "BREACHING FIREWALL...", "VULNERABILITY FOUND", "INJECTING PAYLOAD",
            "ROOT ACCESS GRANTED", "EXTRACTING DATA...", "DECRYPTING LAYER 3",
            "BYPASSING IDS...", "SNIFFING PACKETS", "BACKDOOR INSTALLED",
            "PORT 443 OPEN", "SQL INJECTION OK", "BUFFER OVERFLOW >>",
            "PRIVILEGE ESCALATION", "CRACKING HASH...", "MAN-IN-THE-MIDDLE",
            "DEDSEC WAS HERE", "WE ARE WATCHING", "NO SYSTEM IS SAFE",
            "HACK THE PLANET", "ACCESS DENIED? LOL", "ENCRYPTING TRAIL...",
            "GHOST PROTOCOL ON", "ZERO-DAY EXPLOIT", "REVERSE SHELL ACTIVE"
        };

        private final String[] SKULL_ART = {
            "  ___  ", " /x x\\ ", " \\_^_/ ", "  |_|  "
        };

        private final String[] SKULL_ART2 = {
            " [XXX] ", " |o.o| ", " |_-_| ", "  /_\\  "
        };

        private final String[] BUG_ART = {
            " /\\_/\\ ", "( o.o )", " > ^ < ", " /| |\\ "
        };

        public PanelCyberOverlay() {
            setOpaque(false);
            // Timer a 40ms (~25fps) para animaciones fluidas
            overlayTimer = new Timer(40, e -> {
                tick++;
                actualizarOverlay();
                // Tick de la maquina de estados del bug residente
                if (bugResidente != null) {
                    bugResidente.getAnimState().tick();
                }
                repaint();
            });
            overlayTimer.start();
        }

        public void detener() {
            if (overlayTimer != null) {
                overlayTimer.stop();
                overlayTimer = null;
            }
            popups.clear();
            fragments.clear();
            bugResidente = null;
            bugResVisible = false;
            attackActive = false;
        }

        // === API PUBLICA: llamadas desde InterfazAhorcado al procesar letras ===

        /** Asegura que haya un bug residente activo (de los bugs activos). */
        public void asegurarBugResidente() {
            if (bugResidente == null && gestorBugs != null) {
                bugResidente = gestorBugs.aleatorioActivo();
                if (bugResidente != null) {
                    bugResidente.getAnimState().reset();
                    // Posicion: esquina inferior derecha del overlay, con offset
                    int w = getWidth() > 100 ? getWidth() : 1200;
                    int h = getHeight() > 100 ? getHeight() : 800;
                    bugResX = w - bugResSize - 40;
                    bugResY = h - bugResSize - 120;
                    bugResVisible = true;
                }
            }
        }

        /** Dispara gesto LAUGH cerca de la matriz de decodificacion. */
        public void onAcierto() {
            asegurarBugResidente();
            if (bugResidente == null) return;
            bugResidente.dispararGesto(Bug.BugGesture.LAUGH);
            // Mover bug cerca del centro (zona de cajas)
            int w = getWidth() > 100 ? getWidth() : 1200;
            int h = getHeight() > 100 ? getHeight() : 800;
            bugResX = w / 2 - bugResSize / 2 + rnd.nextInt(80) - 40;
            bugResY = h - bugResSize - 160 + rnd.nextInt(40) - 20;
            bugResVisible = true;
        }

        /** Dispara gesto GROWL en esquina con temblor. */
        public void onFallo() {
            asegurarBugResidente();
            if (bugResidente == null) return;
            bugResidente.dispararGesto(Bug.BugGesture.GROWL);
            // Mover bug a esquina superior izquierda
            bugResX = 30 + rnd.nextInt(60);
            bugResY = 80 + rnd.nextInt(60);
            bugResVisible = true;
        }

        /** Dispara ATTACK_LETTER sobre una caja de letra oculta. Funcionalidad "brutal". */
        public void onAtaqueMordida(int cajaScreenX, int cajaScreenY) {
            asegurarBugResidente();
            if (bugResidente == null || !bugResidente.isDesbloqueado()) return;
            bugResidente.dispararGesto(Bug.BugGesture.ATTACK_LETTER);
            // Posicionar el bug directamente sobre la caja de letra
            attackTargetX = cajaScreenX;
            attackTargetY = cajaScreenY;
            bugResX = cajaScreenX - bugResSize / 2 + 16;
            bugResY = cajaScreenY - bugResSize + 10;
            bugResVisible = true;
            attackActive = true;
            attackStartTime = System.currentTimeMillis();
        }

        /** Dispara VICTORY_DANCE al ganar. */
        public void onVictoria() {
            asegurarBugResidente();
            if (bugResidente == null) return;
            bugResidente.dispararGesto(Bug.BugGesture.VICTORY_DANCE);
            int w = getWidth() > 100 ? getWidth() : 1200;
            int h = getHeight() > 100 ? getHeight() : 800;
            bugResX = w / 2 - bugResSize / 2;
            bugResY = h / 2 - bugResSize / 2 - 40;
            bugResVisible = true;
        }

        private void actualizarOverlay() {
            long now = System.currentTimeMillis();

            if (now - lastPopupTime > 1500 + rnd.nextInt(1500)) {
                lastPopupTime = now;
                crearPopup();
            }

            if (now - lastFragmentTime > 2000 + rnd.nextInt(3000)) {
                lastFragmentTime = now;
                crearFragmento();
            }

            for (int i = popups.size() - 1; i >= 0; i--) {
                GlitchPopup p = popups.get(i);
                p.vida--;
                if (p.vida <= 0) popups.remove(i);
            }

            for (int i = fragments.size() - 1; i >= 0; i--) {
                GlitchFragment f = fragments.get(i);
                f.vida--;
                f.y += f.vy;
                f.x += f.vx;
                if (f.vida <= 0) fragments.remove(i);
            }

            // Actualizar shake para GROWL
            if (bugResidente != null && bugResidente.getAnimState().getGestoActual() == Bug.BugGesture.GROWL) {
                shakeOffsetX = rnd.nextInt(5) - 2;
                shakeOffsetY = rnd.nextInt(3) - 1;
            } else {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }

            // Desactivar ataque cuando el gesto termine
            if (attackActive && bugResidente != null &&
                bugResidente.getAnimState().getGestoActual() != Bug.BugGesture.ATTACK_LETTER) {
                attackActive = false;
            }
        }

        private void crearPopup() {
            GlitchPopup p = new GlitchPopup();
            int anchoRef = getWidth() > 0 ? getWidth() : InterfazAhorcado.this.getWidth();
            int altoRef  = getHeight() > 0 ? getHeight() : InterfazAhorcado.this.getHeight();
            p.x = 50 + rnd.nextInt(Math.max(1, anchoRef - 250));
            p.y = 80 + rnd.nextInt(Math.max(1, altoRef - 200));
            p.vida = 200 + rnd.nextInt(200);
            // 60% de probabilidad de mostrar un Bug avatar activo (tipo 5)
            if (rnd.nextInt(10) < 6 && gestorBugs != null) {
                p.tipo = 5;
                p.bugRef = gestorBugs.aleatorioActivo();
                p.mensaje = (p.bugRef != null) ? p.bugRef.etiquetaCorta : "GLITCH";
            } else {
                p.tipo = rnd.nextInt(5);
                p.mensaje = MENSAJES_HACK[rnd.nextInt(MENSAJES_HACK.length)];
            }
            p.progreso = rnd.nextInt(100);
            popups.add(p);
            if (popups.size() > 6) popups.remove(0);
        }

        private void crearFragmento() {
            GlitchFragment f = new GlitchFragment();
            f.x = rnd.nextInt(Math.max(1, getWidth()));
            f.y = rnd.nextInt(Math.max(1, getHeight()));
            f.vx = (rnd.nextFloat() - 0.5f) * 2f;
            f.vy = (rnd.nextFloat() - 0.5f) * 1.5f;
            f.vida = 30 + rnd.nextInt(50);
            f.texto = generarTextoAleatorio();
            fragments.add(f);
            if (fragments.size() > 8) fragments.remove(0);
        }

        private String generarTextoAleatorio() {
            int tipo = rnd.nextInt(4);
            if (tipo == 0) return String.format("0x%08X", rnd.nextInt());
            if (tipo == 1) return "[" + rnd.nextInt(255) + "." + rnd.nextInt(255) + "." + rnd.nextInt(255) + "." + rnd.nextInt(255) + "]";
            if (tipo == 2) {
                String m = MENSAJES_HACK[rnd.nextInt(MENSAJES_HACK.length)];
                return ">" + m.substring(0, Math.min(12, m.length()));
            }
            return String.format("PID:%04d", rnd.nextInt(9999));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Aplicar shake offset global
            g2.translate(shakeOffsetX, shakeOffsetY);

            for (GlitchFragment f : fragments) {
                float alpha = Math.min(1f, f.vida / 30f);
                g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
                g2.setColor(new Color(0, 255, 255, (int)(alpha * 50)));
                g2.drawString(f.texto, (int)f.x, (int)f.y);
            }

            for (GlitchPopup p : popups) {
                dibujarPopup(g2, p);
            }

            // Bandas glitch globales periodicas
            if (tick % 120 < 5) {
                int numLineas = 2 + rnd.nextInt(4);
                for (int i = 0; i < numLineas; i++) {
                    int ly = rnd.nextInt(Math.max(1, getHeight()));
                    int lh = 1 + rnd.nextInt(3);
                    int lx = rnd.nextInt(50);
                    g2.setColor(new Color(255, 0, 255, 30 + rnd.nextInt(40)));
                    g2.fillRect(lx, ly, getWidth() - lx * 2, lh);
                }
            }

            // === DIBUJAR BUG RESIDENTE ANIMADO ===
            dibujarBugResidente(g2);

            // === EFECTO MORDIDA: corrupcion visual sobre caja de letra ===
            if (attackActive && attackTargetX > 0) {
                dibujarEfectoMordida(g2);
            }

            g2.dispose();
        }

        /** Dibuja el bug residente con su frame animado actual. */
        private void dibujarBugResidente(Graphics2D g2) {
            if (!bugResVisible || bugResidente == null) return;

            Bug.BugGesture gesto = bugResidente.getAnimState().getGestoActual();
            float progreso = bugResidente.getAnimState().getProgreso();

            // Para IDLE, usar un "progreso" basado en tick para parpadeo
            if (gesto == Bug.BugGesture.IDLE) {
                // Ciclo de parpadeo cada 3 segundos (~75 ticks a 40ms)
                int ciclo = bugResidente.getAnimState().getTickIdle() % 75;
                progreso = (ciclo > 70) ? (ciclo - 70) / 5.0f : 0f;
            }

            BufferedImage frame = BugAvatarFactory.getFrameForGesture(bugResidente, gesto, progreso);
            if (frame == null) return;

            // Flotacion suave
            int floatY = (int)(3 * Math.sin(tick * 0.08));
            int drawX = bugResX;
            int drawY = bugResY + floatY;

            // No dibujar si el mouse esta exactamente sobre el bug residente
            if (mouseSobreArea(drawX, drawY, bugResSize, bugResSize)) return;

            java.awt.Composite prev = g2.getComposite();

            // Sombra bajo el bug
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.25f));
            g2.setColor(Color.BLACK);
            g2.fillOval(drawX + 10, drawY + bugResSize - 5, bugResSize - 20, 12);

            // Dibujar el frame del bug
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.92f));
            g2.drawImage(frame, drawX, drawY, bugResSize, bugResSize, null);

            // RGB shift glitch sutil en ciertos ticks
            if (tick % 10 < 2) {
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.15f));
                g2.drawImage(frame, drawX + rnd.nextInt(4) - 2, drawY, bugResSize, bugResSize, null);
            }

            g2.setComposite(prev);
        }

        /** Dibuja corrupcion visual sobre la caja de letra atacada. */
        private void dibujarEfectoMordida(Graphics2D g2) {
            long elapsed = System.currentTimeMillis() - attackStartTime;
            float t = Math.min(1.0f, elapsed / 1200f); // 1.2 segundos de ATTACK_LETTER

            int cx = attackTargetX + 16;
            int cy = attackTargetY + 20;

            // Fase 1 (0.0-0.4): Lineas de corrupcion convergentes
            if (t < 0.4f) {
                float intensity = t / 0.4f;
                g2.setColor(new Color(0xFF, 0x00, 0xFF, (int)(200 * intensity)));
                g2.setStroke(new BasicStroke(2f));
                int range = (int)(40 * (1 - intensity));
                g2.drawLine(cx - range, cy - range, cx, cy);
                g2.drawLine(cx + range, cy - range, cx, cy);
                g2.drawLine(cx - range, cy + range, cx, cy);
                g2.drawLine(cx + range, cy + range, cx, cy);
            }

            // Fase 2 (0.4-0.7): Explosion de mordida - "crunch"
            if (t >= 0.4f && t < 0.7f) {
                float phase = (t - 0.4f) / 0.3f;
                // Fragmentos de "pixel corruption"
                Random cr = new Random((long)(t * 100));
                for (int i = 0; i < 12; i++) {
                    int fx = cx - 20 + cr.nextInt(40);
                    int fy = cy - 15 + cr.nextInt(30);
                    int fw = 2 + cr.nextInt(6);
                    int fh = 2 + cr.nextInt(4);
                    int colorPick = cr.nextInt(3);
                    Color fragColor;
                    if (colorPick == 0) fragColor = new Color(0xFF, 0x00, 0xFF, (int)(220 * (1 - phase)));
                    else if (colorPick == 1) fragColor = new Color(0x00, 0xFF, 0xFF, (int)(180 * (1 - phase)));
                    else fragColor = new Color(0xFF, 0xFF, 0x00, (int)(200 * (1 - phase)));
                    g2.setColor(fragColor);
                    g2.fillRect(fx, fy, fw, fh);
                }
                // Borde de impacto
                g2.setColor(new Color(0xFF, 0x00, 0x00, (int)(150 * (1 - phase))));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(attackTargetX - 4, attackTargetY - 4, 40, 48, 6, 6);
            }

            // Fase 3 (0.7-1.0): Fade out con residuo glitch
            if (t >= 0.7f) {
                float fade = (t - 0.7f) / 0.3f;
                g2.setColor(new Color(0xFF, 0x00, 0xFF, (int)(60 * (1 - fade))));
                // Lineas horizontales de glitch residual
                for (int i = 0; i < 3; i++) {
                    int gy = attackTargetY + rnd.nextInt(40);
                    g2.fillRect(attackTargetX - 10, gy, 52, 1);
                }
            }
        }

        private void dibujarPopup(Graphics2D g2, GlitchPopup p) {
            float alpha = Math.min(1f, p.vida / 20f);
            float fadeIn = Math.min(1f, (float)(140 - p.vida) / 15f);
            float vis = Math.min(alpha, fadeIn);
            if (vis <= 0) return;

            // Determinar tamaño del popup según tipo
            int pw = 200, ph = 45;
            switch (p.tipo) {
                case 1: pw = 50; ph = 50; break; // Skull
                case 2: pw = 120; ph = 40; break; // Breach
                case 3: pw = 60; ph = 60; break; // Bug
                case 4: pw = 100; ph = 35; break; // Warning
                case 5: pw = 90; ph = 90; break; // BugAvatar
            }

            // No dibujar si el mouse esta exactamente sobre este popup
            if (mouseSobreArea(p.x, p.y, pw, ph)) return;

            int baseAlpha = (int)(vis * 200);
            int gx = p.x + (tick % 7 == 0 ? rnd.nextInt(5) - 2 : 0);
            int gy = p.y + (tick % 11 == 0 ? rnd.nextInt(3) - 1 : 0);

            switch (p.tipo) {
                case 0: dibujarPopupMensaje(g2, gx, gy, p, baseAlpha); break;
                case 1: dibujarPopupSkull(g2, gx, gy, p, baseAlpha); break;
                case 2: dibujarPopupBreach(g2, gx, gy, p, baseAlpha); break;
                case 3: dibujarPopupBug(g2, gx, gy, p, baseAlpha); break;
                case 4: dibujarPopupWarning(g2, gx, gy, p, baseAlpha); break;
                case 5: dibujarPopupBugAvatar(g2, gx, gy, p, baseAlpha); break;
            }
        }

        private void dibujarPopupMensaje(Graphics2D g2, int x, int y, GlitchPopup p, int alpha) {
            int pw = 200, ph = 45;
            g2.setColor(new Color(20, 0, 30, Math.min(180, alpha)));
            g2.fillRect(x, y, pw, ph);
            g2.setColor(new Color(255, 0, 255, Math.min(255, alpha)));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(x, y, pw, ph);

            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(new Color(255, 0, 255, Math.min(255, alpha)));
            g2.drawString("[!]", x + 5, y + 15);

            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0, 255, 255, Math.min(255, alpha)));
            g2.drawString(p.mensaje, x + 30, y + 15);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.setColor(new Color(0, 255, 0, Math.min(200, alpha)));
            g2.drawString("SESSION:" + String.format("%06X", rnd.nextInt(0xFFFFFF)), x + 5, y + 35);

            if (tick % 13 < 2) {
                g2.setColor(new Color(255, 0, 255, Math.min(100, alpha)));
                g2.fillRect(x, y + rnd.nextInt(ph), pw, 2);
            }
        }

        private void dibujarPopupSkull(Graphics2D g2, int x, int y, GlitchPopup p, int alpha) {
            int pw = 80, ph = 60;
            g2.setColor(new Color(10, 0, 15, Math.min(160, alpha)));
            g2.fillRect(x, y, pw, ph);
            g2.setColor(new Color(255, 0, 255, Math.min(220, alpha)));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x, y, pw, ph);

            String[] art = (tick % 2 == 0) ? SKULL_ART : SKULL_ART2;
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.setColor(new Color(255, 80, 255, Math.min(255, alpha)));
            for (int i = 0; i < art.length; i++) {
                g2.drawString(art[i], x + 10, y + 14 + i * 12);
            }
        }

        private void dibujarPopupBreach(Graphics2D g2, int x, int y, GlitchPopup p, int alpha) {
            int pw = 180, ph = 38;
            g2.setColor(new Color(0, 10, 15, Math.min(180, alpha)));
            g2.fillRect(x, y, pw, ph);
            g2.setColor(new Color(0, 255, 255, Math.min(200, alpha)));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x, y, pw, ph);

            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.setColor(new Color(255, 136, 0, Math.min(255, alpha)));
            g2.drawString("BREACHING TARGET...", x + 5, y + 12);

            int barX = x + 5, barY = y + 18, barW = pw - 10, barH = 8;
            g2.setColor(new Color(30, 30, 30, Math.min(200, alpha)));
            g2.fillRect(barX, barY, barW, barH);

            int progreso = Math.min(100, p.progreso + tick % 60);
            Color barColor = progreso > 80 ? new Color(0, 255, 0, Math.min(255, alpha)) : new Color(0, 255, 255, Math.min(255, alpha));
            g2.setColor(barColor);
            g2.fillRect(barX, barY, (int)(barW * progreso / 100.0), barH);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.setColor(new Color(200, 200, 200, Math.min(200, alpha)));
            g2.drawString(progreso + "% COMPLETE", x + 5, y + 34);
        }

        private void dibujarPopupBug(Graphics2D g2, int x, int y, GlitchPopup p, int alpha) {
            int pw = 70, ph = 58;
            g2.setColor(new Color(15, 0, 20, Math.min(140, alpha)));
            g2.fillRect(x, y, pw, ph);
            g2.setColor(new Color(0, 255, 0, Math.min(180, alpha)));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x, y, pw, ph);

            g2.setFont(new Font("Monospaced", Font.BOLD, 8));
            g2.setColor(new Color(0, 255, 136, Math.min(255, alpha)));
            for (int i = 0; i < BUG_ART.length; i++) {
                g2.drawString(BUG_ART[i], x + 8, y + 13 + i * 11);
            }
        }

        private void dibujarPopupWarning(Graphics2D g2, int x, int y, GlitchPopup p, int alpha) {
            int pw = 160, ph = 30;
            float flash = (float)(Math.sin(tick * 0.5) * 0.5 + 0.5);
            g2.setColor(new Color(40, 0, 0, (int)(flash * Math.min(120, alpha))));
            g2.fillRect(x, y, pw, ph);
            g2.setColor(new Color(255, 50, 50, Math.min(220, alpha)));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(x, y, pw, ph);

            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            g2.setColor(new Color(255, 50, 50, Math.min(255, alpha)));
            g2.drawString("!! " + p.mensaje, x + 8, y + 20);
        }

        private void dibujarPopupBugAvatar(Graphics2D g2, int x, int y, GlitchPopup p, int alpha) {
            if (p.bugRef == null) { dibujarPopupBug(g2, x, y, p, alpha); return; }

            int imgSize = 80;

            // Obtener frame animado del bug (usa su AnimState)
            Bug.BugGesture gesto = p.bugRef.getAnimState().getGestoActual();
            float progreso = p.bugRef.getAnimState().getProgreso();
            BufferedImage avatarImg = BugAvatarFactory.getFrameForGesture(p.bugRef, gesto, progreso);
            if (avatarImg == null) avatarImg = BugAvatarFactory.obtener(p.bugRef);

            if (avatarImg != null) {
                java.awt.Composite prev = g2.getComposite();
                float imgAlpha = Math.min(1f, alpha / 200f);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, imgAlpha));
                g2.drawImage(avatarImg, x + 5, y + 3, imgSize, imgSize, null);
                // RGB shift glitch sutil
                if (tick % 8 < 2) {
                    g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.20f));
                    g2.drawImage(avatarImg, x + 5 + rnd.nextInt(4) - 2, y + 3, imgSize, imgSize, null);
                }
                g2.setComposite(prev);
            }

            Color neon = p.bugRef.colorPrincipal;
            int nR = neon.getRed(), nG = neon.getGreen(), nB = neon.getBlue();

            // Linea de datos parpadeante bajo el avatar
            if (tick % 20 < 15) {
                g2.setFont(new Font("Monospaced", Font.BOLD, 8));
                g2.setColor(new Color(nR, nG, nB, Math.min(255, alpha)));
                String label = p.bugRef.etiquetaCorta;
                if (label.length() > 14) label = label.substring(0, 14);
                g2.drawString(label, x + 5, y + imgSize + 16);
            }

            // Rareza badge minimalista
            g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
            Color rc = p.bugRef.rareza.color;
            g2.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), Math.min(180, alpha)));
            g2.drawString("[" + p.bugRef.rareza.etiqueta + "]", x + 5, y + imgSize + 25);

            // Glitch horizontal ocasional (linea de interferencia)
            if (tick % 13 < 2) {
                int gy2 = y + rnd.nextInt(imgSize);
                g2.setColor(new Color(p.bugRef.colorAccento.getRed(),
                                      p.bugRef.colorAccento.getGreen(),
                                      p.bugRef.colorAccento.getBlue(),
                                      Math.min(60, alpha)));
                g2.fillRect(x, gy2, imgSize + 10, 1);
            }
        }

        class GlitchPopup {
            int x, y, vida, tipo, progreso;
            String mensaje;
            Bug bugRef; // para popup tipo 5 (avatar de bug desbloqueado)
        }

        class GlitchFragment {
            float x, y, vx, vy;
            int vida;
            String texto;
        }
    }

    // === PANEL KALI LOGO ANIMADO: Breathing + Float con Drop Shadow Cyan ===
    class PanelKaliLogoAnimado extends JPanel {
        private static final long serialVersionUID = 1L;
        private int tick = 0;

        PanelKaliLogoAnimado() {
            setOpaque(false);
            // Timer dedicado a ~30fps (33ms) para animacion fluida sin saturar EDT
            timerLogoAnimacion = new Timer(33, e -> {
                tick++;
                repaint();
            });
            timerLogoAnimacion.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2;

            // === BREATHING: escala oscila 0.98 - 1.02 ===
            double breathScale = 1.0 + 0.02 * Math.sin(tick * 0.06);
            // === FLOAT: desplazamiento Y oscila +/- 1.5px ===
            double floatY = 1.5 * Math.sin(tick * 0.04);

            AffineTransform oldTransform = g2.getTransform();
            g2.translate(cx, cy + floatY);
            g2.scale(breathScale, breathScale);

            // === DROP SHADOW / GLOW CYAN ===
            float glowPulse = 0.4f + 0.25f * (float) Math.sin(tick * 0.05);
            for (int r = 22; r > 0; r -= 4) {
                g2.setColor(new Color(0x00, 0xFF, 0xFF, (int)(glowPulse * 10)));
                g2.fillOval(-r, -r, r * 2, r * 2);
            }

            // === DRAGON KALI SILHOUETTE (escalado para 52x52) ===
            float sc = Math.min(w / 110f, h / 110f);
            g2.scale(sc, sc);

            Color c1 = new Color(0x00, 0xEE, 0xFF); // cyan neon
            Color c2 = new Color(0x00, 0xAA, 0xCC);
            Color c3 = new Color(0x00, 0x77, 0x99);

            // Cresta superior (5 puntas)
            g2.setColor(c1);
            int[] crX = {-28, -17, -12, -7, 0, 7, 12, 17, 28};
            int[] crY = {-5, -30, -10, -35, -40, -35, -10, -30, -5};
            g2.fillPolygon(crX, crY, 9);

            // Cuerpo central
            g2.setColor(c2);
            int[] bdX = {-24, 0, 24, 17, 0, -17};
            int[] bdY = {-3, -15, -3, 10, 5, 10};
            g2.fillPolygon(bdX, bdY, 6);

            // Mandibula
            g2.setColor(c3);
            int[] jwX = {-17, 0, 17, 12, 0, -12};
            int[] jwY = {10, 5, 10, 22, 18, 22};
            g2.fillPolygon(jwX, jwY, 6);

            // Cuello
            g2.setColor(new Color(0x00, 0x55, 0x66));
            int[] nkX = {-9, 0, 9};
            int[] nkY = {22, 33, 22};
            g2.fillPolygon(nkX, nkY, 3);

            // Ojo central brillante
            g2.setColor(new Color(0xFF, 0xFF, 0xFF, 220));
            g2.fillOval(-4, -10, 8, 6);
            g2.setColor(new Color(0x00, 0xFF, 0xFF));
            g2.fillOval(-2, -9, 4, 4);

            // Detalle lineas
            g2.setColor(new Color(0x00, 0x55, 0x66));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawLine(-17, -30, -14, -12);
            g2.drawLine(17, -30, 14, -12);
            g2.drawLine(-7, -35, -5, -18);
            g2.drawLine(7, -35, 5, -18);
            g2.drawLine(0, -40, 0, -22);

            g2.setTransform(oldTransform);
            g2.dispose();
        }
    }

    // === PANEL DE FONDO ANIMADO: renderiza el wallpaper activo detras de toda la UI ===
    class PanelFondoAnimado extends JPanel {
        private static final long serialVersionUID = 1L;
        private int tick = 0;
        private Timer timerFondo;

        PanelFondoAnimado() {
            setOpaque(true);
            setBackground(FONDO_NEGRO);
            timerFondo = new Timer(50, e -> {
                tick++;
                repaint();
            });
            timerFondo.start();
        }

        public void detener() {
            if (timerFondo != null) {
                timerFondo.stop();
                timerFondo = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            FondoJuego fondo = (gestorFondos != null) ? gestorFondos.getFondoActivo() : null;
            if (fondo == null || "default_void".equals(fondo.id)) {
                // Fondo por defecto: negro puro con sutil grid
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(FONDO_NEGRO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Grid tenue default
                g2.setColor(new Color(0x00, 0x44, 0x55, 10));
                int gs = 30;
                int off = (tick * 1) % gs;
                for (int y = off; y < getHeight(); y += gs) g2.drawLine(0, y, getWidth(), y);
                for (int x = off; x < getWidth(); x += gs) g2.drawLine(x, 0, x, getHeight());
                g2.dispose();
            } else {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FondoFactory.renderizar(g2, getWidth(), getHeight(), fondo, tick);
                // Capa oscura semi-transparente para que la UI sea legible
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}

