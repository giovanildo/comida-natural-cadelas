package io.github.giova.comidanatural

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DayType(val label: String) {
    NATURAL("Comida natural"),
    RACAO("Ração"),
    JEJUM("Jejum"),
}

data class Dog(val name: String, val kgPerDay: Double)

data class Meat(val name: String, val pricePerKg: Double)

data class Config(
    val dogs: List<Dog> = listOf(Dog("Mari", 0.4), Dog("Poranga", 0.8)),
    val pctCarne: Double = 30.0,
    val pctVisceras: Double = 5.0,
    val pctVegetais: Double = 30.0,
    val pctCarbo: Double = 30.0,
    val priceCarbo: Double = 3.0,
    val priceVegetais: Double = 2.0,
    val priceVisceras: Double = 10.0,
    val meats: List<Meat> = listOf(
        Meat("Peixe", 10.0),
        Meat("Bovino", 25.0),
        Meat("Porco", 20.0),
        Meat("Frango", 10.0),
    ),
    // Arroz cozido rende 2,5x o peso do arroz cru.
    val riceFactor: Double = 2.5,
    val racaoEvery: Int = 5,
    val jejumEvery: Int = 15,
    // Dia 1 do ciclo: a ração cai nos dias 5, 10... e o jejum no dia 15.
    val cycleStart: LocalDate = LocalDate.now(),
) {
    val dailyKg: Double get() = dogs.sumOf { it.kgPerDay }

    // Como na planilha: preço da carne é a média simples dos tipos.
    val meatPrice: Double get() = if (meats.isEmpty()) 0.0 else meats.map { it.pricePerKg }.average()

    val pctSum: Double get() = pctCarne + pctVisceras + pctVegetais + pctCarbo

    val ingredients: List<Ingredient>
        get() = listOf(
            Ingredient("Carne", pctCarne, meatPrice),
            Ingredient("Vísceras", pctVisceras, priceVisceras),
            Ingredient("Vegetais", pctVegetais, priceVegetais),
            Ingredient("Carboidrato", pctCarbo, priceCarbo),
        )

    /** Fração do total que cabe a cada ingrediente, sempre somando 1. */
    fun share(i: Ingredient): Double = if (pctSum <= 0) 0.0 else i.pct / pctSum

    fun dayType(date: LocalDate): DayType {
        val n = ChronoUnit.DAYS.between(cycleStart, date) + 1
        if (jejumEvery > 0 && Math.floorMod(n, jejumEvery.toLong()) == 0L) return DayType.JEJUM
        if (racaoEvery > 0 && Math.floorMod(n, racaoEvery.toLong()) == 0L) return DayType.RACAO
        return DayType.NATURAL
    }

    fun next(type: DayType, from: LocalDate): LocalDate? =
        generateSequence(from) { it.plusDays(1) }.take(3660).firstOrNull { dayType(it) == type }

    fun batchByDays(start: LocalDate, days: Int): Batch {
        val types = (0 until days.coerceIn(0, 3650)).map { dayType(start.plusDays(it.toLong())) }
        val natural = types.count { it == DayType.NATURAL }
        return Batch(
            start = start,
            end = start.plusDays((days - 1).coerceAtLeast(0).toLong()),
            calendarDays = types.size,
            naturalDays = natural,
            racaoDays = types.count { it == DayType.RACAO },
            jejumDays = types.count { it == DayType.JEJUM },
            totalKg = natural * dailyKg,
            leftoverKg = 0.0,
        )
    }

    /** Anda no calendário consumindo a comida só nos dias de comida natural. */
    fun batchByKg(start: LocalDate, kg: Double): Batch {
        var remaining = kg
        var d = start
        var last = start.minusDays(1)
        var cal = 0
        var nat = 0
        var rac = 0
        var jej = 0
        while (dailyKg > 0 && remaining + 1e-9 >= dailyKg && cal < 3650) {
            when (dayType(d)) {
                DayType.NATURAL -> { nat++; remaining -= dailyKg }
                DayType.RACAO -> rac++
                DayType.JEJUM -> jej++
            }
            last = d
            d = d.plusDays(1)
            cal++
        }
        return Batch(start, last, cal, nat, rac, jej, kg, remaining.coerceAtLeast(0.0))
    }
}

data class Ingredient(val name: String, val pct: Double, val pricePerKg: Double)

data class Batch(
    val start: LocalDate,
    val end: LocalDate,
    val calendarDays: Int,
    val naturalDays: Int,
    val racaoDays: Int,
    val jejumDays: Int,
    val totalKg: Double,
    /** Sobra que não completa mais um dia (só no modo por kg). */
    val leftoverKg: Double,
)
