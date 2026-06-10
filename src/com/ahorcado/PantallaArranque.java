package com.ahorcado;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.FlowLayout;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.ahorcado.util.GestorEstadisticas;
import com.ahorcado.util.AudioController;
import com.ahorcado.util.GestorEconomia;
import com.ahorcado.util.GestorMisiones;

/**
 * PantallaArranque - Pantalla de inicio / menu principal del juego.
 * 
 * Esta clase EXTIENDE JFrame (HERENCIA) y muestra la pantalla de arranque
 * con una animacion tipo "boot" de sistema operativo hacker, el formulario
 * pa que el jugador ponga su nombre, seleccione dificultad y tema,
 * y el boton pa iniciar la partida.
 * 
 * Tambien tiene el mapa de progresion, las misiones, la tienda, el perfil
 * del jugador con sus estadisticas y nivel... basicamente todo el menu.
 * 
 * NOTA: esta clase quedo muy larga perdon profe, es que tiene muchos paneles
 * y componentes de Swing. Intente separar en metodos pero aun asi quedo grande.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Proyecto final POO
 */
public class PantallaArranque extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private Font globalFont = new Font("Monospaced", Font.PLAIN, 12);
    private Random random = new Random();
    
    private PanelBootMatrix panelMatrix;
    private JPanel panelLogin;
    private JPanel pnlWallet;
    private PanelMapa panelMapa;
    private PanelLiveMatrix panelLiveMatrix;
    private AudioController audioController;
    private InterfazAhorcado interfazActiva; // Referencia a la interfaz activa para sincronización
    private Timer timerEstado;
    private Timer bootTimer;
    private Timer timerReloj;
    private PanelKaliLogo panelKaliLogo;
    
    public PantallaArranque() {
        this(false);
    }
    
    public PantallaArranque(boolean skipBoot) {
        setTitle("DEEP-THOUGHT OS BOOT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.BLACK);
        
        // Initialize sound manager
        audioController = AudioController.getInstance();
        
        // Add window listener to stop sounds when closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                limpiarRecursos();
            }
        });
        
        // No longer using separate menu bar - consolidated into single header
        
        panelMatrix = new PanelBootMatrix();
        getContentPane().add(panelMatrix, BorderLayout.CENTER);
        
        if (skipBoot) {
            // Skip boot sequence and go directly to login
            mostrarLogin();
        } else {
            // Boot sequence lasts 6.5 seconds
            bootTimer = new Timer(6500, e -> {
                ((Timer)e.getSource()).stop();
                bootTimer = null;
                mostrarLogin();
            });
            bootTimer.start();
        }
    }

    @Override
    public void dispose() {
        limpiarRecursos();
        super.dispose();
    }

    private void limpiarRecursos() {
        if (bootTimer != null) { bootTimer.stop(); bootTimer = null; }
        if (timerReloj != null) { timerReloj.stop(); timerReloj = null; }
        if (timerEstado != null) { timerEstado.stop(); timerEstado = null; }
        if (panelMatrix != null) { panelMatrix.stopTimer(); panelMatrix = null; }
        if (panelKaliLogo != null) { panelKaliLogo.detener(); panelKaliLogo = null; }
        if (panelLiveMatrix != null) { panelLiveMatrix.detener(); panelLiveMatrix = null; }
        if (audioController != null) audioController.stopAllSounds();
    }
    
    private void configurarMenuKali() {
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        menuBar.setBackground(new Color(0x5B, 0x2A, 0x86)); // Kali Purple
        menuBar.setBorder(new javax.swing.border.MatteBorder(0, 0, 2, 0, new Color(0x3A, 0x1A, 0x56)));
        
        // Logo ASCII del dragón de Kali
        javax.swing.JMenu menuLogo = new javax.swing.JMenu(" 🐉 ");
        menuLogo.setForeground(Color.WHITE);
        menuLogo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        
        javax.swing.JMenu menuApli = new javax.swing.JMenu("| APLICACIONES ");
        menuApli.setForeground(Color.LIGHT_GRAY);
        menuApli.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        
        javax.swing.JMenuItem mnuExit = new javax.swing.JMenuItem("Cerrar Sesión");
        mnuExit.setBackground(new Color(0x1A, 0x1A, 0x1A));
        mnuExit.setForeground(Color.WHITE);
        mnuExit.addActionListener(e -> System.exit(0));
        menuApli.add(mnuExit);
        
        javax.swing.JMenu menuLugar = new javax.swing.JMenu("| LUGARES ");
        menuLugar.setForeground(Color.LIGHT_GRAY);
        menuLugar.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        
        javax.swing.JMenu menuHerra = new javax.swing.JMenu("| HERRAMIENTAS ");
        menuHerra.setForeground(Color.LIGHT_GRAY);
        menuHerra.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        
        javax.swing.JMenu menuSist = new javax.swing.JMenu("| SISTEMA ");
        menuSist.setForeground(Color.LIGHT_GRAY);
        menuSist.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        
        menuBar.add(menuLogo);
        menuBar.add(menuApli);
        menuBar.add(menuLugar);
        menuBar.add(menuHerra);
        menuBar.add(menuSist);
        
        // Panel derecho con indicadores de sistema
        JPanel panelIndicadores = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        panelIndicadores.setBackground(new Color(0x5B, 0x2A, 0x86));
        panelIndicadores.setOpaque(true);
        
        JLabel lblReloj = new JLabel();
        lblReloj.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        lblReloj.setForeground(Color.WHITE);
        
        JLabel lblUsuario = new JLabel("root@kali");
        lblUsuario.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        lblUsuario.setForeground(new Color(0x00, 0xFF, 0x88));
        
        JLabel lblRed = new JLabel("🌐 CONNECTED");
        lblRed.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        lblRed.setForeground(new Color(0x00, 0xFF, 0xFF));
        
        panelIndicadores.add(lblReloj);
        panelIndicadores.add(lblUsuario);
        panelIndicadores.add(lblRed);
        
        menuBar.add(panelIndicadores);
        
        // Timer para actualizar reloj
        if (timerReloj != null) timerReloj.stop();
        timerReloj = new Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            lblReloj.setText(sdf.format(new java.util.Date()));
        });
        timerReloj.start();
        
        setJMenuBar(menuBar);
    }
    
    private void mostrarLogin() {
        if (panelMatrix != null) panelMatrix.stopTimer();
        panelMatrix.setLayout(new java.awt.CardLayout());
        
        // Start background music for menu
        if (audioController != null) {
            audioController.reproducirMusicaMenu();
        }
        
        JPanel pnlDashboard = new JPanel(new BorderLayout());
        pnlDashboard.setBackground(Color.BLACK);
        
        // --- BARRA SUPERIOR (KALI HEADER) - CONSOLIDATED SINGLE BANNER ---
        JPanel pnlKaliHeader = new JPanel(new BorderLayout());
        pnlKaliHeader.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        pnlKaliHeader.setBackground(new Color(94, 28, 107));
        pnlKaliHeader.setBorder(new javax.swing.border.MatteBorder(0, 0, 2, 0, new Color(0x3A, 0x1A, 0x56)));
        
        // Left side: Logo and menu items
        JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelIzquierdo.setBackground(new Color(94, 28, 107));
        panelIzquierdo.setOpaque(true);
        
        JLabel lblLogo = new JLabel("🐉");
        lblLogo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        lblLogo.setForeground(Color.WHITE);
        
        JLabel lblMenu = new JLabel("APPLICATIONS | PLACES | TOOLS | SYSTEM");
        lblMenu.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblMenu.setForeground(Color.WHITE);
        
        panelIzquierdo.add(lblLogo);
        panelIzquierdo.add(lblMenu);
        pnlKaliHeader.add(panelIzquierdo, BorderLayout.WEST);
        
        // Right side: All system indicators (time, root@kali, connected, CPU, RAM)
        JPanel panelIndicadores = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        panelIndicadores.setBackground(new Color(94, 28, 107));
        panelIndicadores.setOpaque(true);
        
        JLabel lblReloj = new JLabel();
        lblReloj.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        lblReloj.setForeground(Color.WHITE);
        
        JLabel lblUsuario = new JLabel("root@kali");
        lblUsuario.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        lblUsuario.setForeground(new Color(0x00, 0xFF, 0x88));
        
        JLabel lblRed = new JLabel("🌐 CONNECTED");
        lblRed.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        lblRed.setForeground(new Color(0x00, 0xFF, 0xFF));
        
        JLabel lblCPU = new JLabel("CPU: " + (15 + random.nextInt(20)) + "%");
        lblCPU.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblCPU.setForeground(new Color(0x00, 0xFF, 0x88));
        
        JLabel lblRAM = new JLabel("RAM: " + (2 + random.nextInt(3)) + "." + random.nextInt(9) + "GB");
        lblRAM.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblRAM.setForeground(new Color(0x00, 0xFF, 0xFF));
        
        // Sound control button
        JButton btnSonido = new JButton("[SOUND: ON]");
        btnSonido.setFont(new Font("Monospaced", Font.BOLD, 10));
        btnSonido.setForeground(new Color(0x00, 0xFF, 0x88));
        btnSonido.setBackground(new Color(94, 28, 107));
        btnSonido.setBorderPainted(false);
        btnSonido.setFocusPainted(false);
        btnSonido.setContentAreaFilled(false);
        btnSonido.addActionListener(e -> {
            boolean nuevoEstado = !audioController.isSilenciado();
            audioController.setSilenciado(nuevoEstado);
            btnSonido.setText(nuevoEstado ? "[SOUND: OFF]" : "[SOUND: ON]");
            btnSonido.setForeground(nuevoEstado ? new Color(0xFF, 0x00, 0x00) : new Color(0x00, 0xFF, 0x88));
            if (!nuevoEstado) {
                audioController.reproducirMusicaMenu();
            }
        });
        
        panelIndicadores.add(lblReloj);
        panelIndicadores.add(lblUsuario);
        panelIndicadores.add(lblRed);
        panelIndicadores.add(lblCPU);
        panelIndicadores.add(lblRAM);
        panelIndicadores.add(btnSonido);
        pnlKaliHeader.add(panelIndicadores, BorderLayout.EAST);
        
        // Timer para actualizar timestamp y estado del sistema (2000ms para movimiento más suave)
        if (timerEstado != null) timerEstado.stop();
        timerEstado = new Timer(2000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            lblReloj.setText(sdf.format(new java.util.Date()));
            int cpuValue = 15 + random.nextInt(20);
            int ramValue = 2 + random.nextInt(3);
            int ramDecimal = random.nextInt(9);
            lblCPU.setText("CPU: " + cpuValue + "%");
            lblRAM.setText("RAM: " + ramValue + "." + ramDecimal + "GB");
        });
        timerEstado.start();
        
        // --- PANEL PRINCIPAL DIVIDIDO EN 3 COLUMNAS (25%-40%-35%) ---
        JPanel pnlMainSplit = new JPanel();
        pnlMainSplit.setLayout(new BoxLayout(pnlMainSplit, BoxLayout.X_AXIS));
        pnlMainSplit.setBackground(Color.BLACK);
        pnlMainSplit.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // --- COLUMNA IZQUIERDA: LIVE MATRIX (25%) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.BLACK);
        leftPanel.setPreferredSize(new Dimension(400, Integer.MAX_VALUE));
        
        // Panel Logo Kali Linux (arriba)
        panelKaliLogo = new PanelKaliLogo();
        panelKaliLogo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));
        leftPanel.add(panelKaliLogo);
        leftPanel.add(Box.createVerticalStrut(10));
        
        // Panel Decorativo Técnico (debajo del logo)
        PanelDecorativoTecnico panelDecorativo = new PanelDecorativoTecnico();
        panelDecorativo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 140));
        leftPanel.add(panelDecorativo);
        leftPanel.add(Box.createVerticalStrut(10));
        
        // Panel Live Matrix Datafeed (columna izquierda completa)
        panelLiveMatrix = new PanelLiveMatrix();
        leftPanel.add(panelLiveMatrix);
        leftPanel.add(Box.createVerticalGlue());
        
        // --- COLUMNA CENTRAL: LOGIN (40%) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.BLACK);
        centerPanel.setPreferredSize(new Dimension(640, Integer.MAX_VALUE));
        
        // Usar instancia singleton de GestorEstadisticas para consistencia
        GestorEstadisticas stats = GestorEstadisticas.getInstancia();
        
        // --- PANEL CENTRAL REDISE?ADO (NEXUS-7 COMMAND CENTER) ---
        JPanel pnlCentral = new JPanel();
        pnlCentral.setLayout(new BoxLayout(pnlCentral, BoxLayout.Y_AXIS));
        pnlCentral.setOpaque(false);
        pnlCentral.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(new Color(0x00, 0xFF, 0xFF), 2),
                "[ NEXUS-7 COMMAND CENTER ]",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 13),
                new Color(0x00, 0xFF, 0xFF)
            ),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        // === SECCION 1: LOGIN COMPACTO ===
        JPanel pnlLoginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        pnlLoginRow.setOpaque(false);
        pnlLoginRow.setMaximumSize(new Dimension(600, 45));
        JLabel lblLoginTag = new JLabel("OPERADOR:");
        lblLoginTag.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblLoginTag.setForeground(new Color(0x00, 0xFF, 0xFF));
        JTextField campoUsuario = new JTextField("GUEST_77");
        campoUsuario.setPreferredSize(new Dimension(220, 32));
        campoUsuario.setBackground(new Color(0x0A, 0x0E, 0x14));
        campoUsuario.setForeground(Color.CYAN);
        campoUsuario.setCaretColor(Color.CYAN);
        campoUsuario.setFont(new Font("Monospaced", Font.BOLD, 14));
        campoUsuario.setHorizontalAlignment(JTextField.CENTER);
        campoUsuario.setBorder(new LineBorder(new Color(0x00, 0xFF, 0xFF), 1));
        // Limitar nombre a 15 caracteres para evitar desborde en la interfaz
        ((javax.swing.text.AbstractDocument) campoUsuario.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            private static final int MAX_CHARS = 15;
            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (string == null) return;
                if ((fb.getDocument().getLength() + string.length()) <= MAX_CHARS)
                    super.insertString(fb, offset, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                if (text == null) return;
                int newLength = fb.getDocument().getLength() - length + text.length();
                if (newLength <= MAX_CHARS)
                    super.replace(fb, offset, length, text, attrs);
            }
        });
        pnlLoginRow.add(lblLoginTag);
        pnlLoginRow.add(campoUsuario);
        pnlCentral.add(pnlLoginRow);
        pnlCentral.add(Box.createVerticalStrut(8));

        // Linea separadora decorativa
        JPanel sep1 = new JPanel() { protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setColor(new Color(0x00,0xFF,0xFF,60)); g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{4,4},0)); g2.drawLine(10,1,getWidth()-10,1); } };
        sep1.setOpaque(false);
        sep1.setMaximumSize(new Dimension(550, 3));
        sep1.setPreferredSize(new Dimension(550, 3));
        pnlCentral.add(sep1);
        pnlCentral.add(Box.createVerticalStrut(5));

        // === SECCION 2: KALI KOINTS WALLET + BLACK MARKET (alta prioridad, arriba) ===
        GestorEconomia eco = GestorEconomia.getInstancia();
        pnlWallet = new JPanel();
        pnlWallet.setLayout(new BoxLayout(pnlWallet, BoxLayout.X_AXIS));
        pnlWallet.setOpaque(false);
        pnlWallet.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFF, 0xD7, 0x00, 180), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        pnlWallet.setMaximumSize(new Dimension(550, 42));
        pnlWallet.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblKKIcon = new JLabel("\u00A4 ");
        lblKKIcon.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblKKIcon.setForeground(new Color(0xFF, 0xD7, 0x00));
        pnlWallet.add(lblKKIcon);

        JLabel lblKK = new JLabel(String.format("KALI KOINTS: %,d", eco.getSaldo()));
        lblKK.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblKK.setForeground(new Color(0xFF, 0xD7, 0x00));
        pnlWallet.add(lblKK);

        pnlWallet.add(Box.createHorizontalGlue());

        JButton btnTienda = new JButton("BLACK MARKET");
        btnTienda.setForeground(new Color(0xFF, 0xD7, 0x00));
        btnTienda.setBackground(new Color(0x1A, 0x12, 0x00));
        btnTienda.setFont(new Font("Monospaced", Font.BOLD, 11));
        btnTienda.setFocusPainted(false);
        btnTienda.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFF, 0xD7, 0x00), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btnTienda.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTienda.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnTienda.setBackground(new Color(0x2A, 0x1E, 0x00));
                btnTienda.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0xFF, 0xD7, 0x00), 2),
                    BorderFactory.createEmptyBorder(3, 9, 3, 9)));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnTienda.setBackground(new Color(0x1A, 0x12, 0x00));
                btnTienda.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0xFF, 0xD7, 0x00), 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)));
            }
        });
        btnTienda.addActionListener(e -> {
            TiendaClandestina.abrir(this);
            lblKK.setText(String.format("KALI KOINTS: %,d", eco.getSaldo()));
        });
        pnlWallet.add(btnTienda);
        pnlCentral.add(pnlWallet);
        pnlCentral.add(Box.createVerticalStrut(6));

        // === SECCION 3: TEMA Y DIFICULTAD EN FILA ===
        JPanel pnlConfigRow = new JPanel();
        pnlConfigRow.setLayout(new BoxLayout(pnlConfigRow, BoxLayout.X_AXIS));
        pnlConfigRow.setOpaque(false);
        pnlConfigRow.setMaximumSize(new Dimension(600, 60));
        pnlConfigRow.setAlignmentX(CENTER_ALIGNMENT);

        JPanel pnlTemaBox = new JPanel();
        pnlTemaBox.setLayout(new BoxLayout(pnlTemaBox, BoxLayout.Y_AXIS));
        pnlTemaBox.setOpaque(false);
        JLabel lblTm = new JLabel("TEMA VISUAL");
        lblTm.setFont(new Font("Monospaced", Font.BOLD, 10));
        lblTm.setForeground(new Color(0xFF, 0x88, 0x00));
        lblTm.setAlignmentX(CENTER_ALIGNMENT);
        String[] temas = {"Cian Neon (Estandar)", "Verde Matrix (Legacy)", "Ambar Sistema (Warning)", "Rosa Neon (Glitch)"};
        JComboBox<String> comboTema = new JComboBox<>(temas);
        comboTema.setMaximumSize(new Dimension(240, 26));
        comboTema.setBackground(new Color(0x0A, 0x0E, 0x14));
        comboTema.setForeground(Color.CYAN);
        comboTema.setFont(new Font("Monospaced", Font.PLAIN, 10));
        comboTema.setBorder(new LineBorder(new Color(0xFF, 0x88, 0x00), 1));
        pnlTemaBox.add(lblTm);
        pnlTemaBox.add(Box.createVerticalStrut(3));
        pnlTemaBox.add(comboTema);

        JPanel pnlDifBox = new JPanel();
        pnlDifBox.setLayout(new BoxLayout(pnlDifBox, BoxLayout.Y_AXIS));
        pnlDifBox.setOpaque(false);
        JLabel lblDf = new JLabel("ENCRIPTACION");
        lblDf.setFont(new Font("Monospaced", Font.BOLD, 10));
        lblDf.setForeground(new Color(0xFF, 0x44, 0x44));
        lblDf.setAlignmentX(CENTER_ALIGNMENT);
        String[] dificultades = {"NORMAL (7 int / 3 pistas)", "DIFICIL (6 int / 0 pistas)", "PESADILLA (5 int / 0 pistas)"};
        JComboBox<String> comboDif = new JComboBox<>(dificultades);
        comboDif.setMaximumSize(new Dimension(240, 26));
        comboDif.setBackground(new Color(0x0A, 0x0E, 0x14));
        comboDif.setForeground(Color.CYAN);
        comboDif.setFont(new Font("Monospaced", Font.PLAIN, 10));
        comboDif.setBorder(new LineBorder(new Color(0xFF, 0x44, 0x44), 1));
        pnlDifBox.add(lblDf);
        pnlDifBox.add(Box.createVerticalStrut(3));
        pnlDifBox.add(comboDif);

        pnlConfigRow.add(Box.createHorizontalGlue());
        pnlConfigRow.add(pnlTemaBox);
        pnlConfigRow.add(Box.createHorizontalStrut(12));
        pnlConfigRow.add(pnlDifBox);
        pnlConfigRow.add(Box.createHorizontalGlue());
        pnlCentral.add(pnlConfigRow);
        pnlCentral.add(Box.createVerticalStrut(4));

        // === SECCION 4: CANALES DE AUDIO (compacto) ===
        JPanel pnlCanales = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 1));
        pnlCanales.setOpaque(false);
        pnlCanales.setMaximumSize(new Dimension(600, 26));
        final JCheckBox cbFeedback = new JCheckBox("FEEDBACK", false);
        cbFeedback.setFont(new Font("Monospaced", Font.BOLD, 9));
        cbFeedback.setForeground(new Color(0xAA, 0xAA, 0xAA));
        cbFeedback.setOpaque(false); cbFeedback.setFocusPainted(false);
        // Listener para sincronizar con InterfazAhorcado activa
        cbFeedback.addActionListener(e -> {
            if (interfazActiva != null) {
                interfazActiva.setAudioOperativoHabilitado(cbFeedback.isSelected());
            }
        });
        JCheckBox cbSfx = new JCheckBox("SFX", true);
        cbSfx.setFont(new Font("Monospaced", Font.BOLD, 9));
        cbSfx.setForeground(new Color(0xAA, 0xAA, 0xAA));
        cbSfx.setOpaque(false); cbSfx.setFocusPainted(false);
        JLabel lblCh = new JLabel("CANALES:");
        lblCh.setFont(new Font("Monospaced", Font.BOLD, 9));
        lblCh.setForeground(new Color(0x00, 0xFF, 0x88));
        pnlCanales.add(lblCh);
        pnlCanales.add(cbFeedback);
        pnlCanales.add(cbSfx);
        pnlCentral.add(pnlCanales);
        pnlCentral.add(Box.createVerticalStrut(5));

        // === SECCION 5: PERFIL DEL OPERADOR (compacto) ===
        JPanel pnlPerfil = new JPanel();
        pnlPerfil.setLayout(new BoxLayout(pnlPerfil, BoxLayout.Y_AXIS));
        pnlPerfil.setOpaque(false);
        pnlPerfil.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x00, 0xFF, 0xFF, 100), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        pnlPerfil.setMaximumSize(new Dimension(550, 72));

        JPanel perfilRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        perfilRow1.setOpaque(false);
        JLabel lblPRango = new JLabel("RANGO: " + stats.getRango());
        lblPRango.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblPRango.setForeground(new Color(0x00, 0xFF, 0x88));
        JLabel lblPNivel = new JLabel("   LV." + stats.getNivel());
        lblPNivel.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblPNivel.setForeground(new Color(0x00, 0xFF, 0xFF));
        perfilRow1.add(lblPRango);
        perfilRow1.add(lblPNivel);
        pnlPerfil.add(perfilRow1);

        int xpA = stats.getExperiencia();
        int xpN = stats.getXpSiguienteNivel();
        int xpPct = xpN > 0 ? Math.min(xpA * 100 / xpN, 100) : 0;
        JPanel pnlXPBar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth() - 4;
                int h = getHeight() - 4;
                g2.setColor(new Color(0x1A, 0x1A, 0x1A));
                g2.fillRoundRect(2, 2, w, h, 6, 6);
                g2.setColor(new Color(0x00, 0xFF, 0xFF));
                g2.fillRoundRect(2, 2, (int)(w * xpPct / 100.0), h, 6, 6);
                g2.setColor(new Color(0x00, 0x88, 0x99));
                g2.drawRoundRect(2, 2, w, h, 6, 6);
                g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2.setColor(Color.WHITE);
                String txt = "XP: " + xpA + "/" + xpN + " (" + xpPct + "%)";
                int tw = g2.getFontMetrics().stringWidth(txt);
                g2.drawString(txt, (getWidth() - tw) / 2, h / 2 + 5);
            }
        };
        pnlXPBar.setOpaque(false);
        pnlXPBar.setPreferredSize(new Dimension(500, 16));
        pnlXPBar.setMaximumSize(new Dimension(550, 16));
        pnlPerfil.add(pnlXPBar);
        pnlPerfil.add(Box.createVerticalStrut(2));

        JPanel perfilRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        perfilRow2.setOpaque(false);
        JLabel lblPStats = new JLabel(String.format("V:%d  P:%.0f%%  R:%d  LOGROS:%d", stats.getJuegosGanados(), stats.getPrecision(), stats.getRachaActual(), stats.getLogrosResumen().equals("SIN LOGROS") ? 0 : stats.getLogrosResumen().split("\\|").length));
        lblPStats.setFont(new Font("Monospaced", Font.PLAIN, 9));
        lblPStats.setForeground(new Color(0x77, 0x77, 0x77));
        perfilRow2.add(lblPStats);
        pnlPerfil.add(perfilRow2);

        pnlCentral.add(pnlPerfil);
        pnlCentral.add(Box.createVerticalStrut(6));

        // === SECCION 6: BOTON EJECUTAR (prominente) ===
        JButton btnStart = new JButton(">>> EJECUTAR PROTOCOLO DE EXTRACCION <<<");
        btnStart.setForeground(new Color(0x00, 0xFF, 0x88));
        btnStart.setBackground(new Color(0x05, 0x15, 0x08));
        btnStart.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnStart.setFocusPainted(false);
        btnStart.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x00, 0xFF, 0x88), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btnStart.setAlignmentX(CENTER_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(550, 46));
        btnStart.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnStart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnStart.setBackground(new Color(0x00, 0x33, 0x11));
                btnStart.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0x00, 0xFF, 0x00), 3),
                    BorderFactory.createEmptyBorder(9, 19, 9, 19)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnStart.setBackground(new Color(0x05, 0x15, 0x08));
                btnStart.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0x00, 0xFF, 0x88), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
        });
        btnStart.addActionListener(e -> {
            String temaSel = (String) comboTema.getSelectedItem();
            aplicarTema(temaSel);
            int diff = comboDif.getSelectedIndex();
            dispose();
            // Trivia siempre inicia en false, se controla desde el banner in-game
            iniciarJuego(diff, false, cbFeedback.isSelected(), cbSfx.isSelected(), campoUsuario.getText());
        });
        pnlCentral.add(btnStart);
        pnlCentral.add(Box.createVerticalStrut(6));

        // === SECCION 7: MISIONES DIARIAS (con reset automatico) ===
        GestorMisiones gm = GestorMisiones.getInstancia();
        JPanel pnlMisiones = new JPanel();
        pnlMisiones.setLayout(new BoxLayout(pnlMisiones, BoxLayout.Y_AXIS));
        pnlMisiones.setOpaque(false);
        pnlMisiones.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFF, 0x88, 0x00, 100), 1),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        pnlMisiones.setMaximumSize(new Dimension(550, 80));

        JPanel pnlMisionHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlMisionHeader.setOpaque(false);
        JLabel lblMT = new JLabel("MISIONES DIARIAS ");
        lblMT.setFont(new Font("Monospaced", Font.BOLD, 10));
        lblMT.setForeground(new Color(0xFF, 0x88, 0x00));
        JLabel lblMC = new JLabel("[" + gm.getMisionesDiariasCompletadas() + "/" + gm.getMisionesDiarias().size() + "]");
        lblMC.setFont(new Font("Monospaced", Font.PLAIN, 9));
        lblMC.setForeground(new Color(0x88, 0x88, 0x88));
        pnlMisionHeader.add(lblMT);
        pnlMisionHeader.add(lblMC);
        pnlMisiones.add(pnlMisionHeader);
        pnlMisiones.add(Box.createVerticalStrut(2));

        JPanel pnlMisionGrid = new JPanel(new java.awt.GridLayout(2, 3, 6, 1));
        pnlMisionGrid.setOpaque(false);
        pnlMisionGrid.setMaximumSize(new Dimension(550, 36));
        java.util.List<GestorMisiones.MisionDiaria> mDiarias = gm.getMisionesDiarias();
        for (int mi = 0; mi < Math.min(6, mDiarias.size()); mi++) {
            GestorMisiones.MisionDiaria md = mDiarias.get(mi);
            String icono = md.completada ? "\u2713" : "\u25CB";
            JLabel lbl = new JLabel(icono + " " + md.nombre + " [" + Math.min(md.progreso, md.meta) + "/" + md.meta + "]");
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 8));
            lbl.setForeground(md.completada ? new Color(0x00, 0xFF, 0x88) : new Color(0x88, 0x88, 0x88));
            pnlMisionGrid.add(lbl);
        }
        pnlMisiones.add(pnlMisionGrid);
        pnlCentral.add(pnlMisiones);
        pnlCentral.add(Box.createVerticalStrut(5));

        // === SECCION 8: BOTONES UTILITARIOS (Manual + Rangos + Operaciones en fila) ===
        JPanel pnlUtils = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pnlUtils.setOpaque(false);
        pnlUtils.setMaximumSize(new Dimension(550, 30));

        JButton btnManual = new JButton("MANUAL");
        btnManual.setForeground(new Color(0x88, 0x66, 0xFF));
        btnManual.setBackground(new Color(0x0A, 0x0E, 0x14));
        btnManual.setFont(new Font("Monospaced", Font.BOLD, 10));
        btnManual.setFocusPainted(false);
        btnManual.setBorder(new LineBorder(new Color(0x88, 0x66, 0xFF), 1));
        btnManual.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnManual.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnManual.setBackground(new Color(0x15, 0x0A, 0x25));
                btnManual.setBorder(new LineBorder(new Color(0xAA, 0x88, 0xFF), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnManual.setBackground(new Color(0x0A, 0x0E, 0x14));
                btnManual.setBorder(new LineBorder(new Color(0x88, 0x66, 0xFF), 1));
            }
        });
        btnManual.addActionListener(e -> mostrarManualJuego());

        JButton btnRangos = new JButton("RANGOS");
        btnRangos.setForeground(new Color(0xFF, 0x88, 0x00));
        btnRangos.setBackground(new Color(0x0A, 0x0E, 0x14));
        btnRangos.setFont(new Font("Monospaced", Font.BOLD, 10));
        btnRangos.setFocusPainted(false);
        btnRangos.setBorder(new LineBorder(new Color(0xFF, 0x88, 0x00), 1));
        btnRangos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRangos.addActionListener(e -> mostrarDialogoRangos(stats));

        JButton btnOperaciones = new JButton("OPERACIONES");
        btnOperaciones.setForeground(new Color(0x00, 0xFF, 0xCC));
        btnOperaciones.setBackground(new Color(0x0A, 0x0E, 0x14));
        btnOperaciones.setFont(new Font("Monospaced", Font.BOLD, 10));
        btnOperaciones.setFocusPainted(false);
        btnOperaciones.setBorder(new LineBorder(new Color(0x00, 0xFF, 0xCC), 1));
        btnOperaciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOperaciones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnOperaciones.setBackground(new Color(0x0A, 0x1A, 0x18));
                btnOperaciones.setBorder(new LineBorder(new Color(0x00, 0xFF, 0xCC), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnOperaciones.setBackground(new Color(0x0A, 0x0E, 0x14));
                btnOperaciones.setBorder(new LineBorder(new Color(0x00, 0xFF, 0xCC), 1));
            }
        });
        btnOperaciones.addActionListener(e -> mostrarDialogoOperaciones(stats));

        pnlUtils.add(btnManual);
        pnlUtils.add(btnRangos);
        pnlUtils.add(btnOperaciones);
        pnlCentral.add(pnlUtils);
        pnlCentral.add(Box.createVerticalStrut(4));

        // Texto decorativo inferior
        JLabel lblHash = new JLabel("SESSION: 0x" + Long.toHexString(System.currentTimeMillis()).toUpperCase());
        lblHash.setFont(new Font("Monospaced", Font.PLAIN, 8));
        lblHash.setForeground(new Color(0x33, 0x33, 0x33));
        lblHash.setAlignmentX(CENTER_ALIGNMENT);
        pnlCentral.add(lblHash);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(pnlCentral);
        centerPanel.add(Box.createVerticalGlue());
        
        // --- COLUMNA DERECHA: MAPA/HUD/LOG (35%) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.BLACK);
        rightPanel.setPreferredSize(new Dimension(560, Integer.MAX_VALUE));
        
        // Panel Mapa (con barra de control de sector integrada)
        PanelMapa panelMapa = new PanelMapa(stats);
        panelMapa.setPreferredSize(new Dimension(Integer.MAX_VALUE, 450));
        rightPanel.add(panelMapa);
        rightPanel.add(Box.createVerticalStrut(8));
        
        // Panel Operational HUD (compacto)
        PanelOperationalHUD panelHUD = new PanelOperationalHUD(stats);
        panelHUD.setPreferredSize(new Dimension(Integer.MAX_VALUE, 260));
        rightPanel.add(panelHUD);
        rightPanel.add(Box.createVerticalStrut(8));
        
        // Panel Feedback Log (compacto)
        PanelFeedbackLog panelLog = new PanelFeedbackLog();
        panelLog.setPreferredSize(new Dimension(Integer.MAX_VALUE, 140));
        rightPanel.add(panelLog);
        
        pnlMainSplit.add(leftPanel);
        pnlMainSplit.add(Box.createHorizontalStrut(10));
        pnlMainSplit.add(centerPanel);
        pnlMainSplit.add(Box.createHorizontalStrut(10));
        pnlMainSplit.add(rightPanel);
        
        pnlDashboard.add(pnlKaliHeader, BorderLayout.NORTH);
        pnlDashboard.add(pnlMainSplit, BorderLayout.CENTER);
        
        // --- PANEL DE SCANLINES (Efecto visual general mejorado) ---
        JPanel panelScanlines = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                
                // Scanlines más sutiles y realistas
                g2.setColor(new Color(0, 0, 0, 20));
                for (int y = 0; y < h; y += 2) {
                    g2.drawLine(0, y, w, y);
                }
                
                // Efecto de viñeta sutil en los bordes
                int vignetteSize = 50;
                for (int i = 0; i < vignetteSize; i++) {
                    float alpha = (vignetteSize - i) / (float) vignetteSize * 0.15f;
                    g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                    g2.drawRect(i, i, w - 2 * i, h - 2 * i);
                }
            }
        };
        panelScanlines.setOpaque(false);
        panelScanlines.setLayout(new BorderLayout());
        panelScanlines.add(pnlDashboard, BorderLayout.CENTER);
        
        panelMatrix.add(panelScanlines, "LOGIN");
        ((java.awt.CardLayout)panelMatrix.getLayout()).show(panelMatrix, "LOGIN");
        
        revalidate();
        repaint();
    }
    
    private void mostrarDialogoRangos(GestorEstadisticas stats) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "CLASIFICACION DE RANGOS", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(0x05, 0x1A, 0x1E));
        dialog.setLayout(new BorderLayout());
        
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setBackground(new Color(0x05, 0x1A, 0x1E));
        pnlCentro.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] rangos = {
            "RECLUTA DIGITAL (Nivel 1)", 
            "OPERADOR JUNIOR (Nivel 2-3)", 
            "ANALISTA DE SISTEMAS (Nivel 4-5)", 
            "TECNICO DE RED (Nivel 6-7)", 
            "ESPECIALISTA CYBER (Nivel 8-9)", 
            "HACKER ETICO (Nivel 10-11)",
            "ARQUITECTO NEXUS (Nivel 12-14)",
            "MAESTRO DECODER (Nivel 15-17)",
            "LEYENDA DEL SISTEMA (Nivel 18+)"
        };
        
        // Determinar rango actual del usuario
        int nivelActual = stats.getNivel();
        String rangoActual = stats.getRango();
        
        for (String r : rangos) {
            JLabel lbl = new JLabel(r);
            lbl.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            if (r.startsWith(rangoActual)) {
                lbl.setForeground(new Color(0x00, 0xFF, 0x00));
                lbl.setText(">> " + r + " << [ACTUAL]");
            } else {
                lbl.setForeground(new Color(0x00, 0x88, 0x99));
            }
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            pnlCentro.add(lbl);
            pnlCentro.add(Box.createVerticalStrut(15));
        }
        
        JButton btnCerrar = new JButton("CERRAR");
        btnCerrar.setBackground(Color.BLACK);
        btnCerrar.setForeground(Color.CYAN);
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(e -> dialog.dispose());
        
        JPanel pnlSur = new JPanel();
        pnlSur.setOpaque(false);
        pnlSur.add(btnCerrar);
        
        dialog.add(pnlCentro, BorderLayout.CENTER);
        dialog.add(pnlSur, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    
    private void mostrarDialogoOperaciones(GestorEstadisticas stats) {
        GestorMisiones gm = GestorMisiones.getInstancia();
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "CENTRO DE OPERACIONES", true);
        dialog.setSize(700, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(0x05, 0x0A, 0x10));
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(0x05, 0x0A, 0x10));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 5, 20));
        JLabel lblTitulo = new JLabel("CENTRO DE OPERACIONES // MISIONES & DESAFIOS");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(0x00, 0xFF, 0xCC));
        pnlHeader.add(lblTitulo);
        dialog.add(pnlHeader, BorderLayout.NORTH);

        // Panel principal con tabs (Diarias / Desafios)
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(new Color(0x05, 0x0A, 0x10));
        pnlMain.setBorder(new EmptyBorder(10, 20, 10, 20));

        // === SECCION: MISIONES DIARIAS ===
        JLabel lblDiarias = new JLabel("MISIONES DIARIAS [Se reinician cada dia]");
        lblDiarias.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblDiarias.setForeground(new Color(0xFF, 0x88, 0x00));
        lblDiarias.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMain.add(lblDiarias);
        pnlMain.add(Box.createVerticalStrut(6));

        java.util.List<GestorMisiones.MisionDiaria> diarias = gm.getMisionesDiarias();
        for (GestorMisiones.MisionDiaria m : diarias) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(new Color(0x0A, 0x12, 0x18));
            row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(m.completada ? new Color(0x00, 0xFF, 0x88, 100) : new Color(0x33, 0x33, 0x33), 1),
                new EmptyBorder(4, 8, 4, 8)
            ));
            row.setMaximumSize(new Dimension(660, 36));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            String estado = m.completada ? "\u2713 " : "\u25CB ";
            JLabel lblNombre = new JLabel(estado + m.nombre + "  -  " + m.descripcion);
            lblNombre.setFont(new Font("Monospaced", Font.PLAIN, 10));
            lblNombre.setForeground(m.completada ? new Color(0x00, 0xFF, 0x88) : new Color(0xAA, 0xAA, 0xAA));
            row.add(lblNombre, BorderLayout.WEST);

            JLabel lblProg = new JLabel("[" + Math.min(m.progreso, m.meta) + "/" + m.meta + "]  +" + m.xpRecompensa + "XP");
            lblProg.setFont(new Font("Monospaced", Font.BOLD, 10));
            lblProg.setForeground(m.completada ? new Color(0x00, 0xFF, 0x88) : new Color(0xFF, 0x88, 0x00));
            row.add(lblProg, BorderLayout.EAST);

            pnlMain.add(row);
            pnlMain.add(Box.createVerticalStrut(3));
        }

        pnlMain.add(Box.createVerticalStrut(12));

        // === SECCION: DESAFIOS PERMANENTES ===
        JLabel lblDesafios = new JLabel("DESAFIOS DE OPERADOR [Permanentes - Recompensa XP]");
        lblDesafios.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblDesafios.setForeground(new Color(0x00, 0xFF, 0xCC));
        lblDesafios.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMain.add(lblDesafios);
        pnlMain.add(Box.createVerticalStrut(4));

        JLabel lblProgDesafios = new JLabel("Completados: " + gm.getDesafiosCompletados() + "/" + gm.getTotalDesafios());
        lblProgDesafios.setFont(new Font("Monospaced", Font.PLAIN, 9));
        lblProgDesafios.setForeground(new Color(0x88, 0x88, 0x88));
        lblProgDesafios.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMain.add(lblProgDesafios);
        pnlMain.add(Box.createVerticalStrut(6));

        java.util.List<GestorMisiones.Desafio> desafios = gm.getDesafios();
        for (GestorMisiones.Desafio d : desafios) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(new Color(0x0A, 0x12, 0x18));

            Color borderColor;
            switch (d.dificultad) {
                case "PLATA": borderColor = new Color(0xC0, 0xC0, 0xC0, 120); break;
                case "ORO": borderColor = new Color(0xFF, 0xD7, 0x00, 120); break;
                case "PLATINO": borderColor = new Color(0x00, 0xFF, 0xFF, 120); break;
                default: borderColor = new Color(0xCD, 0x7F, 0x32, 120); break; // BRONCE
            }
            if (d.completado) borderColor = new Color(0x00, 0xFF, 0x88, 150);

            row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(3, 8, 3, 8)
            ));
            row.setMaximumSize(new Dimension(660, 34));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            String estado = d.completado ? "\u2713 " : "\u25CB ";
            JLabel lblNombre = new JLabel(estado + "[" + d.dificultad + "] " + d.nombre + "  -  " + d.descripcion);
            lblNombre.setFont(new Font("Monospaced", Font.PLAIN, 9));
            lblNombre.setForeground(d.completado ? new Color(0x00, 0xFF, 0x88) : new Color(0xAA, 0xAA, 0xAA));
            row.add(lblNombre, BorderLayout.WEST);

            JLabel lblProg = new JLabel("[" + Math.min(d.progreso, d.meta) + "/" + d.meta + "]  +" + d.xpRecompensa + "XP");
            lblProg.setFont(new Font("Monospaced", Font.BOLD, 9));
            Color progColor;
            switch (d.dificultad) {
                case "PLATA": progColor = new Color(0xC0, 0xC0, 0xC0); break;
                case "ORO": progColor = new Color(0xFF, 0xD7, 0x00); break;
                case "PLATINO": progColor = new Color(0x00, 0xFF, 0xFF); break;
                default: progColor = new Color(0xCD, 0x7F, 0x32); break;
            }
            if (d.completado) progColor = new Color(0x00, 0xFF, 0x88);
            lblProg.setForeground(progColor);
            row.add(lblProg, BorderLayout.EAST);

            pnlMain.add(row);
            pnlMain.add(Box.createVerticalStrut(2));
        }

        // Scroll wrapper
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(pnlMain);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0x05, 0x0A, 0x10));
        scroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dialog.add(scroll, BorderLayout.CENTER);

        // Footer with close and claim buttons
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        pnlSur.setBackground(new Color(0x05, 0x0A, 0x10));

        int xpPendiente = gm.getXpPendienteDiarias() + gm.getXpPendienteDesafios();
        if (xpPendiente > 0) {
            JButton btnReclamar = new JButton("RECLAMAR RECOMPENSAS (+" + xpPendiente + " XP)");
            btnReclamar.setForeground(new Color(0x00, 0xFF, 0x88));
            btnReclamar.setBackground(new Color(0x05, 0x15, 0x08));
            btnReclamar.setFont(new Font("Monospaced", Font.BOLD, 11));
            btnReclamar.setFocusPainted(false);
            btnReclamar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x00, 0xFF, 0x88), 2),
                new EmptyBorder(6, 12, 6, 12)
            ));
            btnReclamar.addActionListener(e -> {
                int totalXP = 0;
                for (GestorMisiones.MisionDiaria m : gm.getMisionesDiarias()) {
                    totalXP += gm.reclamarRecompensaDiaria(m.id);
                }
                for (GestorMisiones.Desafio d : gm.getDesafios()) {
                    totalXP += gm.reclamarRecompensaDesafio(d.id);
                }
                if (totalXP > 0) {
                    stats.agregarXP(totalXP);
                    stats.guardarEstadisticas();
                }
                dialog.dispose();
                mostrarDialogoOperaciones(stats); // Refresh
            });
            pnlSur.add(btnReclamar);
        }

        JButton btnCerrar = new JButton("CERRAR");
        btnCerrar.setBackground(Color.BLACK);
        btnCerrar.setForeground(Color.CYAN);
        btnCerrar.setFont(new Font("Monospaced", Font.BOLD, 11));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(new EmptyBorder(6, 16, 6, 16));
        btnCerrar.addActionListener(e -> dialog.dispose());
        pnlSur.add(btnCerrar);

        dialog.add(pnlSur, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void mostrarManualJuego() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "MANUAL DE OPERACIONES", true);
        dialog.setSize(700, 620);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(0x05, 0x0A, 0x10));
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(0x05, 0x0A, 0x10));
        pnlHeader.setBorder(new EmptyBorder(15, 20, 10, 20));
        JLabel lblTitulo = new JLabel("MANUAL DE OPERACIONES NEXUS-7");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(0x00, 0xFF, 0xFF));
        pnlHeader.add(lblTitulo);
        dialog.add(pnlHeader, BorderLayout.NORTH);

        // Contenido principal en JTextPane con scroll
        javax.swing.JTextPane txtManual = new javax.swing.JTextPane();
        txtManual.setEditable(false);
        txtManual.setBackground(new Color(0x05, 0x0A, 0x10));
        txtManual.setCaretColor(new Color(0x00, 0xFF, 0xFF));
        txtManual.setContentType("text/html");
        
        String html = "<html><body style='color:#00FFFF; font-family:monospace; font-size:11px; padding:15px; background:#050A10;'>"
            + "<h2 style='color:#00FF88; text-align:center;'>PROTOCOLO DE DESCIFRADO CYBER-DECODER</h2>"
            + "<hr style='border-color:#00FF88;'>"
            + "<h3 style='color:#FF8800;'>1. OBJETIVO DE LA MISION</h3>"
            + "<p style='color:#CCCCCC;'>Tu mision como operador del sistema Nexus-7 es descifrar palabras "
            + "codificadas antes de que el androide conectado al sistema sea ejecutado. "
            + "Cada letra incorrecta activa un componente del protocolo de ejecucion.</p>"
            + "<h3 style='color:#FF8800;'>2. MECANICA DE JUEGO</h3>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#00FF88;'>Selecciona letras</span> usando el teclado virtual o tu teclado fisico</li>"
            + "<li><span style='color:#00FFFF;'>Letras correctas</span> se revelan en la palabra codificada</li>"
            + "<li><span style='color:#FF4444;'>Letras incorrectas</span> acumulan fallos y construyen al androide</li>"
            + "<li>El juego termina cuando descifras la palabra o se completa el androide</li>"
            + "</ul>"
            + "<h3 style='color:#FF8800;'>3. NIVELES DE ENCRIPTACION</h3>"
            + "<table style='color:#CCCCCC; width:100%;'>"
            + "<tr><td style='color:#00FF88; width:35%;'>NORMAL</td><td>7 intentos, 3 pistas disponibles</td></tr>"
            + "<tr><td style='color:#FFAA00;'>DIFICIL</td><td>6 intentos, sin pistas</td></tr>"
            + "<tr><td style='color:#FF4444;'>PESADILLA</td><td>5 intentos, sin pistas</td></tr>"
            + "</table>"
            + "<h3 style='color:#FF8800;'>4. SISTEMA DE PISTAS</h3>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#8866FF;'>Pista de Categoria:</span> Revela el dominio de la palabra</li>"
            + "<li><span style='color:#8866FF;'>Pista de Letra:</span> Revela una letra aleatoria no descubierta</li>"
            + "<li><span style='color:#8866FF;'>Pista de Descripcion:</span> Muestra una pista contextual</li>"
            + "</ul>"
            + "<h3 style='color:#FF8800;'>5. SISTEMA DE PROGRESION</h3>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#00FFFF;'>XP (Experiencia):</span> Se gana al completar partidas. Victoria = +100 XP base</li>"
            + "<li><span style='color:#00FFFF;'>Nivel:</span> Cada 250 XP avanzas de nivel</li>"
            + "<li><span style='color:#00FFFF;'>Rango:</span> Se desbloquean rangos segun tu nivel (9 rangos totales)</li>"
            + "<li><span style='color:#00FFFF;'>Bonificadores:</span> Dificultad alta (+35/75% XP), tiempo rapido (+50 XP)</li>"
            + "</ul>"
            + "<h3 style='color:#FF8800;'>6. CONTROLES</h3>"
            + "<table style='color:#CCCCCC; width:100%;'>"
            + "<tr><td style='color:#00FF88; width:35%;'>A-Z</td><td>Seleccionar letra</td></tr>"
            + "<tr><td style='color:#00FF88;'>ESC</td><td>Menu de pausa</td></tr>"
            + "<tr><td style='color:#00FF88;'>F1</td><td>Usar pista</td></tr>"
            + "</table>"
            + "<h3 style='color:#FF8800;'>7. PALANCA DE EJECUCION</h3>"
            + "<p style='color:#FF5555;'>Cuando el androide esta completo (7 fallos), aparece una palanca interactiva. "
            + "Al hacer clic en ella, se activa el protocolo de ejecucion con animacion de caida.</p>"
            + "<h3 style='color:#FF8800;'>8. LOGROS</h3>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#FFD700;'>PRIMER_ACCESO:</span> Jugar tu primera partida</li>"
            + "<li><span style='color:#FFD700;'>PRIMERA_VICTORIA:</span> Ganar por primera vez</li>"
            + "<li><span style='color:#FFD700;'>SIN_ERRORES:</span> Ganar sin ningun fallo</li>"
            + "<li><span style='color:#FFD700;'>ANALISTA_RAPIDO:</span> Ganar en menos de 60 segundos</li>"
            + "<li><span style='color:#FFD700;'>RACHA_X3 / X5:</span> Ganar 3 o 5 partidas seguidas</li>"
            + "</ul>"
            + "<h3 style='color:#FF8800;'>9. KALI KOINTS (KK) - ECONOMIA VIRTUAL</h3>"
            + "<p style='color:#CCCCCC;'>Kali Koints es la moneda del sistema Nexus-7. Se ganan al completar partidas:</p>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#FFD700;'>+50 KK</span> por partida ganada</li>"
            + "<li><span style='color:#FFD700;'>+10 KK</span> por cada intento sobrante</li>"
            + "<li><span style='color:#FFD700;'>+5 KK</span> de consolacion por derrota</li>"
            + "<li><span style='color:#FFD700;'>Bonificadores:</span> Dificultad alta, velocidad rapida y partida perfecta otorgan KK extra</li>"
            + "</ul>"
            + "<p style='color:#CCCCCC;'>Tu saldo se muestra en la esquina superior derecha durante el juego y en la pantalla principal.</p>"
            + "<h3 style='color:#FF8800;'>10. BUGS / GLITCHES</h3>"
            + "<p style='color:#CCCCCC;'>Durante el juego aparecen pop-ups de Bugs en la pantalla. "
            + "Son artefactos visuales cyberpunk que decoran tu experiencia. "
            + "Solo aparecen los Bugs que hayas desbloqueado. El bug por defecto viene incluido.</p>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li>Cada Bug tiene un <span style='color:#00FFFF;'>avatar unico</span> generado algoritmicamente</li>"
            + "<li>Rareza: <span style='color:#AAAAAA;'>Comun</span>, <span style='color:#00AAFF;'>Raro</span>, "
            + "<span style='color:#AA44FF;'>Epico</span>, <span style='color:#FFD700;'>Legendario</span></li>"
            + "</ul>"
            + "<h3 style='color:#FF8800;'>11. BLACK MARKET (TIENDA CLANDESTINA)</h3>"
            + "<p style='color:#CCCCCC;'>Accede al Black Market desde la pantalla principal o el menu del juego para gastar tus Kali Koints:</p>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#00FF88;'>Pestana BUGS:</span> Desbloquea nuevos avatares de Bugs/Glitches que aparecen durante el gameplay</li>"
            + "<li><span style='color:#00FF88;'>Pestana SKINS:</span> Personaliza los colores del ahorcado (horca, cuerpo, efectos de glow)</li>"
            + "</ul>"
            + "<p style='color:#CCCCCC;'>Los items bloqueados se muestran en gris con su precio. "
            + "Si tienes saldo suficiente, haz clic en <span style='color:#00FFFF;'>HACKEAR</span> para desbloquear. "
            + "Los Skins adquiridos se pueden equipar con el boton <span style='color:#00FFFF;'>EQUIPAR</span>.</p>"
            + "<h3 style='color:#FF8800;'>12. PERSONALIZACION DEL AHORCADO (SKINS)</h3>"
            + "<p style='color:#CCCCCC;'>El sistema de skins te permite cambiar la apariencia visual del androide:</p>"
            + "<ul style='color:#CCCCCC;'>"
            + "<li><span style='color:#8CFFFF;'>Cian Neon:</span> Esquema clasico (gratis)</li>"
            + "<li><span style='color:#00FF41;'>Matrix Green:</span> Protocolo Matrix</li>"
            + "<li><span style='color:#FF3333;'>Blood Circuit:</span> Circuitos de sangre digital</li>"
            + "<li><span style='color:#FFAA00;'>Retro Amber:</span> Terminal retro de los 80s</li>"
            + "<li><span style='color:#FF00FF;'>Neon Pink Glitch:</span> Estetica vaporwave</li>"
            + "<li><span style='color:#FFD700;'>Gold Elite:</span> Solo para operadores de elite</li>"
            + "<li><span style='color:#88CCFF;'>Arctic Ice:</span> Frio como el zero-day</li>"
            + "<li><span style='color:#DDDDDD;'>Phantom White:</span> Invisible como un rootkit</li>"
            + "</ul>"
            + "<hr style='border-color:#00FF88;'>"
            + "<p style='color:#00FF88; text-align:center;'>[ NEXUS-7 DEEP-THOUGHT SYSTEM v8.0 ]</p>"
            + "</body></html>";
        
        txtManual.setText(html);
        txtManual.setCaretPosition(0);
        
        javax.swing.JScrollPane scrollManual = new javax.swing.JScrollPane(txtManual);
        scrollManual.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x00, 0xFF, 0xFF), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollManual.getViewport().setBackground(new Color(0x05, 0x0A, 0x10));
        scrollManual.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dialog.add(scrollManual, BorderLayout.CENTER);

        // Footer
        JPanel pnlFooter = new JPanel();
        pnlFooter.setBackground(new Color(0x05, 0x0A, 0x10));
        pnlFooter.setBorder(new EmptyBorder(10, 20, 15, 20));
        JButton btnCerrar = new JButton("[ CERRAR MANUAL ]");
        btnCerrar.setForeground(new Color(0x00, 0xFF, 0xFF));
        btnCerrar.setBackground(new Color(0x0A, 0x0E, 0x14));
        btnCerrar.setFont(new Font("Monospaced", Font.BOLD, 12));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(new LineBorder(new Color(0x00, 0xFF, 0xFF), 2));
        btnCerrar.addActionListener(ev -> dialog.dispose());
        pnlFooter.add(btnCerrar);
        dialog.add(pnlFooter, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void aplicarTema(String tema) {
        Color mainColor, tenueColor, glowColor;
        if (tema.contains("Matrix")) {
            mainColor = new Color(0x00, 0xFF, 0x00);
            tenueColor = new Color(0x00, 0x66, 0x00, 150);
            glowColor = new Color(0x00, 0xFF, 0x00, 40);
        } else if (tema.contains("Ámbar")) {
            mainColor = new Color(0xFF, 0x88, 0x00);
            tenueColor = new Color(0x99, 0x44, 0x00, 150);
            glowColor = new Color(0xFF, 0x88, 0x00, 40);
        } else if (tema.contains("Rosa")) {
            mainColor = new Color(0xFF, 0x00, 0xFF);
            tenueColor = new Color(0x99, 0x00, 0x99, 150);
            glowColor = new Color(0xFF, 0x00, 0xFF, 40);
        } else {
            mainColor = new Color(0x00, 0xFF, 0xFF);
            tenueColor = new Color(0x00, 0x88, 0x99, 150);
            glowColor = new Color(0x00, 0xFF, 0xFF, 40);
        }
        
        InterfazAhorcado.CIAN_NEON = mainColor;
        InterfazAhorcado.CIAN_TENUE = tenueColor;
        InterfazAhorcado.CIAN_GLOW = glowColor;
        
        PanelSujeto.CIAN = mainColor;
        PanelSujeto.GLOW_CIAN = glowColor;
    }
    
    private void iniciarJuego(int dificultad, boolean trivias, boolean feedback, boolean sfx, String alias) {
        // Stop menu background music
        if (audioController != null) {
            audioController.detenerMusicaMenu();
        }
        
        BancoPalabras banco = new BancoPalabras();
        LogicaJuego motor = new LogicaJuego(banco.obtenerPalabraAlAzar("ALEATORIO"));
        
        String dificultadNombre = "NORMAL";
        if (dificultad == 1) {
            dificultadNombre = "DIFICIL";
            motor.setPistasRestantes(0);
            motor.setMaxIntentos(6);
        } else if (dificultad == 2) {
            dificultadNombre = "PESADILLA";
            motor.setPistasRestantes(0);
            motor.setMaxIntentos(5);
        }
        
        // Ocultar menu en lugar de dispose para poder reutilizarlo
        this.setVisible(false);
        
        // Pass audio config to constructor + callback para volver al menu
        InterfazAhorcado v = new InterfazAhorcado(motor, banco, trivias, feedback, sfx, alias, dificultadNombre);
        interfazActiva = v; // Guardar referencia para sincronización
        v.setOnVolverAlMenu(() -> {
            interfazActiva = null; // Limpiar referencia al volver al menú
            v.dispose(); // Asegurar limpieza completa
            // Mostrar efecto glitch y luego restaurar este menu
            GlitchScreen glitch = new GlitchScreen(() -> {
                this.setVisible(true);
                // Actualizar saldo de KK para sincronización
                GestorEconomia eco = GestorEconomia.getInstancia();
                int saldoActual = eco.getSaldo();
                System.out.println("[DEBUG MENU] Volviendo al menú. Saldo KK: " + saldoActual);
                // Forzar actualización de UI de KK
                for (java.awt.Component c : pnlWallet.getComponents()) {
                    if (c instanceof JLabel && ((JLabel) c).getText().startsWith("KALI KOINTS:")) {
                        ((JLabel) c).setText(String.format("KALI KOINTS: %,d", saldoActual));
                        break;
                    }
                }
                // Reiniciar musica de menu - asegurar audio global activo
                if (audioController != null) {
                    System.out.println("[DEBUG MENU] Intentando reproducir música de menú. audioGlobal=" + audioController.isAudioGlobalActivo());
                    audioController.reactivarAudio();
                    audioController.reproducirMusicaMenu();
                }
            });
            glitch.setVisible(true);
        });
        v.setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
        
        final boolean skipBoot;
        boolean skipBootTemp = false;
        if (args != null) {
            for (String arg : args) {
                if ("--skip-boot".equals(arg)) {
                    skipBootTemp = true;
                    break;
                }
            }
        }
        skipBoot = skipBootTemp;
        
        SwingUtilities.invokeLater(() -> {
            PantallaArranque pantalla = new PantallaArranque(skipBoot);
            pantalla.setVisible(true);
        });
    }
    
    // --- Inner class for Network Recon Map (PanelMapa) - Pixel-Perfect Design ---
    class PanelMapa extends JPanel {
        private static final long serialVersionUID = 1L;
        private GestorEstadisticas stats;
        private int nivelActual = 12;
        private static final int MAX_NIVELES = 300;
        private static final int NODOS_POR_FILA = 5;
        private static final int FILAS = 2;
        private int animationPhase = 0;
        private int hoverNivel = -1;
        
        // Offset for scrolling through levels
        private int offset = 0;
        
        // Expand mode flag
        private boolean mapaExpandido = false;
        
        // Node dimensions
        private static final int NODE_WIDTH = 60;
        private static final int NODE_HEIGHT = 45;
        private static final int NODE_PADDING = 20;
        
        // Navigation button areas
        private java.awt.Rectangle btnIzquierda;
        private java.awt.Rectangle btnDerecha;
        private java.awt.Rectangle btnExpandir;
        
        public PanelMapa(GestorEstadisticas stats) {
            this.stats = stats;
            // Usar nivel real basado en XP, no juegos ganados
            this.nivelActual = Math.min(stats.getNivel(), MAX_NIVELES);
            
            // Calculate offset to show user's current level in view
            calcularOffsetAutomatico();
            
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x00, 255, 0), 2),
                    "[ NETWORK RECON (MAPA INTERACTIVO) - 300 NIVELES ]",
                    javax.swing.border.TitledBorder.RIGHT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 11),
                    new Color(0x00, 255, 0)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            // Mouse listener for interactivity
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    hoverNivel = encontrarNivelEnPosicion(e.getX(), e.getY());
                    repaint();
                }
            });
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Check expand button
                    if (btnExpandir != null && btnExpandir.contains(e.getPoint())) {
                        mapaExpandido = !mapaExpandido;
                        repaint();
                        return;
                    }
                    
                    // Check navigation buttons (only in normal mode)
                    if (!mapaExpandido) {
                        if (btnIzquierda != null && btnIzquierda.contains(e.getPoint())) {
                            if (offset > 0) {
                                offset -= 10;
                                repaint();
                            }
                        } else if (btnDerecha != null && btnDerecha.contains(e.getPoint())) {
                            if (offset + 10 < MAX_NIVELES) {
                                offset += 10;
                                repaint();
                            }
                        }
                    }
                    
                    // Check level nodes
                    int nivel = encontrarNivelEnPosicion(e.getX(), e.getY());
                    if (nivel > 0 && nivel <= nivelActual) {
                        mostrarInfoNivel(nivel);
                    }
                }
            });
            
            // Timer for animation
            Timer animTimer = new Timer(33, e -> {
                animationPhase = (animationPhase + 1) % 20;
                repaint();
            });
            animTimer.start();
        }
        
        private void calcularOffsetAutomatico() {
            // Calculate offset so that current level is visible
            // Show 10 levels at a time, center current level in view
            offset = ((nivelActual - 1) / 10) * 10;
            // Ensure offset doesn't go beyond bounds
            if (offset < 0) offset = 0;
            if (offset > MAX_NIVELES - 10) offset = MAX_NIVELES - 10;
        }
        
        private int encontrarNivelEnPosicion(int x, int y) {
            int w = getWidth();
            int h = getHeight();
            int totalWidth = NODOS_POR_FILA * (NODE_WIDTH + NODE_PADDING);
            int startX = (w - totalWidth) / 2;
            int startY = 80;
            
            // Check top row
            for (int i = 0; i < NODOS_POR_FILA; i++) {
                int nodeX = startX + i * (NODE_WIDTH + NODE_PADDING);
                int nodeY = startY;
                if (x >= nodeX && x <= nodeX + NODE_WIDTH && y >= nodeY && y <= nodeY + NODE_HEIGHT) {
                    return offset + i + 1;
                }
            }
            
            // Check bottom row
            for (int i = 0; i < NODOS_POR_FILA; i++) {
                int nodeX = startX + i * (NODE_WIDTH + NODE_PADDING);
                int nodeY = startY + NODE_HEIGHT + NODE_PADDING;
                if (x >= nodeX && x <= nodeX + NODE_WIDTH && y >= nodeY && y <= nodeY + NODE_HEIGHT) {
                    return offset + i + 6;
                }
            }
            
            return -1;
        }
        
        private void mostrarInfoNivel(int nivel) {
            String estado = nivel <= nivelActual ? "COMPLETADO" : "BLOQUEADO";
            javax.swing.JOptionPane.showMessageDialog(this,
                String.format("NIVEL %d\nEstado: %s", nivel, estado),
                "INFORMACIÓN DE NIVEL",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Background black
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            // Technical grid
            g2.setColor(new Color(0, 20, 20));
            for (int i = 0; i < w; i += 30) {
                g2.drawLine(i, 0, i, h);
            }
            for (int j = 0; j < h; j += 30) {
                g2.drawLine(0, j, w, j);
            }
            
            // Decorative border lines
            g2.setColor(new Color(0x00, 255, 0, 50));
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(5, 5, w - 10, h - 10);
            g2.drawRect(8, 8, w - 16, h - 16);
            
            // Draw expand button
            dibujarBotonExpandir(g2, w, h);
            
            if (mapaExpandido) {
                // Draw full expanded map
                dibujarMapaExpandido(g2, w, h);
            } else {
                // Draw normal 10-level view
                dibujarMapaNormal(g2, w, h);
            }
            
            // Draw status panel at bottom
            dibujarPanelEstado(g2, w, h);
        }
        
        private void dibujarBotonExpandir(Graphics2D g2, int w, int h) {
            int buttonWidth = 100;
            int buttonHeight = 25;
            int buttonX = w - buttonWidth - 15;
            int buttonY = 15;
            
            btnExpandir = new java.awt.Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
            
            g2.setColor(Color.BLACK);
            g2.fillRect(btnExpandir.x, btnExpandir.y, buttonWidth, buttonHeight);
            g2.setColor(new Color(0x00, 255, 255));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(btnExpandir.x, btnExpandir.y, buttonWidth, buttonHeight);
            
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0x00, 255, 255));
            String buttonText = mapaExpandido ? "CONTRAER" : "EXPANDIR";
            int textWidth = g2.getFontMetrics().stringWidth(buttonText);
            g2.drawString(buttonText, buttonX + (buttonWidth - textWidth) / 2, buttonY + 17);
        }
        
        private void dibujarMapaNormal(Graphics2D g2, int w, int h) {
            // Draw "Fila 01" label
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(new Color(0x00, 255, 255));
            int filaNum = (offset / 10) + 1;
            g2.drawString("Fila " + String.format("%02d", filaNum), 20, 55);
            
            // Calculate node positions (2 horizontal rows, 5 nodes each)
            int totalWidth = NODOS_POR_FILA * (NODE_WIDTH + NODE_PADDING);
            int startX = (w - totalWidth) / 2;
            int startY = 90;
            
            int[][] nodePositions = new int[10][4]; // x, y, width, height
            
            // Top row positions
            for (int i = 0; i < NODOS_POR_FILA; i++) {
                nodePositions[i][0] = startX + i * (NODE_WIDTH + NODE_PADDING);
                nodePositions[i][1] = startY;
                nodePositions[i][2] = NODE_WIDTH;
                nodePositions[i][3] = NODE_HEIGHT;
            }
            
            // Bottom row positions
            for (int i = 0; i < NODOS_POR_FILA; i++) {
                nodePositions[i + 5][0] = startX + i * (NODE_WIDTH + NODE_PADDING);
                nodePositions[i + 5][1] = startY + NODE_HEIGHT + NODE_PADDING;
                nodePositions[i + 5][2] = NODE_WIDTH;
                nodePositions[i + 5][3] = NODE_HEIGHT;
            }
            
            // Draw connection lines (inactive path - dashed)
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
            g2.setColor(new Color(0, 100, 0));
            
            // Horizontal connections in top row
            for (int i = 0; i < NODOS_POR_FILA - 1; i++) {
                int x1 = nodePositions[i][0] + NODE_WIDTH;
                int y1 = nodePositions[i][1] + NODE_HEIGHT / 2;
                int x2 = nodePositions[i + 1][0];
                int y2 = nodePositions[i + 1][1] + NODE_HEIGHT / 2;
                g2.drawLine(x1, y1, x2, y2);
            }
            
            // Horizontal connections in bottom row
            for (int i = 0; i < NODOS_POR_FILA - 1; i++) {
                int x1 = nodePositions[i + 5][0] + NODE_WIDTH;
                int y1 = nodePositions[i + 5][1] + NODE_HEIGHT / 2;
                int x2 = nodePositions[i + 6][0];
                int y2 = nodePositions[i + 6][1] + NODE_HEIGHT / 2;
                g2.drawLine(x1, y1, x2, y2);
            }
            
            // Vertical connection between rows
            int midX = nodePositions[2][0] + NODE_WIDTH / 2;
            int y1 = nodePositions[2][1] + NODE_HEIGHT;
            int y2 = nodePositions[7][1];
            g2.drawLine(midX, y1, midX, y2);
            
            // Draw active path (dashed cyan from current level to next - same as inactive)
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
            g2.setColor(new Color(0, 255, 255));
            
            // Find current level position relative to offset
            int currentIdx = nivelActual - offset - 1;
            if (currentIdx >= 0 && currentIdx < 10) {
                int cx = nodePositions[currentIdx][0] + NODE_WIDTH / 2;
                int cy = nodePositions[currentIdx][1] + NODE_HEIGHT / 2;
                
                // Draw arrow to next level
                if (currentIdx < 9 && offset + currentIdx + 2 <= MAX_NIVELES) {
                    int nextIdx = currentIdx + 1;
                    int nx = nodePositions[nextIdx][0] + NODE_WIDTH / 2;
                    int ny = nodePositions[nextIdx][1] + NODE_HEIGHT / 2;
                    
                    // Draw line
                    g2.drawLine(cx, cy, nx, ny);
                    
                    // Draw arrow head
                    drawArrowHead(g2, cx, cy, nx, ny);
                }
            }
            
            // Draw nodes
            for (int i = 0; i < 10; i++) {
                int x = nodePositions[i][0];
                int y = nodePositions[i][1];
                int nivel = offset + i + 1; // Use offset to calculate actual level number
                
                // Only draw if within valid range
                if (nivel > MAX_NIVELES) continue;
                
                // Glow effect
                g2.setColor(new Color(0, 255, 255, 40));
                g2.fillRoundRect(x - 5, y - 5, NODE_WIDTH + 10, NODE_HEIGHT + 10, 15, 15);
                
                // Node border color
                Color borderColor;
                if (nivel == nivelActual) {
                    borderColor = new Color(0, 255, 255);
                    // Pulsing glow for current level
                    float pulse = 0.5f + 0.5f * (float) Math.sin(animationPhase * 0.3);
                    g2.setColor(new Color(0, 255, 255, (int)(pulse * 80)));
                    g2.fillRoundRect(x - 8, y - 8, NODE_WIDTH + 16, NODE_HEIGHT + 16, 20, 20);
                } else if (nivel < nivelActual) {
                    borderColor = new Color(0, 255, 136);
                } else {
                    borderColor = new Color(80, 80, 80);
                }
                
                // Draw organic node (rounded rectangle with thick border)
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 15, 15);
                
                // Draw level number
                g2.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2.setColor(Color.WHITE);
                String numStr = String.valueOf(nivel);
                int strW = g2.getFontMetrics().stringWidth(numStr);
                g2.drawString(numStr, x + (NODE_WIDTH - strW) / 2, y + 25);
                
                // Draw "NIVEL X" label below
                g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
                g2.setColor(new Color(0x00, 255, 255));
                String nivelStr = "NIVEL " + nivel;
                int nivelStrW = g2.getFontMetrics().stringWidth(nivelStr);
                g2.drawString(nivelStr, x + (NODE_WIDTH - nivelStrW) / 2, y + NODE_HEIGHT - 8);
                
                // Hover effect
                if (nivel == hoverNivel) {
                    g2.setColor(new Color(0, 255, 255, 60));
                    g2.fillRoundRect(x - 3, y - 3, NODE_WIDTH + 6, NODE_HEIGHT + 6, 18, 18);
                }

                // Indicador de nivel actual: punto pulsante dentro del nodo
                if (nivel == nivelActual) {
                    float ap = 0.5f + 0.5f * (float) Math.sin(animationPhase * 0.4);
                    int dotX = x + NODE_WIDTH / 2;
                    int dotY = y + NODE_HEIGHT + 6;
                    // Triangulo indicador
                    g2.setColor(new Color(0, 255, 255, (int)(ap * 220)));
                    int[] triX = {dotX - 4, dotX, dotX + 4};
                    int[] triY = {dotY + 6, dotY, dotY + 6};
                    g2.fillPolygon(triX, triY, 3);
                }
            }
            
            // Draw navigation arrows below the levels (inside the grid)
            dibujarFlechasNavegacion(g2, w, h);
        }
        
        private void dibujarMapaExpandido(Graphics2D g2, int w, int h) {
            // Header de mapa expandido
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(new Color(0x00, 255, 255));
            g2.drawString("NETWORK MAP [300 NODOS]", 20, 50);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(new Color(0x00, 255, 0));
            g2.drawString("PROGRESO: " + nivelActual + "/300  (" + (nivelActual * 100 / 300) + "%)", 20, 65);
            
            // Barra de progreso general arriba
            int barX = w / 2 - 50;
            int barW = 200;
            g2.setColor(new Color(0x22, 0x22, 0x22));
            g2.fillRoundRect(barX, 52, barW, 10, 4, 4);
            g2.setColor(new Color(0x00, 255, 0));
            g2.fillRoundRect(barX, 52, (int)(barW * nivelActual / 300.0), 10, 4, 4);
            g2.setColor(new Color(0x00, 100, 0));
            g2.drawRoundRect(barX, 52, barW, 10, 4, 4);

            int cols = 20;
            int smallNodeWidth = 22;
            int smallNodeHeight = 18;
            int smallPadding = 6;
            int totalWidth = cols * (smallNodeWidth + smallPadding);
            int startX = (w - totalWidth) / 2;
            int startY = 78;
            
            for (int i = 0; i < MAX_NIVELES; i++) {
                int col = i % cols;
                int row = i / cols;
                int x = startX + col * (smallNodeWidth + smallPadding);
                int y = startY + row * (smallNodeHeight + smallPadding);
                
                Color borderColor;
                Color fillColor;
                if (i + 1 == nivelActual) {
                    borderColor = new Color(0, 255, 255);
                    fillColor = new Color(0, 255, 255, 40);
                    float pulse = 0.5f + 0.5f * (float) Math.sin(animationPhase * 0.3);
                    g2.setColor(new Color(0, 255, 255, (int)(pulse * 60)));
                    g2.fillRoundRect(x - 3, y - 3, smallNodeWidth + 6, smallNodeHeight + 6, 6, 6);
                } else if (i + 1 < nivelActual) {
                    borderColor = new Color(0, 200, 100);
                    fillColor = new Color(0, 80, 40, 50);
                } else if ((i + 1) % 50 == 0) {
                    borderColor = new Color(255, 136, 0);
                    fillColor = new Color(60, 30, 0, 40);
                } else if ((i + 1) % 10 == 0) {
                    borderColor = new Color(180, 80, 255);
                    fillColor = new Color(40, 10, 60, 30);
                } else {
                    borderColor = new Color(50, 50, 50);
                    fillColor = new Color(10, 10, 10, 30);
                }
                
                // Relleno
                g2.setColor(fillColor);
                g2.fillRoundRect(x, y, smallNodeWidth, smallNodeHeight, 4, 4);
                // Borde
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, smallNodeWidth, smallNodeHeight, 4, 4);
                
                // Numero de nivel
                g2.setFont(new Font("Monospaced", Font.BOLD, 7));
                g2.setColor(i + 1 <= nivelActual ? Color.WHITE : new Color(80, 80, 80));
                String numStr = String.valueOf(i + 1);
                int strW = g2.getFontMetrics().stringWidth(numStr);
                g2.drawString(numStr, x + (smallNodeWidth - strW) / 2, y + smallNodeHeight - 4);

                // Mini hacker en nivel actual
                if (i + 1 == nivelActual) {
                    g2.setColor(new Color(0, 255, 255));
                    g2.fillRect(x + smallNodeWidth / 2 - 2, y + 1, 4, 5);
                    g2.fillOval(x + smallNodeWidth / 2 - 2, y - 2, 4, 4);
                }
            }

            // Leyenda
            int ly = h - 30;
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2.setColor(new Color(0, 200, 100)); g2.fillRoundRect(20, ly, 10, 10, 3, 3);
            g2.setColor(new Color(0xCC, 0xCC, 0xCC)); g2.drawString("COMPLETADO", 35, ly + 9);
            g2.setColor(new Color(0, 255, 255)); g2.fillRoundRect(110, ly, 10, 10, 3, 3);
            g2.setColor(new Color(0xCC, 0xCC, 0xCC)); g2.drawString("ACTUAL", 125, ly + 9);
            g2.setColor(new Color(255, 136, 0)); g2.fillRoundRect(180, ly, 10, 10, 3, 3);
            g2.setColor(new Color(0xCC, 0xCC, 0xCC)); g2.drawString("BOSS x50", 195, ly + 9);
            g2.setColor(new Color(180, 80, 255)); g2.fillRoundRect(270, ly, 10, 10, 3, 3);
            g2.setColor(new Color(0xCC, 0xCC, 0xCC)); g2.drawString("CHECKPOINT x10", 285, ly + 9);
        }
        
        private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = 6; // Reduced from 10 to 6 for more delicate appearance
            
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            
            xPoints[0] = x2;
            yPoints[0] = y2;
            xPoints[1] = (int)(x2 - arrowSize * Math.cos(angle - Math.PI / 6));
            yPoints[1] = (int)(y2 - arrowSize * Math.sin(angle - Math.PI / 6));
            xPoints[2] = (int)(x2 - arrowSize * Math.cos(angle + Math.PI / 6));
            yPoints[2] = (int)(y2 - arrowSize * Math.sin(angle + Math.PI / 6));
            
            g2.fillPolygon(xPoints, yPoints, 3);
        }
        
        private void dibujarHackerAvatar(Graphics2D g2, int cx, int cy) {
            // Draw hacker sprite (hoodie and glasses)
            int size = 24;
            
            // Hoodie body (cyan)
            g2.setColor(new Color(0, 200, 200));
            g2.fillOval(cx - size / 2, cy - size / 2, size, size);
            
            // Head (skin tone)
            g2.setColor(new Color(255, 220, 180));
            g2.fillOval(cx - 8, cy - 10, 16, 18);
            
            // Glasses (dark lenses)
            g2.setColor(new Color(20, 20, 20));
            g2.fillOval(cx - 10, cy - 6, 8, 6);
            g2.fillOval(cx + 2, cy - 6, 8, 6);
            
            // Glasses frame (cyan)
            g2.setColor(new Color(0, 255, 255));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - 10, cy - 6, 8, 6);
            g2.drawOval(cx + 2, cy - 6, 8, 6);
            g2.drawLine(cx - 2, cy - 3, cx + 2, cy - 3);
            
            // Hood (cyan)
            g2.setColor(new Color(0, 150, 200));
            g2.fillArc(cx - size / 2, cy - size / 2 - 5, size, size + 5, 0, 180);
        }
        
        private void dibujarFlechasNavegacion(Graphics2D g2, int w, int h) {
            // Calculate position just above the status panel
            int totalWidth = NODOS_POR_FILA * (NODE_WIDTH + NODE_PADDING);
            int centerX = w / 2;
            int panelY = h - 70; // Status panel position
            int buttonY = panelY - 30; // Just above the status panel
            
            int buttonSize = 18; // Small and delicate
            
            // Left arrow
            btnIzquierda = new java.awt.Rectangle(centerX - 30, buttonY, buttonSize, buttonSize);
            g2.setColor(new Color(0x00, 255, 255));
            g2.setStroke(new BasicStroke(1.5f)); // Delicate stroke
            g2.drawLine(btnIzquierda.x + buttonSize, btnIzquierda.y + buttonSize / 2, 
                       btnIzquierda.x, btnIzquierda.y + buttonSize / 2);
            g2.drawLine(btnIzquierda.x, btnIzquierda.y + buttonSize / 2, 
                       btnIzquierda.x + 4, btnIzquierda.y + buttonSize / 2 - 4);
            g2.drawLine(btnIzquierda.x, btnIzquierda.y + buttonSize / 2, 
                       btnIzquierda.x + 4, btnIzquierda.y + buttonSize / 2 + 4);
            
            // Right arrow
            btnDerecha = new java.awt.Rectangle(centerX + 12, buttonY, buttonSize, buttonSize);
            g2.setColor(new Color(0x00, 255, 255));
            g2.setStroke(new BasicStroke(1.5f)); // Delicate stroke
            g2.drawLine(btnDerecha.x, btnDerecha.y + buttonSize / 2, 
                       btnDerecha.x + buttonSize, btnDerecha.y + buttonSize / 2);
            g2.drawLine(btnDerecha.x + buttonSize, btnDerecha.y + buttonSize / 2, 
                       btnDerecha.x + buttonSize - 4, btnDerecha.y + buttonSize / 2 - 4);
            g2.drawLine(btnDerecha.x + buttonSize, btnDerecha.y + buttonSize / 2, 
                       btnDerecha.x + buttonSize - 4, btnDerecha.y + buttonSize / 2 + 4);
            
            // Draw hacker details in the black space between levels and arrows
            dibujarDetallesHackers(g2, w, h, buttonY);
        }
        
        private void dibujarDetallesHackers(Graphics2D g2, int w, int h, int arrowY) {
            int levelsBottomY = 80 + NODE_HEIGHT * 2 + NODE_PADDING + 10;
            int spaceHeight = arrowY - levelsBottomY;
            int centerX = w / 2;
            
            if (spaceHeight > 20) {
                // Draw decorative hacker symbols in the black space
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g2.setColor(new Color(0x00, 255, 255, 80));
                
                // Left side decorations
                g2.drawString(">> SYSTEM", 20, levelsBottomY + 15);
                g2.drawString(">> RECON", 20, levelsBottomY + 28);
                
                // Right side decorations
                g2.drawString("SECURE <<", w - 80, levelsBottomY + 15);
                g2.drawString("SCAN <<", w - 80, levelsBottomY + 28);
                
                // Center decorations
                g2.setColor(new Color(0x00, 255, 0, 60));
                g2.drawString("[ NETWORK MAP ACTIVE ]", centerX - 90, levelsBottomY + 22);
                
                // Draw small decorative lines
                g2.setColor(new Color(0x00, 255, 255, 40));
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(20, levelsBottomY + 35, w - 20, levelsBottomY + 35);
            }
        }
        
        private void dibujarPanelEstado(Graphics2D g2, int w, int h) {
            int panelY = h - 70;
            
            // Background
            g2.setColor(new Color(0x0A, 0x0E, 14));
            g2.fillRect(10, panelY, w - 20, 60);
            g2.setColor(new Color(0x00, 255, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(10, panelY, w - 20, 60);
            
            // Calculate current range
            int nivelInicio = offset + 1;
            int nivelFin = Math.min(offset + 10, MAX_NIVELES);
            
            // Status text
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            g2.setColor(new Color(0x00, 255, 0));
            String statusText = String.format("[ MODULO ACTUAL: %d / %d. MOSTRANDO FILA 01 (%d-%d) ]", 
                nivelActual, MAX_NIVELES, nivelInicio, nivelFin);
            int textWidth = g2.getFontMetrics().stringWidth(statusText);
            g2.drawString(statusText, (w - textWidth) / 2, panelY + 25);
            
            // Status label (Neon Green)
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0x00, 255, 0));
            g2.drawString("ESTADO: NIVEL ACTUAL ACTIVADO", 20, panelY + 45);
            
            // Rank label (Red)
            g2.setColor(new Color(255, 0, 0));
            String rankText = "RANGO: " + stats.getRango();
            g2.drawString(rankText, w - 200, panelY + 45);
        }
    }
    
    // Clase interna para representar un nodo de nivel
    class NodoNivel {
        int numero;
        int fila;
        int columna;
        String estado; // "completado", "actual", "bloqueado", "especial"
        
        public NodoNivel(int numero, int fila, int columna, int nivelActual) {
            this.numero = numero;
            this.fila = fila;
            this.columna = columna;
            
            if (numero == nivelActual) {
                this.estado = "actual";
            } else if (numero < nivelActual) {
                this.estado = "completado";
            } else if (numero % 10 == 0) {
                this.estado = "especial";
            } else {
                this.estado = "bloqueado";
            }
        }
    }
    
    // --- Inner class for Live Matrix Datafeed with Oscilloscope Packet Sniffer ---
    class PanelLiveMatrix extends JPanel {
        private static final long serialVersionUID = 1L;
        private Random random = new Random();
        private int[] waveformData;
        private int phase;
        private int glitchCounter = 0;
        private boolean isGlitching = false;
        private Timer liveTimer;
        
        public PanelLiveMatrix() {
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x00, 0xFF, 0x88), 2),
                    "LIVE MATRIX DATAFEED",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 11),
                    new Color(0x00, 0xFF, 0x88)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            inicializarDatosOnda();
            
            // Timer para actualizar datos de onda (150ms para animación más lenta y suave)
            liveTimer = new Timer(150, e -> {
                actualizarDatosOnda();
                phase = (phase + 1) % 360;
                
                // Efecto de glitch ocasional
                glitchCounter++;
                if (glitchCounter > 100 && random.nextDouble() > 0.95) {
                    isGlitching = true;
                    glitchCounter = 0;
                } else if (glitchCounter > 5) {
                    isGlitching = false;
                }
                
                repaint();
            });
            liveTimer.start();
        }
        
        public void detener() {
            if (liveTimer != null) { liveTimer.stop(); liveTimer = null; }
        }
        
        private void inicializarDatosOnda() {
            waveformData = new int[150];
            for (int i = 0; i < waveformData.length; i++) {
                waveformData[i] = random.nextInt(100);
            }
        }
        
        private void actualizarDatosOnda() {
            // Transición suave entre valores (más lenta)
            for (int i = 0; i < waveformData.length; i++) {
                int targetValue = random.nextInt(100);
                // Interpolación más suave y gradual
                waveformData[i] = waveformData[i] + (targetValue - waveformData[i]) / 8;
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Fondo negro
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            // Efecto de glitch ocasional
            if (isGlitching) {
                g2.setColor(new Color(random.nextInt(50), random.nextInt(255), random.nextInt(50), 50));
                g2.fillRect(0, 0, w, h);
            }
            
            // Área estricta: 55% superior para texto de matriz (más espacio para histograma)
            int matrixAreaHeight = (int) (h * 0.58);
            int oscilloscopeAreaHeight = h - matrixAreaHeight;
            
            // Texto de matriz hexadecimal mejorado (70% superior)
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            int matrixY = 25;
            
            // Generar líneas de datos técnicos aleatorios
            String[] hexLines = generateRandomHexLines();
            
            for (int i = 0; i < hexLines.length && matrixY < matrixAreaHeight - 15; i++) {
                // Colores diferentes para diferentes tipos de datos
                if (hexLines[i].startsWith("0x")) {
                    g2.setColor(new Color(0x00, 0xFF, 0x88)); // Verde para hex
                } else if (hexLines[i].contains("[SUCCESS]")) {
                    g2.setColor(new Color(0x00, 0xFF, 0xFF)); // Cian para éxito
                } else if (hexLines[i].contains("[ERROR]")) {
                    g2.setColor(new Color(0xFF, 0x44, 0x44)); // Rojo para error
                } else if (hexLines[i].contains("[ALERT]")) {
                    g2.setColor(new Color(0xFF, 0x88, 0x00)); // Ámbar para alerta
                } else {
                    g2.setColor(new Color(0x00, 0xBB, 0xCC)); // Cian tenue para info
                }
                g2.drawString(hexLines[i], 10, matrixY);
                matrixY += 13;
            }
            
            // Histograma Técnico Packet Sniffer mejorado (30% inferior)
            int oscY = matrixAreaHeight + 10;
            int oscH = oscilloscopeAreaHeight - 15;
            int oscW = w - 20;
            
            // Marco decorativo técnico alrededor del histograma
            g2.setColor(new Color(0x00, 0x88, 0x44, 80));
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(25, oscY - 15, oscW - 10, oscH + 35);
            
            // Título del histograma mejorado con más detalles
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0x00, 0xFF, 0xFF));
            g2.drawString("[PACKET SNIFFER - TECHNICAL HISTOGRAM] | FREQ: " + (50 + random.nextInt(100)) + "Hz", 10, oscY - 5);
            
            // Hash técnico decorativo
            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.setColor(new Color(0x00, 0x88, 0x88));
            String hash = "0x" + Long.toHexString(random.nextLong()).substring(0, 8);
            g2.drawString("HASH: " + hash, oscW - 80, oscY - 5);
            
            // Fondo del histograma con gradiente sutil
            g2.setColor(new Color(0, 25, 0));
            g2.fillRect(30, oscY, oscW - 30, oscH);
            g2.setColor(new Color(0, 100, 0));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(30, oscY, oscW - 30, oscH);
            
            // Líneas de referencia decorativas
            g2.setColor(new Color(0x00, 0x66, 0x33, 60));
            for (int i = 1; i < 5; i++) {
                int lineY = oscY + (oscH * i) / 5;
                g2.drawLine(30, lineY, oscW - 30, lineY);
            }
            
            // Líneas de referencia (thresholds)
            g2.setColor(new Color(0xFF, 0x44, 0x44, 100));
            g2.drawLine(30, (int)(oscY + oscH * 0.3f), oscW - 30, (int)(oscY + oscH * 0.3f)); // Threshold alto
            g2.setColor(new Color(0xFF, 0x88, 0x00, 100));
            g2.drawLine(30, (int)(oscY + oscH * 0.7f), oscW - 30, (int)(oscY + oscH * 0.7f)); // Threshold medio
            
            // Dibujar histograma con barras rectas cian (Packet Sniffer)
            int barWidth = 8;
            int barSeparation = 3;
            int totalBarWidth = barWidth + barSeparation;
            int numBars = (oscW - 60) / totalBarWidth;
            
            for (int i = 0; i < numBars; i++) {
                int barHeight = (waveformData[i % waveformData.length] * oscH) / 100;
                int x = 30 + i * totalBarWidth;
                int y = oscY + oscH - barHeight;
                
                // Straight cyan bars
                g2.setColor(new Color(0x00, 0xFF, 0xFF));
                g2.fillRect(x, y, barWidth, barHeight);
            }
            
            // Eje Y (Packets) con valores numéricos
            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.setColor(new Color(0x00, 0xFF, 0x88));
            g2.drawString("100", 5, oscY + 5);
            g2.drawString("50", 8, oscY + oscH / 2);
            g2.drawString("0", 12, oscY + oscH - 2);
            g2.drawString("PKT", 5, oscY + oscH / 2 - 10);
            
            // Eje X (Sniffer) con valores numéricos
            g2.drawString("0", 30, oscY + oscH + 12);
            g2.drawString("50", oscW / 2 - 5, oscY + oscH + 12);
            g2.drawString("100", oscW - 50, oscY + oscH + 12);
            g2.drawString("SNIFFER", oscW / 2 + 5, oscY + oscH + 22);
        }
        
        private String[] generateRandomHexLines() {
            String[] lines = new String[16];
            for (int i = 0; i < 10; i++) {
                String offset = String.format("0x%08X", i * 16);
                StringBuilder hex = new StringBuilder(offset + "  ");
                for (int j = 0; j < 6; j++) {
                    hex.append(String.format("%02X ", random.nextInt(256)));
                }
                lines[i] = hex.toString();
            }
            
            // Mensajes de ciberseguridad más detallados
            String[] securityMessages = {
                "[SUCCESS] SSH handshake established [KEY: RSA-4096]",
                "[INFO] Port scan detected: 192.168." + random.nextInt(256) + "." + random.nextInt(256) + " [BLOCKED]",
                "[ALERT] Brute force attempt: user@kali [FAILED]",
                "[INFO] Firewall rule updated: DROP 10.0.0.0/8",
                "[SUCCESS] VPN tunnel established [IPSEC/IKEv2]",
                "[WARNING] Certificate expires in 30 days [SHA-256]"
            };
            
            for (int i = 0; i < 6; i++) {
                lines[10 + i] = securityMessages[i];
            }
            return lines;
        }
    }
    
    // --- Inner class for Operational HUD ---
    class PanelOperationalHUD extends JPanel {
        private static final long serialVersionUID = 1L;
        private GestorEstadisticas stats;
        private int pulsePhase = 0;
        
        public PanelOperationalHUD(GestorEstadisticas stats) {
            this.stats = stats;
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0xFF, 0x88, 0x00), 2),
                    "[ OPERATIONAL HUB - OBJECTIVES ]",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 11),
                    new Color(0xFF, 0x88, 0x00)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            Timer pulseTimer = new Timer(80, e -> { pulsePhase = (pulsePhase + 1) % 40; repaint(); });
            pulseTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            int y = 28;
            int barHeight = 10;
            int barWidth = Math.max(50, w - 100);

            // Definir 5 objetivos diarios
            String[][] objetivos = {
                {"GANAR 3 PARTIDAS", String.valueOf(Math.min(stats.getJuegosGanados(), 3)), "3"},
                {"RACHA DE 2 SEGUIDAS", String.valueOf(Math.min(stats.getRachaActual(), 2)), "2"},
                {"ACUMULAR 200 XP", String.valueOf(Math.min(stats.getExperiencia(), 200)), "200"},
                {"JUGAR 5 PARTIDAS", String.valueOf(Math.min(stats.getJuegosJugados(), 5)), "5"},
                {"PRECISION > 50%", String.valueOf((int) Math.min(stats.getPrecision(), 100)), "50"}
            };

            Color[] colores = {
                new Color(0x00, 0xFF, 0x88),
                new Color(0x00, 0xFF, 0xFF),
                new Color(0xFF, 0x88, 0x00),
                new Color(0xFF, 0x55, 0xAA),
                new Color(0x88, 0x66, 0xFF)
            };

            int completados = 0;
            for (int i = 0; i < objetivos.length; i++) {
                int actual = Integer.parseInt(objetivos[i][1]);
                int meta = Integer.parseInt(objetivos[i][2]);
                int progreso = Math.min(actual * 100 / Math.max(meta, 1), 100);
                boolean completo = actual >= meta;
                if (completo) completados++;

                g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                g2.setColor(colores[i]);
                String icono = completo ? "\u2713 " : "\u25B8 ";
                g2.drawString(icono + objetivos[i][0], 15, y);
                y += 14;

                // Barra de progreso
                g2.setColor(new Color(0x22, 0x22, 0x22));
                g2.fillRect(15, y, barWidth, barHeight);
                g2.setColor(colores[i]);
                g2.fillRect(15, y, (int)(barWidth * progreso / 100.0), barHeight);
                g2.setColor(new Color(0x44, 0x44, 0x44));
                g2.drawRect(15, y, barWidth, barHeight);
                g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2.setColor(Color.WHITE);
                String pctTxt = progreso + "%";
                int pctW = g2.getFontMetrics().stringWidth(pctTxt);
                g2.drawString(pctTxt, Math.min(15 + barWidth + 5, w - pctW - 5), y + 9);
                y += barHeight + 8;
            }

            // Resumen inferior
            y += 4;
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            float pulse = 0.6f + 0.4f * (float) Math.sin(pulsePhase * 0.16);
            g2.setColor(new Color(0xFF, 0x88, 0x00, (int)(pulse * 255)));
            g2.drawString("OPERACIONES COMPLETADAS: " + completados + "/" + objetivos.length, 15, y);
            g2.setColor(new Color(0x00, 0xFF, 0xFF, (int)(pulse * 200)));
            String estado = completados == objetivos.length ? "MISION CUMPLIDA" : "PENDIENTE";
            String estadoTxt = "ESTADO: " + estado;
            int estadoW = g2.getFontMetrics().stringWidth(estadoTxt);
            g2.drawString(estadoTxt, Math.max(15, w - estadoW - 15), y);
        }
    }
    
    // --- Inner class for Feedback Log ---
    class PanelFeedbackLog extends JPanel {
        private static final long serialVersionUID = 1L;
        private java.util.List<String> messages;
        private int messageIndex = 0;
        
        public PanelFeedbackLog() {
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0xFF, 0x55, 0xAA), 2),
                    "[ R7 NEXUS LIVE FEEDBACK LOG ]",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 11),
                    new Color(0xFF, 0x55, 0xAA)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            messages = new ArrayList<>();
            inicializarMensajes();
            
            // Timer para agregar mensajes nuevos
            Timer timer = new Timer(2000, e -> {
                agregarMensajeAleatorio();
                repaint();
            });
            timer.start();
        }
        
        private void inicializarMensajes() {
            messages.add("[INFO] Sistema NEXUS-7 inicializado correctamente");
            messages.add("[SUCCESS] Conexión con servidor de estadísticas establecida");
            messages.add("[INFO] Cargando módulos de interfaz...");
            messages.add("[SUCCESS] Módulo de mapa de red cargado");
            messages.add("[INFO] Esperando autenticación de usuario...");
        }
        
        private void agregarMensajeAleatorio() {
            String[] tipos = {"[INFO]", "[SUCCESS]", "[ALERT]", "[WARNING]"};
            String[] mensajes = {
                "Escaneando puertos del sistema...",
                "Verificando integridad de datos...",
                "Actualizando métricas de rendimiento...",
                "Monitoreando actividad de red...",
                "Sincronizando con servidor central...",
                "Optimizando caché de memoria...",
                "Analizando patrones de tráfico...",
                "Procesando solicitudes de usuario..."
            };
            
            String tipo = tipos[random.nextInt(tipos.length)];
            String mensaje = mensajes[random.nextInt(mensajes.length)];
            messages.add(tipo + " " + mensaje);
            
            if (messages.size() > 8) {
                messages.remove(0);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Fondo negro
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            // Dibujar mensajes
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            int y = 25;
            
            for (int i = 0; i < messages.size(); i++) {
                String msg = messages.get(i);
                
                if (msg.startsWith("[SUCCESS]")) {
                    g2.setColor(new Color(0x00, 0xFF, 0x88));
                } else if (msg.startsWith("[ALERT]")) {
                    g2.setColor(new Color(0xFF, 0x44, 0x44));
                } else if (msg.startsWith("[WARNING]")) {
                    g2.setColor(new Color(0xFF, 0x88, 0x00));
                } else {
                    g2.setColor(new Color(0x00, 0xFF, 0xFF));
                }
                
                // Timestamp
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                String timestamp = sdf.format(new java.util.Date());
                g2.drawString("[" + timestamp + "] " + msg, 10, y);
                y += 14;
            }
        }
    }
    
    // --- Inner class for Panel Decorativo Técnico ---
    class PanelDecorativoTecnico extends JPanel {
        private static final long serialVersionUID = 1L;
        private int animationPhase = 0;
        private Random random = new Random();
        
        public PanelDecorativoTecnico() {
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x5B, 0x2A, 0x86), 1),
                    "[ SYSTEM STATUS ]",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 10),
                    new Color(0x5B, 0x2A, 0x86)
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // Timer para animación sutil
            Timer animTimer = new Timer(200, e -> {
                animationPhase = (animationPhase + 1) % 30;
                repaint();
            });
            animTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Fondo negro con gradiente sutil
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            // Grid de ciberseguridad (más detallado)
            g2.setColor(new Color(0x5B, 0x2A, 0x86, 30));
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i < w; i += 20) {
                g2.drawLine(i, 0, i, h);
            }
            for (int j = 0; j < h; j += 20) {
                g2.drawLine(0, j, w, j);
            }
            
            // Indicadores de estado (más grandes y detallados)
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            int y = 25;
            int x = 15;
            
            // Estado del kernel con icono
            g2.setColor(new Color(0x00, 0xFF, 0x88));
            g2.drawString("⚡ KERNEL: ACTIVE [v5.15.0-kali3]", x, y);
            y += 18;
            
            // Estado de red con detalles
            g2.setColor(new Color(0x00, 0xFF, 0xFF));
            String ip = "192.168." + random.nextInt(256) + "." + random.nextInt(256);
            g2.drawString("🌐 NETWORK: CONNECTED [" + ip + "]", x, y);
            y += 18;
            
            // Estado de firewall
            g2.setColor(new Color(0xFF, 0x88, 0x00));
            g2.drawString("🛡️ FIREWALL: ACTIVE [UFW]", x, y);
            y += 18;
            
            // Estado de encriptación
            g2.setColor(new Color(0xFF, 0x55, 0xAA));
            g2.drawString("🔐 ENCRYPTION: AES-256-GCM", x, y);
            y += 18;
            
            // Estado de intrusion detection
            g2.setColor(new Color(0xFF, 0x44, 0x44));
            g2.drawString("🚨 IDS: SNORT [MONITORING]", x, y);
            y += 18;
            
            // Estado de VPN
            g2.setColor(new Color(0x88, 0x66, 0xAA));
            g2.drawString("🔒 VPN: DISCONNECTED", x, y);
            y += 25;
            
            // Gráfico de uso de CPU (más grande)
            int cpuBarWidth = w - 30;
            int cpuBarHeight = 12;
            int cpuBarX = 15;
            int cpuBarY = y;
            
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0x00, 0xFF, 0xFF));
            g2.drawString("CPU USAGE:", cpuBarX, cpuBarY - 5);
            
            // Fondo de la barra
            g2.setColor(new Color(0x1A, 0x1A, 0x1A));
            g2.fillRect(cpuBarX, cpuBarY, cpuBarWidth, cpuBarHeight);
            
            // Progreso animado
            float cpuProgress = 0.3f + 0.4f * (float) Math.sin(animationPhase * 0.15);
            int cpuProgressWidth = (int)(cpuBarWidth * cpuProgress);
            
            // Gradiente de la barra (verde a rojo según uso)
            if (cpuProgress < 0.5f) {
                g2.setColor(new Color(0x00, 0xFF, 0x88));
            } else if (cpuProgress < 0.8f) {
                g2.setColor(new Color(0xFF, 0x88, 0x00));
            } else {
                g2.setColor(new Color(0xFF, 0x44, 0x44));
            }
            g2.fillRect(cpuBarX, cpuBarY, cpuProgressWidth, cpuBarHeight);
            
            // Borde de la barra
            g2.setColor(new Color(0x5B, 0x2A, 0x86));
            g2.drawRect(cpuBarX, cpuBarY, cpuBarWidth, cpuBarHeight);
            
            // Texto de porcentaje
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2.setColor(Color.WHITE);
            String cpuPercent = String.format("%.1f%%", cpuProgress * 100);
            g2.drawString(cpuPercent, cpuBarX + cpuBarWidth + 10, cpuBarY + 10);
            
            y += 25;
            
            // Gráfico de uso de RAM (más grande)
            int ramBarWidth = w - 30;
            int ramBarHeight = 12;
            int ramBarX = 15;
            int ramBarY = y;
            
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0x00, 0xFF, 0x88));
            g2.drawString("RAM USAGE:", ramBarX, ramBarY - 5);
            
            // Fondo de la barra
            g2.setColor(new Color(0x1A, 0x1A, 0x1A));
            g2.fillRect(ramBarX, ramBarY, ramBarWidth, ramBarHeight);
            
            // Progreso animado
            float ramProgress = 0.4f + 0.3f * (float) Math.cos(animationPhase * 0.12);
            int ramProgressWidth = (int)(ramBarWidth * ramProgress);
            
            // Gradiente de la barra
            g2.setColor(new Color(0x5B, 0x2A, 0x86));
            g2.fillRect(ramBarX, ramBarY, ramProgressWidth, ramBarHeight);
            
            // Borde de la barra
            g2.setColor(new Color(0x5B, 0x2A, 0x86));
            g2.drawRect(ramBarX, ramBarY, ramBarWidth, ramBarHeight);
            
            // Texto de porcentaje
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2.setColor(Color.WHITE);
            String ramPercent = String.format("%.1f%%", ramProgress * 100);
            g2.drawString(ramPercent, ramBarX + ramBarWidth + 10, ramBarY + 10);

            // Timestamp decorativo (más grande)
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss:SSS");
            String timestamp = sdf.format(new java.util.Date());
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.setColor(new Color(0x88, 0x66, 0xAA));
            g2.drawString("UPTIME: " + timestamp, w - 150, h - 10);
            
            // Iconos de seguridad decorativos
            g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
            g2.setColor(new Color(0x00, 0xFF, 0x88, 100));
            g2.drawString("🔒", 20, h - 15);
            g2.setColor(new Color(0xFF, 0x88, 0x00, 100));
            g2.drawString("⚠️", 45, h - 15);
            g2.setColor(new Color(0x00, 0xFF, 0xFF, 100));
            g2.drawString("📡", 70, h - 15);
        }
    }
    
    // --- Inner class for Kali Linux Logo (Realista basado en PNG) ---
    class PanelKaliLogo extends JPanel {
        private static final long serialVersionUID = 1L;
        private int animationPhase = 0;
        private Timer animTimer;
        
        public PanelKaliLogo() {
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x5B, 0x2A, 0x86), 2),
                    "[ KALI LINUX SYSTEM ]",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 11),
                    new Color(0x5B, 0x2A, 0x86)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            // Timer para animación sutil del logo
            animTimer = new Timer(100, e -> {
                animationPhase = (animationPhase + 1) % 60;
                repaint();
            });
            animTimer.start();
        }
        
        public void detener() {
            if (animTimer != null) { animTimer.stop(); animTimer = null; }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int w = getWidth();
            int h = getHeight();
            
            // Fondo negro
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            // Centro del panel
            int cx = w / 2;
            int cy = h / 2 - 2;
            float pulse = 0.4f + 0.3f * (float) Math.sin(animationPhase * 0.1);

            // Glow circular pulsante
            for (int r = 65; r > 0; r -= 7) {
                g2.setColor(new Color(0x5B, 0x2A, 0x86, (int)(pulse * 14)));
                g2.fill(new Ellipse2D.Float(cx - r, cy - r + 5, r * 2, r * 2));
            }

            // === DRAGON KALI (silueta de multiples crestas - escalado) ===
            float sc = Math.min(w / 180f, (h - 30) / 190f);
            if (sc < 0.5f) sc = 0.5f;
            if (sc > 1.5f) sc = 1.5f;
            java.awt.geom.AffineTransform old = g2.getTransform();
            g2.translate(cx, cy - 10 * sc);
            g2.scale(sc, sc);

            Color c1 = new Color(0x7B, 0x4A, 0xA6);
            Color c2 = new Color(0x5B, 0x2A, 0x86);
            Color c3 = new Color(0x3A, 0x1A, 0x56);
            Color c4 = new Color(0x2A, 0x0A, 0x36);

            // Cresta superior (5 puntas)
            g2.setColor(c1);
            int[] crX = {-40,-25,-18,-10,0,10,18,25,40};
            int[] crY = {-8,-42,-14,-50,-58,-50,-14,-42,-8};
            g2.fillPolygon(crX, crY, 9);

            // Cuerpo central
            g2.setColor(c2);
            int[] bdX = {-35,0,35,25,0,-25};
            int[] bdY = {-5,-22,-5,15,8,15};
            g2.fillPolygon(bdX, bdY, 6);

            // Mandibula
            g2.setColor(c3);
            int[] jwX = {-25,0,25,18,0,-18};
            int[] jwY = {15,8,15,32,26,32};
            g2.fillPolygon(jwX, jwY, 6);

            // Cuello
            g2.setColor(c4);
            int[] nkX = {-14,0,14};
            int[] nkY = {32,48,32};
            g2.fillPolygon(nkX, nkY, 3);

            // Ojo central brillante
            g2.setColor(new Color(0x5A, 0xBC, 0xFF));
            g2.fill(new Ellipse2D.Float(-8, -14, 16, 11));
            g2.setColor(new Color(0x1A, 0x3C, 0x9F));
            g2.fill(new Ellipse2D.Float(-4, -12, 8, 7));
            g2.setColor(new Color(0xFF, 0xFF, 0xFF, 220));
            g2.fill(new Ellipse2D.Float(-1, -14, 3, 3));

            // Lineas de detalle
            g2.setColor(c4);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawLine(-25, -42, -20, -18);
            g2.drawLine(25, -42, 20, -18);
            g2.drawLine(-10, -50, -6, -28);
            g2.drawLine(10, -50, 6, -28);
            g2.drawLine(0, -58, 0, -32);

            g2.setTransform(old);

            // Texto KALI grande
            g2.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2.setColor(new Color(0x7B, 0x4A, 0xA6));
            String kt = "KALI";
            int tw = g2.getFontMetrics().stringWidth(kt);
            g2.drawString(kt, cx - tw / 2, cy + (int)(58 * sc));
            // Texto LINUX
            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.setColor(new Color(0x88, 0x66, 0xCC));
            String lt = "LINUX";
            int lw = g2.getFontMetrics().stringWidth(lt);
            g2.drawString(lt, cx - lw / 2, cy + (int)(74 * sc));

            // Marco decorativo
            int mw = (int)(60 * sc);
            int mh = (int)(78 * sc);
            g2.setColor(new Color(0x5B, 0x2A, 0x86, 60));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(cx - mw, cy - mh + 10, mw * 2, mh * 2, 8, 8);
        }
    }
    
    // --- Inner class for Matrix Boot Effect ---
    class PanelBootMatrix extends JPanel {
        private static final long serialVersionUID = 1L;
        private char[][] grid;
        private int[] drops;
        private Random random = new Random();
        private Timer timer;
        
        public PanelBootMatrix() {
            setBackground(Color.BLACK);
            // Tamaño gigante para soportar resoluciones 4K incluso
            int cols = 3840 / 14; 
            int rows = 2160 / 14; 
            grid = new char[cols][rows];
            drops = new int[cols];
            
            for (int i = 0; i < cols; i++) {
                drops[i] = random.nextInt(rows);
            }
            
            timer = new Timer(33, e -> updateMatrix());
            timer.start();
        }

        public void stopTimer() {
            if (timer != null) timer.stop();
        }
        
        private void updateMatrix() {
            for (int i = 0; i < drops.length; i++) {
                grid[i][drops[i]] = (char) (33 + random.nextInt(94));
                drops[i]++;
                if (drops[i] >= grid[0].length || random.nextDouble() > 0.95) {
                    drops[i] = 0;
                }
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            
            for (int i = 0; i < drops.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    if (grid[i][j] != 0) {
                        if (j == drops[i] - 1) {
                            g2.setColor(Color.WHITE);
                        } else {
                            g2.setColor(new Color(0, 255, 0, 100)); // slightly dimmer
                        }
                        g2.drawString(String.valueOf(grid[i][j]), i * 14, j * 14 + 14);
                        if (random.nextDouble() > 0.9) {
                            grid[i][j] = (char) (33 + random.nextInt(94));
                        }
                    }
                }
            }
            
            if (panelLogin == null || !panelLogin.isVisible()) {
                // Draw fake boot text over matrix only before login shows
                g2.setColor(Color.WHITE);
                g2.drawString("DEEP-THOUGHT KERNEL V8.0", 20, 30);
                g2.drawString("INITIALIZING MEMORY BANKS... OK", 20, 50);
                g2.drawString("MOUNTING VIRTUAL FILE SYSTEM... OK", 20, 70);
                g2.drawString("ESTABLISHING NEURAL LINK... PENDING", 20, 90);
                
                // Giant Title
                g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 60));
                g2.setColor(new Color(0, 255, 0, 80));
                g2.drawString("NEXUS-7 BOOT", getWidth()/2 - 200, getHeight()/2);
            }
            
            // Draw Scanlines overlay (opacidad 30, espaciado 3px, color negro puro)
            g2.setColor(new Color(0, 0, 0, 30));
            for (int i = 0; i < getHeight(); i += 3) {
                g2.drawLine(0, i, getWidth(), i);
            }
        }
    }
    
}
