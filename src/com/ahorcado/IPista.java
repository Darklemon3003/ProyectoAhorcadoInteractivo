package com.ahorcado;

/**
 * Interfaz IPista - Define el contrato pa las pistas del juego.
 * 
 * AQUI SE APLICA POLIMORFIZMO: esta interfaz define un metodo ejecutarPista()
 * que cada clase concreta implementa de forma diferente:
 * - PistaTexto: muestra la descripcion de la palabra
 * - PistaCategoria: muestra la categoria
 * - PistaLetraAleatoria: revela una letra que falta
 * 
 * Esto es el patron Strategy que vimos en clase, cada pista tiene su
 * propia logica pero todas cumplen el mismo contrato (interfaz).
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Proyecto final POO
 */
public interface IPista {
    
    /**
     * Ejecuta la pista y retorna un String con la ayuda pa el jugador.
     * Cada clase que implemente esta interfaz decide que tipo de pista dar.
     * 
     * @param palabraTarget la palabra sobre la que se genera la pista
     * @return String con la pista generada
     */
    String ejecutarPista(Palabra palabraTarget);
}
