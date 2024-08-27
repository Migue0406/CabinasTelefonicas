import kotlin.math.roundToInt
import kotlin.random.Random

data class Llamada(val tipo: TipoLlamada, val duracion: Int)

enum class TipoLlamada(val tarifa: Int) {
    LOCAL(50),
    LARGA_DISTANCIA(350),
    CELULAR(150)
}

class CabinaTelefonica(val numero: Int) {
    private val llamadas = mutableListOf<Llamada>()
    private var llamadasHistorial = mutableListOf<Llamada>()

    fun registrarLlamada(tipo: TipoLlamada, duracion: Int) {
        if (duracion <= 0) throw IllegalArgumentException("La duración debe ser mayor que cero")
        llamadas.add(Llamada(tipo, duracion))
        llamadasHistorial.add(Llamada(tipo, duracion))
    }

    fun obtenerInformacion(): String {
        return "Cabina $numero: ${numeroLlamadas()} llamadas, ${duracionTotal()} minutos, ${costoTotal()} pesos"
    }

    fun reiniciar() {
        llamadas.clear()
    }

    fun numeroLlamadas() = llamadas.size
    fun duracionTotal() = llamadas.sumOf { it.duracion }
    fun costoTotal() = llamadas.sumOf { it.tipo.tarifa * it.duracion }

    fun obtenerHistorialLlamadas(): String {
        val llamadasPorTipo = llamadasHistorial.groupingBy { it.tipo }.eachCount()
        val historialInfo = llamadasPorTipo.entries.joinToString(", ") { "${it.key}: ${it.value} llamadas" }
        return "Historial de llamadas - $historialInfo"
    }

    fun estaOcupada(): Boolean = llamadas.isNotEmpty()

    fun obtenerLlamadasPorTipo(): Map<TipoLlamada, Int> {
        return llamadas.groupingBy { it.tipo }.eachCount()
    }
}

class ControlGastosTelefonicos {
    private val cabinas = mutableListOf<CabinaTelefonica>()

    fun agregarCabina(cabina: CabinaTelefonica) {
        if (cabinas.none { it.numero == cabina.numero }) {
            cabinas.add(cabina)
        }
    }

    fun registrarLlamada(numeroCabina: Int, tipo: TipoLlamada) {
        val cabina = cabinas.find { it.numero == numeroCabina }
            ?: throw IllegalArgumentException("Cabina no encontrada")

        if (cabina.estaOcupada()) {
            println("La cabina $numeroCabina está ocupada.")
            println("No se puede registrar una nueva llamada en esta cabina.")
            return
        }

        val duracion = Random.nextInt(1, 11)
        cabina.registrarLlamada(tipo, duracion)
    }

    fun obtenerInformacionCabina(numeroCabina: Int): String {
        val cabina = cabinas.find { it.numero == numeroCabina }
            ?: throw IllegalArgumentException("Cabina no encontrada")
        return "${cabina.obtenerInformacion()}\n${cabina.obtenerHistorialLlamadas()}"
    }

    fun obtenerConsolidadoTotal(): String {
        val costoTotal = cabinas.sumOf { it.costoTotal() }
        val totalLlamadas = cabinas.sumOf { it.numeroLlamadas() }
        val duracionTotal = cabinas.sumOf { it.duracionTotal() }
        val costoPromedio = if (duracionTotal > 0) (costoTotal.toDouble() / duracionTotal).roundToInt() else 0

        val llamadasPorTipo = cabinas.flatMap { it.obtenerLlamadasPorTipo().entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.sum() }

        val llamadasInfo = llamadasPorTipo.entries.joinToString(", ") { "${it.key}: ${it.value} llamadas" }

        return "Total: $costoTotal pesos, $totalLlamadas llamadas, $duracionTotal minutos, Promedio: $costoPromedio pesos/min, $llamadasInfo"
    }

    fun reiniciarCabina(numeroCabina: Int) {
        val cabina = cabinas.find { it.numero == numeroCabina }
            ?: throw IllegalArgumentException("Cabina no encontrada")
        cabina.reiniciar()
    }

    fun cabinasDisponibles(): List<CabinaTelefonica> {
        return cabinas.filter { !it.estaOcupada() }
    }

    fun cabinaPorNumero(numero: Int): CabinaTelefonica? {
        return cabinas.find { it.numero == numero }
    }

    fun crearCabina(numero: Int) {
        if (cabinas.none { it.numero == numero }) {
            cabinas.add(CabinaTelefonica(numero))
        }
    }
}

fun main() {
    val control = ControlGastosTelefonicos()

    // Inicializar al menos una cabina
    control.crearCabina(1)

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
                    print("Número de cabina: ")
                    val numeroCabina = readLine()?.toIntOrNull() ?: throw IllegalArgumentException("Número de cabina inválido")

                    val cabinaSeleccionada = control.cabinaPorNumero(numeroCabina)
                    if (cabinaSeleccionada == null) {
                        println("La cabina $numeroCabina no existe. Creando cabina...")
                        control.crearCabina(numeroCabina)
                    }

                    val cabina = control.cabinaPorNumero(numeroCabina) ?: throw IllegalArgumentException("Cabina no encontrada")

                    if (cabina.estaOcupada()) {
                        println("La cabina $numeroCabina está ocupada.")
                        println("No se puede registrar una nueva llamada en esta cabina.")
                    } else {
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
                    print("Número de cabina a reiniciar: ")
                    val numeroCabina = readLine()?.toIntOrNull() ?: throw IllegalArgumentException("Número de cabina inválido")
                    control.reiniciarCabina(numeroCabina)
                    println("Cabina reiniciada exitosamente")
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
            "5" -> break
            else -> println("Opción inválida")
        }
    }
}
