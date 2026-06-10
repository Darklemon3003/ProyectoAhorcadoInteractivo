package com.ahorcado;

import com.ahorcado.util.Bug;
import com.ahorcado.util.BugAvatarFactory;
import com.ahorcado.util.GestorBugs;
import com.ahorcado.util.GestorEconomia;
import com.ahorcado.util.SkinAhorcado;
import com.ahorcado.util.GestorSkins;
import com.ahorcado.util.FondoJuego;
import com.ahorcado.util.FondoFactory;
import com.ahorcado.util.GestorFondos;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * TiendaClandestina - Ventana de la tienda del juego (tipo "mercado negro").
 * 
 * EXTIENDE JDialog (HERENCIA de Swing). Aqui el jugador puede gastar
 * sus Kali Koints (moneda virtual del juego) en desbloquear:
 * - Bugs/avatares coleccionables
 * - Skins pa el ahorcado
 * - Fondos/wallpapers pa la interfaz
 * 
 * Tiene 3 pestañas y usa GridLayout pa organizar las tarjetas de los items.
 * Los ActionListeners en los botones manejan las compras y el equipamiento.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class TiendaClandestina extends JDialog {

    private static final long serialVersionUID = 1L;

    public static final Color FONDO = new Color(0x05, 0x08, 0x0C);
    public static final Color CIAN = new Color(0x00, 0xFF, 0xFF);
    public static final Color VERDE = new Color(0x00, 0xFF, 0x88);
    public static final Color ROJO = new Color(0xFF, 0x44, 0x44);
    public static final Color AMBAR = new Color(0xFF, 0xD7, 0x00);
    public static final Color ROSA = new Color(0xFF, 0x55, 0xAA);

    private final GestorEconomia eco = GestorEconomia.getInstancia();
    private final GestorBugs bugs = GestorBugs.getInstancia();
    private final GestorSkins skins = GestorSkins.getInstancia();
    private final GestorFondos fondos = GestorFondos.getInstancia();

    private JLabel labelSaldo;
    private JLabel labelMensaje;
    private JPanel panelGrid;
    private JPanel panelSkinGrid;
    private JPanel panelFondoGrid;
    private JPanel contenedorCentral;
    private String tabActual = "BUGS";
    private GestorEconomia.SaldoListener saldoListener;

    public TiendaClandestina(Window owner) {
        super(owner, "BLACK MARKET // KALI_VAULT", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(960, 720);
        setLocationRelativeTo(owner);
        construirUI();

        saldoListener = (ant, nuevo, delta) -> {
            actualizarSaldoLabel();
        };
        eco.addListener(saldoListener);
    }

    @Override
    public void dispose() {
        if (saldoListener != null) eco.removeListener(saldoListener);
        super.dispose();
    }

    private void construirUI() {
        // Marco principal con fondo glitch
        FondoGlitchPanel root = new FondoGlitchPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(VERDE, 2),
            new EmptyBorder(0, 0, 0, 0)
        ));

        root.add(crearHeader(), BorderLayout.NORTH);
        root.add(crearContenedorCentral(), BorderLayout.CENTER);
        root.add(crearFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, VERDE),
            new EmptyBorder(14, 22, 14, 22)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel titulo = new JLabel("[ BLACK MARKET // KALI_VAULT ]");
        titulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        titulo.setForeground(VERDE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titulo);

        JLabel subt = new JLabel("> Hackea los exploits del bazar clandestino");
        subt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        subt.setForeground(CIAN);
        subt.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(subt);

        header.add(left, BorderLayout.WEST);

        // Saldo a la derecha
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        labelSaldo = new JLabel();
        labelSaldo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        labelSaldo.setForeground(AMBAR);
        labelSaldo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(labelSaldo);

        JLabel cap = new JLabel("[ KALI KOINTS ]");
        cap.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        cap.setForeground(new Color(0xCCCCCC));
        cap.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(cap);

        header.add(right, BorderLayout.EAST);
        actualizarSaldoLabel();

        return header;
    }

    private JPanel crearContenedorCentral() {
        contenedorCentral = new JPanel(new BorderLayout());
        contenedorCentral.setOpaque(false);

        // Barra de pestanas
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x00, 0xFF, 0x88, 80)));

        JButton tabBugs = botonTab("BUGS / GLITCHES", true);
        JButton tabSkins = botonTab("SKINS / AHORCADO", false);
        JButton tabFondos = botonTab("FONDOS / WALLPAPERS", false);

        tabBugs.addActionListener(e -> {
            tabActual = "BUGS";
            actualizarTab(tabBugs, tabSkins, tabFondos);
        });
        tabSkins.addActionListener(e -> {
            tabActual = "SKINS";
            actualizarTab(tabBugs, tabSkins, tabFondos);
        });
        tabFondos.addActionListener(e -> {
            tabActual = "FONDOS";
            actualizarTab(tabBugs, tabSkins, tabFondos);
        });

        tabs.add(tabBugs);
        tabs.add(tabSkins);
        tabs.add(tabFondos);
        contenedorCentral.add(tabs, BorderLayout.NORTH);
        contenedorCentral.add(crearGridBugs(), BorderLayout.CENTER);

        return contenedorCentral;
    }

    private JButton botonTab(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        aplicarEstiloTab(btn, activo);
        return btn;
    }

    private void aplicarEstiloTab(JButton btn, boolean activo) {
        if (activo) {
            btn.setForeground(VERDE);
            btn.setBackground(new Color(0x05, 0x15, 0x0A));
        } else {
            btn.setForeground(new Color(0x66, 0x66, 0x66));
            btn.setBackground(FONDO);
        }
    }

    private void actualizarTab(JButton tabBugs, JButton tabSkins, JButton tabFondos) {
        aplicarEstiloTab(tabBugs, "BUGS".equals(tabActual));
        aplicarEstiloTab(tabSkins, "SKINS".equals(tabActual));
        aplicarEstiloTab(tabFondos, "FONDOS".equals(tabActual));

        // Reemplazar el contenido central
        java.awt.Component center = ((BorderLayout) contenedorCentral.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center != null) contenedorCentral.remove(center);

        if ("BUGS".equals(tabActual)) {
            contenedorCentral.add(crearGridBugs(), BorderLayout.CENTER);
        } else if ("SKINS".equals(tabActual)) {
            contenedorCentral.add(crearGridSkins(), BorderLayout.CENTER);
        } else {
            contenedorCentral.add(crearGridFondos(), BorderLayout.CENTER);
        }
        contenedorCentral.revalidate();
        contenedorCentral.repaint();
    }

    private JScrollPane crearGridBugs() {
        panelGrid = new JPanel(new GridLayout(0, 4, 14, 14));
        panelGrid.setOpaque(false);
        panelGrid.setBorder(new EmptyBorder(16, 22, 16, 22));

        for (Bug b : bugs.getCatalogo()) {
            panelGrid.add(new TarjetaBug(b));
        }

        JScrollPane scroll = new JScrollPane(panelGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JScrollPane crearGridSkins() {
        panelSkinGrid = new JPanel(new GridLayout(0, 4, 14, 14));
        panelSkinGrid.setOpaque(false);
        panelSkinGrid.setBorder(new EmptyBorder(16, 22, 16, 22));

        for (SkinAhorcado s : skins.getCatalogo()) {
            panelSkinGrid.add(new TarjetaSkin(s));
        }

        JScrollPane scroll = new JScrollPane(panelSkinGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JScrollPane crearGridFondos() {
        panelFondoGrid = new JPanel(new GridLayout(0, 3, 14, 14));
        panelFondoGrid.setOpaque(false);
        panelFondoGrid.setBorder(new EmptyBorder(16, 22, 16, 22));

        for (FondoJuego f : fondos.getCatalogo()) {
            panelFondoGrid.add(new TarjetaFondo(f));
        }

        JScrollPane scroll = new JScrollPane(panelFondoGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel crearFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, VERDE),
            new EmptyBorder(10, 22, 12, 22)
        ));

        labelMensaje = new JLabel("> SISTEMA EN LINEA. Selecciona un bug para hackearlo.");
        labelMensaje.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        labelMensaje.setForeground(CIAN);
        f.add(labelMensaje, BorderLayout.WEST);

        JButton btnCerrar = botonNeon("[ CERRAR // ESC ]", ROJO);
        btnCerrar.addActionListener(e -> dispose());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnCerrar);
        f.add(right, BorderLayout.EAST);

        // ESC para cerrar
        getRootPane().registerKeyboardAction(e -> dispose(),
            javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        return f;
    }

    private JButton botonNeon(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setForeground(color);
        btn.setBackground(new Color(0x0A, 0x10, 0x14));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(color, 2));
        btn.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(color.getRed() / 6, color.getGreen() / 6, color.getBlue() / 6));
                btn.setBorder(new LineBorder(color, 3));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0x0A, 0x10, 0x14));
                btn.setBorder(new LineBorder(color, 2));
            }
        });
        return btn;
    }

    private void actualizarSaldoLabel() {
        if (labelSaldo != null) {
            labelSaldo.setText(String.format("KK %,d", eco.getSaldo()));
        }
    }

    private void mostrarMensaje(String texto, Color color) {
        labelMensaje.setText(texto);
        labelMensaje.setForeground(color);
    }

    private void refrescarTarjetas() {
        System.out.println("[DEBUG refrescarTarjetas] Iniciando actualización de tarjetas");
        System.out.println("[DEBUG refrescarTarjetas] Bugs activos: " + bugs.getBugsActivos());
        if (panelGrid != null) {
            int count = 0;
            for (Component c : panelGrid.getComponents()) {
                if (c instanceof TarjetaBug) {
                    ((TarjetaBug) c).refrescar();
                    count++;
                }
            }
            System.out.println("[DEBUG refrescarTarjetas] Actualizadas " + count + " tarjetas de bugs");
        }
        if (panelSkinGrid != null) {
            for (Component c : panelSkinGrid.getComponents()) {
                if (c instanceof TarjetaSkin) ((TarjetaSkin) c).refrescar();
            }
        }
        if (panelFondoGrid != null) {
            for (Component c : panelFondoGrid.getComponents()) {
                if (c instanceof TarjetaFondo) ((TarjetaFondo) c).refrescar();
            }
        }
    }

    /** Convenience launcher. */
    public static void abrir(Window owner) {
        TiendaClandestina d = new TiendaClandestina(owner);
        d.setVisible(true);
    }

    // ============================================================
    // Componentes internos
    // ============================================================

    /** Fondo con scanlines + glitch sutiles. */
    private static class FondoGlitchPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Random rnd = new Random();
        private int tick = 0;
        private final Timer glitchTimer;

        FondoGlitchPanel() {
            setOpaque(true);
            setBackground(FONDO);
            glitchTimer = new Timer(80, e -> { tick++; repaint(); });
            glitchTimer.start();
            // Detener el timer automaticamente cuando el panel sea removido del UI
            addHierarchyListener(e -> {
                if (!isDisplayable()) glitchTimer.stop();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            // Gradiente verticalmente
            g2.setPaint(new GradientPaint(0, 0, new Color(0x05, 0x10, 0x14),
                                          0, h, new Color(0x02, 0x06, 0x0A)));
            g2.fillRect(0, 0, w, h);
            // Scanlines
            g2.setColor(new Color(0, 255, 136, 18));
            for (int y = 0; y < h; y += 3) g2.drawLine(0, y, w, y);
            // Bandas glitch ocasionales
            if (tick % 9 == 0) {
                int n = 1 + rnd.nextInt(3);
                for (int i = 0; i < n; i++) {
                    int by = rnd.nextInt(h);
                    int bh = 1 + rnd.nextInt(4);
                    g2.setColor(new Color(255, 0, 255, 35 + rnd.nextInt(40)));
                    g2.fillRect(0, by, w, bh);
                }
            }
            g2.dispose();
        }
    }

    /** Tarjeta para un bug. */
    private class TarjetaBug extends JPanel {
        private static final long serialVersionUID = 1L;
        final Bug bug;
        private boolean hover = false;
        private final JLabel imgLabel;
        private final JLabel nombreLabel;
        private final JLabel precioLabel;
        private JButton accion;
        private final JLabel rarezaLabel;

        TarjetaBug(Bug b) {
            this.bug = b;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(b.colorPrincipal, 1),
                new EmptyBorder(10, 10, 10, 10)
            ));
            setPreferredSize(new Dimension(210, 320));

            // Imagen
            imgLabel = new JLabel();
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(imgLabel, BorderLayout.NORTH);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            nombreLabel = new JLabel();
            nombreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            nombreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(Box.createVerticalStrut(8));
            info.add(nombreLabel);

            rarezaLabel = new JLabel();
            rarezaLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
            rarezaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(rarezaLabel);

            JLabel desc = new JLabel(htmlWrap(b.descripcion));
            desc.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            desc.setForeground(new Color(0xAAAAAA));
            desc.setAlignmentX(Component.CENTER_ALIGNMENT);
            desc.setHorizontalAlignment(SwingConstants.CENTER);
            info.add(Box.createVerticalStrut(4));
            info.add(desc);

            precioLabel = new JLabel();
            precioLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            precioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(Box.createVerticalStrut(6));
            info.add(precioLabel);

            accion = new JButton();
            accion.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            accion.setFocusPainted(false);
            accion.setCursor(new Cursor(Cursor.HAND_CURSOR));
            accion.setAlignmentX(Component.CENTER_ALIGNMENT);
            accion.setMaximumSize(new Dimension(180, 28));
            // ActionListener se agregará en refrescar()
            info.add(Box.createVerticalStrut(8));
            info.add(accion);

            add(info, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });

            refrescar();
        }

        private String htmlWrap(String s) {
            return "<html><div style='text-align:center;width:170px'>" + s + "</div></html>";
        }

        void refrescar() {
            BufferedImage img = bug.isDesbloqueado()
                ? BugAvatarFactory.obtener(bug)
                : BugAvatarFactory.obtenerGris(bug);
            imgLabel.setIcon(new javax.swing.ImageIcon(img));

            nombreLabel.setText(bug.nombre.toUpperCase());
            nombreLabel.setForeground(bug.isDesbloqueado() ? bug.colorPrincipal : new Color(0x888888));
            rarezaLabel.setText("[ " + bug.rareza.etiqueta + " ]");
            rarezaLabel.setForeground(bug.rareza.color);

            // Remover botón existente y crear uno nuevo para forzar actualización
            JPanel info = (JPanel) accion.getParent();
            info.remove(accion);

            if (bug.isDesbloqueado()) {
                boolean esActivo = bugs.isActive(bug.id);
                System.out.println("[DEBUG refrescar] Bug: " + bug.id + ", esActivo: " + esActivo);
                precioLabel.setText(esActivo ? "// ACTIVO" : "// ADQUIRIDO");
                precioLabel.setForeground(esActivo ? VERDE : new Color(0xAAAAAA));
                
                accion = new JButton(esActivo ? "[ DESACTIVAR ]" : "[ ACTIVAR ]");
                accion.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
                accion.setFocusPainted(false);
                accion.setCursor(new Cursor(Cursor.HAND_CURSOR));
                accion.setAlignmentX(Component.CENTER_ALIGNMENT);
                accion.setMaximumSize(new Dimension(180, 28));
                accion.setForeground(esActivo ? ROJO : CIAN);
                accion.setBackground(esActivo ? new Color(0x1A, 0x05, 0x05) : new Color(0x05, 0x10, 0x16));
                accion.setBorder(new LineBorder(esActivo ? ROJO : CIAN, 1));
                accion.setEnabled(true);
                // Capturar bug en variable local para evitar problemas de referencia
                final String bugId = bug.id;
                accion.addActionListener(e -> {
                    System.out.println("[DEBUG CLICK] Click en botón. Bug ID capturado: " + bugId);
                    bugs.toggleBugActivo(bugId);
                    String mensaje = esActivo ? "> BUG DESACTIVADO :: " + bug.nombre.toUpperCase() : "> BUG ACTIVADO :: " + bug.nombre.toUpperCase();
                    mostrarMensaje(mensaje, esActivo ? AMBAR : CIAN);
                    refrescarTarjetas();
                });
            } else {
                precioLabel.setText("KK " + bug.costoKK);
                boolean alcanza = eco.puedePermitirse(bug.costoKK);
                precioLabel.setForeground(alcanza ? AMBAR : ROJO);
                
                accion = new JButton(alcanza ? "[ HACKEAR // UNLOCK ]" : "[ FONDOS INSUFICIENTES ]");
                accion.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
                accion.setFocusPainted(false);
                accion.setCursor(new Cursor(Cursor.HAND_CURSOR));
                accion.setAlignmentX(Component.CENTER_ALIGNMENT);
                accion.setMaximumSize(new Dimension(180, 28));
                accion.setForeground(alcanza ? CIAN : ROJO);
                accion.setBackground(new Color(0x05, 0x10, 0x16));
                accion.setBorder(new LineBorder(alcanza ? CIAN : ROJO, 1));
                accion.setEnabled(alcanza);
                // Capturar bug en variable local para evitar problemas de referencia
                final String bugId = bug.id;
                accion.addActionListener(e -> {
                    System.out.println("[DEBUG CLICK] Click en botón de compra. Bug ID capturado: " + bugId);
                    GestorBugs.ResultadoCompra r = bugs.comprar(bugId);
                    if (r.exito) {
                        mostrarMensaje("> ACCESO CONCEDIDO :: " + bug.nombre.toUpperCase() + " AGREGADO AL ARSENAL [ -" + bug.costoKK + " KK ]", VERDE);
                        animarExito();
                        refrescarTarjetas();
                    } else if ("FONDOS_INSUFICIENTES".equals(r.codigo)) {
                        mostrarMensaje("!! ACCESO DENEGADO :: FONDOS INSUFICIENTES (" + bug.costoKK + " KK requeridos)", ROJO);
                        animarRechazo();
                    } else if ("YA_ADQUIRIDO".equals(r.codigo)) {
                        mostrarMensaje("// EXPLOIT YA INSTALADO", AMBAR);
                    } else {
                        mostrarMensaje("!! ERROR DESCONOCIDO :: " + r.codigo, ROJO);
                    }
                });
            }
            
            // Agregar botón nuevo al panel
            info.add(accion);
            info.revalidate();
            info.repaint();
            revalidate();
            repaint();
        }

        private void animarExito() {
            final int[] frames = { 0 };
            Timer t = new Timer(40, null);
            t.addActionListener(e -> {
                frames[0]++;
                repaint();
                if (frames[0] > 12) t.stop();
            });
            t.start();
        }

        private void animarRechazo() {
            final int[] frames = { 0 };
            Timer t = new Timer(35, null);
            t.addActionListener(e -> {
                frames[0]++;
                int dx = (frames[0] % 2 == 0) ? -3 : 3;
                setLocation(getX() + dx, getY());
                if (frames[0] > 8) {
                    t.stop();
                    setLocation(getX() - dx, getY());
                }
            });
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo card
            g2.setColor(hover ? new Color(0x0A, 0x16, 0x1A) : new Color(0x06, 0x0C, 0x10));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (hover) {
                // Glow
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(bug.colorPrincipal.getRed(), bug.colorPrincipal.getGreen(),
                                      bug.colorPrincipal.getBlue(), 120));
                g2.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
                // Bandas glitch al hover
                Random r = new Random(System.currentTimeMillis() / 80);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                for (int i = 0; i < 2; i++) {
                    int by = r.nextInt(getHeight());
                    g2.setColor(bug.colorAccento);
                    g2.fillRect(0, by, getWidth(), 1 + r.nextInt(3));
                }
            }
            g2.dispose();
        }
    }

    /** Tarjeta para un skin del ahorcado. */
    private class TarjetaSkin extends JPanel {
        private static final long serialVersionUID = 1L;
        final SkinAhorcado skin;
        private boolean hover = false;
        private final JLabel nombreLabel;
        private final JLabel precioLabel;
        private final JButton accion;

        TarjetaSkin(SkinAhorcado s) {
            this.skin = s;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(s.colorHorca, 1),
                new EmptyBorder(10, 10, 10, 10)
            ));
            setPreferredSize(new Dimension(210, 290));

            // Preview visual del skin (mini ahorcado con colores del skin)
            JPanel preview = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();

                    // Fondo del preview
                    g2.setColor(skin.desbloqueado ? skin.colorFondo : new Color(0x10, 0x10, 0x10));
                    g2.fillRect(0, 0, w, h);

                    Color lineColor = skin.desbloqueado ? skin.colorHorca : new Color(0x55, 0x55, 0x55);
                    Color bodyColor = skin.desbloqueado ? skin.colorCuerpo : new Color(0x55, 0x55, 0x55);
                    Color glowColor = skin.desbloqueado
                        ? new Color(skin.colorGlow.getRed(), skin.colorGlow.getGreen(), skin.colorGlow.getBlue(), 30)
                        : new Color(40, 40, 40, 30);

                    // Grid de fondo
                    g2.setColor(glowColor);
                    g2.setStroke(new BasicStroke(0.5f));
                    for (int gx = 0; gx < w; gx += 15) g2.drawLine(gx, 0, gx, h);
                    for (int gy = 0; gy < h; gy += 15) g2.drawLine(0, gy, w, gy);

                    // Mini horca
                    g2.setColor(lineColor);
                    g2.setStroke(new BasicStroke(2f));
                    int cx = w / 2, bY = h - 15;
                    // Base
                    g2.drawLine(cx - 35, bY, cx + 15, bY);
                    // Poste
                    g2.drawLine(cx - 20, bY, cx - 20, 20);
                    // Barra superior
                    g2.drawLine(cx - 20, 20, cx + 10, 20);
                    // Cuerda
                    g2.drawLine(cx + 10, 20, cx + 10, 35);

                    // Mini cuerpo
                    g2.setColor(bodyColor);
                    // Cabeza
                    g2.drawOval(cx + 2, 35, 16, 16);
                    // Torso
                    g2.drawLine(cx + 10, 51, cx + 10, 75);
                    // Brazos
                    g2.drawLine(cx + 10, 58, cx - 2, 68);
                    g2.drawLine(cx + 10, 58, cx + 22, 68);
                    // Piernas
                    g2.drawLine(cx + 10, 75, cx, 92);
                    g2.drawLine(cx + 10, 75, cx + 20, 92);

                    // Glow del piso
                    g2.setColor(glowColor);
                    g2.fillRect(0, bY - 3, w, 8);

                    // Label ACTIVO si es el skin actual
                    if (skin.id.equals(skins.getSkinActivo().id)) {
                        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
                        g2.setColor(VERDE);
                        String act = "ACTIVO";
                        int tw = g2.getFontMetrics().stringWidth(act);
                        g2.drawString(act, w - tw - 4, 12);
                    }

                    g2.dispose();
                }
            };
            preview.setPreferredSize(new Dimension(180, 110));
            preview.setOpaque(false);
            add(preview, BorderLayout.NORTH);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            nombreLabel = new JLabel();
            nombreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            nombreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(Box.createVerticalStrut(8));
            info.add(nombreLabel);

            JLabel desc = new JLabel(htmlSkinWrap(s.descripcion));
            desc.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            desc.setForeground(new Color(0xAAAAAA));
            desc.setAlignmentX(Component.CENTER_ALIGNMENT);
            desc.setHorizontalAlignment(SwingConstants.CENTER);
            info.add(Box.createVerticalStrut(4));
            info.add(desc);

            precioLabel = new JLabel();
            precioLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            precioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(Box.createVerticalStrut(6));
            info.add(precioLabel);

            accion = new JButton();
            accion.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            accion.setFocusPainted(false);
            accion.setCursor(new Cursor(Cursor.HAND_CURSOR));
            accion.setAlignmentX(Component.CENTER_ALIGNMENT);
            accion.setMaximumSize(new Dimension(180, 28));
            accion.addActionListener(e -> interactuar());
            info.add(Box.createVerticalStrut(8));
            info.add(accion);

            add(info, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });

            refrescar();
        }

        private String htmlSkinWrap(String s) {
            return "<html><div style='text-align:center;width:170px'>" + s + "</div></html>";
        }

        void refrescar() {
            nombreLabel.setText(skin.nombre.toUpperCase());
            boolean esActivo = skin.id.equals(skins.getSkinActivo().id);

            if (skin.desbloqueado) {
                nombreLabel.setForeground(skin.colorHorca);
                precioLabel.setText(esActivo ? "// EN USO" : "// ADQUIRIDO");
                precioLabel.setForeground(esActivo ? VERDE : new Color(0xAAAAAA));
                accion.setText(esActivo ? "[ EQUIPADO ]" : "[ EQUIPAR ]");
                accion.setForeground(esActivo ? VERDE : CIAN);
                accion.setBackground(esActivo ? new Color(0x07, 0x18, 0x0E) : new Color(0x05, 0x10, 0x16));
                accion.setBorder(new LineBorder(esActivo ? VERDE : CIAN, 1));
                accion.setEnabled(!esActivo);
            } else {
                nombreLabel.setForeground(new Color(0x888888));
                precioLabel.setText("KK " + skin.costoKK);
                boolean alcanza = eco.puedePermitirse(skin.costoKK);
                precioLabel.setForeground(alcanza ? AMBAR : ROJO);
                accion.setText(alcanza ? "[ HACKEAR // UNLOCK ]" : "[ FONDOS INSUFICIENTES ]");
                accion.setForeground(alcanza ? CIAN : ROJO);
                accion.setBackground(new Color(0x05, 0x10, 0x16));
                accion.setBorder(new LineBorder(alcanza ? CIAN : ROJO, 1));
                accion.setEnabled(alcanza);
            }
            revalidate();
            repaint();
        }

        private void interactuar() {
            if (!skin.desbloqueado) {
                // Comprar
                if (skins.comprar(skin.id)) {
                    mostrarMensaje("> SKIN DESBLOQUEADO :: " + skin.nombre.toUpperCase() + " [ -" + skin.costoKK + " KK ]", VERDE);
                    refrescarTarjetas();
                } else {
                    mostrarMensaje("!! ACCESO DENEGADO :: FONDOS INSUFICIENTES (" + skin.costoKK + " KK requeridos)", ROJO);
                }
            } else {
                // Equipar
                skins.setSkinActivo(skin.id);
                mostrarMensaje("> SKIN EQUIPADO :: " + skin.nombre.toUpperCase(), CIAN);
                refrescarTarjetas();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? new Color(0x0A, 0x16, 0x1A) : new Color(0x06, 0x0C, 0x10));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (hover) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(skin.colorHorca.getRed(), skin.colorHorca.getGreen(),
                                      skin.colorHorca.getBlue(), 120));
                g2.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
            }
            g2.dispose();
        }
    }

    // ============================================================
    // TarjetaFondo - Tarjeta de fondo/wallpaper para la tienda
    // ============================================================
    private class TarjetaFondo extends JPanel {
        private static final long serialVersionUID = 1L;
        final FondoJuego fondo;
        private boolean hover = false;
        private final JLabel nombreLabel;
        private final JLabel precioLabel;
        private final JLabel descripcionLabel;
        private final JButton accion;

        TarjetaFondo(FondoJuego f) {
            this.fondo = f;
            setLayout(new BorderLayout(0, 4));
            setOpaque(false);
            setPreferredSize(new Dimension(270, 220));

            // Panel superior: preview del fondo
            JPanel previewPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    BufferedImage thumb = fondo.desbloqueado
                        ? FondoFactory.obtenerThumbnail(fondo)
                        : FondoFactory.obtenerThumbnailGris(fondo);
                    if (thumb != null) {
                        g2.drawImage(thumb, 0, 0, getWidth(), getHeight(), null);
                    }
                    // Rareza badge
                    g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
                    g2.setColor(new Color(0, 0, 0, 160));
                    g2.fillRect(4, 4, 70, 14);
                    g2.setColor(fondo.rareza.color);
                    g2.drawString("[" + fondo.rareza.etiqueta + "]", 6, 15);
                    // Lock overlay
                    if (!fondo.desbloqueado) {
                        g2.setColor(new Color(0, 0, 0, 120));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
                        g2.setColor(new Color(0xFF, 0x44, 0x44, 180));
                        String lockTxt = "LOCKED";
                        int tw = g2.getFontMetrics().stringWidth(lockTxt);
                        g2.drawString(lockTxt, (getWidth() - tw) / 2, getHeight() / 2 + 6);
                    }
                    // Activo badge
                    if (fondo.desbloqueado && fondo.id.equals(fondos.getFondoActivo().id)) {
                        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
                        g2.setColor(new Color(0, 0, 0, 180));
                        int badgeW = 60;
                        g2.fillRect(getWidth() - badgeW - 4, 4, badgeW, 14);
                        g2.setColor(VERDE);
                        g2.drawString("[ACTIVO]", getWidth() - badgeW - 2, 15);
                    }
                    g2.dispose();
                }
            };
            previewPanel.setPreferredSize(new Dimension(270, 120));
            previewPanel.setOpaque(false);

            // Info panel
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            infoPanel.setBorder(new EmptyBorder(4, 6, 4, 6));

            nombreLabel = new JLabel(fondo.nombre.toUpperCase());
            nombreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            nombreLabel.setForeground(fondo.desbloqueado ? fondo.colorPrimario : new Color(0x66, 0x66, 0x66));
            nombreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            descripcionLabel = new JLabel("<html><body style='width:220px'>" + fondo.descripcion + "</body></html>");
            descripcionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
            descripcionLabel.setForeground(new Color(0x99, 0x99, 0x99));
            descripcionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            precioLabel = new JLabel();
            precioLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
            precioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            accion = new JButton();
            accion.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
            accion.setFocusPainted(false);
            accion.setBorderPainted(false);
            accion.setCursor(new Cursor(Cursor.HAND_CURSOR));
            accion.setMaximumSize(new Dimension(260, 24));
            accion.setAlignmentX(Component.LEFT_ALIGNMENT);
            accion.addActionListener(e -> interactuar());

            infoPanel.add(nombreLabel);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(descripcionLabel);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(precioLabel);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(accion);

            add(previewPanel, BorderLayout.NORTH);
            add(infoPanel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
            refrescar();
        }

        void refrescar() {
            boolean esActivo = fondo.id.equals(fondos.getFondoActivo().id);
            if (fondo.desbloqueado) {
                nombreLabel.setForeground(fondo.colorPrimario);
                if (esActivo) {
                    precioLabel.setText("[EQUIPADO]");
                    precioLabel.setForeground(VERDE);
                    accion.setText(">> ACTIVO <<");
                    accion.setForeground(VERDE);
                    accion.setBackground(new Color(0x00, 0x33, 0x11));
                    accion.setEnabled(false);
                } else {
                    precioLabel.setText("[DESBLOQUEADO]");
                    precioLabel.setForeground(CIAN);
                    accion.setText("[ EQUIPAR ]");
                    accion.setForeground(CIAN);
                    accion.setBackground(new Color(0x05, 0x15, 0x1A));
                    accion.setEnabled(true);
                }
            } else {
                nombreLabel.setForeground(new Color(0x66, 0x66, 0x66));
                if (fondo.costoKK == 0) {
                    precioLabel.setText("[GRATIS]");
                    precioLabel.setForeground(VERDE);
                } else {
                    precioLabel.setText(fondo.costoKK + " KK");
                    precioLabel.setForeground(AMBAR);
                }
                accion.setText("[ COMPRAR ]");
                accion.setForeground(AMBAR);
                accion.setBackground(new Color(0x15, 0x10, 0x05));
                accion.setEnabled(true);
            }
            repaint();
        }

        private void interactuar() {
            if (!fondo.desbloqueado) {
                if (fondos.comprar(fondo.id)) {
                    mostrarMensaje("> FONDO DESBLOQUEADO :: " + fondo.nombre.toUpperCase() + " [ -" + fondo.costoKK + " KK ]", VERDE);
                    refrescarTarjetas();
                } else {
                    mostrarMensaje("!! ACCESO DENEGADO :: FONDOS INSUFICIENTES (" + fondo.costoKK + " KK requeridos)", ROJO);
                }
            } else {
                fondos.setFondoActivo(fondo.id);
                mostrarMensaje("> FONDO EQUIPADO :: " + fondo.nombre.toUpperCase(), CIAN);
                refrescarTarjetas();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? new Color(0x0A, 0x14, 0x1A) : new Color(0x06, 0x0C, 0x10));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            if (hover) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(fondo.colorPrimario.getRed(), fondo.colorPrimario.getGreen(),
                                      fondo.colorPrimario.getBlue(), 120));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 6, 6);
            }
            g2.dispose();
        }
    }
}
