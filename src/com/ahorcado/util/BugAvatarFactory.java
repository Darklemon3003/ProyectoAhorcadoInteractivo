package com.ahorcado.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * BugAvatarFactory - Genera los avatares de los bugs de forma programatica.
 * 
 * Dibuja avatares circulares de 160x160 pixeles usando Graphics2D puro
 * (sin imagenes externas). Cada bug tiene su propio diseño con colores
 * tematicos, accesorios y efectos de glitch.
 * 
 * Usa un cache (HashMap) pa no redibujar los avatares cada vez que se
 * necesitan, eso ahorra memoria y procesamiento.
 * 
 * NOTA: esta clase quedo bien larga pero cada metodo dibuja un accesorio
 * diferente (arañas, capuchas, sombreros, etc). No sabia como hacerlo
 * mas corto sin romper nada, perdon profe.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public final class BugAvatarFactory {

    public static final int TAM = 160;

    // Cache estatico: base (sin gesto) para Tienda y fallback
    private static final Map<String, BufferedImage> cache = new HashMap<>();
    private static final Map<String, BufferedImage> cacheGris = new HashMap<>();

    // Cache de frames gestuales: key = bugId + "_" + gesture + "_" + frameKey
    private static final Map<String, BufferedImage> cacheFrames = new HashMap<>();
    private static final int MAX_CACHE_FRAMES = 200;

    private BugAvatarFactory() {}

    // ===== API PUBLICA =====

    /** Avatar base circular (para Tienda, popups estaticos). */
    public static BufferedImage obtener(Bug b) {
        if (b == null) return null;
        return cache.computeIfAbsent(b.id, id -> generarCircular(b, Bug.BugGesture.IDLE, 0f, false));
    }

    /** Avatar base circular en escala de grises (para Tienda, bugs bloqueados). */
    public static BufferedImage obtenerGris(Bug b) {
        if (b == null) return null;
        return cacheGris.computeIfAbsent(b.id, id -> generarCircular(b, Bug.BugGesture.IDLE, 0f, true));
    }

    /**
     * Frame animado para un gesto con progreso dado (0.0 a 1.0).
     * Cuantiza el progreso a 1/10 para cachear eficientemente (~10 frames por gesto).
     */
    public static BufferedImage getFrameForGesture(Bug b, Bug.BugGesture gesture, float progress) {
        if (b == null) return null;
        if (gesture == null) gesture = Bug.BugGesture.IDLE;
        // Cuantizar a decimos para cache eficiente
        int frameKey = (int)(progress * 10);
        if (frameKey > 10) frameKey = 10;
        String key = b.id + "_" + gesture.name() + "_" + frameKey;
        BufferedImage cached = cacheFrames.get(key);
        if (cached != null) return cached;
        // Evitar desbordamiento de cache
        if (cacheFrames.size() > MAX_CACHE_FRAMES) cacheFrames.clear();
        BufferedImage frame = generarCircular(b, gesture, progress, false);
        cacheFrames.put(key, frame);
        return frame;
    }

    /** Limpia caches de frames (llamar al reiniciar partida si se desea). */
    public static void limpiarCacheFrames() { cacheFrames.clear(); }

    // ===== GENERACION CIRCULAR CON GESTO =====

    private static BufferedImage generarCircular(Bug b, Bug.BugGesture gesto, float progreso, boolean grayscale) {
        BufferedImage img = new BufferedImage(TAM, TAM, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Random rnd = new Random(b.id.hashCode());

        int margen = 4;
        int diametro = TAM - margen * 2;
        Ellipse2D.Double clipCirculo = new Ellipse2D.Double(margen, margen, diametro, diametro);

        // === CLIP CIRCULAR: todo el contenido interior se recorta a un circulo ===
        g.setClip(clipCirculo);

        // Fondo oscuro circular
        g.setColor(new Color(0x05, 0x08, 0x0C));
        g.fillOval(margen, margen, diametro, diametro);

        // Ruido RGB de fondo
        for (int i = 0; i < 400; i++) {
            int x = rnd.nextInt(TAM);
            int y = rnd.nextInt(TAM);
            int c = rnd.nextInt(3);
            int alpha = 30 + rnd.nextInt(70);
            Color col = (c == 0) ? new Color(0, 255, 255, alpha)
                       : (c == 1) ? new Color(255, 0, 255, alpha)
                                  : new Color(0, 255, 100, alpha);
            g.setColor(col);
            g.fillRect(x, y, 1 + rnd.nextInt(2), 1);
        }

        // Bandas glitch horizontales
        for (int i = 0; i < 6; i++) {
            int y = rnd.nextInt(TAM);
            int h = 1 + rnd.nextInt(3);
            int alpha = 25 + rnd.nextInt(50);
            g.setColor(new Color(b.colorAccento.getRed(), b.colorAccento.getGreen(),
                                 b.colorAccento.getBlue(), alpha));
            g.fillRect(0, y, TAM, h);
        }

        // Silueta con expresion segun gesto
        BufferedImage silueta = construirSiluetaConGesto(b, rnd, gesto, progreso);

        // RGB shift sutil
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.drawImage(tintar(silueta, new Color(255, 0, 100, 200)), -2, 0, null);
        g.drawImage(tintar(silueta, new Color(0, 200, 255, 200)),  2, 0, null);
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(silueta, 0, 0, null);

        // Particulas de gesto (dentro del clip circular)
        dibujarParticulasGesto(g, gesto, progreso, b);

        // Quitar clip para dibujar el borde circular
        g.setClip(null);

        // === BORDE NEON CIRCULAR PARPADEANTE ===
        dibujarBordeNeonCircular(g, b, margen, diametro, progreso, gesto);

        // === ETIQUETA Y RAREZA (fuera del clip, sobre el borde) ===
        dibujarEtiquetas(g, b);

        g.dispose();

        if (grayscale) return convertirGris(img);
        return img;
    }

    // ===== SILUETA CON EXPRESIONES POR GESTO =====

    private static BufferedImage construirSiluetaConGesto(Bug b, Random rnd, Bug.BugGesture gesto, float progreso) {
        BufferedImage img = new BufferedImage(TAM, TAM, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = TAM / 2;
        int cy = TAM / 2 + 4;
        int rCabeza = 36;

        // Deformacion segun gesto
        int headOffsetY = 0;
        float escalaX = 1.0f;
        if (gesto == Bug.BugGesture.LAUGH) {
            headOffsetY = (int)(-3 * Math.sin(progreso * Math.PI * 3)); // rebote risa
        } else if (gesto == Bug.BugGesture.GROWL) {
            headOffsetY = (int)(2 * Math.sin(progreso * Math.PI * 6)); // temblor
            escalaX = 1.0f + 0.04f * (float)Math.sin(progreso * Math.PI * 4);
        } else if (gesto == Bug.BugGesture.ATTACK_LETTER) {
            headOffsetY = (int)(-8 * progreso + 12 * Math.max(0, progreso - 0.5f) * 2); // lunge forward
        } else if (gesto == Bug.BugGesture.VICTORY_DANCE) {
            headOffsetY = (int)(-5 * Math.sin(progreso * Math.PI * 4));
            escalaX = 1.0f + 0.03f * (float)Math.sin(progreso * Math.PI * 6);
        }

        int ajustadoCy = cy + headOffsetY;

        // Cabeza base (huevoide)
        Color piel = new Color(0xEFEFEF);
        g.setColor(piel);
        int headW = (int)(rCabeza * 2 * escalaX);
        int headX = cx - headW / 2;
        g.fillOval(headX, ajustadoCy - rCabeza - 6, headW, (int)(rCabeza * 2.2));

        // Sombra mejilla
        g.setColor(new Color(0xBBBBBB));
        g.fillOval(headX + 6, ajustadoCy + 8, headW - 12, 16);

        // === OJOS CON EXPRESION ===
        dibujarOjos(g, cx, ajustadoCy, rCabeza, gesto, progreso);

        // === BOCA CON EXPRESION ===
        dibujarBoca(g, cx, ajustadoCy, rCabeza, gesto, progreso, b);

        // Accesorio segun bug
        dibujarAccesorio(g, b, cx, ajustadoCy, rCabeza, rnd);

        // Highlight neon en contorno
        g.setStroke(new BasicStroke(2f));
        g.setColor(b.colorPrincipal);
        g.drawOval(headX, ajustadoCy - rCabeza - 6, headW, (int)(rCabeza * 2.2));

        g.dispose();
        return img;
    }

    private static void dibujarOjos(Graphics2D g, int cx, int cy, int rCabeza,
                                     Bug.BugGesture gesto, float progreso) {
        int ojoY = cy - 12;
        int ojoR = 14;
        int pupilaR = 6;
        int pupilaOffX = 0, pupilaOffY = 0;
        float apertura = 1.0f; // 1.0 = abierto, 0.0 = cerrado

        switch (gesto) {
            case IDLE:
                // Parpadeo ciclico cada ~2 segundos (basado en progreso que se ignora, usar tick)
                // Para IDLE estatico, simplemente abiertos
                break;
            case LAUGH:
                // Ojos entrecerrados de risa, curvados hacia arriba
                apertura = 0.4f + 0.3f * (float)Math.sin(progreso * Math.PI * 2);
                pupilaOffY = -1;
                break;
            case GROWL:
                // Ojos muy abiertos, pupilas contraidas, mirada al frente
                apertura = 1.3f;
                pupilaR = 4; // pupilas contraidas
                break;
            case ATTACK_LETTER:
                // Ojos furiosos apuntando hacia abajo (mirando la letra)
                apertura = 0.6f;
                pupilaOffY = 3;
                pupilaR = 5;
                break;
            case VICTORY_DANCE:
                // Ojos felices, "^  ^" estilo anime
                apertura = 0.3f;
                pupilaOffY = -2;
                break;
        }

        // Escleriotica (blanco del ojo)
        int ojoH = (int)(ojoR * apertura);
        if (ojoH < 3) ojoH = 3;
        g.setColor(Color.WHITE);
        g.fillOval(cx - 22 - ojoR / 2, ojoY - ojoH / 2, ojoR, ojoH);
        g.fillOval(cx + 22 - ojoR / 2, ojoY - ojoH / 2, ojoR, ojoH);

        // Pupilas
        g.setColor(Color.BLACK);
        int px = pupilaR / 2;
        g.fillOval(cx - 22 - px + pupilaOffX, ojoY - px + pupilaOffY, pupilaR, pupilaR);
        g.fillOval(cx + 22 - px + pupilaOffX, ojoY - px + pupilaOffY, pupilaR, pupilaR);

        // Brillo especular
        g.setColor(Color.WHITE);
        g.fillOval(cx - 22 - px + pupilaOffX + 1, ojoY - px + pupilaOffY - 1, 2, 2);
        g.fillOval(cx + 22 - px + pupilaOffX + 1, ojoY - px + pupilaOffY - 1, 2, 2);

        // Para GROWL: venas rojas alrededor
        if (gesto == Bug.BugGesture.GROWL) {
            g.setColor(new Color(0xFF, 0x20, 0x20, 120));
            g.setStroke(new BasicStroke(1f));
            g.drawLine(cx - 22 - ojoR / 2, ojoY, cx - 22 - ojoR / 2 - 4, ojoY - 3);
            g.drawLine(cx + 22 + ojoR / 2, ojoY, cx + 22 + ojoR / 2 + 4, ojoY - 3);
        }
    }

    private static void dibujarBoca(Graphics2D g, int cx, int cy, int rCabeza,
                                     Bug.BugGesture gesto, float progreso, Bug b) {
        int bocaY = cy + 14;
        int bocaW = 36;
        int bocaH = 16;

        switch (gesto) {
            case LAUGH:
                // Boca abierta de risa, mas grande
                bocaH = 20 + (int)(8 * Math.sin(progreso * Math.PI));
                bocaW = 40;
                break;
            case GROWL:
                // Boca contraida, dientes apretados
                bocaH = 10;
                bocaW = 30;
                break;
            case ATTACK_LETTER:
                // Boca MUY abierta para morder
                bocaH = 24 + (int)(10 * (progreso < 0.5f ? progreso * 2 : (1 - progreso) * 2));
                bocaW = 42;
                break;
            case VICTORY_DANCE:
                bocaH = 14 + (int)(6 * Math.abs(Math.sin(progreso * Math.PI * 3)));
                break;
            default:
                break;
        }

        // Boca oscura
        g.setColor(new Color(0x111111));
        Polygon boca = new Polygon();
        boca.addPoint(cx - bocaW / 2, bocaY);
        boca.addPoint(cx + bocaW / 2, bocaY);
        boca.addPoint(cx + bocaW / 2 - 4, bocaY + bocaH);
        boca.addPoint(cx - bocaW / 2 + 4, bocaY + bocaH);
        g.fillPolygon(boca);

        // Dientes sierra
        g.setColor(Color.WHITE);
        int numDientes = 5;
        int dienteW = bocaW / numDientes;
        for (int i = 0; i < numDientes; i++) {
            int tx = cx - bocaW / 2 + i * dienteW + dienteW / 2;
            Polygon diente = new Polygon();
            diente.addPoint(tx - dienteW / 2 + 1, bocaY);
            diente.addPoint(tx + dienteW / 2 - 1, bocaY);
            diente.addPoint(tx, bocaY + Math.min(bocaH / 2, 8));
            g.fillPolygon(diente);
        }

        // Lengua (visible si boca abierta)
        if (bocaH > 14) {
            g.setColor(new Color(0x88FF00));
            int lenguaH = Math.min(14, bocaH - 6);
            g.fillOval(cx - 6, bocaY + bocaH - lenguaH, 12, lenguaH);
        }

        // ATTACK_LETTER: efecto de mordida con "sparks"
        if (gesto == Bug.BugGesture.ATTACK_LETTER && progreso > 0.4f && progreso < 0.7f) {
            g.setColor(new Color(0xFF, 0xFF, 0x00, 200));
            Random sparkRnd = new Random((long)(progreso * 1000));
            for (int i = 0; i < 6; i++) {
                int sx = cx - 20 + sparkRnd.nextInt(40);
                int sy = bocaY + bocaH - 4 + sparkRnd.nextInt(8);
                g.fillRect(sx, sy, 2 + sparkRnd.nextInt(3), 1);
            }
        }
    }

    // ===== PARTICULAS DE GESTO =====

    private static void dibujarParticulasGesto(Graphics2D g, Bug.BugGesture gesto, float progreso, Bug b) {
        if (gesto == Bug.BugGesture.IDLE) return;

        int cx = TAM / 2;
        Random pRnd = new Random((long)(progreso * 100) + gesto.ordinal());

        switch (gesto) {
            case LAUGH:
                // Estrellitas de risa
                g.setColor(new Color(0xFF, 0xFF, 0x00, (int)(180 * (1 - progreso))));
                for (int i = 0; i < 4; i++) {
                    int sx = 20 + pRnd.nextInt(TAM - 40);
                    int sy = 10 + pRnd.nextInt(TAM / 2);
                    dibujarEstrella(g, sx, sy, 3 + pRnd.nextInt(4));
                }
                break;
            case GROWL:
                // Particulas rojas de furia
                g.setColor(new Color(0xFF, 0x22, 0x00, (int)(150 * (1 - progreso))));
                for (int i = 0; i < 8; i++) {
                    int sx = 15 + pRnd.nextInt(TAM - 30);
                    int sy = 15 + pRnd.nextInt(TAM - 30);
                    g.fillRect(sx, sy, 2 + pRnd.nextInt(4), 2);
                }
                // Lineas de impacto
                g.setStroke(new BasicStroke(1.5f));
                g.setColor(new Color(0xFF, 0x00, 0x00, (int)(100 * progreso)));
                g.drawLine(cx - 30, 20, cx - 40, 10);
                g.drawLine(cx + 30, 20, cx + 40, 10);
                break;
            case ATTACK_LETTER:
                // Rayos de ataque y corrupcion visual
                if (progreso > 0.3f) {
                    g.setColor(new Color(0xFF, 0x00, 0xFF, (int)(200 * Math.min(1, (progreso - 0.3f) * 3))));
                    g.setStroke(new BasicStroke(2f));
                    // Lightning bolt
                    g.drawLine(cx, TAM / 2 + 30, cx - 8, TAM / 2 + 50);
                    g.drawLine(cx - 8, TAM / 2 + 50, cx + 4, TAM / 2 + 55);
                    g.drawLine(cx + 4, TAM / 2 + 55, cx - 4, TAM / 2 + 75);
                }
                break;
            case VICTORY_DANCE:
                // Confeti multicolor
                for (int i = 0; i < 10; i++) {
                    int r = pRnd.nextInt(256);
                    int gv = pRnd.nextInt(256);
                    int bv = pRnd.nextInt(256);
                    g.setColor(new Color(r, gv, bv, (int)(200 * (1 - progreso * 0.5f))));
                    int sx = pRnd.nextInt(TAM);
                    int sy = pRnd.nextInt(TAM);
                    g.fillRect(sx, sy, 3 + pRnd.nextInt(4), 2);
                }
                break;
            default:
                break;
        }
    }

    private static void dibujarEstrella(Graphics2D g, int x, int y, int r) {
        int puntas = 4;
        for (int i = 0; i < puntas; i++) {
            double ang = Math.PI * 2 * i / puntas;
            int ex = x + (int)(r * Math.cos(ang));
            int ey = y + (int)(r * Math.sin(ang));
            g.drawLine(x, y, ex, ey);
        }
    }

    // ===== BORDE NEON CIRCULAR PARPADEANTE =====

    private static void dibujarBordeNeonCircular(Graphics2D g, Bug b, int margen, int diametro,
                                                  float progreso, Bug.BugGesture gesto) {
        // Glow exterior (halo difuso)
        float glowPulse = 0.6f + 0.4f * (float)Math.sin(System.currentTimeMillis() * 0.005);
        int glowAlpha = (int)(40 * glowPulse);

        // Color del borde segun gesto
        Color borderColor;
        switch (gesto) {
            case LAUGH:   borderColor = new Color(0x00, 0xFF, 0x88); break;
            case GROWL:   borderColor = new Color(0xFF, 0x22, 0x00); break;
            case ATTACK_LETTER: borderColor = new Color(0xFF, 0x00, 0xFF); break;
            case VICTORY_DANCE: borderColor = new Color(0xFF, 0xD7, 0x00); break;
            default: borderColor = b.colorPrincipal; break;
        }

        // Halo externo
        g.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), glowAlpha));
        g.setStroke(new BasicStroke(6f));
        g.drawOval(margen - 1, margen - 1, diametro + 2, diametro + 2);

        // Borde principal
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(margen, margen, diametro, diametro);

        // Borde interior tenue
        g.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 60));
        g.setStroke(new BasicStroke(1f));
        g.drawOval(margen + 3, margen + 3, diametro - 6, diametro - 6);
    }

    // ===== ETIQUETAS (sobre el borde) =====

    private static void dibujarEtiquetas(Graphics2D g, Bug b) {
        // Etiqueta inferior centrada
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
        String label = b.etiquetaCorta;
        if (label.length() > 14) label = label.substring(0, 14);
        int lw = g.getFontMetrics().stringWidth(label);
        int lx = (TAM - lw) / 2;
        int ly = TAM - 12;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(lx - 4, ly - 9, lw + 8, 13, 6, 6);
        g.setColor(b.colorPrincipal);
        g.drawString(label, lx, ly);

        // Rareza badge superior
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
        String r = "[" + b.rareza.etiqueta + "]";
        int rw = g.getFontMetrics().stringWidth(r);
        int rx = (TAM - rw) / 2;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(rx - 3, 4, rw + 6, 12, 6, 6);
        g.setColor(b.rareza.color);
        g.drawString(r, rx, 14);
    }

    // ===== ACCESORIOS (sin cambios funcionales, preservados) =====

    private static void dibujarAccesorio(Graphics2D g, Bug b, int cx, int cy, int rCabeza, Random rnd) {
        switch (b.accesorio) {
            case "brain_spider":
                // Cerebro encima de la cabeza
                g.setColor(new Color(0xFF77CC));
                g.fillOval(cx - 30, cy - rCabeza - 26, 60, 26);
                g.setColor(new Color(0xCC2288));
                g.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < 4; i++) {
                    int x1 = cx - 24 + i * 14;
                    g.drawArc(x1, cy - rCabeza - 22, 14, 14, 0, 180);
                }
                // Patas de arana
                g.setColor(new Color(0x222222));
                g.setStroke(new BasicStroke(2f));
                for (int i = -2; i <= 2; i++) {
                    if (i == 0) continue;
                    int sx = cx + i * 12;
                    g.drawLine(sx, cy + 30, sx + i * 8, cy + 60);
                }
                break;
            case "reaper":
                // Capucha negra
                g.setColor(new Color(0x111111));
                g.fillArc(cx - rCabeza - 6, cy - rCabeza - 16, rCabeza * 2 + 12, rCabeza * 2 + 4, 0, 180);
                g.fillRect(cx - rCabeza - 6, cy - 16, rCabeza * 2 + 12, 30);
                // Guadana
                g.setColor(new Color(0x888888));
                g.setStroke(new BasicStroke(3f));
                g.drawLine(cx + rCabeza + 4, cy - rCabeza - 10, cx + rCabeza + 30, cy + 40);
                g.setColor(b.colorAccento);
                g.fillArc(cx + rCabeza + 4, cy - rCabeza - 20, 30, 26, 180, 180);
                break;
            case "hoodie":
                g.setColor(new Color(0x0A2A14));
                g.fillArc(cx - rCabeza - 4, cy - rCabeza - 14, rCabeza * 2 + 8, rCabeza * 2 + 4, 0, 180);
                g.fillRect(cx - rCabeza - 4, cy - 16, rCabeza * 2 + 8, 30);
                g.setColor(b.colorPrincipal);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
                g.drawString("DED", cx - 14, cy + 32);
                break;
            case "tophat":
                g.setColor(new Color(0x111111));
                g.fillRect(cx - 30, cy - rCabeza - 26, 60, 22);
                g.fillRect(cx - 22, cy - rCabeza - 36, 44, 14);
                g.setColor(b.colorAccento);
                g.fillRect(cx - 22, cy - rCabeza - 10, 44, 4);
                break;
            case "vr":
                g.setColor(new Color(0x111111));
                g.fillRoundRect(cx - 34, cy - 22, 68, 22, 8, 8);
                g.setColor(b.colorPrincipal);
                g.fillOval(cx - 22, cy - 18, 14, 14);
                g.setColor(b.colorAccento);
                g.fillOval(cx + 8, cy - 18, 14, 14);
                g.setColor(Color.WHITE);
                g.fillOval(cx - 17, cy - 13, 4, 4);
                g.fillOval(cx + 13, cy - 13, 4, 4);
                break;
            case "dynamite":
                // Tubos de dinamita detras
                g.setColor(new Color(0xCC2222));
                g.fillRect(cx - 36, cy - 10, 12, 50);
                g.fillRect(cx + 24, cy - 10, 12, 50);
                g.setColor(new Color(0xFFAA00));
                g.fillOval(cx - 36, cy - 18, 12, 12);
                g.fillOval(cx + 24, cy - 18, 12, 12);
                // Llama
                g.setColor(new Color(0xFFEE00));
                g.fillOval(cx - 4, cy - rCabeza - 22, 10, 14);
                g.setColor(new Color(0xFF6600));
                g.fillOval(cx - 2, cy - rCabeza - 18, 6, 8);
                break;
            case "octopus":
                g.setColor(new Color(0xAA00FF));
                g.fillOval(cx - rCabeza - 4, cy - rCabeza - 8, rCabeza * 2 + 8, rCabeza * 2);
                g.setColor(new Color(0x55FFFF));
                for (int i = -3; i <= 3; i++) {
                    int sx = cx + i * 9;
                    g.setStroke(new BasicStroke(3f));
                    g.drawLine(sx, cy + 28, sx + i * 4, cy + 60);
                }
                // Ojos rojos sobre el body
                g.setColor(new Color(0xFF0044));
                g.fillOval(cx - 18, cy - 10, 10, 10);
                g.fillOval(cx + 8, cy - 10, 10, 10);
                break;
            case "devil":
                // Cuernos rojos
                g.setColor(new Color(0xAA0000));
                Polygon c1 = new Polygon();
                c1.addPoint(cx - 24, cy - rCabeza);
                c1.addPoint(cx - 14, cy - rCabeza);
                c1.addPoint(cx - 20, cy - rCabeza - 24);
                g.fillPolygon(c1);
                Polygon c2 = new Polygon();
                c2.addPoint(cx + 24, cy - rCabeza);
                c2.addPoint(cx + 14, cy - rCabeza);
                c2.addPoint(cx + 20, cy - rCabeza - 24);
                g.fillPolygon(c2);
                // Tridente
                g.setColor(b.colorAccento);
                g.setStroke(new BasicStroke(3f));
                g.drawLine(cx + rCabeza + 10, cy - rCabeza, cx + rCabeza + 20, cy + 40);
                g.drawLine(cx + rCabeza + 6, cy - rCabeza, cx + rCabeza + 14, cy - rCabeza - 12);
                g.drawLine(cx + rCabeza + 14, cy - rCabeza, cx + rCabeza + 14, cy - rCabeza - 12);
                g.drawLine(cx + rCabeza + 14, cy - rCabeza, cx + rCabeza + 22, cy - rCabeza - 12);
                break;
            case "alien":
                // Orbe verde
                g.setColor(new Color(0x55FF88));
                g.fillOval(cx - rCabeza - 2, cy - rCabeza - 8, rCabeza * 2 + 4, (int)(rCabeza * 2.1));
                g.setColor(new Color(0x008844));
                g.fillOval(cx - 24, cy - 16, 18, 12);
                g.fillOval(cx + 6, cy - 16, 18, 12);
                // Antenas
                g.setColor(b.colorAccento);
                g.setStroke(new BasicStroke(2f));
                g.drawLine(cx - 18, cy - rCabeza - 6, cx - 26, cy - rCabeza - 24);
                g.drawLine(cx + 18, cy - rCabeza - 6, cx + 26, cy - rCabeza - 24);
                g.fillOval(cx - 30, cy - rCabeza - 28, 8, 8);
                g.fillOval(cx + 22, cy - rCabeza - 28, 8, 8);
                break;
            case "skullking":
                // Corona dorada
                g.setColor(new Color(0xFFD700));
                Polygon corona = new Polygon();
                int baseY = cy - rCabeza - 4;
                corona.addPoint(cx - 26, baseY);
                corona.addPoint(cx + 26, baseY);
                corona.addPoint(cx + 22, baseY - 18);
                corona.addPoint(cx + 12, baseY - 6);
                corona.addPoint(cx, baseY - 22);
                corona.addPoint(cx - 12, baseY - 6);
                corona.addPoint(cx - 22, baseY - 18);
                g.fillPolygon(corona);
                g.setColor(new Color(0xFF0066));
                g.fillOval(cx - 3, baseY - 22, 6, 6);
                // Ojos huecos
                g.setColor(Color.BLACK);
                g.fillOval(cx - 22 - 8, cy - 20, 16, 16);
                g.fillOval(cx + 22 - 8, cy - 20, 16, 16);
                g.setColor(b.colorAccento);
                g.fillOval(cx - 22 - 3, cy - 14, 6, 6);
                g.fillOval(cx + 22 - 3, cy - 14, 6, 6);
                break;
            case "exploit":
                // Pantalla "EXPLOIT" en el pecho
                g.setColor(new Color(0x111111));
                g.fillRect(cx - 30, cy + 18, 60, 20);
                g.setColor(b.colorPrincipal);
                g.drawRect(cx - 30, cy + 18, 60, 20);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
                g.drawString("0DAY", cx - 16, cy + 33);
                // Gorra
                g.setColor(new Color(0x222222));
                g.fillArc(cx - rCabeza, cy - rCabeza - 18, rCabeza * 2, 30, 0, 180);
                g.fillRect(cx - rCabeza - 6, cy - rCabeza - 4, rCabeza * 2 + 12, 4);
                break;
            case "static":
            default:
                // Estatica intensa sobre la cabeza
                for (int i = 0; i < 200; i++) {
                    int x = cx - rCabeza + rnd.nextInt(rCabeza * 2);
                    int y = cy - rCabeza + rnd.nextInt(rCabeza * 2);
                    g.setColor(rnd.nextBoolean() ? Color.WHITE : Color.BLACK);
                    g.fillRect(x, y, 1, 1);
                }
                g.setColor(b.colorPrincipal);
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Float(cx - rCabeza, cy - rCabeza - 6, rCabeza * 2, (int)(rCabeza * 2.2)));
                break;
        }
    }

    // ===== UTILIDADES =====

    private static BufferedImage tintar(BufferedImage src, Color tint) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) continue;
                int na = (a * tint.getAlpha()) / 255;
                out.setRGB(x, y, (na << 24) | (tint.getRGB() & 0x00FFFFFF));
            }
        }
        return out;
    }

    private static BufferedImage convertirGris(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int gr = (argb >> 8) & 0xFF;
                int bv = argb & 0xFF;
                int v = (int)(0.299 * r + 0.587 * gr + 0.114 * bv);
                v = Math.min(255, (int)(v * 0.55)); // oscurecer
                int gris = (a / 2 << 24) | (v << 16) | (v << 8) | v;
                out.setRGB(x, y, gris);
            }
        }
        return out;
    }
}
