package com.ahorcado;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.ahorcado.util.SkinAhorcado;
import com.ahorcado.util.GestorSkins;

/**
 * PanelSujeto - Panel que dibuja el muñeco del ahorcado.
 * 
 * EXTIENDE JPanel (HERENCIA) pa poder hacer paintComponent personalizado.
 * Aqui se dibuja la horca, el cuerpo, las particulas y las animaciones
 * de cuando el jugador pierde (la animacion de caida y pendulo).
 * 
 * Le puse un Timer de Swing como hilo pa que las animaciones se
 * actualicen sin trabar la interfaz (60 FPS aprox).
 * 
 * TODO: la animacion de muerte quedo un poco rara pero funciona
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class PanelSujeto extends JPanel {
    private static final long serialVersionUID = 1L;

    // colores del panel (se pueden cambiar con las skins)
    public static Color FONDO      = new Color(0x0A, 0x0E, 0x14);
    public static Color CIAN       = new Color(140, 255, 255);
    public static Color GLOW_CIAN  = new Color(0, 255, 255, 50);
    public static Color ROJO       = new Color(0xFF, 0x55, 0x55);
    public static Color GLOW_ROJO  = new Color(0xFF, 0x22, 0x22, 80);
    public static Color VERDE      = new Color(0x00, 0xFF, 0x88);

    private void aplicarSkin() {
        SkinAhorcado skin = GestorSkins.getInstancia().getSkinActivo();
        CIAN      = skin.colorHorca;
        GLOW_CIAN = skin.glowDe(skin.colorGlow, 50);
        ROJO      = skin.colorAlerta;
        GLOW_ROJO = skin.glowDe(skin.colorAlerta, 80);
        FONDO     = skin.colorFondo;
        setBackground(FONDO);
        cachedW = -1; // forzar recrear buffers
        repaint();
    }

    private LogicaJuego logica;
    private int fallosActuales = 0;
    private int ultimoFallo = 0;
    private long tiempoUltimoFallo = 0;
    
    private boolean palancaJalada = false;
    private long tiempoDerrota = 0;
    private Rectangle2D.Float areaPalanca = new Rectangle2D.Float(280f, 380f, 40f, 60f);
    
    private Timer animTimer;
    private GestorSkins.SkinListener skinListener;
    private long startTime;
    
    private long lastTime = System.currentTimeMillis();
    private float elapsedTotal = 0;
    private java.util.List<Particula> particulas = new java.util.ArrayList<>();
    private java.util.Random rnd = new java.util.Random();

    // --- Sistema de Partículas Interno ---
    private class Particula {
        float x, y, vx, vy;
        float tiempoVida;
        float maxVida;
        float size;
        Color color;

        public Particula(float x, float y, float vx, float vy, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = color;
            this.tiempoVida = 0.5f + rnd.nextFloat() * 1.5f;
            this.maxVida = this.tiempoVida;
            this.size = 2f + rnd.nextFloat() * 4f;
        }

        public void actualizar(float dt) {
            x += vx * dt;
            y += vy * dt;
            vy += 200f * dt; // Gravedad
            tiempoVida -= dt;
        }
    }
    // Buffers gráficos cacheados (evita allocations en cada frame)
    private BufferedImage cachedOffscreen;
    private BufferedImage cachedBloom;
    private int cachedW = -1, cachedH = -1;
    private Runnable onDerrotaCallback;

    private static final int REF_W = 350;
    private static final int REF_H = 450;

    public PanelSujeto() {
        setOpaque(false);
        aplicarSkin();
        setPreferredSize(new java.awt.Dimension(REF_W, REF_H));
        startTime = System.currentTimeMillis();
        
        skinListener = skin -> aplicarSkin();
        GestorSkins.getInstancia().addListener(skinListener);
        
        animTimer = new Timer(20, e -> repaint());
        animTimer.start();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int f = (logica != null) ? logica.obtenerIntentosFallidos() : fallosActuales;
                if (f >= 7 && !palancaJalada) {
                    float scale = Math.min((float)getWidth() / REF_W, (float)getHeight() / REF_H);
                    float offsetX = (getWidth() - REF_W * scale) / 2f;
                    float offsetY = (getHeight() - REF_H * scale) / 2f;
                    float mx = (e.getX() - offsetX) / scale;
                    float my = (e.getY() - offsetY) / scale;
                    
                    if (areaPalanca.contains(mx, my)) {
                        palancaJalada = true;
                        tiempoDerrota = System.currentTimeMillis();
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        repaint();
                        if (onDerrotaCallback != null) {
                            onDerrotaCallback.run();
                        }
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int f = (logica != null) ? logica.obtenerIntentosFallidos() : fallosActuales;
                if (f >= 7 && !palancaJalada) {
                    float scale = Math.min((float)getWidth() / REF_W, (float)getHeight() / REF_H);
                    float offsetX = (getWidth() - REF_W * scale) / 2f;
                    float offsetY = (getHeight() - REF_H * scale) / 2f;
                    float mx = (e.getX() - offsetX) / scale;
                    float my = (e.getY() - offsetY) / scale;
                    
                    if (areaPalanca.contains(mx, my)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    public void detenerTimer() {
        if (animTimer != null) { animTimer.stop(); animTimer = null; }
        if (skinListener != null) {
            GestorSkins.getInstancia().removeListener(skinListener);
            skinListener = null;
        }
    }

    public void setOnDerrotaCallback(Runnable onDerrotaCallback) {
        this.onDerrotaCallback = onDerrotaCallback;
    }

    public boolean isPalancaJalada() {
        return palancaJalada;
    }

    public void establecerLogica(LogicaJuego l) {
        logica = l;
        fallosActuales = 0;
        ultimoFallo = 0;
        palancaJalada = false;
        tiempoDerrota = 0;
        repaint();
    }

    public void actualizarFallos(int f) {
        if (f > fallosActuales && f <= 7) {
            tiempoUltimoFallo = System.currentTimeMillis();
        }
        fallosActuales = f;
        ultimoFallo = f;
        if (f < 7) {
            palancaJalada = false;
            tiempoDerrota = 0;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Recrear buffers SOLO si las dimensiones cambiaron
        if (w != cachedW || h != cachedH) {
            cachedOffscreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            cachedBloom = null; // Forzar recálculo de bloom
            cachedW = w;
            cachedH = h;
        }

        long now = System.currentTimeMillis();
        float dt = (now - lastTime) / 1000f;
        lastTime = now;
        elapsedTotal += dt;
        
        // Generar particulas ambientales (fragmentos de datos flotantes)
        if (rnd.nextFloat() < 0.12f) {
            float px = rnd.nextFloat() * 350f;
            float py = 400f + rnd.nextFloat() * 50f;
            Color pColor = new Color(0, 255, 255, 80 + rnd.nextInt(80));
            Particula amb = new Particula(px, py, (rnd.nextFloat() - 0.5f) * 20f, -30f - rnd.nextFloat() * 40f, pColor);
            amb.size = 1f + rnd.nextFloat() * 2f;
            amb.tiempoVida = 2f + rnd.nextFloat() * 3f;
            amb.maxVida = amb.tiempoVida;
            particulas.add(amb);
        }
        // Chispas de la horca cuando hay fallos
        if ((fallosActuales >= 4 && rnd.nextFloat() < 0.2f)) {
            particulas.add(new Particula(
                80f + rnd.nextFloat() * 30f,
                60f + rnd.nextFloat() * 300f,
                (rnd.nextFloat() - 0.5f) * 40f,
                -rnd.nextFloat() * 60f,
                new Color(0, 255, 200, 120)
            ));
        }
        // Particulas de chispa en la plataforma
        if ((fallosActuales >= 6 && rnd.nextFloat() < 0.3f) || palancaJalada) {
            for (int i = 0; i < (palancaJalada ? 5 : 1); i++) {
                particulas.add(new Particula(
                    300f + rnd.nextFloat() * 100f - 50f,
                    350f + rnd.nextFloat() * 100f,
                    (rnd.nextFloat() - 0.5f) * 120f,
                    -rnd.nextFloat() * 180f,
                    palancaJalada ? ROJO : CIAN
                ));
            }
        }
        
        // Actualizar particulas
        for (int i = particulas.size() - 1; i >= 0; i--) {
            Particula p = particulas.get(i);
            p.actualizar(dt);
            if (p.tiempoVida <= 0) particulas.remove(i);
        }
        
        // 1. Limpiar y redibujar offscreen
        Graphics2D gOff = cachedOffscreen.createGraphics();
        gOff.setComposite(java.awt.AlphaComposite.Clear);
        gOff.fillRect(0, 0, w, h);
        gOff.setComposite(java.awt.AlphaComposite.SrcOver);

        gOff.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gOff.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gOff.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        float scale = Math.min((float)w / REF_W, (float)h / REF_H);
        float offsetX = (w - REF_W * scale) / 2f;
        float offsetY = (h - REF_H * scale) / 2f;

        gOff.translate(offsetX, offsetY);
        gOff.scale(scale, scale);

        int f = (logica != null) ? logica.obtenerIntentosFallidos() : fallosActuales;
        f = Math.max(0, Math.min(7, f));

        dibujarEscena(gOff, f);
        gOff.dispose();

        // 2. Aplicar Gaussian Blur (reutiliza buffer de bloom)
        cachedBloom = createBloom(cachedOffscreen);

        // 3. Dibujar composición final
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(cachedBloom, 0, 0, null);
        g2.drawImage(cachedBloom, 0, 0, null);
        g2.drawImage(cachedOffscreen, 0, 0, null);
        g2.dispose();
    }

    private BufferedImage createBloom(BufferedImage src) {
        float weight = 1.0f / 159.0f;
        float[] gaussian = {
            2*weight, 4*weight, 5*weight, 4*weight, 2*weight,
            4*weight, 9*weight, 12*weight, 9*weight, 4*weight,
            5*weight, 12*weight, 15*weight, 12*weight, 5*weight,
            4*weight, 9*weight, 12*weight, 9*weight, 4*weight,
            2*weight, 4*weight, 5*weight, 4*weight, 2*weight
        };
        ConvolveOp op = new ConvolveOp(new Kernel(5, 5, gaussian), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(src, null);
    }

    private void dibujarEscena(Graphics2D g2, int fallos) {
        float elapsedTotal = (System.currentTimeMillis() - startTime) / 1000f;
        
        // === EFECTOS VISUALES AMBIENTALES ===
        // Grid de fondo (campo de datos)
        g2.setColor(new Color(0, 255, 255, 8));
        g2.setStroke(new BasicStroke(0.5f));
        for (int gx = 0; gx < REF_W; gx += 25) {
            g2.drawLine(gx, 0, gx, REF_H);
        }
        for (int gy = 0; gy < REF_H; gy += 25) {
            g2.drawLine(0, gy, REF_W, gy);
        }
        
        // Texto hexadecimal decorativo flotante
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(new Color(0, 255, 255, 15 + (int)(Math.sin(elapsedTotal * 0.5) * 10)));
        for (int i = 0; i < 6; i++) {
            float ty = ((elapsedTotal * 15f + i * 70f) % (float)REF_H);
            g2.drawString(String.format("%04X", (int)(elapsedTotal * 1000 + i * 123) & 0xFFFF), 5 + i * 58, ty);
        }
        
        // Glow del piso (resplandor ambiental)
        float floorGlow = 0.3f + 0.15f * (float)Math.sin(elapsedTotal * 1.2);
        g2.setColor(new Color(0, 255, 255, (int)(floorGlow * 25)));
        g2.fillRect(0, 390, REF_W, 60);
        
        // Control de Glitch / Partículas
        float tiempoGlitch = (System.currentTimeMillis() - tiempoUltimoFallo) / 1000f;
        boolean enGlitch = (tiempoGlitch < 0.5f && fallos > 0 && fallos <= 7 && !palancaJalada);
        
        AffineTransform sceneTransform = g2.getTransform();
        if (enGlitch) {
            float shiftX = (float)((Math.random() - 0.5) * 6.0);
            float shiftY = (float)((Math.random() - 0.5) * 6.0);
            g2.translate(shiftX, shiftY);
        }

        float anguloCaidaCuerpo = 0f;
        float offsetCaidaY = 0f;
        float alphaFade = 1.0f;
        
        if (palancaJalada && tiempoDerrota > 0) {
            float t = (System.currentTimeMillis() - tiempoDerrota) / 1000f;
            
            // Caida ultra suave con easing cúbico (muy lento al inicio)
            float duracionCaida = 3.0f; // 3 segundos para caer completamente
            float progresoCaida = Math.min(t / duracionCaida, 1.0f);
            // Ease-in cubic: inicio muy suave, luego acelera naturalmente
            float easedProgress = progresoCaida * progresoCaida * progresoCaida;
            offsetCaidaY = easedProgress * 25f;
            
            // Oscilación pendular ultra suave (apenas perceptible)
            float amortiguacion = 1.5f;
            float omega = 1.8f;
            float theta0 = 0.12f;
            
            anguloCaidaCuerpo = (float)(theta0 * Math.exp(-amortiguacion * t) * Math.cos(omega * t));
            
            // Fade out sutil después de 4 segundos
            if (t > 4.0f) {
                float fadeT = (t - 4.0f) / 3.0f;
                alphaFade = Math.max(0.4f, 1.0f - fadeT * 0.3f);
            }
            
        }

        float posteX = 80f;
        float baseY = 400f;
        float barraY = 60f;
        float barraX2 = 220f;
        float cuerdaX = barraX2 - 10f;
        float cuerdaTop = barraY + 5f;
        
        // Generar gotas de sangre que caen desde el cuerpo tras la caida
        if (palancaJalada && tiempoDerrota > 0) {
            float t = (System.currentTimeMillis() - tiempoDerrota) / 1000f;
            if (t > 0.5f && t < 8.0f && rnd.nextFloat() < 0.15f) {
                float gx = cuerdaX + (rnd.nextFloat() - 0.5f) * 20f;
                float gy = cuerdaTop + 60f + offsetCaidaY;
                float gvx = (rnd.nextFloat() - 0.5f) * 15f;
                float gvy = rnd.nextFloat() * 30f + 10f;
                int r = 180 + rnd.nextInt(75);
                particulas.add(new Particula(gx, gy, gvx, gvy, new Color(r, 0, 0)));
            }
        }
        
        float cuerdaBot = cuerdaTop + 35f + offsetCaidaY;

        dibujarHorcaPro(g2, posteX, baseY, barraY, barraX2, cuerdaX, cuerdaTop, cuerdaBot, fallos);

        float pivotX = cuerdaX;
        float pivotY = cuerdaBot;

        float oscilacionVivoRot = 0f;
        if (!palancaJalada && fallos > 0) {
            oscilacionVivoRot = (float) Math.sin(elapsedTotal * 1.5) * 0.02f;
        }

        float anguloCuerpoFinal = palancaJalada ? anguloCaidaCuerpo : oscilacionVivoRot;

        AffineTransform oldTransform = g2.getTransform();
        
        // Aplicar alpha fade para efecto de muerte suave
        java.awt.Composite compositeOriginal = g2.getComposite();
        if (palancaJalada && alphaFade < 1.0f) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alphaFade));
        }
        
        g2.translate(pivotX, pivotY);
        g2.rotate(anguloCuerpoFinal);

        float cabezaR = 18f;
        float cabezaCy = cabezaR;

        // Dibujar Particulas
        for (Particula p : particulas) {
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int)(Math.max(0, p.tiempoVida / p.maxVida) * 255)));
            g2.fill(new Rectangle2D.Float(p.x, p.y, p.size, p.size));
        }
        
        // 1. Cabeza
        if (fallos >= 1) {
            dibujarCabezaPro(g2, 0, cabezaCy, cabezaR, palancaJalada);
        }

        float torsoAncho = 30f;
        float torsoAlto = 48f;
        float torsoTop = cabezaCy + cabezaR + 2f; 

        // 2. Torso
        if (fallos >= 2) {
            dibujarTorsoPro(g2, 0, torsoTop, torsoAncho, torsoAlto, palancaJalada);
        }

        float angBrazoI = 0f;
        float angBrazoD = 0f;
        float angPiernaI = 0f;
        float angPiernaD = 0f;

        if (palancaJalada) {
            // Hanging naturally straight down
            angBrazoI = -0.05f;
            angBrazoD = 0.05f;
            angPiernaI = -0.02f;
            angPiernaD = 0.02f;
        } else {
            // Standing gracefully without crossing limbs
            angBrazoD = 0.25f + (float) Math.sin(elapsedTotal * 1.5) * 0.03f; // Outward right
            angBrazoI = -0.25f + (float) Math.cos(elapsedTotal * 1.6) * 0.03f; // Outward left
            angPiernaD = 0.12f + (float) Math.sin(elapsedTotal * 1.2) * 0.02f; // Slight right
            angPiernaI = -0.12f + (float) Math.cos(elapsedTotal * 1.1) * 0.02f; // Slight left
        }

        float hombroX_I = -torsoAncho / 2f;
        float hombroX_D = torsoAncho / 2f;
        float hombroY = torsoTop;

        float caderaX_I = -torsoAncho / 2f + 4f;
        float caderaX_D = torsoAncho / 2f - 4f;
        float caderaY = torsoTop + torsoAlto;

        // 3. Brazo Derecho
        if (fallos >= 3) {
            dibujarBrazoPro(g2, hombroX_D, hombroY, 22f, 20f, angBrazoD, angBrazoD + (palancaJalada ? 0.02f : 0.1f));
        }
        
        // 4. Brazo Izquierdo
        if (fallos >= 4) {
            dibujarBrazoPro(g2, hombroX_I, hombroY, 22f, 20f, angBrazoI, angBrazoI - (palancaJalada ? 0.02f : 0.1f));
        }

        // 5. Pierna Derecha
        if (fallos >= 5) {
            dibujarPiernaPro(g2, caderaX_D, caderaY, 25f, 25f, angPiernaD, angPiernaD + (palancaJalada ? 0.01f : 0.05f));
        }

        // 6. Pierna Izquierda
        if (fallos >= 6) {
            dibujarPiernaPro(g2, caderaX_I, caderaY, 25f, 25f, angPiernaI, angPiernaI - (palancaJalada ? 0.01f : 0.05f));
        }
        
        // Restaurar composite original después de dibujar el cuerpo
        g2.setComposite(compositeOriginal);
        g2.setTransform(oldTransform);

        // Chispas/Partículas de Glitch cuando aparece un nuevo miembro
        if (enGlitch) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < 15; i++) {
                float rx = pivotX + (float) ((Math.random() - 0.5) * 80);
                float ry = pivotY + 30f + (float) ((Math.random() - 0.5) * 80);
                float dx = (float) ((Math.random() - 0.5) * 15);
                float dy = (float) ((Math.random() - 0.5) * 15);
                g2.draw(new Line2D.Float(rx, ry, rx + dx, ry + dy));
            }
        }
        
        // Restaurar matriz por si el glitch desplazó el mundo completo
        g2.setTransform(sceneTransform);

        // --- TARIMA / ATRIO SUPER DETALLADO ---
        float platY = 236f; 
        float pisoY = baseY; 
        float platW = 70f;
        float platH = 12f;
        float platX = cuerdaX - platW / 2f;
        
        g2.setColor(CIAN);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Estructura principal de la tarima bajando hasta el piso
        float pilarW = 34f;
        float pilarX = cuerdaX - pilarW / 2f;
        dibujarRectHollow(g2, pilarX, platY + platH, pilarW, pisoY - (platY + platH));
        
        // Detalles internos del pilar (Líneas tecnológicas)
        dibujarLinea(g2, pilarX + 6f, platY + platH, pilarX + 6f, pisoY);
        dibujarLinea(g2, pilarX + pilarW - 6f, platY + platH, pilarX + pilarW - 6f, pisoY);
        
        // Efecto de panel central iluminado (indicador de estado)
        if (!palancaJalada && fallos < 7) {
            g2.setColor(new Color(0, 255, 150, 60)); // Verde si está seguro
        } else if (!palancaJalada && fallos >= 7) {
            // Titila en rojo cuando es inminente
            float alerta = (float)(Math.sin(elapsedTotal * 10) * 0.5 + 0.5);
            g2.setColor(new Color(255, 0, 0, (int)(alerta * 150)));
        } else {
            g2.setColor(new Color(50, 0, 0, 100)); // Apagado
        }
        g2.fill(new Rectangle2D.Float(pilarX + 12f, platY + platH + 10f, pilarW - 24f, pisoY - (platY + platH) - 30f));
        
        g2.setColor(CIAN);
        
        // Escalones en la base del atrio (Más anchos y detallados)
        dibujarRectHollow(g2, pilarX - 20f, pisoY - 8f, pilarW + 40f, 8f);
        dibujarRectHollow(g2, pilarX - 10f, pisoY - 16f, pilarW + 20f, 8f);
        
        // Dibujo de las compuertas
        if (!palancaJalada) {
            // Plataforma cerrada soportando al androide
            dibujarRectHollow(g2, platX, platY, platW, platH);
            dibujarLinea(g2, platX + platW/2f, platY, platX + platW/2f, platY + platH); // División central
            
            // Detalles tech en las compuertas (Alert Stripes u otros)
            g2.fill(new Rectangle2D.Float(platX + 5f, platY + 3f, 10f, 3f));
            g2.fill(new Rectangle2D.Float(platX + platW - 15f, platY + 3f, 10f, 3f));
            
            // Leds de estado en la plataforma
            g2.setColor(fallos >= 7 ? ROJO : VERDE);
            g2.fill(new Ellipse2D.Float(platX + platW/2f - 12f, platY + 4f, 4f, 4f));
            g2.fill(new Ellipse2D.Float(platX + platW/2f + 8f, platY + 4f, 4f, 4f));
            g2.setColor(CIAN);
            
        } else {
            // Plataforma abierta (compuertas caídas con ángulo dinámico hacia abajo)
            float compuertaL = platW / 2f;
            
            // Izquierda colgando
            dibujarLinea(g2, platX, platY, platX, platY + compuertaL);
            dibujarLinea(g2, platX - platH, platY, platX - platH, platY + compuertaL);
            dibujarLinea(g2, platX, platY + compuertaL, platX - platH, platY + compuertaL);
            
            // Derecha colgando
            dibujarLinea(g2, platX + platW, platY, platX + platW, platY + compuertaL);
            dibujarLinea(g2, platX + platW + platH, platY, platX + platW + platH, platY + compuertaL);
            dibujarLinea(g2, platX + platW, platY + compuertaL, platX + platW + platH, platY + compuertaL);
        }

        // Si fallos == 7, dibujar la palanca elegante con warning visual
        if (fallos >= 7) {
            // Warning pulsante alrededor de la palanca
            float warn = (float)(Math.sin(elapsedTotal * 5.0) * 0.5 + 0.5);
            g2.setColor(new Color(255, 0, 0, (int)(warn * 40)));
            g2.fillRoundRect((int)areaPalanca.x - 8, (int)areaPalanca.y - 8, 
                (int)areaPalanca.width + 16, (int)areaPalanca.height + 16, 10, 10);
            // Texto WARNING parpadeante
            if (warn > 0.5f && !palancaJalada) {
                g2.setFont(new Font("Monospaced", Font.BOLD, 8));
                g2.setColor(new Color(255, 50, 50, (int)(warn * 255)));
                g2.drawString("PULL", (int)areaPalanca.x + 6, (int)areaPalanca.y - 10);
            }
            dibujarPalancaElegante(g2);
        }
        
        // Indicadores de estado del sistema en la esquina
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(new Color(0, 255, 255, 40));
        g2.drawString("SYS:" + (fallos < 7 ? "ACTIVE" : "CRITICAL"), 5, REF_H - 15);
        g2.drawString("INTEGRITY:" + ((7 - fallos) * 100 / 7) + "%", 5, REF_H - 5);
    }

    private void dibujarHorcaPro(Graphics2D g2, float px, float baseY, float barraY, float barraX2, float cuerdaX, float cuerdaTop, float cuerdaBot, int fallos) {
        g2.setColor(CIAN);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        float w1 = 70f, w2 = 60f, w3 = 50f;
        float b1y = baseY, b2y = baseY - 8f, b3y = baseY - 16f;
        
        dibujarRectHollow(g2, px - w1, b1y, w1 * 2, 6f);
        dibujarRectHollow(g2, px - w2, b2y, w2 * 2, 6f);
        dibujarRectHollow(g2, px - w3, b3y, w3 * 2, 6f);

        g2.fill(new Rectangle2D.Float(px - w1 + 5f, b1y + 2f, 8f, 2f));
        g2.fill(new Rectangle2D.Float(px - w1 + 15f, b1y + 2f, 4f, 2f));

        float pW_bot = 12f;
        float pW_top = 8f;
        
        g2.setColor(CIAN);
        dibujarLinea(g2, px - pW_bot, b3y, px - pW_bot, b3y - 30f);
        dibujarLinea(g2, px + pW_bot, b3y, px + pW_bot, b3y - 30f);
        
        dibujarLinea(g2, px - pW_bot, b3y - 30f, px - pW_top, b3y - 45f);
        dibujarLinea(g2, px + pW_bot, b3y - 30f, px + pW_top, b3y - 45f);

        dibujarLinea(g2, px - pW_top, b3y - 45f, px - pW_top, barraY);
        dibujarLinea(g2, px + pW_top, b3y - 45f, px + pW_top, barraY);
        
        dibujarLineaDashed(g2, px, b3y - 15f, px, barraY + 10f, 4f, 4f);

        dibujarLinea(g2, px - 6f, b3y - 5f, px - 6f, b3y - 20f);
        dibujarLinea(g2, px + 6f, b3y - 5f, px + 6f, b3y - 20f);
        for(int i=0; i<3; i++) {
            dibujarLinea(g2, px - 6f, b3y - 10f - i*4f, px - 2f, b3y - 10f - i*4f);
        }

        float bH = 10f;
        dibujarLinea(g2, px - pW_top, barraY, barraX2, barraY);
        dibujarLinea(g2, px - pW_top, barraY + bH, barraX2, barraY + bH);
        dibujarLinea(g2, barraX2, barraY, barraX2, barraY + bH);
        
        dibujarLinea(g2, px + 15f, barraY + 3f, barraX2 - 40f, barraY + 3f);
        dibujarLinea(g2, px + 15f, barraY + 7f, barraX2 - 40f, barraY + 7f);
        
        dibujarLinea(g2, px + pW_top, barraY + 40f, px + 40f, barraY + bH);
        dibujarLinea(g2, px + pW_top, barraY + 32f, px + 32f, barraY + bH);

        g2.draw(new Arc2D.Float(px + 30f, barraY + bH, 90f, 20f, 180, 180, Arc2D.OPEN));

        // Cuerda Final: Aparece solo con 7 fallos (sentencia definitiva)
        if (fallos >= 7) {
            dibujarLineaDashed(g2, cuerdaX, cuerdaTop, cuerdaX, cuerdaBot, 3f, 2f);
        }
    }

    private void dibujarCabezaPro(Graphics2D g2, float cx, float cy, float r, boolean derrotado) {
        g2.setColor(CIAN);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        g2.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2f, r * 2f));
        
        if (derrotado) {
            float offX = r * 0.35f;
            float offY = r * 0.15f;
            float s = 3.5f;
            
            dibujarLinea(g2, cx - offX - s, cy - offY - s, cx - offX + s, cy - offY + s);
            dibujarLinea(g2, cx - offX - s, cy - offY + s, cx - offX + s, cy - offY - s);
            dibujarLinea(g2, cx + offX - s, cy - offY - s, cx + offX + s, cy - offY + s);
            dibujarLinea(g2, cx + offX - s, cy - offY + s, cx + offX + s, cy - offY - s);
            
            g2.draw(new Arc2D.Float(cx - r*0.4f, cy + r*0.3f, r*0.8f, r*0.3f, 20, 140, Arc2D.OPEN));
        } else {
            float offX = r * 0.35f;
            float offY = r * 0.15f;
            float s = 2.5f;
            dibujarLinea(g2, cx - offX - s, cy - offY, cx - offX + s, cy - offY);
            dibujarLinea(g2, cx + offX - s, cy - offY, cx + offX + s, cy - offY);
            dibujarLinea(g2, cx - r*0.2f, cy + r*0.4f, cx + r*0.2f, cy + r*0.4f);
        }
    }

    private void dibujarTorsoPro(Graphics2D g2, float cx, float ty, float w, float h, boolean derrotado) {
        g2.setColor(CIAN);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        float x = cx - w/2f;
        float y = ty;
        
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 6f, 6f));
        
        float m = 3f;
        g2.draw(new RoundRectangle2D.Float(x + m, y + m, w - m*2, h - m*2, 4f, 4f));
        
        float ix = x + m + 2f;
        float iy = y + m + 2f;
        
        dibujarRectHollow(g2, ix, iy, 10f, 12f);
        
        dibujarLinea(g2, x + w - m - 4f, iy, x + w - m - 4f, y + h - m - 4f);
        dibujarLinea(g2, x + w - m - 4f, iy + 10f, x + w - m - 8f, iy + 10f);
        dibujarLinea(g2, x + w - m - 8f, iy + 10f, x + w - m - 8f, iy + 15f);
        dibujarLinea(g2, x + w - m - 8f, iy + 15f, x + w - m - 4f, iy + 15f);

        g2.draw(new Ellipse2D.Float(cx - 2f, y + h*0.6f, 4f, 4f));
        
        dibujarLinea(g2, ix, y + h - m - 10f, ix, y + h - m - 4f);
        dibujarLinea(g2, ix, y + h - m - 4f, cx - 4f, y + h - m - 4f);
    }

    private void dibujarBrazoPro(Graphics2D g2, float x, float y, float len1, float len2, float ang1, float ang2) {
        g2.setColor(CIAN);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        float codoX = x + (float) Math.sin(ang1) * len1;
        float codoY = y + (float) Math.cos(ang1) * len1;
        float manoX = codoX + (float) Math.sin(ang2) * len2;
        float manoY = codoY + (float) Math.cos(ang2) * len2;

        dibujarCilindroHueco(g2, x, y, codoX, codoY, 2.5f);
        dibujarCilindroHueco(g2, codoX, codoY, manoX, manoY, 2.0f);
        
        dibujarArticulacion(g2, codoX, codoY, 3.0f);
        dibujarArticulacion(g2, x, y, 3.5f);
        
        dibujarRectHollow(g2, manoX - 2.5f, manoY, 5f, 4f);
    }

    private void dibujarPiernaPro(Graphics2D g2, float x, float y, float len1, float len2, float ang1, float ang2) {
        g2.setColor(CIAN);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        float rodillaX = x + (float) Math.sin(ang1) * len1;
        float rodillaY = y + (float) Math.cos(ang1) * len1;
        float pieX = rodillaX + (float) Math.sin(ang2) * len2;
        float pieY = rodillaY + (float) Math.cos(ang2) * len2;

        dibujarCilindroHueco(g2, x, y, rodillaX, rodillaY, 2.5f);
        dibujarCilindroHueco(g2, rodillaX, rodillaY, pieX, pieY, 2.5f);
        
        dibujarArticulacion(g2, rodillaX, rodillaY, 3.0f);
        dibujarArticulacion(g2, x, y, 3.5f);
        
        dibujarRectHollow(g2, pieX - 4f, pieY, 8f, 4f);
    }

    private void dibujarArticulacion(Graphics2D g2, float cx, float cy, float r) {
        g2.setColor(FONDO);
        g2.fill(new Ellipse2D.Float(cx - r, cy - r, r*2, r*2));
        g2.setColor(CIAN);
        g2.draw(new Ellipse2D.Float(cx - r, cy - r, r*2, r*2));
        g2.draw(new Ellipse2D.Float(cx - 0.5f, cy - 0.5f, 1f, 1f));
    }

    private void dibujarCilindroHueco(Graphics2D g2, float x1, float y1, float x2, float y2, float ancho) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.hypot(dx, dy);
        float nx = -dy / length * ancho;
        float ny = dx / length * ancho;
        
        java.awt.geom.Path2D.Float path = new java.awt.geom.Path2D.Float();
        path.moveTo(x1 + nx, y1 + ny);
        path.lineTo(x2 + nx, y2 + ny);
        path.lineTo(x2 - nx, y2 - ny);
        path.lineTo(x1 - nx, y1 - ny);
        path.closePath();
        g2.draw(path);
    }

    private void dibujarRectHollow(Graphics2D g2, float x, float y, float w, float h) {
        g2.draw(new Rectangle2D.Float(x, y, w, h));
    }

    private void dibujarLinea(Graphics2D g2, float x1, float y1, float x2, float y2) {
        g2.draw(new Line2D.Float(x1, y1, x2, y2));
    }

    private void dibujarLineaDashed(Graphics2D g2, float x1, float y1, float x2, float y2, float dashLen, float spaceLen) {
        Stroke old = g2.getStroke();
        float[] dash = {dashLen, spaceLen};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 0f));
        g2.draw(new Line2D.Float(x1, y1, x2, y2));
        g2.setStroke(old);
    }

    private void dibujarPalancaElegante(Graphics2D g2) {
        float x = areaPalanca.x;
        float y = areaPalanca.y;
        float w = areaPalanca.width;
        float h = areaPalanca.height;
        
        g2.setColor(new Color(0x05, 0x1A, 0x1E, 150));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 5f, 5f));
        
        g2.setColor(new Color(0, 255, 255, 100));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 5f, 5f));
        
        g2.draw(new Line2D.Float(x + w/2f - 2f, y + 10f, x + w/2f - 2f, y + h - 10f));
        g2.draw(new Line2D.Float(x + w/2f + 2f, y + 10f, x + w/2f + 2f, y + h - 10f));
        
        float palancaY1 = y + h/2f;
        float palancaY2 = palancaJalada ? (y + h - 12f) : (y + 12f);
        
        g2.setColor(new Color(0xEE, 0xEE, 0xEE));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Float(x + w/2f, palancaY1, x + w/2f, palancaY2));
        
        g2.setColor(palancaJalada ? ROJO : CIAN);
        g2.fill(new Ellipse2D.Float(x + w/2f - 4.5f, palancaY2 - 4.5f, 9f, 9f));
        
        g2.setColor(palancaJalada ? GLOW_ROJO : GLOW_CIAN);
        g2.fill(new Ellipse2D.Float(x + w/2f - 8f, palancaY2 - 8f, 16f, 16f));
    }
}
