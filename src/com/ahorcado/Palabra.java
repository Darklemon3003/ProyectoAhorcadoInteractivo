package com.ahorcado;

/**
 * Clase Palabra - Representa una palabra del juego del Ahorcado.
 * 
 * Aqui se aplica ENCAPSULAMIENTO (uno de los pilares de la POO):
 * los atributos son privados y se accede a ellos con getters y setters.
 * Cada palabra tiene su texto, su categoria y una pista pa ayudar al jugador.
 * 
 * Se usa en el BancoPalabras que es donde estan guardadas todas las
 * palabras en un ArrayList<Palabra>.
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Proyecto final POO
 */
public class Palabra {
    
    // === Atributos privados (encapsulamiento) ===
    
    // el texto de la palabra que hay que adivinar
    private String texto;
    
    // la categoria a la que pertenece (Animales, Paises, etc)
    private String categoria;
    
    // una pista descriptiva pa que el jugador no se quede tan perdido
    private String pistaEscrita;
    
    
    // === Constructor ===
    
    /**
     * Constructor que inicializa todos los atributos de la palabra.
     * Se usa cuando se crean las palabras en el BancoPalabras.
     * 
     * @param texto el texto de la palabra
     * @param categoria la categoria (ej: "Animales", "Paises")
     * @param pistaEscrita la pista que se le muestra al jugador
     */
    public Palabra(String texto, String categoria, String pistaEscrita) {
        this.texto = texto;
        this.categoria = categoria;
        this.pistaEscrita = pistaEscrita;
    }
    
    
    // === Getters (metodos de acceso) ===
    
    /** @return el texto de la palabra */
    public String getTexto() {
        return texto;
    }
    
    /** @return la categoria de la palabra */
    public String getCategoria() {
        return categoria;
    }
    
    /** @return la pista escrita de la palabra */
    public String getPistaEscrita() {
        return pistaEscrita;
    }
    
    
    // === Setters (metodos de modificacion) ===
    
    /** @param texto nuevo texto pa la palabra */
    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    /** @param categoria nueva categoria */
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    /** @param pistaEscrita nueva pista */
    public void setPistaEscrita(String pistaEscrita) {
        this.pistaEscrita = pistaEscrita;
    }
    
    
    // === toString - pa imprimir el objeto como texto ===
    
    /**
     * Representacion en String del objeto Palabra.
     * Esto es polimorfizmo porque sobreescribimos el metodo de Object.
     * 
     * @return cadena con la info de la palabra
     */
    @Override
    public String toString() {
        return "Palabra{" +
                "texto='" + texto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", pista='" + pistaEscrita + '\'' +
                '}';
    }
}
