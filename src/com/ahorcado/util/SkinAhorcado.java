package com.ahorcado.util;

import java.awt.Color;

/**
 * SkinAhorcado - Modelo de un tema/skin visual pa el ahorcado.
 * 
 * Cada skin define los colores de la horca, cuerpo, glow, alerta,
 * fondo y particulas. Se compran con KK en la tienda.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class SkinAhorcado {

    public final String id;
    public final String nombre;
    public final String descripcion;
    public final int costoKK;
    public final Color colorHorca;
    public final Color colorCuerpo;
    public final Color colorGlow;
    public final Color colorAlerta;
    public final Color colorFondo;
    public final Color colorParticula;
    public boolean desbloqueado;

    public SkinAhorcado(String id, String nombre, String descripcion, int costoKK,
                        Color colorHorca, Color colorCuerpo, Color colorGlow,
                        Color colorAlerta, Color colorFondo, Color colorParticula,
                        boolean desbloqueado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoKK = costoKK;
        this.colorHorca = colorHorca;
        this.colorCuerpo = colorCuerpo;
        this.colorGlow = colorGlow;
        this.colorAlerta = colorAlerta;
        this.colorFondo = colorFondo;
        this.colorParticula = colorParticula;
        this.desbloqueado = desbloqueado;
    }

    public Color glowDe(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }
}
