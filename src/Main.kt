import kotlin.math.roundToInt
import kotlin.random.Random

// Definición de la clase de datos que representa una llamada telefónica.
data class Llamada(val tipo: TipoLlamada, val duracion: Int)

// Enum para representar los tipos de llamada con sus tarifas respectivas.
enum class TipoLlamada(val tarifa: Int) {
    LOCAL(50),                // Tarifa para llamadas locales
    LARGA_DISTANCIA(350),    // Tarifa para llamadas de larga distancia
    CELULAR(150)             // Tarifa para llamadas celulares
}

// Clase que representa una cabina telefónica.
class CabinaTelefonica(val numero: Int) {
    private val llamadas = mutableListOf<Llamada>()  // Lista de llamadas actuales en la cabina
    private var llamadasHistorial = mutableListOf<Llamada>()  // Lista de todas las llamadas registradas en la cabina

    // Método para registrar una llamada en la cabina.
    fun registrarLlamada(tipo: TipoLlamada, duracion: Int) {
        // Validación de duración de llamada
        if (duracion <= 0) throw IllegalArgumentException("La duración debe ser mayor que cero")
        // Añadir la llamada a la lista de llamadas actuales y al historial
        llamadas.add(Llamada(tipo, duracion))
        llamadasHistorial.add(Llamada(tipo, duracion))
    }

    // Método para obtener información sobre la cabina.
    fun obtenerInformacion(): String {
        return "Cabina $numero: ${numeroLlamadas()} llamadas, ${duracionTotal()} minutos, ${costoTotal()} pesos"
    }

    // Método para reiniciar la cabina, eliminando las llamadas actuales.
    fun reiniciar() {
        llamadas.clear()
    }

    // Método para obtener el número total de llamadas registradas en la cabina.
    fun numeroLlamadas() = llamadas.size

    // Método para obtener la duración total de todas las llamadas en minutos.
    fun duracionTotal() = llamadas.sumOf { it.duracion }

    // Método para calcular el costo total de todas las llamadas en pesos.
    fun costoTotal() = llamadas.sumOf { it.tipo.tarifa * it.duracion }

    // Método para obtener el historial de llamadas de la cabina.
    fun obtenerHistorialLlamadas(): String {
        // Agrupar las llamadas por tipo y contar cuántas veces ocurrió cada tipo
        val llamadasPorTipo = llamadasHistorial.groupingBy { it.tipo }.eachCount()
        // Crear una cadena de texto con la información del historial
        val historialInfo = llamadasPorTipo.entries.joinToString(", ") { "${it.key}: ${it.value} llamadas" }
        return "Historial de llamadas - $historialInfo"
    }

    // Método para verificar si la cabina está ocupada.
    fun estaOcupada(): Boolean = llamadas.isNotEmpty()

    // Método para obtener el número de llamadas por tipo.
    fun obtenerLlamadasPorTipo(): Map<TipoLlamada, Int> {
        return llamadas.groupingBy { it.tipo }.eachCount()
    }
}

// Clase para controlar el gasto telefónico y la gestión de cabinas.
class ControlGastosTelefonicos {
    private val cabinas = mutableListOf<CabinaTelefonica>()  // Lista de cabinas telefónicas

    // Método para agregar una cabina a la lista.
    fun agregarCabina(cabina: CabinaTelefonica) {
        if (cabinas.none { it.numero == cabina.numero }) {
            cabinas.add(cabina)
        }
    }

    // Método para registrar una llamada en una cabina específica.
    fun registrarLlamada(numeroCabina: Int, tipo: TipoLlamada) {
        val cabina = cabinas.find { it.numero == numeroCabina }
            ?: throw IllegalArgumentException("Cabina no encontrada")

        // Verificar si la cabina está ocupada antes de registrar una nueva llamada
        if (cabina.estaOcupada()) {
            println("La cabina $numeroCabina está ocupada.")
            println("No se puede registrar una nueva llamada en esta cabina.")
            return
        }

        // Generar una duración aleatoria para la llamada entre 1 y 10 minutos
        val duracion = Random.nextInt(1, 11)
        cabina.registrarLlamada(tipo, duracion)
    }

    // Método para obtener la información de una cabina específica.
    fun obtenerInformacionCabina(numeroCabina: Int): String {
        val cabina = cabinas.find { it.numero == numeroCabina }
            ?: throw IllegalArgumentException("Cabina no encontrada")
        return "${cabina.obtenerInformacion()}\n${cabina.obtenerHistorialLlamadas()}"
    }

