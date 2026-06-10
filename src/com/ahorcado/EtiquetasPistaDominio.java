package com.ahorcado;

import java.text.Normalizer;
import java.util.Locale;

/**
 * EtiquetasPistaDominio - Convierte nombres de categoria a etiquetas cortas.
 * 
 * Clase utilitaria que transforma el nombre de la categoria (ej: "Animales")
 * en una etiqueta tipo terminal (ej: "ANIMAL"). Se usa pa mostrar las pistas
 * en formato bonito en la consola del juego.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public final class EtiquetasPistaDominio {

    private EtiquetasPistaDominio() {
    }

    /**
     * @param categoria texto de {@link Palabra#getCategoria()}
     * @return etiqueta en mayúsculas sin tildes para consola (ej. PAIS, ANIMAL)
     */
    public static String codigoDominio(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "GENERAL";
        }
        String c = categoria.trim();
        switch (c) {
            case "Animales":
                return "ANIMAL";
            case "Países":
            case "Paises":
                return "PAIS";
            case "Deportes":
                return "DEPORTE";
            case "Objetos":
                return "OBJETO";
            case "Comidas":
                return "COMIDA";
            default:
                return sinTildesMayus(c);
        }
    }

    private static String sinTildesMayus(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }
}
