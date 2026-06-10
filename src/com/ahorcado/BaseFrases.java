package com.ahorcado;

import java.util.Random;

/**
 * BaseFrases - Base de datos de frases aleatorias pa el juego.
 * 
 * Tiene chistes de programadores, frases de seguridad informatica
 * y datos curiosos. Se muestran de forma aleatoria durante el juego
 * en la consola de mensajes pa que sea mas divertido.
 * 
 * Usa un enum Categoria pa organizar los tipos de frases y un
 * arreglo de Strings pa cada categoria.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0
 */
public class BaseFrases {

    public enum Categoria {
        CHISTES_DEV,
        MOTIVACION_SEGURIDAD,
        SABIAS_QUE
    }

    // ========== CHISTES DE PROGRAMADORES (Espanol) ==========
    private static final String[] CHISTES_DEV = {
        "Un programador se va a dormir y pone dos vasos en su mesita: uno con agua por si tiene sed, y uno vacio por si no tiene.",
        "Hardware es lo que golpeas, software es la parte a la que le gritas.",
        "Hay 10 tipos de personas: las que saben binario y las que no.",
        "Que es un terapeuta? 1024 Gigapeutas.",
        "Un SQL entra en un bar, se acerca a dos mesas y pregunta: Puedo unirme?",
        "Por que los programadores prefieren el modo oscuro? Porque la luz atrae a los bugs.",
        "Cuantos programadores se necesitan para cambiar una bombilla? Ninguno, es un problema de hardware.",
        "Programar es como escribir un libro... excepto que si te equivocas en una coma, todo el edificio se derrumba.",
        "El debugging es como ser detective en una pelicula donde tu eres el asesino.",
        "Yo no tengo errores de codigo, tengo funciones experimentales no documentadas.",
        "Git push --force: porque a veces el problema no es el codigo, es el universo que esta mal.",
        "Esto funcionaba ayer. Lo juro.",
        "Todo es un archivo si tienes suficiente coraje... o rm -rf /",
        "Los arrays empiezan en cero porque uno es el numero de la bestia en binario.",
        "Java es muy pesado porque arrastra toda la maquina virtual... como yo los lunes.",
        "Python: porque a veces solo quieres que funcione sin explicar por que.",
        "JavaScript es como una caja de sorpresas: typeof NaN es 'number'.",
        "Programador: persona que convierte cafe en codigo. Full stack: persona que convierte cafe en errores en todas las capas.",
        "La recursion es divertida. La recursion es divertida. La recursion es divertida... Stack Overflow.",
        "Mi codigo no funciona y no se por que. Mi codigo funciona y no se por que.",
        "System.out.println('Hola Mundo'); // La tradicion sigue viva"
    };

    // ========== FRASES DE MOTIVACION (Seguridad) ==========
    private static final String[] MOTIVACION_SEGURIDAD = {
        "Cada contrasena segura es una puerta cerrada para los atacantes.",
        "El cifrado es la democracia: cualquiera puede usarlo, nadie puede romperlo.",
        "Paranoia es simplemente conciencia de seguridad bien aplicada.",
        "Un backup sin probar no es un backup, es una ilusion.",
        "La seguridad no es un producto, es un proceso continuo.",
        "Confiar es bueno, pero verificar logs es mejor.",
        "El firewall mas seguro es el que esta apagado... pero no muy util.",
        "La unica red segura es la que no esta conectada a internet.",
        "Los patches de seguridad son como vitaminas para tu sistema.",
        "La autenticacion de dos factores: porque una puerta no es suficiente.",
        "El analista de seguridad duerme con un ojo abierto... mirando los logs.",
        "Todo sistema es hackeable, solo necesitas suficiente tiempo y cafe.",
        "El social engineering: la vulnerabilidad que no puedes parchear con codigo.",
        "Cifra todo. Confia en nadie. Verifica siempre.",
        "Los logs no mienten, los usuarios si."
    };

    // ========== SABIAS QUE (Curiosidades) ==========
    private static final String[] SABIAS_QUE = {
        "Sabias que? El primer bug fue una polilla real atrapada en un relevador en 1947.",
        "Dato curioso: Java fue nombrado asi por el cafe, no por la isla.",
        "Historia: El primer programador fue Ada Lovelace en 1843.",
        "Tecnologia: 1 KB son 1024 bytes, no 1000.",
        "Unix epoch comenzo el 1 de enero de 1970.",
        "Curiosidad: QWERTY fue disenado para evitar atascos mecanicos.",
        "El primer email fue '@QWERTYUIOP' enviado por Ray Tomlinson.",
        "TCP/IP fue inventado por Vint Cerf y Bob Kahn en 1974.",
        "Hexadecimal famoso: 0xDEADBEEF se usa como marca de debugging.",
        "ARPANET tenia solo 4 nodos cuando comenzo en 1969.",
        "Un byte puede representar exactamente 256 valores diferentes.",
        "Git fue creado por Linus Torvalds en 2005 para el kernel Linux.",
        "La RAM mas antigua data de 1965, hecha con anillos de ferrita.",
        "8 bits forman 1 byte, la unidad basica de informacion.",
        "Hello World aparecio por primera vez en 1978 en el libro de C de Kernighan y Ritchie."
    };

    private static final Random random = new Random();

    /**
     * Obtiene una frase aleatoria de la categoria especificada.
     */
    public static String obtenerFraseAleatoria(Categoria categoria) {
        String[] fuente;
        switch (categoria) {
            case CHISTES_DEV: fuente = CHISTES_DEV; break;
            case MOTIVACION_SEGURIDAD: fuente = MOTIVACION_SEGURIDAD; break;
            case SABIAS_QUE: fuente = SABIAS_QUE; break;
            default: fuente = CHISTES_DEV; break;
        }
        return fuente[random.nextInt(fuente.length)];
    }

    /**
     * Obtiene cualquier frase aleatoria de todas las categorias.
     */
    public static String obtenerFraseAleatoria() {
        Categoria[] categorias = Categoria.values();
        return obtenerFraseAleatoria(categorias[random.nextInt(categorias.length)]);
    }
}
