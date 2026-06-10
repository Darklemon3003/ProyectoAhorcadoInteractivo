package com.ahorcado;

import java.util.ArrayList;
import java.util.Random;

/**
 * Clase BancoPalabras - Almacena todas las palabras del juego en un ArrayList.
 * 
 * Aqui es donde se guardan TODAS las palabras que se usan en el ahorcado.
 * Usa un ArrayList<Palabra> pa almacenarlas de forma dinamica (el profe
 * dijo que teniamos que usar ArrayList obligatoriamente en el proyecto).
 * 
 * Tiene mas de 300 palabras en 15 categorias diferentes, cada una con
 * su pista pa que el jugador tenga una ayuda.
 * 
 * 
 * @author Brandon Rios Gómez
 * @version 1.0 - Proyecto final POO
 */
public class BancoPalabras {

    // === Atributos privados ===

    // ArrayList principal donde se guardan todas las palabras del juego
    // NOTA: se usa ArrayList porque es dinamico y podemos agregar/quitar palabras
    private ArrayList<Palabra> listaPalabras;

    // pa generar numeros aleatorios cuando se selecciona una palabra al azar
    private Random generadorAleatorio;

    // === Constructor ===

    /**
     * Constructor - crea el banco y carga todas las palabras.
     * El ArrayList se inicializa vacio y despues se llena con
     * cargarDatosIniciales().
     */
    public BancoPalabras() {
        this.listaPalabras = new ArrayList<>();
        this.generadorAleatorio = new Random();
        cargarDatosIniciales();
    }

    // === Metodo privado pa cargar las palabras ===

