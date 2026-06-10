# Proyecto Final - Juego del Ahorcado Interactivo

## Descripcion

Proyecto final de la materia de Programacion Orientada a Objetos (POO).
Es un juego del ahorcado con interfaz grafica hecha en Java Swing, con tematica
de hacking/ciberseguridad. Tiene mas de 300 palabras, sistema de economia virtual,
coleccionables, skins, fondos animados, misiones diarias y mas.

**Autor:** Brandon Rios Gómez
**Materia:** Programacion Orientada a Objetos
**Lenguaje:** Java (Swing para la GUI)

## Estructura del Proyecto

```
ProyectoAhorcadoInteractivo/
├── src/com/ahorcado/
│   ├── Main.java                  <-- PUNTO DE ENTRADA (ejecutar este)
│   ├── Palabra.java               <-- Modelo de palabra (encapsulamiento)
│   ├── BancoPalabras.java         <-- Banco de 300+ palabras en ArrayList
│   ├── LogicaJuego.java           <-- Logica del juego (ArrayLists de letras)
│   ├── IPista.java                <-- Interfaz para pistas (POLIMORFISMO)
│   ├── PistaTexto.java            <-- Implementa IPista (descripcion)
│   ├── PistaCategoria.java        <-- Implementa IPista (categoria)
│   ├── PistaLetraAleatoria.java   <-- Implementa IPista (letra random)
│   ├── InterfazAhorcado.java      <-- Ventana del juego (extiende JFrame)
│   ├── PantallaArranque.java      <-- Menu principal (extiende JFrame)
│   ├── PanelSujeto.java           <-- Dibujo del ahorcado (extiende JPanel)
│   ├── TiendaClandestina.java     <-- Tienda virtual (extiende JDialog)
│   ├── GlitchScreen.java          <-- Efecto de transicion
│   ├── BaseFrases.java            <-- Frases aleatorias
│   ├── EtiquetasPistaDominio.java <-- Utilidad para etiquetas
│   └── util/                      <-- Clases utilitarias
│       ├── GestorEstadisticas.java    <-- Persistencia de stats
│       ├── GestorEconomia.java        <-- Sistema de moneda virtual (KK)
│       ├── GestorMisiones.java        <-- Misiones diarias y desafios
│       ├── GestorBugs.java            <-- Catalogo de bugs coleccionables
│       ├── GestorFondos.java          <-- Catalogo de fondos/wallpapers
│       ├── GestorSkins.java           <-- Catalogo de skins
│       ├── Bug.java                   <-- Modelo de bug (enum Rareza)
│       ├── FondoJuego.java            <-- Modelo de fondo
│       ├── SkinAhorcado.java          <-- Modelo de skin
│       ├── BugAvatarFactory.java      <-- Genera avatares programaticamente
│       ├── FondoFactory.java          <-- Genera fondos programaticamente
│       ├── AudioController.java       <-- Control de audio (Singleton)
│       ├── GestorSonido.java          <-- Carga y reproduce sonidos
│       └── MotorVoz.java              <-- Texto a voz (Windows TTS)
├── sounds/                        <-- Archivos de audio .wav
├── images/                        <-- Imagenes del juego
└── README.md                      <-- Este archivo
```

## Conceptos de POO Aplicados

### Encapsulamiento
- Todas las clases usan atributos **privados** con getters y setters
- Ejemplo: `Palabra.java` tiene `texto`, `categoria`, `pistaEscrita` privados

### Herencia
- `InterfazAhorcado` extiende `JFrame`
- `PantallaArranque` extiende `JFrame`
- `PanelSujeto` extiende `JPanel`
- `TiendaClandestina` extiende `JDialog`

### Polimorfismo
- Interfaz `IPista` con 3 implementaciones diferentes:
  - `PistaTexto` - muestra la descripcion
  - `PistaCategoria` - muestra la categoria
  - `PistaLetraAleatoria` - revela una letra
- El metodo `solicitarPista(IPista)` en `LogicaJuego` acepta cualquier tipo de pista

### ArrayList
- `ArrayList<Palabra>` en `BancoPalabras` para las 300+ palabras
- `ArrayList<Character>` en `LogicaJuego` para letras adivinadas y fallidas
- `ArrayList<String>` para obtener categorias unicas
- Se usa `.add()`, `.get()`, `.size()`, `.contains()`, `.isEmpty()` y for-each

### Manejo de Eventos (Swing)
- `ActionListener` en botones del teclado virtual y menu
- `MouseListener` para efectos hover y clicks
- `KeyBindings` para capturar teclas del teclado fisico
- `Timer` de Swing para animaciones y temporizadores

## Como Ejecutar el Proyecto

### Opcion 1: Desde Visual Studio Code (RECOMENDADO)

1. **Instalar VS Code** si no lo tienes: https://code.visualstudio.com/

2. **Instalar el Extension Pack for Java**:
   - Abrir VS Code
   - Ir a la pestaña de Extensiones (icono de cuadraditos en la barra lateral izquierda, o presionar `Ctrl+Shift+X`)
   - Buscar **"Extension Pack for Java"** (de Microsoft)
   - Darle clic en **Install** y esperar a que se instale todo

3. **Abrir el proyecto**:
   - En VS Code ir a `File > Open Folder...`
   - Seleccionar la carpeta `ProyectoAhorcadoInteractivo`
   - Esperar a que VS Code cargue el proyecto (puede tardar un poco la primera vez)

4. **Ejecutar el programa**:
   - Abrir el archivo `src/com/ahorcado/Main.java`
   - Hacer clic en el boton **"Run"** que aparece arriba del metodo `main` (es un triangulito verde que dice "Run" o "Run Java")
   - O tambien se puede hacer clic derecho en el archivo y seleccionar **"Run Java"**
   - El juego deberia abrirse en una ventana nueva

### Opcion 2: Desde la Terminal / Linea de Comandos

Si no quieres usar VS Code, puedes compilar y ejecutar desde cualquier terminal:

```bash
# 1. Ir a la carpeta del proyecto
cd ProyectoAhorcadoInteractivo

# 2. Compilar todos los archivos Java
javac -d out src/com/ahorcado/*.java src/com/ahorcado/util/*.java

# 3. Ejecutar el programa
java -cp out com.ahorcado.Main
```

### Opcion 3: Desde NetBeans o Eclipse

1. Importar como proyecto Java existente
2. Configurar `src` como carpeta de fuentes
3. Ejecutar la clase `com.ahorcado.Main`

## Requisitos

- **Java 11** o superior (JDK, no solo JRE)
- **Windows** recomendado (el sistema de voz usa PowerShell, pero en otros SO el juego funciona sin voz)
- Los archivos de sonido `.wav` se generan automaticamente si no existen

## Notas

- El punto de entrada es la clase `Main.java` con el metodo `main()`
- Se usa `SwingUtilities.invokeLater()` para arrancar la GUI en el hilo correcto (EDT)
- Los datos del jugador se guardan en archivos .txt en la carpeta raiz del proyecto (stats.txt, economia.txt, etc)
- Si se borran esos archivos .txt simplemente se reinician las estadisticas


