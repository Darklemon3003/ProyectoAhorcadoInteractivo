package com.ahorcado;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * LogicaJuego - Controla toda la logica del juego del Ahorcado.
 * 
 * Esta clase es como el "cerebro" del juego. Maneja la palabra actual,
 * las letras que el jugador va adivinando (guardadas en ArrayLists de Character),
 * los intentos fallidos, y el sistema de pistas que usa POLIMORFIZMO
 * (patron Strategy con la interfaz IPista).
 * 
 * Los ArrayList<Character> son pa guardar las letras que ya se intentaron,
 * tanto las correctas como las fallidas.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Proyecto final POO
 */
public class LogicaJuego {
    
    // === Atributos privados (encapsulamiento) ===
    
    // la palabra que el jugador tiene que adivinar
    private Palabra palabraActual;
    
    // ArrayList pa guardar las letras que el jugador ya adivino bien
    private ArrayList<Character> letrasAdivinadas;

    // ArrayList pa guardar las letras que el jugador fallo
    private ArrayList<Character> letrasFallidas;

    // contador de intentos fallidos
    private int intentosFallidos;
    
    // maximo de intentos antes de perder (7 partes del cuerpo)
    private int MAX_INTENTOS = 7;
    
    // cuantas pistas le quedan al jugador
    private int pistasRestantes = 3;
    
    
    // === Constructor ===
    
    /**
     * Constructor que recibe un objeto Palabra y prepara la partida.
     * Inicializa los ArrayLists vacios pa las letras adivinadas y fallidas.
     * Si la palabra tiene espacios (como "estados unidos") los revela automatico.
     * 
     * @param palabra la palabra que hay que adivinar
     */
    public LogicaJuego(Palabra palabra) {
        this.palabraActual = palabra;
        this.letrasAdivinadas = new ArrayList<>();
        this.letrasFallidas = new ArrayList<>();
        this.intentosFallidos = 0;

        // si la palabra tiene espacios se revelan desde el inicio pa que no cuente como fallo
        String textoWord = palabra.getTexto().toLowerCase();
        if (textoWord.contains(" ")) {
            letrasAdivinadas.add(' ');
        }
    }
    
    
    // === Metodos de procesamiento ===
    
    /**
     * Quita los acentos de un caracter pa poder comparar sin problemas.
     * Usa Normalizer de Java (me costo encontrar como hacer esto).
     * 
     * @param c caracter con posible acento
     * @return caracter sin acento en minuscula
     */
    private char normalizarCaracter(char c) {
        String temp = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return Character.toLowerCase(temp.charAt(0));
    }
    
