package io.github.giova.comidanatural

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DayType(val label: String) {
    NATURAL("Comida natural"),
    RACAO("Ração"),
    JEJUM("Jejum"),
}

/**
 * [weightKg] é o peso ideal do adulto, ou o peso atual do filhote; 0 quando não informado.
 * [adultSize] só importa para filhotes: a tabela deles depende do porte que terão adultos.
 */
data class Dog(
    val name: String,
    val kgPerDay: Double,
    val weightKg: Double = 0.0,
    val birth: LocalDate? = null,
    val adultSize: AdultSize = AdultSize.MEDIO,
    val neutered: Boolean = false,
) {
    fun ageMonths(today: LocalDate = LocalDate.now()): Int? =
        birth?.let { ChronoUnit.MONTHS.between(it, today).toInt().coerceAtLeast(0) }

    fun isPuppy(today: LocalDate = LocalDate.now()): Boolean =
        ageMonths(today)?.let { it < adultSize.adultMonths } ?: false
}

/**
 * Porte que o filhote terá adulto e com quantos meses ele termina de crescer:
 * pequenos por volta dos 12 meses, médios e grandes aos 18, gigantes aos 24.
 */
enum class AdultSize(val label: String, val range: String, val adultMonths: Int) {
    PEQUENO("Pequeno", "até 10 kg", 12),
    MEDIO("Médio", "10 a 25 kg", 18),
    GRANDE("Grande", "25 a 35 kg", 18),
    GIGANTE("Gigante", "mais de 35 kg", 24),
}

/** Faixa de % do peso atual para filhotes, por idade e porte adulto (Cachorro Verde). */
fun puppyPct(months: Int, size: AdultSize): Pair<Double, Double>? {
    if (months < 2 || months >= size.adultMonths) return null
    val big = size == AdultSize.GRANDE || size == AdultSize.GIGANTE
    return when {
        months < 4 -> if (big) 8.0 to 8.0 else 10.0 to 10.0
        months < 6 -> if (big) 7.0 to 7.0 else 8.0 to 8.0
        months < 8 -> if (size == AdultSize.GIGANTE) 6.0 to 6.0 else 6.0 to 7.0
        months < 10 -> if (size == AdultSize.GIGANTE) 5.0 to 5.0 else 5.0 to 6.0
        size == AdultSize.GIGANTE ->
            // O texto dá 4-5% de 10 a 14 meses e 4% de 18 a 24; de 14 a 18 mantém 4-5%.
            if (months < 18) 4.0 to 5.0 else 4.0 to 4.0
        size == AdultSize.GRANDE -> 4.0 to 5.0
        else -> 4.0 to 6.0
    }
}

/** Refeições por dia, pela idade (Cachorro Verde). */
fun mealsPerDay(months: Int?): String = when {
    months == null || months >= 6 -> "2 refeições por dia"
    months < 4 -> "3 a 4 refeições por dia"
    else -> "3 refeições por dia"
}

/** Idade em que começa a fase sênior, pelo porte (Cachorro Verde). */
fun seniorYears(porte: Porte): Int = when (porte.name) {
    "gigante" -> 5
    "grande" -> 7
    "médio" -> 8
    else -> 9
}

/**
 * Fases da vida adulta. O texto dá o exemplo de um beagle: 5% com 12 meses (jovem adulto),
 * 4,5% com 2 anos (adulto), 3,5% com 5 anos (meia-idade) e 4 a 4,5% aos 12 anos (idoso).
 * Daí: jovem adulto no teto da faixa, adulto no meio, meia-idade no piso e idoso de volta ao meio.
 */
enum class LifeStage(val label: String) {
    JOVEM("Jovem adulta"),
    ADULTO("Adulta"),
    MEIA_IDADE("Meia-idade"),
    IDOSO("Idosa"),
}

fun lifeStage(months: Int, porte: Porte): LifeStage {
    val senior = seniorYears(porte) * 12
    return when {
        months >= senior -> LifeStage.IDOSO
        months >= 5 * 12 -> LifeStage.MEIA_IDADE
        months < 2 * 12 -> LifeStage.JOVEM
        else -> LifeStage.ADULTO
    }
}

/** [pct] é o ponto de partida dentro da faixa; ajuste depois conforme a silhueta. */
data class Suggestion(
    val title: String,
    val minPct: Double?,
    val maxPct: Double?,
    val pct: Double?,
    val note: String?,
)

