package com.ahorcado;

import java.util.ArrayList;
import java.util.Random;

/**
 * PistaLetraAleatoria - Implementa IPista pa revelar una letra al azar.
 * 
 * Tercera implementacion de la interfaz IPista (POLIMORFIZMO).
 * Busca en un ArrayList las letras que todavia no se han adivinado
 * y revela una al azar. Es la pista mas "tramposa" pero a veces
 * es necesaria cuando el jugador esta muy atorado.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class PistaLetraAleatoria implements IPista {
    // referencia a la logica pa saber que letras ya se adivinaron
    private LogicaJuego logicaJuego;

    // generador de numeros aleatorios
    private final Random aleatorio = new Random();

    /** Constructor sin contexto (basico) */
    public PistaLetraAleatoria() {
    }

    /**
     * Constructor que recibe la logica del juego pa evitar dar
     * una letra que ya se adivino.
     * 
     * @param logicaJuego la logica de la partida actual
     */
    public PistaLetraAleatoria(LogicaJuego logicaJuego) {
        this.logicaJuego = logicaJuego;
    }

    /**
     * Busca las letras que faltan por adivinar en un ArrayList y
     * devuelve una al azar. Si ya se adivinaron todas dice que no hay.
     * 
     * @param palabraTarget la palabra de la que se saca la letra
     * @return String con la letra revelada
     */
    @Override
    public String ejecutarPista(Palabra palabraTarget) {
        if (palabraTarget == null) {
            return "No fue posible generar la pista de letra.";
        }

        String textoWord = palabraTarget.getTexto().toLowerCase();
        ArrayList<Character> letrasNoAdivinadas = new ArrayList<>();
        ArrayList<Character> letrasAdivinadas = new ArrayList<>();

        if (logicaJuego != null) {
            letrasAdivinadas.addAll(logicaJuego.obtenerLetrasAdivinadas());
        }

        for (char letra : textoWord.toCharArray()) {
            if (letra == ' ') {
                continue;
            }
            if (!letrasAdivinadas.contains(letra) && !letrasNoAdivinadas.contains(letra)) {
                letrasNoAdivinadas.add(letra);
            }
        }

        if (letrasNoAdivinadas.isEmpty()) {
            return "PISTA: LETRA | Sin simbolos pendientes en esta mision.";
        }

        int indiceAleatorio = aleatorio.nextInt(letrasNoAdivinadas.size());
        char letraPista = letrasNoAdivinadas.get(indiceAleatorio);

        return "PISTA: LETRA | Segmento autorizado '" + Character.toUpperCase(letraPista) + "'";
    }
}
