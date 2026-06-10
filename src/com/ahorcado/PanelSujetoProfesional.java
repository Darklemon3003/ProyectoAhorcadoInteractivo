package com.ahorcado;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PanelSujetoProfesional - Panel alternativo del ahorcado con imagenes.
 * 
 * EXTIENDE JPanel (HERENCIA). Este panel usa imagenes reales en vez de
 * dibujar el muñeco con Graphics2D. Tiene efectos de bloom, particulas
 * y animaciones mas sofisticadas.
 * 
 * NOTA: las imagenes se cargan de /images/ y si no existen usa fallback.
 * No borrar la carpeta de imagenes porque se rompe.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class PanelSujetoProfesional extends JPanel {

    private static final long serialVersionUID = 1L;

    // Imágenes del sujeto
    private Image fondoCamara;
    private Image sujetoBase;
    private Image sujetoConectado;
    private Image sujetoDanado;
    private Image piernaRota;
    private Image glowCian;
    private Image particulas;

    // Lógica del juego
    private LogicaJuego logica;

    // Animación
    private Timer animacionTimer;
    private long tiempoInicio;
    private float factorPulso = 0f;
    private List<Particula> listaParticulas;
    private Random random;

    // Estados de transición
    private float opacidadTransicion = 1f;
    private int estadoAnterior = -1;

    // Dimensiones de referencia
    private static final int ANCHO_REFERENCIA = 400;
    private static final int ALTO_REFERENCIA = 600;

    public PanelSujetoProfesional() {
        setOpaque(false);
        setPreferredSize(new Dimension(ANCHO_REFERENCIA, ALTO_REFERENCIA));
        setBackground(new Color(5, 6, 10));

        random = new Random();
        listaParticulas = new ArrayList<>();

        cargarImagenes();
        inicializarParticulas();
        iniciarAnimacion();

        tiempoInicio = System.currentTimeMillis();
    }

    private void cargarImagenes() {
        try {
            fondoCamara = cargarImagen("/images/fondo/camara_contencion.png");
            sujetoBase = cargarImagen("/images/sujeto/base.png");
            sujetoConectado = cargarImagen("/images/sujeto/conectado.png");
            sujetoDanado = cargarImagen("/images/sujeto/danado.png");
            piernaRota = cargarImagen("/images/sujeto/pierna_rota.png");
            glowCian = cargarImagen("/images/efectos/glow_cian.png");
            particulas = cargarImagen("/images/efectos/particulas.png");

            System.out.println("✅ Imágenes cargadas correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error cargando imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Image cargarImagen(String ruta) throws IOException {
        java.net.URL url = getClass().getResource(ruta);
        if (url == null) {
            System.err.println("⚠️ Imagen no encontrada: " + ruta);
            return null;
        }
        Image img = new ImageIcon(url).getImage();
        System.out.println("📷 Cargada: " + ruta + " (" + img.getWidth(null) + "x" + img.getHeight(null) + ")");
        return img;
    }

    private void inicializarParticulas() {
        for (int i = 0; i < 30; i++) {
            listaParticulas.add(new Particula(
                random.nextFloat() * ANCHO_REFERENCIA,
                random.nextFloat() * ALTO_REFERENCIA,
                random.nextFloat() * 2 - 1,
                random.nextFloat() * -1 - 0.5f,
                random.nextFloat() * 0.5f + 0.2f
            ));
        }
    }

    private void iniciarAnimacion() {
        animacionTimer = new Timer(16, e -> {
            long tiempoActual = System.currentTimeMillis();
            float tiempoSegundos = (tiempoActual - tiempoInicio) / 1000f;

            // Factor de pulso para efectos de brillo (0.7 - 1.0)
            factorPulso = 0.85f + 0.15f * (float) Math.sin(tiempoSegundos * 3);

            // Actualizar partículas
            for (Particula p : listaParticulas) {
                p.actualizar(ANCHO_REFERENCIA, ALTO_REFERENCIA);
            }

            // Detectar cambio de estado para transición
            if (logica != null) {
                int estadoActual = logica.obtenerIntentosFallidos();
                if (estadoActual != estadoAnterior) {
                    estadoAnterior = estadoActual;
                    opacidadTransicion = 0f;
                }
                if (opacidadTransicion < 1f) {
                    opacidadTransicion += 0.05f;
                }
            }

            repaint();
        });
        animacionTimer.start();
    }

    public void establecerLogica(LogicaJuego logica) {
        this.logica = logica;
        if (logica != null) {
            estadoAnterior = logica.obtenerIntentosFallidos();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // Fondo oscuro base
        g2.setColor(new Color(5, 6, 10));
        g2.fillRect(0, 0, w, h);

        // 1. Dibujar fondo de cámara de contención
        if (fondoCamara != null) {
            dibujarImagenEscalada(g2, fondoCamara, 0, 0, w, h, 1f);
        }

        // 2. Efecto de vignette (oscurecer bordes)
        dibujarVignette(g2, w, h);

        // 3. Dibujar sujeto según estado
        if (logica != null) {
            int fallos = Math.max(0, Math.min(7, logica.obtenerIntentosFallidos()));
            dibujarSujetoPorEstado(g2, fallos, w, h);
        } else {
            // Estado inicial sin lógica
            dibujarSujetoPorEstado(g2, 0, w, h);
        }

        // 4. Efectos de glow/aura
        dibujarAura(g2, w, h);

        // 5. Partículas flotantes
        dibujarParticulas(g2, w, h);

        // 6. Efecto de scanlines
        dibujarScanlines(g2, w, h);

        // 7. Efecto de vidrio/pantalla
        dibujarOverlayCristal(g2, w, h);

        // 8. Texto de estado
        dibujarTextoEstado(g2, w);

        g2.dispose();
    }

    private void dibujarSujetoPorEstado(Graphics2D g2, int fallos, int w, int h) {
        int centroX = w / 2;
        int centroY = h / 2;

        // Calcular escala para mantener proporción
        float escalaX = w / (float) ANCHO_REFERENCIA;
        float escalaY = h / (float) ALTO_REFERENCIA;
        float escala = Math.min(escalaX, escalaY) * 0.9f;

        int imgAncho = (int) (ANCHO_REFERENCIA * escala);
        int imgAlto = (int) (ALTO_REFERENCIA * escala);
        int x = centroX - imgAncho / 2;
        int y = centroY - imgAlto / 2;

        Image imagenPrincipal = null;
        Image imagenOverlay = null;

        // Seleccionar imagen según estado de fallos
        switch (fallos) {
            case 0:
                imagenPrincipal = sujetoBase;
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                // Sujeto conectado (con cables/tubos)
                imagenPrincipal = sujetoConectado;
                break;
            case 6:
                // Pierna rota - mostrar sujeto dañado + pierna separada
                imagenPrincipal = sujetoDanado;
                imagenOverlay = piernaRota;
                break;
            case 7:
                // Fallo total - sujeto dañado con efectos críticos
                imagenPrincipal = sujetoDanado;
                break;
            default:
                imagenPrincipal = sujetoBase;
        }

        // Dibujar imagen principal con transición suave
        if (imagenPrincipal != null) {
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadTransicion));
            dibujarImagenEscalada(g2, imagenPrincipal, x, y, imgAncho, imgAlto, 1f);
            g2.setComposite(original);
        }

        // Dibujar overlay (pierna rota) con posición ajustada
        if (imagenOverlay != null && fallos == 6) {
            // Posicionar la pierna ligeramente desplazada
            int offsetX = (int) (20 * escala);
            int offsetY = (int) (40 * escala);
            dibujarImagenEscalada(g2, imagenOverlay,
                x + offsetX, y + imgAlto / 2 + offsetY,
                (int) (imgAncho * 0.4f), (int) (imgAlto * 0.5f), 0.9f);
        }

        // Efecto de glow sobre el sujeto
        if (glowCian != null && fallos < 7) {
            int glowSize = (int) (Math.max(imgAncho, imgAlto) * 1.2f);
            int glowX = centroX - glowSize / 2;
            int glowY = centroY - glowSize / 2;

            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f * factorPulso));
            dibujarImagenEscalada(g2, glowCian, glowX, glowY, glowSize, glowSize, 1f);
            g2.setComposite(original);
        }
    }

    private void dibujarImagenEscalada(Graphics2D g2, Image img, int x, int y, int w, int h, float alpha) {
        if (img == null) return;

        Composite original = g2.getComposite();
        if (alpha < 1f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        g2.drawImage(img, x, y, w, h, null);

        if (alpha < 1f) {
            g2.setComposite(original);
        }
    }

    private void dibujarAura(Graphics2D g2, int w, int h) {
        // Gradiente radial desde el centro
        int cx = w / 2;
        int cy = h / 2;
        int radio = Math.max(w, h) / 2;

        float[] dist = {0f, 0.4f, 0.7f, 1f};
        Color[] colors = {
            new Color(0, 200, 255, 20),
            new Color(0, 150, 200, 15),
            new Color(0, 100, 150, 10),
            new Color(0, 0, 0, 0)
        };

        RadialGradientPaint aura = new RadialGradientPaint(cx, cy, radio, dist, colors);
        g2.setPaint(aura);
        g2.fillRect(0, 0, w, h);
    }

    private void dibujarParticulas(Graphics2D g2, int w, int h) {
        if (particulas == null) return;

        Composite original = g2.getComposite();

        for (Particula p : listaParticulas) {
            int px = (int) (p.x / ANCHO_REFERENCIA * w);
            int py = (int) (p.y / ALTO_REFERENCIA * h);
            int size = (int) (p.tamano * Math.min(w, h) / 400);

            float alpha = p.tamano * 0.8f * factorPulso;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(particulas, px - size/2, py - size/2, size, size, null);
        }

        g2.setComposite(original);
    }

    private void dibujarVignette(Graphics2D g2, int w, int h) {
        int cx = w / 2;
        int cy = h / 2;
        int radio = (int) (Math.min(w, h) * 0.7f);

        float[] dist = {0f, 0.6f, 1f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 100),
            new Color(0, 0, 0, 200)
        };

        RadialGradientPaint vignette = new RadialGradientPaint(cx, cy, radio, dist, colors);
        g2.setPaint(vignette);
        g2.fillRect(0, 0, w, h);
    }

    private void dibujarScanlines(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 30));
        int gap = 4;
        for (int y = 0; y < h; y += gap) {
            g2.drawLine(0, y, w, y);
        }

        // Línea de scan horizontal móvil
        int scanY = (int) ((System.currentTimeMillis() / 50) % h);
        g2.setColor(new Color(0, 255, 255, 20));
        g2.drawLine(0, scanY, w, scanY);
    }

    private void dibujarOverlayCristal(Graphics2D g2, int w, int h) {
        // Reflejo de luz en las esquinas (efecto de vidrio)
        GradientPaint reflejo = new GradientPaint(
            0, 0, new Color(255, 255, 255, 15),
            w * 0.3f, h * 0.3f, new Color(255, 255, 255, 0)
        );
        g2.setPaint(reflejo);
        g2.fillRect(0, 0, w/3, h/3);

        // Borde brillante
        g2.setColor(new Color(0, 200, 255, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(2, 2, w-5, h-5);
    }

    private void dibujarTextoEstado(Graphics2D g2, int w) {
        if (logica == null) return;

        int fallos = logica.obtenerIntentosFallidos();
        int maxIntentos = 7;

        g2.setFont(new Font("Consolas", Font.BOLD, 12));

        String texto;
        Color color;

        if (fallos == 0) {
            texto = "● CONEXIÓN ESTABLE - SUJETO #001";
            color = new Color(0, 255, 200);
        } else if (fallos < maxIntentos) {
            texto = "⚠ DESCONEXIÓN PROGRESIVA - INTENTOS: " + fallos + "/" + maxIntentos;
            color = new Color(255, 200, 0);
        } else {
            texto = "✖ FALLO CRÍTICO - CONEXIÓN PERDIDA";
            color = new Color(255, 50, 50);
        }

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(10, 10, 280, 20);

        g2.setColor(color);
        g2.drawString(texto, 15, 25);
    }

    // Clase interna para partículas
    private static class Particula {
        float x, y;
        float vx, vy;
        float tamano;
        float vida;

        Particula(float x, float y, float vx, float vy, float tamano) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.tamano = tamano;
            this.vida = 1f;
        }

        void actualizar(int ancho, int alto) {
            x += vx;
            y += vy;
            vida -= 0.005f;

            if (vida <= 0 || x < 0 || x > ancho || y < 0 || y > alto) {
                // Respawn
                x = (float) Math.random() * ancho;
                y = alto + 10;
                vy = (float) (Math.random() * -1 - 0.5);
                vx = (float) (Math.random() * 2 - 1);
                vida = 1f;
                tamano = (float) (Math.random() * 0.5 + 0.2);
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (animacionTimer != null) {
            animacionTimer.stop();
        }
    }
}