/** Porção sugerida pelo peso e pela idade; null enquanto o peso não foi informado. */
fun suggestion(dog: Dog, today: LocalDate = LocalDate.now()): Suggestion? {
    if (dog.weightKg <= 0) return null
    val months = dog.ageMonths(today)
    if (months != null && months < 2) {
        return Suggestion(
            "Filhote com menos de 2 meses", null, null, null,
            "A tabela começa em 2 meses; siga a orientação do veterinário.",
        )
    }
    if (months != null && dog.isPuppy(today)) {
        val (min, max) = puppyPct(months, dog.adultSize)!!
        return Suggestion(
            "Filhote de $months meses, porte adulto ${dog.adultSize.label.lowercase()}",
            min, max, (min + max) / 2,
            "Calculado sobre o peso atual, não o de adulta; recalcule todo mês. " +
                "Filhotes devem crescer esbeltos: cintura visível e costelas fáceis de sentir.",
        )
    }
    val base = porte(dog.weightKg) ?: return null
    // Castradas tendem a engordar: o texto sugere em geral 0,5% a menos.
    val p = if (dog.neutered) base.copy(minPct = base.minPct - 0.5, maxPct = base.maxPct - 0.5) else base
    val castrada = if (dog.neutered) " castrada" else ""
    if (months == null) {
        return Suggestion(
            "Adulta$castrada, porte ${p.name}", p.minPct, p.maxPct, null,
            "Informe o nascimento para ajustar pela idade.",
        )
    }
    val mid = (p.minPct + p.maxPct) / 2
    val stage = lifeStage(months, p)
    val (pct, note) = when (stage) {
        LifeStage.JOVEM -> p.maxPct to
            "Jovens adultas têm o metabolismo mais ativo: comece pelo teto da faixa."
        LifeStage.ADULTO -> mid to
            "Adulta: comece pelo meio da faixa."
        LifeStage.MEIA_IDADE -> p.minPct to
            "Na meia-idade o metabolismo desacelera: comece pelo piso da faixa."
        LifeStage.IDOSO -> mid to
            "Fase sênior (a partir de ${seniorYears(p)} anos no porte ${p.name}). Idosas podem precisar " +
            "de mais comida que na meia-idade, porque aproveitam pior os nutrientes, principalmente a proteína."
    }
    val fullNote = if (dog.neutered) "$note Por ser castrada, a faixa já está 0,5% abaixo." else note
    return Suggestion("${stage.label}$castrada, porte ${p.name}", p.minPct, p.maxPct, pct, fullNote)
}

/** Porte e faixa de comida diária (% do peso ideal), da tabela do Cachorro Verde para adultos. */
data class Porte(val name: String, val minPct: Double, val maxPct: Double)

fun porte(weightKg: Double): Porte? = when {
    weightKg <= 0 -> null
    weightKg <= 3 -> Porte("miniatura", 7.0, 10.0)
    weightKg <= 5 -> Porte("miniatura", 5.0, 6.0)
    weightKg <= 10 -> Porte("pequeno", 4.0, 6.0)
    weightKg <= 25 -> Porte("médio", 4.0, 5.0)
    weightKg <= 35 -> Porte("grande", 4.0, 5.0)
    weightKg <= 42 -> Porte("grande", 3.0, 4.0)
    else -> Porte("gigante", 3.0, 4.0)
}

/** Dose de manutenção de óleo de peixe, pelo peso (Cachorro Verde). */
fun fishOilDose(weightKg: Double): String? = when {
    weightKg <= 0 -> null
    weightKg <= 5 -> "1 cápsula de 500 mg"
    weightKg <= 20 -> "1 cápsula de 1 g"
    else -> "2 cápsulas de 2 g"
}?.let { "$it, todo dia ou 3 vezes por semana" }

/** Dose de óleo vegetal (azeite, coco), pelo peso (Cachorro Verde). */
fun vegetableOilDose(weightKg: Double): String? = when {
    weightKg <= 0 -> null
    weightKg <= 2 -> "½ colher de chá em 1 refeição"
    weightKg <= 7 -> "½ colher de chá no almoço e ½ no jantar"
    weightKg <= 15 -> "1 colher de sobremesa em 1 refeição"
    weightKg <= 25 -> "1 colher de sopa em 1 refeição"
    else -> "1 colher de sopa no almoço e 1 no jantar"
}

data class Meat(val name: String, val pricePerKg: Double)

data class Config(
    val dogs: List<Dog> = listOf(
        Dog("Mari", 0.4, birth = LocalDate.of(2015, 4, 1)),
        Dog("Poranga", 0.8, birth = LocalDate.of(2021, 3, 1)),
    ),
    val pctCarne: Double = 30.0,
    val pctVisceras: Double = 5.0,
    val pctVegetais: Double = 30.0,
    // Referência: site Cachorro Verde.
    val pctCarbo: Double = 35.0,
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
