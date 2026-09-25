package io.github.giova.comidanatural

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val BR: Locale = Locale.forLanguageTag("pt-BR")
private val money: NumberFormat = NumberFormat.getCurrencyInstance(BR)
private val dayFmt = DateTimeFormatter.ofPattern("EEE, dd/MM", BR)
private val shortFmt = DateTimeFormatter.ofPattern("dd/MM", BR)

private fun num(v: Double, decimals: Int = 2): String =
    NumberFormat.getNumberInstance(BR).apply {
        maximumFractionDigits = decimals
        minimumFractionDigits = 0
    }.format(v)

private fun kg(v: Double) = "${num(v, 2)} kg"
private fun g(vKg: Double) = "${num(vKg * 1000, 0)} g"
private fun parse(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

private fun typeColor(t: DayType): Color = when (t) {
    DayType.NATURAL -> Color(0xFF4E9A5B)
    DayType.RACAO -> Color(0xFFE0A030)
    DayType.JEJUM -> Color(0xFFC8504A)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { App() } }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme(primary = Color(0xFF8FD19B))
        else -> lightColorScheme(primary = Color(0xFF3E7C4A))
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun App() {
    val ctx = LocalContext.current
    var cfg by remember { mutableStateOf(Storage.load(ctx)) }
    val update: (Config) -> Unit = { cfg = it; Storage.save(ctx, it) }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Default.Kitchen, null) }, label = { Text("Preparo") })
                NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("Calendário") })
                NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Default.Lightbulb, null) }, label = { Text("Dicas") })
                NavigationBarItem(tab == 3, { tab = 3 }, { Icon(Icons.Default.Settings, null) }, label = { Text("Ajustes") })
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> PrepScreen(cfg)
                1 -> CalendarScreen(cfg)
                2 -> TipsScreen()
                else -> SettingsScreen(cfg, update)
            }
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun TableRow(vararg cells: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        cells.forEachIndexed { i, c ->
            Text(
                c,
                Modifier.weight(if (i == 0) 1.6f else 1f),
                textAlign = if (i == 0) TextAlign.Start else TextAlign.End,
                fontWeight = if (bold) FontWeight.SemiBold else null,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun TodayBanner(cfg: Config) {
    val today = LocalDate.now()
    val t = cfg.dayType(today)
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = typeColor(t).copy(alpha = 0.18f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Hoje, ${today.format(dayFmt)}", style = MaterialTheme.typography.labelLarge)
            Text(t.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val nr = cfg.next(DayType.RACAO, today.plusDays(1))
            val nj = cfg.next(DayType.JEJUM, today.plusDays(1))
            Text(
                "Próxima ração: ${nr?.format(dayFmt) ?: "-"}  ·  Próximo jejum: ${nj?.format(dayFmt) ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate, onDate: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, Modifier.fillMaxWidth()) {
        Text("$label: ${date.format(dayFmt)}")
    }
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let {
                        onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton({ open = false }) { Text("Cancelar") } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Double,
    onValue: (Double) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String = "",
) {
    var text by remember { mutableStateOf(num(value, 3)) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; parse(it)?.takeIf { v -> v >= 0 }?.let(onValue) },
        label = { Text(label) },
        suffix = if (suffix.isEmpty()) null else ({ Text(suffix) }),
        isError = parse(text)?.takeIf { it >= 0 } == null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

// ---------------------------------------------------------------- Preparo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepScreen(cfg: Config) {
    var byKg by rememberSaveable { mutableStateOf(false) }
    var startEpoch by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var days by rememberSaveable { mutableStateOf(15.0) }
    var kgIn by rememberSaveable { mutableStateOf(4.0) }
    val start = LocalDate.ofEpochDay(startEpoch)

    val batch = if (byKg) cfg.batchByKg(start, kgIn) else cfg.batchByDays(start, days.toInt())

    ScreenColumn {
        TodayBanner(cfg)

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(!byKg, { byKg = false }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("Por dias") }
            SegmentedButton(byKg, { byKg = true }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("Por kg") }
        }

        DateField("Começa em", start) { startEpoch = it.toEpochDay() }

        if (byKg) {
            NumberField("Quanto vou preparar", kgIn, { kgIn = it }, Modifier.fillMaxWidth(), "kg")
        } else {
            NumberField("Por quantos dias", days, { days = it }, Modifier.fillMaxWidth(), "dias")
        }

        Section(if (byKg) "Quanto tempo dura" else "Quanto preparar") {
            if (byKg) {
                if (batch.naturalDays == 0) {
                    Text("Não dá nem um dia (${kg(cfg.dailyKg)} por dia).")
                } else {
                    Text(
                        "Dura até ${batch.end.format(dayFmt)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Text(kg(batch.totalKg), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("de ${batch.start.format(shortFmt)} a ${batch.end.format(shortFmt)}")
            }
            Text(
                "${batch.calendarDays} dias no calendário: ${batch.naturalDays} de comida natural, " +
                    "${batch.racaoDays} de ração, ${batch.jejumDays} de jejum.",
            )
            if (byKg && batch.leftoverKg > 0.001) {
                Text(
                    "Sobram ${kg(batch.leftoverKg)}, que não completam mais um dia.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                "Consumo: ${kg(cfg.dailyKg)} por dia de comida natural.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        IngredientsSection(cfg, batch.totalKg)
        DogsSection(cfg, batch)
        RiceSection(cfg, batch.totalKg)
        StorageSection(batch)
    }
}

@Composable
private fun IngredientsSection(cfg: Config, totalKg: Double) {
    Section("Ingredientes") {
        TableRow("", "%", "Peso", "Custo", bold = true)
        HorizontalDivider()
        var cost = 0.0
        cfg.ingredients.forEach { i ->
            val w = totalKg * cfg.share(i)
            cost += w * i.pricePerKg
            TableRow(i.name, "${num(cfg.share(i) * 100, 1)}%", kg(w), money.format(w * i.pricePerKg))
        }
        HorizontalDivider()
        TableRow("Total", "", kg(totalKg), money.format(cost), bold = true)
        if (totalKg > 0) {
            Text("${money.format(cost / totalKg)} por kg", style = MaterialTheme.typography.bodySmall)
        }
        if (kotlin.math.abs(cfg.pctSum - 100) > 0.01) {
            Text(
                "As proporções somam ${num(cfg.pctSum, 1)}%, então cada uma foi ajustada " +
                    "para o total fechar em 100%. Dá para mudar em Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Text(
            "Carne: média de ${money.format(cfg.meatPrice)}/kg (" +
                cfg.meats.joinToString { "${it.name} ${money.format(it.pricePerKg)}" } + ")",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** Compara a porção diária com a faixa sugerida para o peso ideal. */
@Composable
private fun PortionHint(dog: Dog) {
    val p = porte(dog.weightKg)
    if (p == null) {
        Text(
            "Informe o peso ideal para ver a porção sugerida e as doses de óleo.",
            style = MaterialTheme.typography.bodySmall,
        )
        return
    }
    val min = dog.weightKg * p.minPct / 100
    val max = dog.weightKg * p.maxPct / 100
    val inside = dog.kgPerDay in (min - 1e-9)..(max + 1e-9)
    Text(
        "Porte ${p.name}: ${num(p.minPct, 0)} a ${num(p.maxPct, 0)}% do peso = ${g(min)} a ${g(max)} por dia. " +
            if (inside) "A porção atual está dentro da faixa." else "A porção atual (${g(dog.kgPerDay)}) está fora da faixa.",
        style = MaterialTheme.typography.bodySmall,
        color = if (inside) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun DogsSection(cfg: Config, batch: Batch) {
    val daily = cfg.dailyKg
    cfg.dogs.forEach { dog ->
        val dogShare = if (daily > 0) dog.kgPerDay / daily else 0.0
        Section(dog.name) {
            Text(
                "Porção diária: ${g(dog.kgPerDay)}  ·  no lote: ${kg(batch.totalKg * dogShare)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            PortionHint(dog)
            TableRow("", "Por dia", "No lote", bold = true)
            HorizontalDivider()
            cfg.ingredients.forEach { i ->
                TableRow(i.name, g(dog.kgPerDay * cfg.share(i)), kg(batch.totalKg * dogShare * cfg.share(i)))
            }
            val fish = fishOilDose(dog.weightKg)
            val veg = vegetableOilDose(dog.weightKg)
            if (fish != null && veg != null) {
                HorizontalDivider()
                Text("Óleos (pelo peso de ${kg(dog.weightKg)})", style = MaterialTheme.typography.labelLarge)
                Text("Óleo de peixe: $fish", style = MaterialTheme.typography.bodyMedium)
                Text("Óleo vegetal (azeite, coco): $veg", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StorageSection(batch: Batch) {
    Section("Conservação") {
        Text("Geladeira: comida cozida até 3 dias, em pote tampado; crua até 2 dias.", style = MaterialTheme.typography.bodyMedium)
        Text("Congelador: o ideal é usar em 30 a 45 dias.", style = MaterialTheme.typography.bodyMedium)
        Text("Descongele na parte de baixo da geladeira: leva de 12 a 36 horas.", style = MaterialTheme.typography.bodyMedium)
        if (batch.naturalDays > 3) {
            Text(
                "Este lote dá ${batch.naturalDays} dias de comida: deixe até 3 dias na geladeira e congele o resto.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

// ---------------------------------------------------------------- Dicas

@Composable
private fun Bullets(vararg items: String) {
    items.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
}

@Composable
fun TipsScreen() {
    ScreenColumn {
        Section("Conservação") {
            Bullets(
                "Geladeira: comida cozida dura até 3 dias, em pote tampado; crua, até 2 dias. " +
                    "Depois disso começa a estragar, e nem sempre dá para perceber.",
                "Congelador: o ideal é usar em 30 a 45 dias.",
                "Descongele na parte de baixo da geladeira (12 a 36 horas). " +
                    "Para aquecer, no máximo em banho-maria, para preservar as vitaminas.",
            )
        }
        Section("Preparo") {
            Bullets(
                "Não use panela de pressão: perde nutrientes demais.",
                "Não lave as carnes: isso leva as partículas da superfície para dentro.",
                "Não cozinhe demais: deixe a carne ao ponto, com o miolo rosado.",
                "Cozinhe os vegetais no mesmo caldo em que cozinhou a carne, para aproveitar os minerais.",
                "Carboidratos sempre bem cozidos: cru, o cão não digere.",
                "Deixar os grãos de molho por 6 a 8 horas antes de cozinhar reduz os antinutrientes.",
            )
        }
        Section("Alimentos tóxicos: nunca dar") {
            Bullets(
                "Cebola: causa anemia grave.",
                "Uva e uva-passa: prejudicam os rins.",
                "Carambola: prejudica os rins.",
                "Chocolate: a teobromina intoxica (taquicardia, vômito, diarreia).",
                "Xilitol (adoçante): hipoglicemia e convulsões; pode matar.",
                "Macadâmia: fraqueza, tremores, vômito.",
                "Abacate: a polpa não é tóxica, mas a casca e o caroço são.",
            )
        }
        Section("Evitar") {
            Bullets(
                "Milho e trigo: associados a alergias.",
                "Soja: proteína inadequada para carnívoros, ligada a alergias e desequilíbrio hormonal.",
            )
        }
        Section("Quantidade por dia (adultos)") {
            TableRow("Peso ideal", "% do peso", bold = true)
            HorizontalDivider()
            TableRow("Até 3 kg", "7 a 10%")
            TableRow("3 a 5 kg", "5 a 6%")
            TableRow("5 a 10 kg", "4 a 6%")
            TableRow("10 a 25 kg", "4 a 5%")
            TableRow("25 a 35 kg", "4 a 5%")
            TableRow("35 a 42 kg", "3 a 4%")
            TableRow("Mais de 42 kg", "3 a 4%")
            Text(
                "Exemplo: 12 kg × 4% = 480 g por dia. Varia com a atividade e o metabolismo: " +
                    "acompanhe o peso e ajuste.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Section("Óleos") {
            Text("Óleo de peixe (todo dia ou 3 vezes por semana)", style = MaterialTheme.typography.labelLarge)
            TableRow("Até 5 kg", "1 cápsula de 500 mg")
            TableRow("5 a 20 kg", "1 cápsula de 1 g")
            TableRow("Mais de 20 kg", "2 cápsulas de 2 g")
            Text("Óleo vegetal (azeite, coco)", style = MaterialTheme.typography.labelLarge)
            TableRow("Até 2 kg", "½ colher de chá, 1 vez")
            TableRow("3 a 7 kg", "½ colher de chá, 2 vezes")
            TableRow("8 a 15 kg", "1 colher de sobremesa, 1 vez")
            TableRow("15 a 25 kg", "1 colher de sopa, 1 vez")
            TableRow("25 kg ou mais", "1 colher de sopa, 2 vezes")
        }
        Text(
            "Fonte: cachorroverde.com.br (dieta cozida para cães, conservação e alimentos tóxicos). " +
                "Não substitui a orientação de um veterinário.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun RiceSection(cfg: Config, totalKg: Double) {
    val carbo = cfg.ingredients.first { it.name == "Carboidrato" }
    val cooked = totalKg * cfg.share(carbo)
    val raw = if (cfg.riceFactor > 0) cooked / cfg.riceFactor else 0.0
    Section("Arroz") {
        TableRow("Peso final (cozido)", g(cooked))
        TableRow("Arroz cru", g(raw), bold = true)
        TableRow("Água", "${num((cooked - raw) * 1000, 0)} ml", bold = true)
        Text(
            "O arroz cozido pesa ${num(cfg.riceFactor, 2)}x o cru.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

// ---------------------------------------------------------------- Calendário

@Composable
fun CalendarScreen(cfg: Config) {
    val today = LocalDate.now()
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val month = YearMonth.from(today).plusMonths(monthOffset.toLong())

    ScreenColumn {
        TodayBanner(cfg)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ monthOffset-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior") }
                    Text(
                        month.month.getDisplayName(TextStyle.FULL, BR).replaceFirstChar { it.uppercase() } +
                            " ${month.year}",
                        Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton({ monthOffset++ }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês") }
                }
                Row {
                    listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                        Text(
                            it, Modifier.weight(1f), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                // getValue % 7: domingo vira 0, segunda 1...
                val lead = month.atDay(1).dayOfWeek.value % 7
                val cells = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
                cells.chunked(7).forEach { week ->
                    Row {
                        week.forEach { date ->
                            if (date == null) Spacer(Modifier.weight(1f))
                            else DayCell(date, cfg, today, Modifier.weight(1f))
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DayType.entries.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(typeColor(t), CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(t.label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        val all = (1..month.lengthOfMonth()).map { cfg.dayType(month.atDay(it)) }
        Text(
            "Neste mês: ${all.count { it == DayType.NATURAL }} dias de comida natural " +
                "(${kg(all.count { it == DayType.NATURAL } * cfg.dailyKg)}), " +
                "${all.count { it == DayType.RACAO }} de ração, ${all.count { it == DayType.JEJUM }} de jejum.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Ração a cada ${cfg.racaoEvery} dias e jejum a cada ${cfg.jejumEvery}, contando a partir de " +
                "${cfg.cycleStart.format(dayFmt)} (dia 1). Quando os dois caem no mesmo dia, vale o jejum.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DayCell(date: LocalDate, cfg: Config, today: LocalDate, modifier: Modifier) {
    val t = cfg.dayType(date)
    val isToday = date == today
    Box(modifier.aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .background(typeColor(t).copy(alpha = if (t == DayType.NATURAL) 0.25f else 0.85f), CircleShape)
                .then(
                    if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || t != DayType.NATURAL) FontWeight.Bold else null,
                color = if (t == DayType.NATURAL) MaterialTheme.colorScheme.onSurface else Color.White,
            )
        }
    }
}

// ---------------------------------------------------------------- Ajustes

@Composable
fun SettingsScreen(cfg: Config, update: (Config) -> Unit) {
    // Trocar a chave recria os campos de texto com os valores restaurados.
    var resetKey by remember { mutableIntStateOf(0) }
    key(resetKey) {
        ScreenColumn {
            Section("Cadelas") {
                cfg.dogs.forEachIndexed { i, dog ->
                    fun set(f: (Dog) -> Dog) =
                        update(cfg.copy(dogs = cfg.dogs.toMutableList().also { it[i] = f(it[i]) }))
                    if (i > 0) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    var name by remember { mutableStateOf(dog.name) }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { v -> name = v; set { it.copy(name = v) } },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField("Peso ideal", dog.weightKg, { v -> set { it.copy(weightKg = v) } }, Modifier.weight(1f), "kg")
                        NumberField("Come por dia", dog.kgPerDay, { v -> set { it.copy(kgPerDay = v) } }, Modifier.weight(1f), "kg")
                    }
                    PortionHint(dog)
                }
            }

            Section("Proporções") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Carne", cfg.pctCarne, { update(cfg.copy(pctCarne = it)) }, Modifier.weight(1f), "%")
                    NumberField("Vísceras", cfg.pctVisceras, { update(cfg.copy(pctVisceras = it)) }, Modifier.weight(1f), "%")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Vegetais", cfg.pctVegetais, { update(cfg.copy(pctVegetais = it)) }, Modifier.weight(1f), "%")
                    NumberField("Carboidrato", cfg.pctCarbo, { update(cfg.copy(pctCarbo = it)) }, Modifier.weight(1f), "%")
                }
                Text(
                    "Soma: ${num(cfg.pctSum, 1)}%",
                    color = if (kotlin.math.abs(cfg.pctSum - 100) > 0.01) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                )
            }

            Section("Preços por kg") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Carboidrato", cfg.priceCarbo, { update(cfg.copy(priceCarbo = it)) }, Modifier.weight(1f), "R$")
                    NumberField("Vegetais", cfg.priceVegetais, { update(cfg.copy(priceVegetais = it)) }, Modifier.weight(1f), "R$")
                }
                NumberField("Vísceras", cfg.priceVisceras, { update(cfg.copy(priceVisceras = it)) }, Modifier.fillMaxWidth(), "R$")
                Text("Carnes (o app usa a média)", style = MaterialTheme.typography.labelLarge)
                cfg.meats.chunked(2).forEachIndexed { row, pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEachIndexed { col, meat ->
                            val idx = row * 2 + col
                            NumberField(meat.name, meat.pricePerKg, { v ->
                                update(cfg.copy(meats = cfg.meats.toMutableList().also { it[idx] = it[idx].copy(pricePerKg = v) }))
                            }, Modifier.weight(1f), "R$")
                        }
                    }
                }
                Text("Média da carne: ${money.format(cfg.meatPrice)}/kg", style = MaterialTheme.typography.bodySmall)
            }

            Section("Arroz") {
                NumberField("Fator (cozido ÷ cru)", cfg.riceFactor, { update(cfg.copy(riceFactor = it)) }, Modifier.fillMaxWidth(), "x")
            }

            Section("Rotina") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Ração a cada", cfg.racaoEvery.toDouble(), { update(cfg.copy(racaoEvery = it.toInt())) }, Modifier.weight(1f), "dias")
                    NumberField("Jejum a cada", cfg.jejumEvery.toDouble(), { update(cfg.copy(jejumEvery = it.toInt())) }, Modifier.weight(1f), "dias")
                }
                DateField("Dia 1 do ciclo", cfg.cycleStart) { update(cfg.copy(cycleStart = it)) }
                Text(
                    "Com o dia 1 em ${cfg.cycleStart.format(shortFmt)}: " +
                        "ração em ${cfg.cycleStart.plusDays(cfg.racaoEvery - 1L).format(shortFmt)}, " +
                        "jejum em ${cfg.cycleStart.plusDays(cfg.jejumEvery - 1L).format(shortFmt)}.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            OutlinedButton(
                onClick = { update(Config(cycleStart = cfg.cycleStart)); resetKey++ },
                Modifier.fillMaxWidth(),
            ) { Text("Restaurar valores padrão") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
