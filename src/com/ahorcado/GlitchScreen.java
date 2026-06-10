package com.ahorcado;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * GlitchScreen - Pantalla de transicion con efecto "glitch" de hackeo.
 * 
 * EXTIENDE JFrame (HERENCIA). Muestra lineas de colores aleatorias
 * y texto distorsionado por 1 segundo antes de ir al menu principal.
 * Es como un efecto de "sistema hackeado" pa darle ambiente al juego.
 * 
 * Usa dos Timers: uno pa la animacion (30 FPS) y otro pa cerrar
 * la ventana despues de 1 segundo.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class GlitchScreen extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private Timer timer;
    private Timer transitionTimer;
    private Random random = new Random();
    private Runnable onComplete;
    
    public GlitchScreen() {
        this(null);
    }
    
    public GlitchScreen(Runnable onComplete) {
        this.onComplete = onComplete;
        setTitle("SYSTEM OVERRIDE");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                
                int w = getWidth();
                int h = getHeight();
                
                // Fondo negro puro
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, w, h);
                
                // Dibujar líneas horizontales aleatorias rojas y verdes
                int numLines = 30 + random.nextInt(20);
                for (int i = 0; i < numLines; i++) {
                    int y = random.nextInt(h);
                    int lineWidth = 50 + random.nextInt(w - 50);
                    int x = random.nextInt(w - lineWidth);
                    
                    // Alternar entre rojo y verde
                    if (random.nextBoolean()) {
                        g2.setColor(new Color(255, 0, 0, 150 + random.nextInt(105)));
                    } else {
                        g2.setColor(new Color(0, 255, 0, 150 + random.nextInt(105)));
                    }
                    g2.fillRect(x, y, lineWidth, 2 + random.nextInt(4));
                }
                
                // Texto distorsionado
                g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
                g2.setColor(new Color(0, 255, 0));
                
                String texto = "CRITICAL UPDATE: SYSTEM OVERRIDE - REDIRECTING...";
                int textX = (w - g2.getFontMetrics().stringWidth(texto)) / 2;
                int textY = h / 2;
                
                // Efecto de distorsión con múltiples copias desplazadas
                for (int i = 0; i < 5; i++) {
                    int offsetX = (random.nextInt(20) - 10);
                    int offsetY = (random.nextInt(10) - 5);
                    g2.drawString(texto, textX + offsetX, textY + offsetY);
                }
                
                // Texto principal
                g2.setColor(new Color(0, 255, 0, 200));
                g2.drawString(texto, textX, textY);
                
                // Efecto de scanlines
                g2.setColor(new Color(0, 0, 0, 100));
                for (int y = 0; y < h; y += 3) {
                    g2.drawLine(0, y, w, y);
                }
            }
        };
        
        panel.setBackground(Color.BLACK);
        getContentPane().add(panel);
        
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Timer para animación (30 FPS)
        timer = new Timer(33, e -> panel.repaint());
        timer.start();
        
        // Timer para cerrar después de 1 segundo
        transitionTimer = new Timer(1000, e -> {
            if (timer != null) { timer.stop(); timer = null; }
            if (transitionTimer != null) { transitionTimer.stop(); transitionTimer = null; }
            dispose();
            // Ejecutar callback si existe, sino crear nueva PantallaArranque
            if (onComplete != null) {
                onComplete.run();
            } else {
                PantallaArranque.main(new String[]{"--skip-boot"});
            }
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }
    
    @Override
    public void dispose() {
        if (timer != null) { timer.stop(); timer = null; }
        if (transitionTimer != null) { transitionTimer.stop(); transitionTimer = null; }
        super.dispose();
    }
}