    // Método para obtener un consolidado total de todas las cabinas.
    fun obtenerConsolidadoTotal(): String {
        val costoTotal = cabinas.sumOf { it.costoTotal() }
        val totalLlamadas = cabinas.sumOf { it.numeroLlamadas() }
        val duracionTotal = cabinas.sumOf { it.duracionTotal() }
        val costoPromedio = if (duracionTotal > 0) (costoTotal.toDouble() / duracionTotal).roundToInt() else 0

        // Agrupar todas las llamadas por tipo y contar cuántas veces ocurrió cada tipo
        val llamadasPorTipo = cabinas.flatMap { it.obtenerLlamadasPorTipo().entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.sum() }

        // Crear una cadena de texto con la información consolidada
        val llamadasInfo = llamadasPorTipo.entries.joinToString(", ") { "${it.key}: ${it.value} llamadas" }

        return "Total: $costoTotal pesos, $totalLlamadas llamadas, $duracionTotal minutos, Promedio: $costoPromedio pesos/min, $llamadasInfo"
    }

    // Método para reiniciar una cabina específica.
    fun reiniciarCabina(numeroCabina: Int) {
        val cabina = cabinas.find { it.numero == numeroCabina }
            ?: throw IllegalArgumentException("Cabina no encontrada")
        cabina.reiniciar()
    }

    // Método para obtener una lista de cabinas que no están ocupadas.
    fun cabinasDisponibles(): List<CabinaTelefonica> {
        return cabinas.filter { !it.estaOcupada() }
    }

    // Método para obtener una cabina por su número.
    fun cabinaPorNumero(numero: Int): CabinaTelefonica? {
        return cabinas.find { it.numero == numero }
    }

    // Método para crear una nueva cabina si no existe.
    fun crearCabina(numero: Int) {
        if (cabinas.none { it.numero == numero }) {
            cabinas.add(CabinaTelefonica(numero))
        }
    }
}

// Función principal para interactuar con el sistema de gestión de cabinas.
fun main() {
    val control = ControlGastosTelefonicos()

    // Inicializar al menos una cabina
    control.crearCabina(1)

    // Menú de opciones para el usuario
    while (true) {
        println("\n1. Registrar llamada")
        println("2. Ver información de una cabina")
        println("3. Ver consolidado total")
        println("4. Reiniciar una cabina")
        println("5. Salir")
        print("Seleccione una opción: ")

        when (readLine()) {
            "1" -> {
                try {
                    // Solicitar el número de cabina
                    print("Número de cabina: ")
                    val numeroCabina = readLine()?.toIntOrNull() ?: throw IllegalArgumentException("Número de cabina inválido")

                    // Verificar si la cabina existe; si no, crearla
                    val cabinaSeleccionada = control.cabinaPorNumero(numeroCabina)
                    if (cabinaSeleccionada == null) {
                        println("La cabina $numeroCabina no existe. Creando cabina...")
                        control.crearCabina(numeroCabina)
                    }

                    val cabina = control.cabinaPorNumero(numeroCabina) ?: throw IllegalArgumentException("Cabina no encontrada")

                    // Verificar si la cabina está ocupada antes de registrar la llamada
                    if (cabina.estaOcupada()) {
                        println("La cabina $numeroCabina está ocupada.")
                        println("No se puede registrar una nueva llamada en esta cabina.")
                    } else {
                        // Solicitar el tipo de llamada y registrar la llamada
                        print("Tipo de llamada (LOCAL, LARGA_DISTANCIA, CELULAR): ")
                        val tipoLlamada = TipoLlamada.valueOf(readLine()?.toUpperCase() ?: "")

                        control.registrarLlamada(numeroCabina, tipoLlamada)
                        println("Llamada registrada exitosamente")
                    }
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
            "2" -> {
                try {
                    // Solicitar el número de cabina para mostrar su información
                    print("Número de cabina: ")
                    val numeroCabina = readLine()?.toIntOrNull() ?: throw IllegalArgumentException("Número de cabina inválido")
                    println(control.obtenerInformacionCabina(numeroCabina))
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
            "3" -> println(control.obtenerConsolidadoTotal())
            "4" -> {
                try {
                    // Solicitar el número de cabina a reiniciar
                    print("Número de cabina a reiniciar: ")
                    val numeroCabina = readLine()?.toIntOrNull() ?: throw IllegalArgumentException("Número de cabina inválido")
                    control.reiniciarCabina(numeroCabina)
                    println("Cabina reiniciada exitosamente")
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
            "5" -> break  // Salir del bucle y finalizar el programa
            else -> println("Opción inválida")
        }
    }
}

