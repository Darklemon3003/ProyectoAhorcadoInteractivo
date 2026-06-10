package com.ahorcado;

/**
 * PistaTexto - Implementa IPista pa dar la descripcion como pista.
 * 
 * Otra implementacion de la interfaz IPista (POLIMORFIZMO).
 * Esta pista le muestra al jugador la descripcion/pista escrita que
 * tiene cada palabra, que es la ayuda mas directa.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class PistaTexto implements IPista {
    
    /**
     * Retorna la descripcion de la palabra como pista.
     * Es la pista mas util porque dice de que se trata.
     * 
     * @param palabraTarget la palabra de la que se saca la descripcion
     * @return String con la pista de texto
     */
    @Override
    public String ejecutarPista(Palabra palabraTarget) {
        // validar que no sea null
        if (palabraTarget == null) {
            return "Error: Palabra nula";
        }
        return "PISTA: DESCRIPCION | " + palabraTarget.getPistaEscrita();
    }
}