    /**
     * Carga todas las palabras al ArrayList. Son como 300+ palabras
     * en 15 categorias (Hardware, Software, Redes, Ciberseguridad,
     * Programacion, Bases de Datos, Animales, Paises, etc).
     * 
     * Cada palabra se agrega con: new Palabra(texto, categoria, pista)
     * y se mete al ArrayList con .add()
     * 
     * NOTA: si quieren agregar mas palabras solo es hacer otro .add() al final
     */
    private void cargarDatosIniciales() {
        // ==================== CATEGORÍA: HARDWARE ====================
        listaPalabras.add(new Palabra("procesador", "Hardware", "Cerebro principal de la computadora"));
        listaPalabras.add(new Palabra("disipador", "Hardware", "Sistema para extraer calor del procesador"));
        listaPalabras.add(new Palabra("servidor", "Hardware", "Computadora central que provee servicios en red"));
        listaPalabras.add(new Palabra("teclado", "Hardware", "Periferico principal de entrada de texto"));
        listaPalabras.add(new Palabra("monitor", "Hardware", "Pantalla de visualizacion principal"));
        listaPalabras.add(new Palabra("cache", "Hardware", "Memoria ultrarapida integrada para acelerar operaciones"));
        listaPalabras.add(new Palabra("motherboard", "Hardware", "Placa principal que coordina componentes fisicos"));
        listaPalabras.add(new Palabra("chipset", "Hardware", "Conjunto de chips que coordina comunicaciones internas"));
        listaPalabras.add(new Palabra("transistor", "Hardware", "Componente electronico base de los circuitos"));
        listaPalabras.add(new Palabra("microprocesador", "Hardware", "Circuito integrado que ejecuta instrucciones"));
        listaPalabras.add(new Palabra("condensador", "Hardware", "Almacena carga electrica temporal"));
        listaPalabras.add(new Palabra("resistencia", "Hardware", "Limita el flujo de corriente electrica"));
        listaPalabras.add(new Palabra("ventilador", "Hardware", "Enfria los componentes internos"));
        listaPalabras.add(new Palabra("impresora", "Hardware", "Dispositivo que plasma texto en papel"));
        listaPalabras.add(new Palabra("escaner", "Hardware", "Digitaliza documentos fisicos"));
        listaPalabras.add(new Palabra("webcam", "Hardware", "Camara para videoconferencias"));
        listaPalabras.add(new Palabra("auriculares", "Hardware", "Dispositivo de audio personal"));
        listaPalabras.add(new Palabra("pendrive", "Hardware", "Memoria portatil conectada por puerto"));
        listaPalabras.add(new Palabra("raton", "Hardware", "Periferico para apuntar en pantalla"));
        listaPalabras.add(new Palabra("altavoz", "Hardware", "Emite sonido desde la computadora"));

        // ==================== CATEGORÍA: SOFTWARE ====================
        listaPalabras.add(new Palabra("compilador", "Software", "Traduce codigo fuente a lenguaje maquina"));
        listaPalabras.add(new Palabra("algoritmo", "Software", "Secuencia de pasos para resolver un problema"));
        listaPalabras.add(new Palabra("interfaz", "Software", "Punto de interaccion entre sistema y usuario"));
        listaPalabras.add(new Palabra("firmware", "Software", "Codigo grabado en memoria que controla hardware"));
        listaPalabras.add(new Palabra("kernel", "Software", "Nucleo profundo del sistema operativo"));
        listaPalabras.add(new Palabra("script", "Software", "Archivo de texto con comandos automatizados"));
        listaPalabras.add(new Palabra("terminal", "Software", "Consola de comandos en texto"));
        listaPalabras.add(new Palabra("driver", "Software", "Controlador que permite usar un periferico"));
        listaPalabras.add(new Palabra("aplicacion", "Software", "Programa orientado a una tarea del usuario"));
        listaPalabras.add(new Palabra("navegador", "Software", "Software usado para acceder a sitios web"));
        listaPalabras.add(new Palabra("repositorio", "Software", "Almacen de codigo y versiones"));
        listaPalabras.add(new Palabra("framework", "Software", "Estructura base para crear aplicaciones"));
        listaPalabras.add(new Palabra("depurador", "Software", "Herramienta para encontrar errores en codigo"));
        listaPalabras.add(new Palabra("emulador", "Software", "Simula otro sistema en tu computadora"));
        listaPalabras.add(new Palabra("antivirus", "Software", "Detecta y elimina amenazas conocidas"));
        listaPalabras.add(new Palabra("parche", "Software", "Correccion de errores en un programa"));
        listaPalabras.add(new Palabra("libreria", "Software", "Conjunto de funciones reutilizables"));
        listaPalabras.add(new Palabra("modulo", "Software", "Parte independiente de un programa"));
        listaPalabras.add(new Palabra("plugin", "Software", "Extension que agrega funcionalidad"));
        listaPalabras.add(new Palabra("binario", "Software", "Archivo ejecutable compilado"));

        // ==================== CATEGORÍA: REDES ====================
        listaPalabras.add(new Palabra("enrutador", "Redes", "Dispositivo que dirige paquetes de red"));
        listaPalabras.add(new Palabra("firewall", "Redes", "Barrera de seguridad contra accesos no autorizados"));
        listaPalabras.add(new Palabra("protocolo", "Redes", "Conjunto de reglas para comunicacion digital"));
        listaPalabras.add(new Palabra("latencia", "Redes", "Tiempo de retardo en enviar y recibir paquetes"));
        listaPalabras.add(new Palabra("puerto", "Redes", "Punto de conexion logico para red"));
        listaPalabras.add(new Palabra("switch", "Redes", "Conmutador que enlaza dispositivos de red local"));
        listaPalabras.add(new Palabra("gateway", "Redes", "Puerta de enlace hacia redes externas"));
        listaPalabras.add(new Palabra("subred", "Redes", "Segmento logico dentro de una red mayor"));
        listaPalabras.add(new Palabra("ethernet", "Redes", "Tecnologia comun de red cableada"));
        listaPalabras.add(new Palabra("dominio", "Redes", "Nombre legible que identifica un sitio web"));
        listaPalabras.add(new Palabra("paquete", "Redes", "Unidad de datos que viaja por la red"));
        listaPalabras.add(new Palabra("anfitrion", "Redes", "Equipo conectado que ofrece servicios"));
        listaPalabras.add(new Palabra("nodo", "Redes", "Punto de conexion en una topologia"));
        listaPalabras.add(new Palabra("fibra", "Redes", "Medio de transmision por luz"));
        listaPalabras.add(new Palabra("satelite", "Redes", "Dispositivo orbital para comunicaciones"));
        listaPalabras.add(new Palabra("router", "Redes", "Encamina trafico entre redes distintas"));
        listaPalabras.add(new Palabra("broadcast", "Redes", "Envio de datos a todos los nodos"));
        listaPalabras.add(new Palabra("cortafuegos", "Redes", "Filtra conexiones entrantes y salientes"));
        listaPalabras.add(new Palabra("proxy", "Redes", "Intermediario entre cliente y servidor"));
        listaPalabras.add(new Palabra("tunelizado", "Redes", "Encapsular datos dentro de otro protocolo"));

        // ==================== CATEGORÍA: CIBERSEGURIDAD ====================
        listaPalabras.add(new Palabra("encriptacion", "Ciberseguridad", "Ocultamiento de datos mediante criptografia"));
        listaPalabras.add(new Palabra("malware", "Ciberseguridad", "Software malicioso disenado para danar"));
        listaPalabras.add(new Palabra("phishing", "Ciberseguridad", "Suplantacion de identidad para robar datos"));
        listaPalabras.add(new Palabra("ransomware", "Ciberseguridad", "Secuestro de datos a cambio de un pago"));
        listaPalabras.add(new Palabra("vulnerabilidad", "Ciberseguridad", "Fallo de diseno que permite un ataque"));
        listaPalabras.add(new Palabra("botnet", "Ciberseguridad", "Red de equipos zombis controlados remotamente"));
        listaPalabras.add(new Palabra("exploit", "Ciberseguridad", "Tecnica que aprovecha una vulnerabilidad"));
        listaPalabras.add(new Palabra("honeypot", "Ciberseguridad", "Sistema senuelo para detectar atacantes"));
        listaPalabras.add(new Palabra("troyano", "Ciberseguridad", "Malware disfrazado de programa legitimo"));
        listaPalabras.add(new Palabra("rootkit", "Ciberseguridad", "Software oculto con acceso privilegiado"));
        listaPalabras.add(new Palabra("keylogger", "Ciberseguridad", "Registra las pulsaciones del teclado"));
        listaPalabras.add(new Palabra("spyware", "Ciberseguridad", "Programa espia que recolecta informacion"));
        listaPalabras.add(new Palabra("cifrado", "Ciberseguridad", "Transformar datos para hacerlos ilegibles"));
        listaPalabras.add(new Palabra("autenticacion", "Ciberseguridad", "Verificar la identidad de un usuario"));
        listaPalabras.add(new Palabra("intrusion", "Ciberseguridad", "Acceso no autorizado a un sistema"));
        listaPalabras.add(new Palabra("sniffer", "Ciberseguridad", "Captura paquetes de red para analisis"));
        listaPalabras.add(new Palabra("sandbox", "Ciberseguridad", "Entorno aislado para ejecutar codigo sospechoso"));
        listaPalabras.add(new Palabra("pentesting", "Ciberseguridad", "Pruebas de penetracion en sistemas"));
        listaPalabras.add(new Palabra("criptografia", "Ciberseguridad", "Ciencia de proteger informacion"));
        listaPalabras.add(new Palabra("forense", "Ciberseguridad", "Investigacion de incidentes digitales"));

        // ==================== CATEGORÍA: PROGRAMACION ====================
        listaPalabras.add(new Palabra("clase", "Programacion", "Molde que define objetos"));
        listaPalabras.add(new Palabra("objeto", "Programacion", "Instancia con estado y comportamiento"));
        listaPalabras.add(new Palabra("metodo", "Programacion", "Accion definida dentro de una clase"));
        listaPalabras.add(new Palabra("polimorfismo", "Programacion", "Capacidad de usar multiples formas"));
        listaPalabras.add(new Palabra("herencia", "Programacion", "Reutilizar atributos de otra clase"));
        listaPalabras.add(new Palabra("variable", "Programacion", "Espacio de memoria con un nombre"));
        listaPalabras.add(new Palabra("funcion", "Programacion", "Bloque de codigo que realiza una tarea"));
        listaPalabras.add(new Palabra("constructor", "Programacion", "Inicializa un objeto al crearlo"));
        listaPalabras.add(new Palabra("iteracion", "Programacion", "Repeticion de un bloque de codigo"));
        listaPalabras.add(new Palabra("recursion", "Programacion", "Funcion que se llama a si misma"));
        listaPalabras.add(new Palabra("abstraccion", "Programacion", "Ocultar detalles mostrando lo esencial"));
        listaPalabras.add(new Palabra("encapsulamiento", "Programacion", "Proteger datos internos de acceso externo"));
        listaPalabras.add(new Palabra("excepcion", "Programacion", "Error controlado durante la ejecucion"));
        listaPalabras.add(new Palabra("interfaz", "Programacion", "Contrato de metodos sin implementacion"));
        listaPalabras.add(new Palabra("sintaxis", "Programacion", "Reglas gramaticales de un lenguaje"));
        listaPalabras.add(new Palabra("depuracion", "Programacion", "Proceso de encontrar y corregir errores"));
        listaPalabras.add(new Palabra("compilacion", "Programacion", "Convertir codigo a ejecutable"));
        listaPalabras.add(new Palabra("instancia", "Programacion", "Objeto creado a partir de una clase"));
        listaPalabras.add(new Palabra("arreglo", "Programacion", "Estructura que almacena elementos indexados"));
        listaPalabras.add(new Palabra("puntero", "Programacion", "Referencia a una direccion de memoria"));

        // ==================== CATEGORÍA: BASES DE DATOS ====================
        listaPalabras.add(new Palabra("tabla", "Bases de Datos", "Estructura de filas y columnas"));
        listaPalabras.add(new Palabra("consulta", "Bases de Datos", "Solicitud de informacion a una base"));
        listaPalabras.add(new Palabra("indice", "Bases de Datos", "Estructura que acelera busquedas"));
        listaPalabras.add(new Palabra("registro", "Bases de Datos", "Una fila de datos en una tabla"));
        listaPalabras.add(new Palabra("relacion", "Bases de Datos", "Vinculo entre dos tablas"));
        listaPalabras.add(new Palabra("esquema", "Bases de Datos", "Estructura logica de la base de datos"));
        listaPalabras.add(new Palabra("transaccion", "Bases de Datos", "Operacion atomica sobre datos"));
        listaPalabras.add(new Palabra("respaldo", "Bases de Datos", "Copia de seguridad de datos"));
        listaPalabras.add(new Palabra("normalizacion", "Bases de Datos", "Organizar datos para evitar redundancia"));
        listaPalabras.add(new Palabra("cursor", "Bases de Datos", "Apuntador para recorrer resultados"));

        // ==================== CATEGORÍA: ANIMALES ====================
        listaPalabras.add(new Palabra("gato", "Animales", "Felino domesticado que maulla"));
        listaPalabras.add(new Palabra("perro", "Animales", "Mejor amigo del hombre"));
        listaPalabras.add(new Palabra("elefante", "Animales", "Mamifero terrestre mas grande"));
        listaPalabras.add(new Palabra("leon", "Animales", "Rey de la selva africana"));
        listaPalabras.add(new Palabra("tigre", "Animales", "Felino rayado de Asia"));
        listaPalabras.add(new Palabra("delfin", "Animales", "Mamifero marino muy inteligente"));
        listaPalabras.add(new Palabra("aguila", "Animales", "Ave rapaz de excelente vision"));
        listaPalabras.add(new Palabra("cocodrilo", "Animales", "Reptil de mandibulas poderosas"));
        listaPalabras.add(new Palabra("ballena", "Animales", "El animal mas grande del oceano"));
        listaPalabras.add(new Palabra("lobo", "Animales", "Canido salvaje que vive en manada"));
        listaPalabras.add(new Palabra("serpiente", "Animales", "Reptil sin extremidades"));
        listaPalabras.add(new Palabra("pantera", "Animales", "Felino negro sigiloso"));
        listaPalabras.add(new Palabra("halcon", "Animales", "Ave de caza veloz"));
        listaPalabras.add(new Palabra("tortuga", "Animales", "Reptil con caparazon protector"));
        listaPalabras.add(new Palabra("canguro", "Animales", "Marsupial que salta de Australia"));
        listaPalabras.add(new Palabra("pulpo", "Animales", "Molusco de ocho tentaculos"));
        listaPalabras.add(new Palabra("oso", "Animales", "Mamifero grande que hiberna"));
        listaPalabras.add(new Palabra("caballo", "Animales", "Equino veloz domesticado"));
        listaPalabras.add(new Palabra("medusa", "Animales", "Criatura marina gelatinosa"));
        listaPalabras.add(new Palabra("colibri", "Animales", "Ave diminuta que vuela en reversa"));

        // ==================== CATEGORÍA: PAÍSES ====================
        listaPalabras.add(new Palabra("japon", "Paises", "El pais del sol naciente"));
        listaPalabras.add(new Palabra("francia", "Paises", "Hogar de la Torre Eiffel"));
        listaPalabras.add(new Palabra("mexico", "Paises", "Pais conocido por sus piramides y mariachis"));
        listaPalabras.add(new Palabra("argentina", "Paises", "Tierra del tango y la Patagonia"));
        listaPalabras.add(new Palabra("colombia", "Paises", "Famoso por su excelente cafe"));
        listaPalabras.add(new Palabra("brasil", "Paises", "Pais del carnaval y el futbol"));
        listaPalabras.add(new Palabra("alemania", "Paises", "Potencia europea de ingenieria"));
        listaPalabras.add(new Palabra("italia", "Paises", "Cuna del arte renacentista"));
        listaPalabras.add(new Palabra("canada", "Paises", "Segundo pais mas grande del mundo"));
        listaPalabras.add(new Palabra("australia", "Paises", "Continente isla con fauna unica"));
        listaPalabras.add(new Palabra("chile", "Paises", "Pais largo junto al Pacifico"));
        listaPalabras.add(new Palabra("peru", "Paises", "Hogar de Machu Picchu"));
        listaPalabras.add(new Palabra("egipto", "Paises", "Tierra de faraones y piramides"));
        listaPalabras.add(new Palabra("india", "Paises", "Pais de cultura milenaria y diversa"));
        listaPalabras.add(new Palabra("rusia", "Paises", "Nacion mas extensa del planeta"));
        listaPalabras.add(new Palabra("grecia", "Paises", "Cuna de la filosofia occidental"));
        listaPalabras.add(new Palabra("portugal", "Paises", "Vecino occidental de la peninsula iberica"));
        listaPalabras.add(new Palabra("noruega", "Paises", "Pais nordico de fiordos"));
        listaPalabras.add(new Palabra("suecia", "Paises", "Nacion escandinava del premio Nobel"));
        listaPalabras.add(new Palabra("cuba", "Paises", "Isla caribena de musica y color"));

        // ==================== CATEGORÍA: DEPORTES ====================
        listaPalabras.add(new Palabra("futbol", "Deportes", "Deporte rey con un balon y arcos"));
        listaPalabras.add(new Palabra("baloncesto", "Deportes", "Deporte de canastas y rebotes"));
        listaPalabras.add(new Palabra("tenis", "Deportes", "Deporte con raqueta y red"));
        listaPalabras.add(new Palabra("natacion", "Deportes", "Deporte practicado en el agua"));
        listaPalabras.add(new Palabra("atletismo", "Deportes", "Conjunto de carreras saltos y lanzamientos"));
        listaPalabras.add(new Palabra("voleibol", "Deportes", "Deporte de red con seis jugadores"));
        listaPalabras.add(new Palabra("ciclismo", "Deportes", "Competencia sobre bicicleta"));
        listaPalabras.add(new Palabra("boxeo", "Deportes", "Combate con los punos"));
        listaPalabras.add(new Palabra("esgrima", "Deportes", "Lucha con espada deportiva"));
        listaPalabras.add(new Palabra("rugby", "Deportes", "Deporte de equipo con balon ovalado"));
        listaPalabras.add(new Palabra("hockey", "Deportes", "Deporte con disco sobre hielo"));
        listaPalabras.add(new Palabra("surf", "Deportes", "Deslizarse sobre las olas del mar"));
        listaPalabras.add(new Palabra("escalada", "Deportes", "Ascender paredes rocosas"));
        listaPalabras.add(new Palabra("karate", "Deportes", "Arte marcial japones de golpes"));
        listaPalabras.add(new Palabra("judo", "Deportes", "Arte marcial de lanzamientos"));
        listaPalabras.add(new Palabra("beisbol", "Deportes", "Deporte de bate y pelota"));
        listaPalabras.add(new Palabra("golf", "Deportes", "Meter la pelota en el hoyo"));
        listaPalabras.add(new Palabra("esqui", "Deportes", "Descenso sobre nieve"));
        listaPalabras.add(new Palabra("patinaje", "Deportes", "Deslizarse sobre ruedas o hielo"));
        listaPalabras.add(new Palabra("remo", "Deportes", "Impulsar embarcacion con palas"));

        // ==================== CATEGORÍA: COMIDAS ====================
        listaPalabras.add(new Palabra("hamburguesa", "Comidas", "Pan con carne a la parrilla"));
        listaPalabras.add(new Palabra("sushi", "Comidas", "Platillo japones con arroz y pescado"));
        listaPalabras.add(new Palabra("pizza", "Comidas", "Comida redonda italiana con queso"));
        listaPalabras.add(new Palabra("tacos", "Comidas", "Tortilla rellena mexicana"));
        listaPalabras.add(new Palabra("paella", "Comidas", "Arroz tradicional espanol"));
        listaPalabras.add(new Palabra("lasagna", "Comidas", "Pasta en capas con salsa"));
        listaPalabras.add(new Palabra("croissant", "Comidas", "Bollo hojaldrado frances"));
        listaPalabras.add(new Palabra("empanada", "Comidas", "Masa rellena horneada o frita"));
        listaPalabras.add(new Palabra("ceviche", "Comidas", "Pescado marinado en limon"));
        listaPalabras.add(new Palabra("ramen", "Comidas", "Sopa japonesa de fideos"));
        listaPalabras.add(new Palabra("burrito", "Comidas", "Tortilla enrollada con relleno"));
        listaPalabras.add(new Palabra("arepa", "Comidas", "Pan de maiz venezolano y colombiano"));
        listaPalabras.add(new Palabra("chocolate", "Comidas", "Dulce hecho de cacao"));
        listaPalabras.add(new Palabra("galleta", "Comidas", "Dulce horneado crujiente"));
        listaPalabras.add(new Palabra("helado", "Comidas", "Postre cremoso congelado"));
        listaPalabras.add(new Palabra("pancake", "Comidas", "Tortita dulce para desayuno"));
        listaPalabras.add(new Palabra("waffle", "Comidas", "Masa con patron de rejilla"));
        listaPalabras.add(new Palabra("ensalada", "Comidas", "Mezcla de vegetales frescos"));
        listaPalabras.add(new Palabra("sopa", "Comidas", "Plato liquido y caliente"));
        listaPalabras.add(new Palabra("flan", "Comidas", "Postre de caramelo y huevo"));

        // ==================== CATEGORÍA: CIENCIA ====================
        listaPalabras.add(new Palabra("atomo", "Ciencia", "Unidad basica de la materia"));
        listaPalabras.add(new Palabra("molecula", "Ciencia", "Grupo de atomos enlazados"));
        listaPalabras.add(new Palabra("celula", "Ciencia", "Unidad fundamental de la vida"));
        listaPalabras.add(new Palabra("galaxia", "Ciencia", "Sistema de millones de estrellas"));
        listaPalabras.add(new Palabra("gravedad", "Ciencia", "Fuerza que atrae los cuerpos"));
        listaPalabras.add(new Palabra("fotosintesis", "Ciencia", "Las plantas convierten luz en energia"));
        listaPalabras.add(new Palabra("evolucion", "Ciencia", "Cambio gradual de las especies"));
        listaPalabras.add(new Palabra("volcan", "Ciencia", "Montana que expulsa lava"));
        listaPalabras.add(new Palabra("tsunami", "Ciencia", "Ola gigante causada por sismo"));
        listaPalabras.add(new Palabra("eclipse", "Ciencia", "Un astro oculta a otro"));
        listaPalabras.add(new Palabra("fosil", "Ciencia", "Resto petrificado de ser antiguo"));
        listaPalabras.add(new Palabra("neurona", "Ciencia", "Celula del sistema nervioso"));
        listaPalabras.add(new Palabra("cromosoma", "Ciencia", "Estructura con informacion genetica"));
        listaPalabras.add(new Palabra("oxigeno", "Ciencia", "Gas esencial para respirar"));
        listaPalabras.add(new Palabra("plasma", "Ciencia", "Cuarto estado de la materia"));
        listaPalabras.add(new Palabra("reactor", "Ciencia", "Dispositivo para reacciones nucleares"));
        listaPalabras.add(new Palabra("bacteria", "Ciencia", "Microorganismo unicelular"));
        listaPalabras.add(new Palabra("ecosistema", "Ciencia", "Comunidad de seres vivos y su entorno"));
        listaPalabras.add(new Palabra("meteorito", "Ciencia", "Roca espacial que cae a la Tierra"));
        listaPalabras.add(new Palabra("telescopio", "Ciencia", "Instrumento para observar las estrellas"));

        // ==================== CATEGORÍA: OBJETOS ====================
        listaPalabras.add(new Palabra("microscopio", "Objetos", "Permite ver cosas muy pequenas"));
        listaPalabras.add(new Palabra("brujula", "Objetos", "Indica el norte magnetico"));
        listaPalabras.add(new Palabra("reloj", "Objetos", "Mide el paso del tiempo"));
        listaPalabras.add(new Palabra("espejo", "Objetos", "Refleja la imagen de quien mira"));
        listaPalabras.add(new Palabra("candado", "Objetos", "Dispositivo mecanico de seguridad"));
        listaPalabras.add(new Palabra("linterna", "Objetos", "Emite luz portatil"));
        listaPalabras.add(new Palabra("martillo", "Objetos", "Herramienta para clavar"));
        listaPalabras.add(new Palabra("tijeras", "Objetos", "Instrumento para cortar"));
        listaPalabras.add(new Palabra("paraguas", "Objetos", "Protege de la lluvia"));
        listaPalabras.add(new Palabra("mochila", "Objetos", "Bolsa que se lleva en la espalda"));
        listaPalabras.add(new Palabra("guitarra", "Objetos", "Instrumento musical de cuerdas"));
        listaPalabras.add(new Palabra("binoculares", "Objetos", "Para ver objetos lejanos"));
        listaPalabras.add(new Palabra("termometro", "Objetos", "Mide la temperatura"));
        listaPalabras.add(new Palabra("balanza", "Objetos", "Instrumento para pesar"));
        listaPalabras.add(new Palabra("extintor", "Objetos", "Apaga incendios pequenos"));
        listaPalabras.add(new Palabra("llave", "Objetos", "Abre cerraduras"));
        listaPalabras.add(new Palabra("ancla", "Objetos", "Fija un barco al fondo marino"));
        listaPalabras.add(new Palabra("campana", "Objetos", "Produce sonido al golpearla"));
        listaPalabras.add(new Palabra("escalera", "Objetos", "Permite subir a lugares altos"));
        listaPalabras.add(new Palabra("calendario", "Objetos", "Organiza los dias del ano"));

        // ==================== CATEGORÍA: HISTORIA ====================
        listaPalabras.add(new Palabra("piramide", "Historia", "Monumento funerario del antiguo Egipto"));
        listaPalabras.add(new Palabra("gladiador", "Historia", "Luchador del coliseo romano"));
        listaPalabras.add(new Palabra("faraon", "Historia", "Gobernante del antiguo Egipto"));
        listaPalabras.add(new Palabra("samurai", "Historia", "Guerrero medieval japones"));
        listaPalabras.add(new Palabra("vikingo", "Historia", "Navegante nordico explorador"));
        listaPalabras.add(new Palabra("coliseo", "Historia", "Anfiteatro romano para espectaculos"));
        listaPalabras.add(new Palabra("imperio", "Historia", "Estado extenso bajo un gobernante"));
        listaPalabras.add(new Palabra("revolucion", "Historia", "Cambio radical en la sociedad"));
        listaPalabras.add(new Palabra("monarquia", "Historia", "Gobierno de reyes y reinas"));
        listaPalabras.add(new Palabra("democracia", "Historia", "Gobierno del pueblo"));
        listaPalabras.add(new Palabra("medieval", "Historia", "Periodo de castillos y caballeros"));
        listaPalabras.add(new Palabra("conquista", "Historia", "Tomar territorio por la fuerza"));
        listaPalabras.add(new Palabra("civilizacion", "Historia", "Sociedad humana avanzada"));
        listaPalabras.add(new Palabra("arqueologia", "Historia", "Estudio de restos antiguos"));
        listaPalabras.add(new Palabra("jeroglifico", "Historia", "Escritura con simbolos del antiguo Egipto"));
        listaPalabras.add(new Palabra("catapulta", "Historia", "Arma de asedio medieval"));
        listaPalabras.add(new Palabra("pergamino", "Historia", "Material antiguo para escribir"));
        listaPalabras.add(new Palabra("armadura", "Historia", "Proteccion metalica del guerrero"));
        listaPalabras.add(new Palabra("fortaleza", "Historia", "Construccion militar defensiva"));
        listaPalabras.add(new Palabra("cruzada", "Historia", "Expedicion militar religiosa medieval"));

        // ==================== CATEGORÍA: NATURALEZA ====================
        listaPalabras.add(new Palabra("cascada", "Naturaleza", "Caida de agua desde lo alto"));
        listaPalabras.add(new Palabra("arcoiris", "Naturaleza", "Arco de colores tras la lluvia"));
        listaPalabras.add(new Palabra("relampago", "Naturaleza", "Descarga electrica en tormenta"));
        listaPalabras.add(new Palabra("huracan", "Naturaleza", "Tormenta tropical devastadora"));
        listaPalabras.add(new Palabra("terremoto", "Naturaleza", "Temblor violento de la tierra"));
        listaPalabras.add(new Palabra("glaciar", "Naturaleza", "Masa de hielo en movimiento"));
        listaPalabras.add(new Palabra("selva", "Naturaleza", "Bosque tropical muy denso"));
        listaPalabras.add(new Palabra("desierto", "Naturaleza", "Terreno arido con poca vida"));
        listaPalabras.add(new Palabra("coral", "Naturaleza", "Estructura marina de colores vivos"));
        listaPalabras.add(new Palabra("manantial", "Naturaleza", "Fuente natural de agua pura"));
        listaPalabras.add(new Palabra("estalagmita", "Naturaleza", "Formacion rocosa que crece del suelo"));
        listaPalabras.add(new Palabra("tornado", "Naturaleza", "Columna de viento destructiva"));
        listaPalabras.add(new Palabra("pantano", "Naturaleza", "Terreno inundado con vegetacion"));
        listaPalabras.add(new Palabra("pradera", "Naturaleza", "Llanura cubierta de hierba"));
        listaPalabras.add(new Palabra("arrecife", "Naturaleza", "Formacion submarina de corales"));
        listaPalabras.add(new Palabra("aurora", "Naturaleza", "Luces de colores en el cielo polar"));
        listaPalabras.add(new Palabra("tundra", "Naturaleza", "Bioma frio con suelo congelado"));
        listaPalabras.add(new Palabra("manglar", "Naturaleza", "Bosque costero de raices aereas"));
        listaPalabras.add(new Palabra("oasis", "Naturaleza", "Zona fertil en medio del desierto"));
        listaPalabras.add(new Palabra("erupcion", "Naturaleza", "Explosion de un volcan"));

        // ==================== CATEGORÍA: MUSICA ====================
        listaPalabras.add(new Palabra("melodia", "Musica", "Secuencia agradable de notas"));
        listaPalabras.add(new Palabra("acorde", "Musica", "Varias notas sonando juntas"));
        listaPalabras.add(new Palabra("ritmo", "Musica", "Patron repetitivo de sonidos"));
        listaPalabras.add(new Palabra("bateria", "Musica", "Conjunto de tambores y platillos"));
        listaPalabras.add(new Palabra("violin", "Musica", "Instrumento de cuerda frotada"));
        listaPalabras.add(new Palabra("trompeta", "Musica", "Instrumento de viento metalico"));
        listaPalabras.add(new Palabra("orquesta", "Musica", "Gran conjunto de musicos"));
        listaPalabras.add(new Palabra("partitura", "Musica", "Hoja con notas musicales escritas"));
        listaPalabras.add(new Palabra("coro", "Musica", "Grupo de voces cantando juntas"));
        listaPalabras.add(new Palabra("sinfonia", "Musica", "Composicion orquestal extensa"));
    }