    /**
     * Procesa cuando el jugador ingresa una letra.
     * Compara la letra con la palabra secreta (sin importar tildes ni mayusculas).
     * Si acierta la mete al ArrayList de letrasAdivinadas, si falla la mete
     * al ArrayList de letrasFallidas y suma un intento fallido.
     * 
     * @param letra la letra que intento el jugador
     * @return true si la letra era correcta, false si no
     */
    public boolean procesarIntento(char letra) {
        letra = Character.toLowerCase(letra);
        char letraNormalizada = normalizarCaracter(letra);

        if (letrasAdivinadas.contains(letra) || letrasAdivinadas.contains(letraNormalizada)) {
            return false;
        }

        // quitar tildes temporalmente pa comparar bien
        String palabraSinTildes = Normalizer.normalize(palabraActual.getTexto(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String palabraLimpia = pattern.matcher(palabraSinTildes).replaceAll("").toLowerCase();

        if (palabraLimpia.contains(String.valueOf(letraNormalizada))) {
            letrasAdivinadas.add(letraNormalizada);
            return true;
        }

        if (!letrasFallidas.contains(letraNormalizada)) {
            letrasFallidas.add(letraNormalizada);
        }
        intentosFallidos++;
        return false;
    }
    
    
    // === Metodos de visualizacion ===
    
    /**
     * Muestra el progreso actual del jugador.
     * Las letras adivinadas se muestran y las que faltan se ven como guiones bajos (_).
     * Ejemplo: si la palabra es "gato" y adivino la 'a', se ve: _ A _ _
     * 
     * @return un String con el progreso visual
     */
    public String obtenerProgreso() {
        StringBuilder progreso = new StringBuilder();
        String textoOriginal = palabraActual.getTexto();

        for (char letraOriginal : textoOriginal.toCharArray()) {
            char letraNormalizada = normalizarCaracter(letraOriginal);

            if (letraOriginal == ' ') {
                progreso.append("  ");
            } else if (letrasAdivinadas.contains(letraNormalizada)) {
                progreso.append(Character.toUpperCase(letraOriginal));
            } else {
                progreso.append('_');
            }

            progreso.append(' ');
        }

        return progreso.toString().trim();
    }
    
    
    // === Metodos de estado (verificar si gano o perdio) ===
    
    /**
     * Verifica si el jugador ya gano (adivino todas las letras).
     * Recorre la palabra y checa si cada letra esta en el ArrayList de adivinadas.
     * 
     * @return true si ya gano, false si todavia le faltan letras
     */
    public boolean esGanador() {
        String textoWord = palabraActual.getTexto();

        for (char letra : textoWord.toCharArray()) {
            if (letra != ' ' && !letrasAdivinadas.contains(normalizarCaracter(letra))) {
                return false;
            }
        }

        return true;
    }
    
    /** Verifica si perdio (se le acabaron los intentos) */
    public boolean esPerdedor() {
        return intentosFallidos >= MAX_INTENTOS;
    }
    
    /** @return cuantos intentos le quedan */
    public int getIntentosRestantes() {
        return MAX_INTENTOS - intentosFallidos;
    }
    
    /** @return cuantos intentos fallidos lleva */
    public int obtenerIntentosFallidos() {
        return intentosFallidos;
    }
    
    
    // === Sistema de pistas (POLIMORFIZMO con interfaz IPista) ===
    
    /**
     * Solicita una pista usando el patron Strategy.
     * Aqui es donde se aplica el POLIMORFIZMO: el parametro es de tipo IPista
     * (interfaz) y dependiendo de que clase concreta se pase (PistaTexto,
     * PistaCategoria, PistaLetraAleatoria) se ejecuta un comportamiento distinto.
     * 
     * El profe dijo que esto es polimorfizmo dinamico porque se decide
     * en tiempo de ejecucion que metodo ejecutarPista() se llama.
     * 
     * @param tipoDePista objeto que implementa IPista (puede ser cualquier tipo de pista)
     * @return la pista generada o mensaje de error si no hay pistas
     */
    public String solicitarPista(IPista tipoDePista) {
        if (pistasRestantes <= 0) {
            return "No hay pistas disponibles.";
        }

        if (tipoDePista == null) {
            return "El tipo de pista es invalido.";
        }

        pistasRestantes--;
        String pista = tipoDePista.ejecutarPista(palabraActual);

        return pista + " (Pistas restantes: " + pistasRestantes + ")";
    }
    
    /** @return cuantas pistas le quedan al jugador */
    public int obtenerPistasRestantes() {
        return pistasRestantes;
    }

    /** Alias pa obtener intentos restantes (se usa en varias partes) */
    public int obtenerIntentosRestantes() {
        return getIntentosRestantes();
    }
    
    public void setPistasRestantes(int pistas) {
        this.pistasRestantes = pistas;
    }
    
    public void setMaxIntentos(int max) {
        this.MAX_INTENTOS = max;
    }
    
    
    // === Getters ===
    
    /** @return la palabra actual que hay que adivinar */
    public Palabra obtenerPalabraActual() {
        return palabraActual;
    }
    
    /** @return ArrayList con las letras que ya se adivinaron */
    public ArrayList<Character> obtenerLetrasAdivinadas() {
        return letrasAdivinadas;
    }

    /** @return ArrayList con las letras que fallaron */
    public ArrayList<Character> obtenerLetrasFallidas() {
        return letrasFallidas;
    }
}
