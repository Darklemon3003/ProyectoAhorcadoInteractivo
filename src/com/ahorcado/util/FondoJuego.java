package com.ahorcado.util;

import java.awt.Color;

/**
 * FondoJuego - Modelo de un fondo/wallpaper desbloqueable.
 * 
 * Cada fondo tiene id, nombre, descripcion, costo en KK, estilo de
 * renderizado (pa que FondoFactory sepa como dibujarlo), colores
 * tematicos y una rareza (COMUN, RARO, EPICO, LEGENDARIO).
 * 
 * Es similar a la clase Bug en estructura (tiene los mismos tipos
 * de atributos y enum de Rareza).
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class FondoJuego {

    public enum Rareza {
        COMUN("COMUN", new Color(0x00FF88)),
        RARO("RARO", new Color(0x00FFFF)),
        EPICO("EPICO", new Color(0xFF55AA)),
        LEGENDARIO("LEGENDARIO", new Color(0xFFD700));

        public final String etiqueta;
        public final Color color;
        Rareza(String etiqueta, Color color) {
            this.etiqueta = etiqueta;
            this.color = color;
        }
    }

    public final String id;
    public final String nombre;
    public final String descripcion;
    public final int costoKK;
    public final String estilo;         // clave para FondoFactory
    public final Color colorPrimario;
    public final Color colorSecundario;
    public final Color colorAccento;
    public final Rareza rareza;
    public boolean desbloqueado;

    public FondoJuego(String id, String nombre, String descripcion, int costoKK,
                      String estilo, Color colorPrimario, Color colorSecundario,
                      Color colorAccento, Rareza rareza, boolean desbloqueado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoKK = costoKK;
        this.estilo = estilo;
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.colorAccento = colorAccento;
        this.rareza = rareza;
        this.desbloqueado = desbloqueado;
    }
}