    // === Metodos publicos del banco ===

    /**
     * Saca una palabra al azar del ArrayList usando Random.
     * Se usa cuando empieza una nueva partida pa elegir la palabra secreta.
     * 
     * @return un objeto Palabra aleatorio del banco
     */
    public Palabra obtenerPalabraAlAzar() {
        int indiceAleatorio = generadorAleatorio.nextInt(listaPalabras.size());
        return listaPalabras.get(indiceAleatorio);
    }

    // sobrecarga del metodo pa filtrar por categoria tambien (polimorfizmo
    // estatico)
    public Palabra obtenerPalabraAlAzar(String categoria) {
        if (categoria == null || categoria.equals("ALEATORIO"))
            return obtenerPalabraAlAzar();
        ArrayList<Palabra> filtradas = filtrarPorCategoria(categoria);
        if (filtradas.isEmpty())
            return obtenerPalabraAlAzar();
        int indiceAleatorio = generadorAleatorio.nextInt(filtradas.size());
        return filtradas.get(indiceAleatorio);
    }

    /**
     * Filtra las palabras por categoria usando un for-each.
     * Recorre el ArrayList completo y va metiendo en otro ArrayList
     * las que coincidan con la categoria que se pide.
     * 
     * @param categoriaSeleccionada la categoria que queremos filtrar
     * @return un ArrayList con las palabras de esa categoria
     */
    public ArrayList<Palabra> filtrarPorCategoria(String categoriaSeleccionada) {
        ArrayList<Palabra> palabrasFiltradas = new ArrayList<>();

        // recorrer todo el array list y agregar las que coincidan
        for (Palabra palabra : listaPalabras) {
            if (palabra.getCategoria().equalsIgnoreCase(categoriaSeleccionada)) {
                palabrasFiltradas.add(palabra);
            }
        }

        return palabrasFiltradas;
    }

    // === Getters ===

    /** @return el ArrayList completo con todas las palabras */
    public ArrayList<Palabra> getListaPalabras() {
        return listaPalabras;
    }

    /** @return cuantas palabras hay en total (usa .size() del ArrayList) */
    public int obtenerTotalPalabras() {
        return listaPalabras.size();
    }

    /**
     * Saca las categorias unicas que hay en el banco.
     * Recorre el ArrayList y va agregando las que no esten repetidas
     * a otro ArrayList de Strings.
     * 
     * @return ArrayList<String> con los nombres de las categorias
     */
    public ArrayList<String> obtenerCategorias() {
        ArrayList<String> categorias = new ArrayList<>();

        // recorrer y agregar solo si no esta repetida
        for (Palabra palabra : listaPalabras) {
            if (!categorias.contains(palabra.getCategoria())) {
                categorias.add(palabra.getCategoria());
            }
        }

        return categorias;
    }
}
