package com.ahorcado.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * FondoFactory - Genera los fondos/wallpapers del juego de forma programatica.
 * 
 * Dibuja 15 estilos de fondos diferentes (matrix, circuitos, lluvia, etc)
 * usando Graphics2D puro. Genera miniaturas de 220x140 pa la tienda
 * y fondos a tamaño completo pa la interfaz del juego.
 * 
 * Tambien usa un cache (HashMap) pa las miniaturas y asi no se redibujan
 * cada vez que se abre la tienda.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class FondoFactory {

    private static final Map<String, BufferedImage> cacheThumbnails = new HashMap<>();
    private static final int THUMB_W = 220;
    private static final int THUMB_H = 140;

    /**
     * Genera o devuelve el thumbnail cacheado para la tienda.
     */
    public static BufferedImage obtenerThumbnail(FondoJuego fondo) {
        BufferedImage cached = cacheThumbnails.get(fondo.id);
        if (cached != null) return cached;

        BufferedImage img = new BufferedImage(THUMB_W, THUMB_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderizar(g2, THUMB_W, THUMB_H, fondo, 0);
        g2.dispose();

        cacheThumbnails.put(fondo.id, img);
        return img;
    }

    /**
     * Genera un thumbnail en escala de grises para fondos bloqueados.
     */
    public static BufferedImage obtenerThumbnailGris(FondoJuego fondo) {
        BufferedImage color = obtenerThumbnail(fondo);
        BufferedImage gris = new BufferedImage(color.getWidth(), color.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < color.getHeight(); y++) {
            for (int x = 0; x < color.getWidth(); x++) {
                int rgba = color.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                gray = gray / 2; // mas oscuro
                gris.setRGB(x, y, (a << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }
        return gris;
    }

    /**
     * Renderiza el fondo en tiempo real sobre el Graphics2D del panel de juego.
     * @param tick Frame actual para animaciones
     */
    public static void renderizar(Graphics2D g2, int w, int h, FondoJuego fondo, int tick) {
        if (fondo == null) return;
        switch (fondo.estilo) {
            case "matrix":      renderMatrix(g2, w, h, fondo, tick); break;
            case "circuit":     renderCircuit(g2, w, h, fondo, tick); break;
            case "grid":        renderGrid(g2, w, h, fondo, tick); break;
            case "rain":        renderDigitalRain(g2, w, h, fondo, tick); break;
            case "glitch":      renderGlitch(g2, w, h, fondo, tick); break;
            case "particles":   renderParticles(g2, w, h, fondo, tick); break;
            case "waves":       renderWaves(g2, w, h, fondo, tick); break;
            case "hex":         renderHex(g2, w, h, fondo, tick); break;
            case "dedsec":      renderDedsec(g2, w, h, fondo, tick); break;
            case "fire":        renderFire(g2, w, h, fondo, tick); break;
            case "binary":      renderBinary(g2, w, h, fondo, tick); break;
            case "vaporwave":   renderVaporwave(g2, w, h, fondo, tick); break;
            case "galaxy":      renderGalaxy(g2, w, h, fondo, tick); break;
            case "terminal":    renderTerminal(g2, w, h, fondo, tick); break;
            case "skull":       renderSkull(g2, w, h, fondo, tick); break;
            default:            renderGrid(g2, w, h, fondo, tick); break;
        }
    }

    // ===================== ESTILOS DE FONDO =====================

    /** MATRIX - Columnas de caracteres cayendo estilo Matrix. */
    private static void renderMatrix(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(0, 5, 0));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(42);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        int cols = w / 14;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*";
        for (int col = 0; col < cols; col++) {
            int speed = 2 + rnd.nextInt(5);
            int len = 8 + rnd.nextInt(20);
            int startY = ((tick * speed + rnd.nextInt(h)) % (h + len * 14)) - len * 14;
            for (int j = 0; j < len; j++) {
                int cy = startY + j * 14;
                if (cy < 0 || cy > h) continue;
                float brightness = (float)j / len;
                int alpha = (int)(brightness * 180);
                Color c = new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                   f.colorPrimario.getBlue(), Math.min(255, alpha));
                g2.setColor(c);
                char ch = chars.charAt((rnd.nextInt(chars.length()) + tick + j) % chars.length());
                g2.drawString(String.valueOf(ch), col * 14, cy);
            }
            // Head glow
            int headY = startY + len * 14;
            if (headY > 0 && headY < h) {
                g2.setColor(new Color(255, 255, 255, 200));
                char headCh = chars.charAt(rnd.nextInt(chars.length()));
                g2.drawString(String.valueOf(headCh), col * 14, headY);
            }
        }
    }

    /** CIRCUIT - Circuitos PCB con nodos brillantes. */
    private static void renderCircuit(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(5, 8, 12));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(12345);
        g2.setStroke(new BasicStroke(1.5f));
        int numTraces = 30 + w / 20;
        for (int i = 0; i < numTraces; i++) {
            int x1 = rnd.nextInt(w);
            int y1 = rnd.nextInt(h);
            int segments = 3 + rnd.nextInt(5);
            int alpha = 40 + rnd.nextInt(60);
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), alpha));
            int px = x1, py = y1;
            for (int s = 0; s < segments; s++) {
                boolean horizontal = rnd.nextBoolean();
                int len = 20 + rnd.nextInt(60);
                int nx = horizontal ? px + (rnd.nextBoolean() ? len : -len) : px;
                int ny = horizontal ? py : py + (rnd.nextBoolean() ? len : -len);
                g2.drawLine(px, py, nx, ny);
                px = nx;
                py = ny;
            }
            // Nodo al final
            int nodeAlpha = 60 + (int)(40 * Math.sin(tick * 0.05 + i));
            g2.setColor(new Color(f.colorSecundario.getRed(), f.colorSecundario.getGreen(),
                                 f.colorSecundario.getBlue(), Math.max(0, Math.min(255, nodeAlpha))));
            g2.fillOval(px - 3, py - 3, 6, 6);
        }
        // Pulso viajando por las trazas
        g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                             f.colorAccento.getBlue(), 120));
        for (int i = 0; i < 5; i++) {
            int px = (tick * 3 + i * 73) % w;
            int py = (tick * 2 + i * 137) % h;
            g2.fillOval(px - 4, py - 4, 8, 8);
            g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                                 f.colorAccento.getBlue(), 40));
            g2.fillOval(px - 10, py - 10, 20, 20);
        }
    }

    /** GRID - Cuadricula neon perspectiva tipo Tron. */
    private static void renderGrid(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setPaint(new GradientPaint(0, 0, new Color(2, 0, 10), 0, h, new Color(0, 0, 0)));
        g2.fillRect(0, 0, w, h);
        int gridSize = 30;
        int offset = (tick * 2) % gridSize;
        g2.setStroke(new BasicStroke(0.5f));
        // Horizontal lines
        for (int y = offset; y < h; y += gridSize) {
            float dist = (float)y / h;
            int alpha = (int)(20 + 40 * dist);
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), Math.min(255, alpha)));
            g2.drawLine(0, y, w, y);
        }
        // Vertical lines with perspective convergence
        int vanishY = h / 4;
        for (int x = -w; x < w * 2; x += gridSize) {
            int alpha = 25;
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), alpha));
            g2.drawLine(w / 2, vanishY, x, h);
        }
        // Horizon glow
        g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                             f.colorAccento.getBlue(), 30));
        g2.fillRect(0, vanishY - 5, w, 10);
        // Pulsating sun/orb
        int orbR = 20 + (int)(5 * Math.sin(tick * 0.03));
        g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                             f.colorAccento.getBlue(), 60));
        g2.fillOval(w / 2 - orbR, vanishY - orbR, orbR * 2, orbR * 2);
    }

    /** RAIN - Lluvia digital diagonal con splash. */
    private static void renderDigitalRain(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(3, 3, 8));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(7777);
        g2.setStroke(new BasicStroke(1f));
        int numDrops = 60 + w / 8;
        for (int i = 0; i < numDrops; i++) {
            int speed = 3 + rnd.nextInt(6);
            int x = rnd.nextInt(w);
            int y = ((tick * speed + rnd.nextInt(h * 2)) % (h + 100)) - 50;
            int len = 10 + rnd.nextInt(30);
            int alpha = 60 + rnd.nextInt(120);
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), Math.min(255, alpha)));
            g2.drawLine(x, y, x - len / 4, y + len);
            // Splash en la base
            if (y + len > h - 20 && y + len < h + 10) {
                g2.setColor(new Color(f.colorSecundario.getRed(), f.colorSecundario.getGreen(),
                                     f.colorSecundario.getBlue(), 80));
                g2.fillOval(x - len / 4 - 2, h - 5, 4, 2);
            }
        }
    }

    /** GLITCH - Bandas de interferencia, artefactos RGB, ruido pixelado. */
    private static void renderGlitch(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(5, 0, 10));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(tick / 3);
        // Horizontal glitch bands
        int numBands = 5 + rnd.nextInt(8);
        for (int i = 0; i < numBands; i++) {
            int by = rnd.nextInt(h);
            int bh = 1 + rnd.nextInt(8);
            int bx = rnd.nextInt(w / 4);
            int bw = w / 2 + rnd.nextInt(w / 2);
            Color[] colors = {
                new Color(f.colorPrimario.getRed(), 0, 0, 30 + rnd.nextInt(50)),
                new Color(0, f.colorPrimario.getGreen(), 0, 30 + rnd.nextInt(50)),
                new Color(0, 0, f.colorPrimario.getBlue(), 30 + rnd.nextInt(50))
            };
            g2.setColor(colors[rnd.nextInt(3)]);
            g2.fillRect(bx, by, bw, bh);
        }
        // Pixel noise blocks
        for (int i = 0; i < 20; i++) {
            int bx = rnd.nextInt(w);
            int by = rnd.nextInt(h);
            int bs = 2 + rnd.nextInt(6);
            g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                                 f.colorAccento.getBlue(), 40 + rnd.nextInt(60)));
            g2.fillRect(bx, by, bs, bs);
        }
        // Big glitch block
        if (tick % 15 < 3) {
            int blockY = rnd.nextInt(h);
            int blockH = 10 + rnd.nextInt(30);
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), 25));
            g2.fillRect(0, blockY, w, blockH);
        }
        // Scanlines
        g2.setColor(new Color(f.colorSecundario.getRed(), f.colorSecundario.getGreen(),
                             f.colorSecundario.getBlue(), 12));
        for (int y = 0; y < h; y += 3) g2.drawLine(0, y, w, y);
    }

    /** PARTICLES - Particulas flotantes conectadas tipo constelacion. */
    private static void renderParticles(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(3, 5, 10));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(555);
        int numParticles = 40;
        int[][] particles = new int[numParticles][2];
        for (int i = 0; i < numParticles; i++) {
            particles[i][0] = (int)(rnd.nextInt(w) + Math.sin(tick * 0.02 + i * 0.7) * 20) % w;
            particles[i][1] = (int)(rnd.nextInt(h) + Math.cos(tick * 0.015 + i * 0.5) * 15) % h;
            if (particles[i][0] < 0) particles[i][0] += w;
            if (particles[i][1] < 0) particles[i][1] += h;
        }
        // Lines between nearby particles
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < numParticles; i++) {
            for (int j = i + 1; j < numParticles; j++) {
                double dist = Math.hypot(particles[i][0] - particles[j][0],
                                        particles[i][1] - particles[j][1]);
                if (dist < 100) {
                    int alpha = (int)(40 * (1 - dist / 100));
                    g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                         f.colorPrimario.getBlue(), Math.max(0, alpha)));
                    g2.drawLine(particles[i][0], particles[i][1],
                               particles[j][0], particles[j][1]);
                }
            }
        }
        // Particles themselves
        for (int i = 0; i < numParticles; i++) {
            int size = 2 + rnd.nextInt(4);
            float pulse = (float)(0.5 + 0.5 * Math.sin(tick * 0.05 + i));
            int alpha = (int)(80 + 120 * pulse);
            g2.setColor(new Color(f.colorSecundario.getRed(), f.colorSecundario.getGreen(),
                                 f.colorSecundario.getBlue(), Math.min(255, alpha)));
            g2.fillOval(particles[i][0] - size / 2, particles[i][1] - size / 2, size, size);
            // Glow
            g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                                 f.colorAccento.getBlue(), (int)(20 * pulse)));
            g2.fillOval(particles[i][0] - size * 2, particles[i][1] - size * 2, size * 4, size * 4);
        }
    }

    /** WAVES - Ondas sinusoidales neon horizontales. */
    private static void renderWaves(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(2, 2, 8));
        g2.fillRect(0, 0, w, h);
        g2.setStroke(new BasicStroke(1.5f));
        int numWaves = 8;
        for (int wave = 0; wave < numWaves; wave++) {
            double freq = 0.01 + wave * 0.005;
            double amp = 15 + wave * 8;
            int baseY = h / (numWaves + 1) * (wave + 1);
            float interp = (float)wave / numWaves;
            Color c = interpolateColor(f.colorPrimario, f.colorSecundario, interp);
            int alpha = 50 + (int)(30 * Math.sin(tick * 0.03 + wave));
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha))));
            int prevY = baseY + (int)(amp * Math.sin(freq * 0 + tick * 0.04 + wave));
            for (int x = 1; x < w; x++) {
                int curY = baseY + (int)(amp * Math.sin(freq * x + tick * 0.04 + wave));
                g2.drawLine(x - 1, prevY, x, curY);
                prevY = curY;
            }
        }
    }

    /** HEX - Cuadricula hexagonal con pulsos. */
    private static void renderHex(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(4, 4, 8));
        g2.fillRect(0, 0, w, h);
        int hexSize = 20;
        double hexH = hexSize * Math.sqrt(3);
        g2.setStroke(new BasicStroke(0.8f));
        Random rnd = new Random(999);
        for (double row = -1; row * hexH < h + hexH; row++) {
            for (double col = -1; col * hexSize * 1.5 < w + hexSize * 2; col++) {
                double cx = col * hexSize * 1.5;
                double cy = row * hexH + ((int)col % 2 == 0 ? 0 : hexH / 2);
                double dist = Math.hypot(cx - w / 2.0, cy - h / 2.0);
                double pulse = Math.sin(tick * 0.03 - dist * 0.01) * 0.5 + 0.5;
                int alpha = (int)(15 + 35 * pulse);
                g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                     f.colorPrimario.getBlue(), Math.min(255, alpha)));
                dibujarHexagono(g2, (int)cx, (int)cy, hexSize);
                // Fill highlight
                if (pulse > 0.75 && rnd.nextInt(3) == 0) {
                    g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                                         f.colorAccento.getBlue(), 15));
                    rellenarHexagono(g2, (int)cx, (int)cy, hexSize - 2);
                }
            }
        }
    }

    private static void dibujarHexagono(Graphics2D g2, int cx, int cy, int size) {
        int[] xp = new int[6], yp = new int[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i - Math.PI / 6;
            xp[i] = cx + (int)(size * Math.cos(angle));
            yp[i] = cy + (int)(size * Math.sin(angle));
        }
        g2.drawPolygon(xp, yp, 6);
    }

    private static void rellenarHexagono(Graphics2D g2, int cx, int cy, int size) {
        int[] xp = new int[6], yp = new int[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i - Math.PI / 6;
            xp[i] = cx + (int)(size * Math.cos(angle));
            yp[i] = cy + (int)(size * Math.sin(angle));
        }
        g2.fillPolygon(xp, yp, 6);
    }

    /** DEDSEC - Estilo Watch Dogs con iconos DedSec, patches de ruido, texto hacker. */
    private static void renderDedsec(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(5, 5, 5));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(tick / 5);
        // Diagonal slash lines (como las imagenes de referencia)
        g2.setStroke(new BasicStroke(3f));
        for (int i = 0; i < 6; i++) {
            int sx = rnd.nextInt(w);
            int sy = 0;
            int ex = sx - h / 2;
            int ey = h;
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), 25 + rnd.nextInt(30)));
            g2.drawLine(sx, sy, ex, ey);
        }
        // Repeated text pattern
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        String[] texts = {"DEDSEC", "HACK", "ROOT", "EXPLOIT", "0xDEAD", "BREACH"};
        g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                             f.colorAccento.getBlue(), 18));
        Random rnd2 = new Random(1234);
        for (int y = 0; y < h; y += 25) {
            for (int x = 0; x < w; x += 80) {
                String txt = texts[rnd2.nextInt(texts.length)];
                AffineTransform old = g2.getTransform();
                g2.rotate(Math.toRadians(-15), x, y);
                g2.drawString(txt, x, y);
                g2.setTransform(old);
            }
        }
        // Glitch blocks
        for (int i = 0; i < 4; i++) {
            int bx = rnd.nextInt(w);
            int by = rnd.nextInt(h);
            int bw = 30 + rnd.nextInt(80);
            int bh = 5 + rnd.nextInt(20);
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), 20 + rnd.nextInt(25)));
            g2.fillRect(bx, by, bw, bh);
        }
        // Skull icon center
        int skullCx = w / 2;
        int skullCy = h / 2;
        g2.setColor(new Color(f.colorSecundario.getRed(), f.colorSecundario.getGreen(),
                             f.colorSecundario.getBlue(), 20));
        g2.fillOval(skullCx - 25, skullCy - 30, 50, 50);
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(skullCx - 12, skullCy - 15, 10, 12);
        g2.fillOval(skullCx + 2, skullCy - 15, 10, 12);
        g2.fillRect(skullCx - 8, skullCy + 8, 16, 3);
    }

    /** FIRE - Fuego pixelado ascendente con colores calidos. */
    private static void renderFire(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(5, 0, 0));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(42);
        int cols = w / 6;
        for (int col = 0; col < cols; col++) {
            int intensity = rnd.nextInt(100);
            int flamHeight = (int)(h * 0.3 + h * 0.5 * (intensity / 100.0) * (0.5 + 0.5 * Math.sin(tick * 0.06 + col * 0.3)));
            for (int row = 0; row < flamHeight; row += 4) {
                int y = h - row;
                float t = (float)row / flamHeight;
                Color c;
                if (t < 0.3f) c = f.colorPrimario;
                else if (t < 0.6f) c = f.colorSecundario;
                else c = f.colorAccento;
                int alpha = (int)((1 - t) * 60);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha))));
                int px = col * 6 + (int)(Math.sin(tick * 0.08 + row * 0.1 + col) * 3);
                g2.fillRect(px, y, 6, 4);
            }
        }
    }

    /** BINARY - Ceros y unos flotando en columnas con color tematico. */
    private static void renderBinary(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(0, 2, 5));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(31337);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        int cols = w / 12;
        for (int col = 0; col < cols; col++) {
            int speed = 1 + rnd.nextInt(4);
            int len = 10 + rnd.nextInt(25);
            int startY = ((tick * speed + rnd.nextInt(h * 2)) % (h + len * 13)) - len * 13;
            for (int j = 0; j < len; j++) {
                int cy = startY + j * 13;
                if (cy < 0 || cy > h) continue;
                float bright = 0.3f + 0.7f * ((float)j / len);
                int alpha = (int)(bright * 100);
                g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                     f.colorPrimario.getBlue(), Math.min(255, alpha)));
                g2.drawString(rnd.nextBoolean() ? "1" : "0", col * 12, cy);
            }
        }
    }

    /** VAPORWAVE - Estetica retro-futurista con gradientes, grid y sol. */
    private static void renderVaporwave(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        // Sky gradient
        g2.setPaint(new GradientPaint(0, 0, new Color(20, 0, 40),
                                      0, h / 2, new Color(f.colorPrimario.getRed() / 3,
                                                           f.colorPrimario.getGreen() / 3,
                                                           f.colorPrimario.getBlue() / 3)));
        g2.fillRect(0, 0, w, h / 2);
        // Ground
        g2.setPaint(new GradientPaint(0, h / 2, new Color(5, 0, 15),
                                      0, h, new Color(0, 0, 0)));
        g2.fillRect(0, h / 2, w, h / 2);
        // Sun
        int sunR = Math.min(w, h) / 5;
        int sunCx = w / 2;
        int sunCy = h / 2 - sunR / 3;
        for (int r = sunR; r > 0; r -= 2) {
            float t = (float)r / sunR;
            Color c = interpolateColor(f.colorAccento, f.colorPrimario, t);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(80 * (1 - t))));
            g2.fillOval(sunCx - r, sunCy - r, r * 2, r * 2);
        }
        // Horizontal bands through sun
        g2.setColor(new Color(0, 0, 0, 120));
        for (int y = sunCy - sunR; y < sunCy + sunR; y += 6) {
            if ((y / 6) % 2 == 0) g2.fillRect(sunCx - sunR, y, sunR * 2, 3);
        }
        // Grid on ground
        g2.setStroke(new BasicStroke(0.8f));
        int gridSize = 25;
        int offset = (tick * 2) % gridSize;
        g2.setColor(new Color(f.colorSecundario.getRed(), f.colorSecundario.getGreen(),
                             f.colorSecundario.getBlue(), 30));
        for (int y = h / 2 + offset; y < h; y += gridSize) {
            g2.drawLine(0, y, w, y);
        }
        for (int x = -w; x < w * 2; x += gridSize) {
            g2.drawLine(w / 2, h / 2, x, h);
        }
    }

    /** GALAXY - Campo de estrellas con nebulosa. */
    private static void renderGalaxy(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(0, 0, 3));
        g2.fillRect(0, 0, w, h);
        Random rnd = new Random(9876);
        // Nebula clouds
        for (int i = 0; i < 5; i++) {
            int cx = rnd.nextInt(w);
            int cy = rnd.nextInt(h);
            int r = 40 + rnd.nextInt(80);
            float pulse = (float)(0.5 + 0.3 * Math.sin(tick * 0.01 + i));
            Color nebColor = (i % 2 == 0) ? f.colorPrimario : f.colorSecundario;
            for (int ring = r; ring > 0; ring -= 5) {
                int alpha = (int)(8 * pulse * (1 - (float)ring / r));
                g2.setColor(new Color(nebColor.getRed(), nebColor.getGreen(),
                                     nebColor.getBlue(), Math.max(0, Math.min(255, alpha))));
                g2.fillOval(cx - ring, cy - ring, ring * 2, ring * 2);
            }
        }
        // Stars
        for (int i = 0; i < 120; i++) {
            int sx = rnd.nextInt(w);
            int sy = rnd.nextInt(h);
            float twinkle = (float)(0.5 + 0.5 * Math.sin(tick * 0.07 + i * 1.3));
            int alpha = (int)(100 + 155 * twinkle);
            int size = 1 + (rnd.nextInt(3) == 0 ? 1 : 0);
            g2.setColor(new Color(255, 255, 255, Math.min(255, alpha)));
            g2.fillRect(sx, sy, size, size);
        }
        // Shooting star
        int shootTick = tick % 200;
        if (shootTick < 20) {
            float t = shootTick / 20f;
            int sx = (int)(w * t);
            int sy = (int)(h * 0.2 + h * 0.3 * t);
            g2.setColor(new Color(f.colorAccento.getRed(), f.colorAccento.getGreen(),
                                 f.colorAccento.getBlue(), (int)(200 * (1 - t))));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(sx, sy, sx - 30, sy - 10);
        }
    }

    /** TERMINAL - Terminal retro con cursor parpadeante y texto fantasma. */
    private static void renderTerminal(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(0, 3, 0));
        g2.fillRect(0, 0, w, h);
        // Scanlines
        g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                             f.colorPrimario.getBlue(), 8));
        for (int y = 0; y < h; y += 2) g2.drawLine(0, y, w, y);
        // Ghost text lines
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        String[] cmds = {"$ sudo nmap -sV target", "$ cat /etc/shadow", "$ ssh root@192.168.1.1",
                         "$ hydra -l admin -P pass.txt", "$ msfconsole", "$ airmon-ng start wlan0",
                         "$ john --wordlist hash.txt", "$ sqlmap -u target", "$ nc -lvp 4444",
                         "$ python3 exploit.py", "$ chmod 777 backdoor.sh", "$ wireshark -i eth0"};
        int offset = (tick / 3) % 20;
        for (int line = 0; line < h / 16; line++) {
            int alpha = 15 + (int)(10 * Math.sin(tick * 0.02 + line));
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), Math.max(0, Math.min(255, alpha))));
            String cmd = cmds[(line + offset) % cmds.length];
            g2.drawString(cmd, 10, 16 + line * 16);
        }
        // Blinking cursor
        if (tick % 30 < 15) {
            g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                 f.colorPrimario.getBlue(), 80));
            g2.fillRect(10, h - 30, 8, 14);
        }
    }

    /** SKULL - Craneos pixelados repetidos con halftone DedSec. */
    private static void renderSkull(Graphics2D g2, int w, int h, FondoJuego f, int tick) {
        g2.setColor(new Color(5, 5, 5));
        g2.fillRect(0, 0, w, h);
        int skullSize = 40;
        // Halftone dots pattern
        for (int y = 0; y < h; y += 8) {
            for (int x = 0; x < w; x += 8) {
                float dist = (float)(Math.hypot(x - w / 2.0, y - h / 2.0) / (Math.max(w, h) / 2.0));
                int dotSize = (int)(3 * (1 - dist));
                if (dotSize > 0) {
                    g2.setColor(new Color(f.colorPrimario.getRed(), f.colorPrimario.getGreen(),
                                         f.colorPrimario.getBlue(), 15));
                    g2.fillOval(x, y, dotSize, dotSize);
                }
            }
        }
        // Skulls grid
        for (int sy = 0; sy < h + skullSize; sy += skullSize * 2) {
            for (int sx = 0; sx < w + skullSize; sx += skullSize * 2) {
                int offsetX = ((sy / (skullSize * 2)) % 2 == 0) ? 0 : skullSize;
                int px = sx + offsetX;
                float pulse = (float)(0.5 + 0.3 * Math.sin(tick * 0.02 + px * 0.01 + sy * 0.01));
                int alpha = (int)(20 + 20 * pulse);
                dibujarMiniSkull(g2, px, sy, skullSize, f.colorSecundario, alpha);
            }
        }
        // Center large skull
        dibujarMiniSkull(g2, w / 2 - 40, h / 2 - 40, 80, f.colorAccento, 30);
    }

    private static void dibujarMiniSkull(Graphics2D g2, int x, int y, int size, Color c, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
        // Head oval
        g2.drawOval(x + size / 4, y, size / 2, size / 2);
        // Eyes
        int eyeSize = size / 8;
        g2.fillOval(x + size / 3 - eyeSize / 2, y + size / 5, eyeSize, eyeSize);
        g2.fillOval(x + size * 2 / 3 - eyeSize / 2, y + size / 5, eyeSize, eyeSize);
        // Jaw
        g2.drawLine(x + size / 3, y + size / 2 - 2, x + size * 2 / 3, y + size / 2 - 2);
        // Teeth
        for (int t = 0; t < 3; t++) {
            int tx = x + size / 3 + t * (size / 9);
            g2.drawLine(tx, y + size / 2 - 2, tx, y + size / 2 + 3);
        }
    }

    private static Color interpolateColor(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(Math.max(0, Math.min(255, r)),
                        Math.max(0, Math.min(255, g)),
                        Math.max(0, Math.min(255, bl)));
    }
}
