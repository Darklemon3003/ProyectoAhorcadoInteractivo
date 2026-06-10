package com.ahorcado;

/**
 * Clase principal del proyecto - Juego del Ahorcado Interactivo
 * 
 * Este es el punto de entrada unico del programa. Aqui se arranca toda la
 * aplicacion grafica usando SwingUtilities.invokeLater pa que el hilo de
 * Swing no se trabe (me costo entender esto pero basicamente el EDT es
 * el hilo principal de la interfaz y hay que respetarlo).
 * 
 * NOTA: si se ejecuta desde consola usar:
 *   javac -d out src/com/ahorcado/*.java src/com/ahorcado/util/*.java
 *   java -cp out com.ahorcado.Main
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Entrega final proyecto universidad
 */
public class Main {
    
    /**
     * Metodo main - unico punto de entrada del programa.
     * Se usa invokeLater porque el profe dijo que Swing necesita
     * ejecutarse en su propio hilo (Event Dispatch Thread) sino
     * pueden haber problemas de concurrencia y eso.
     * 
     * @param argumentos argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] argumentos) {
        // TODO: el profe dijo que siempre hay que usar invokeLater para GUIs
        // lo puse asi y funciona bien, no tocar
        javax.swing.SwingUtilities.invokeLater(() -> {
            PantallaArranque.main(null);
        });
    }
}
