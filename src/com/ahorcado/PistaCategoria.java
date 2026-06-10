package com.ahorcado;

/**
 * PistaCategoria - Implementa IPista pa dar la categoria como pista.
 * 
 * Esta clase es una de las 3 implementaciones de la interfaz IPista
 * (POLIMORFIZMO). Cuando se ejecuta, le dice al jugador a que categoria
 * pertenece la palabra (ej: "Animales", "Paises", etc).
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class PistaCategoria implements IPista {
    
    /**
     * Retorna la categoria de la palabra como pista.
     * Usa EtiquetasPistaDominio pa formatear bonito el nombre.
     * 
     * @param palabraTarget la palabra de la cual se saca la categoria
     * @return String con la pista de categoria
     */
    @Override
    public String ejecutarPista(Palabra palabraTarget) {
        // validar que no sea null pa evitar esepcion
        if (palabraTarget == null) {
            return "Error: Palabra nula";
        }
        String cod = EtiquetasPistaDominio.codigoDominio(palabraTarget.getCategoria());
        return "PISTA: " + cod + " | " + palabraTarget.getCategoria();
    }
}
