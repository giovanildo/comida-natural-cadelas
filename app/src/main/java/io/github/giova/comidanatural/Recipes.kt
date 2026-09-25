/*
 * Comida das Cadelas
 * Copyright (C) 2026 Giovanildo
 *
 * Este programa é software livre: você pode redistribuí-lo e/ou modificá-lo
 * sob os termos da GNU General Public License, versão 3, publicada pela Free
 * Software Foundation. Distribuído sem nenhuma garantia. Veja o arquivo
 * LICENSE ou <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */

package io.github.giova.comidanatural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Grupos da dieta, na ordem em que aparecem no prato. */
enum class FoodGroup(val label: String, val color: Color) {
    CARNE("Carne", Color(0xFFC8553D)),
    VISCERAS("Vísceras", Color(0xFF7A2E3A)),
    CARBO("Carboidrato", Color(0xFFE9B949)),
    VEGETAIS("Vegetais", Color(0xFF5C9E4F)),
}

/**
 * Como estimar a compra a partir do peso cozido, pelo texto do Cachorro Verde:
 * carnes e vísceras encolhem (130 g crus dão uns 100 g cozidos), tubérculos e vegetais
 * ficam mais ou menos iguais, e grãos pelo menos dobram.
 */
enum class Cooking { ENCOLHE, IGUAL, GRAO }

/** [grams] é o peso cozido na receita original, de 320 g por dia. */
data class RecipeItem(
    val group: FoodGroup,
    val name: String,
    val emoji: String,
    val grams: Double,
    val cooking: Cooking,
    /** Para itens contados em unidades, como ovos: peso de uma unidade. */
    val unitGrams: Double? = null,
    val unitName: String? = null,
    /** Quantidade fixa por cadela por dia, em vez de proporcional (ovos). */
    val perDogPerDay: Int? = null,
)

data class Recipe(
    val title: String,
    val summary: String,
    val oil: String,
    val gradient: List<Color>,
    val items: List<RecipeItem>,
)

/** A receita original é para 320 g por dia (a Polly, 8 kg, 4% do peso). */
const val RECIPE_BASE_G = 320.0

/** Sugestões de combinação da página de AN cozida para cães do Cachorro Verde. */
val RECIPES = listOf(
    Recipe(
        "Opção 1", "Músculo bovino, fígado, mandioquinha, vagem e cenoura",
        "azeite de oliva extravirgem",
        listOf(Color(0xFFFFE3D3), Color(0xFFFFF4E0)),
        listOf(
            RecipeItem(FoodGroup.CARNE, "Músculo bovino", "🥩", 96.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.VISCERAS, "Fígado bovino", "🐄", 16.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.CARBO, "Mandioquinha", "🥔", 112.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Vagem macarrão", "🫛", 46.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Cenoura", "🥕", 50.0, Cooking.IGUAL),
        ),
    ),
    Recipe(
        "Opção 2", "Peito de frango, rim, arroz integral, quinoa, berinjela e abobrinha",
        "óleo de coco",
        listOf(Color(0xFFFFF1CC), Color(0xFFF1F8E0)),
        listOf(
            RecipeItem(FoodGroup.CARNE, "Peito de frango", "🍗", 96.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.VISCERAS, "Rim bovino", "🐄", 16.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.CARBO, "Arroz integral", "🍚", 62.0, Cooking.GRAO),
            RecipeItem(FoodGroup.CARBO, "Quinoa", "🌾", 50.0, Cooking.GRAO),
            RecipeItem(FoodGroup.VEGETAIS, "Berinjela", "🍆", 46.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Abobrinha", "🥒", 50.0, Cooking.IGUAL),
        ),
    ),
    Recipe(
        "Opção 3", "Sardinha, fígado de frango, mandioquinha, batata-doce, cenoura e chuchu",
        "óleo de linhaça",
        listOf(Color(0xFFDDEFFF), Color(0xFFFFF0DC)),
        listOf(
            RecipeItem(FoodGroup.CARNE, "Filé de sardinha (ou outro peixe)", "🐟", 96.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.VISCERAS, "Fígado de frango", "🐔", 16.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.CARBO, "Mandioquinha", "🥔", 60.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.CARBO, "Batata-doce", "🍠", 52.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Cenoura", "🥕", 46.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Chuchu", "🥬", 50.0, Cooking.IGUAL),
        ),
    ),
    Recipe(
        "Opção 4", "Ovo, lombo suíno, baço, arroz parboilizado, inhame, pimentão e quiabo",
        "azeite de oliva extravirgem",
        listOf(Color(0xFFFFE6E0), Color(0xFFE8F5E4)),
        listOf(
            // O ovo (50 g) substitui 50 g de carne, 1 por cadela por dia; o lombo completa o grupo.
            RecipeItem(FoodGroup.CARNE, "Ovo de galinha cozido", "🥚", 50.0, Cooking.IGUAL, 50.0, "ovo", perDogPerDay = 1),
            RecipeItem(FoodGroup.CARNE, "Lombo suíno", "🐖", 46.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.VISCERAS, "Baço de boi", "🐄", 16.0, Cooking.ENCOLHE),
            RecipeItem(FoodGroup.CARBO, "Arroz parboilizado", "🍚", 60.0, Cooking.GRAO),
            RecipeItem(FoodGroup.CARBO, "Inhame", "🥔", 52.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Pimentão vermelho", "🫑", 46.0, Cooking.IGUAL),
            RecipeItem(FoodGroup.VEGETAIS, "Quiabo", "🌿", 50.0, Cooking.IGUAL),
        ),
    ),
)

private fun FoodGroup.share(cfg: Config): Double {
    val name = when (this) {
        FoodGroup.CARNE -> "Carne"
        FoodGroup.VISCERAS -> "Vísceras"
        FoodGroup.CARBO -> "Carboidrato"
        FoodGroup.VEGETAIS -> "Vegetais"
    }
    return cfg.share(cfg.ingredients.first { it.name == name })
}

/**
 * Peso cozido de cada item para [totalG] gramas de comida. O total de cada grupo segue
 * as proporções dos Ajustes; dentro do grupo, a divisão segue a receita.
 */
fun Recipe.scaled(cfg: Config, totalG: Double): List<Pair<RecipeItem, Double>> {
    val days = if (cfg.dailyKg > 0) totalG / (cfg.dailyKg * 1000) else 0.0
    val fixed = items.filter { it.perDogPerDay != null }
        .associateWith { it.perDogPerDay!! * it.unitGrams!! * cfg.dogs.size * days }
    return items.map { item ->
        fixed[item]?.let { return@map item to it }
        val groupTotal = totalG * item.group.share(cfg)
        val rest = (groupTotal - fixed.filterKeys { it.group == item.group }.values.sum()).coerceAtLeast(0.0)
        val flexSum = items.filter { it.group == item.group && it.perDogPerDay == null }.sumOf { it.grams }
        item to rest * item.grams / flexSum
    }
}

/** Estimativa do peso cru a comprar para [cookedG] gramas cozidas. */
fun RecipeItem.rawGrams(cookedG: Double, riceFactor: Double): Double = when (cooking) {
    Cooking.ENCOLHE -> cookedG * 1.3
    Cooking.IGUAL -> cookedG
    Cooking.GRAO -> if (riceFactor > 0) cookedG / riceFactor else cookedG
}

private fun weight(grams: Double): String = if (grams >= 1000) kg(grams / 1000) else g(grams / 1000)

// ---------------------------------------------------------------- Tela

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(cfg: Config, batch: Batch) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var forBatch by rememberSaveable { mutableStateOf(false) }
    val recipe = RECIPES[selected]
    val totalG = (if (forBatch) batch.totalKg else cfg.dailyKg) * 1000

    ScreenColumn {
        RecipeSourceHeader()

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RECIPES.forEachIndexed { i, r ->
                FilterChip(
                    selected = i == selected,
                    onClick = { selected = i },
                    label = { Text("${r.items.first().emoji} ${r.title}") },
                )
            }
        }

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(!forBatch, { forBatch = false }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("1 dia") }
            SegmentedButton(forBatch, { forBatch = true }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("Lote do Preparo") }
        }
        Text(
            if (forBatch) {
                "Lote de ${kg(batch.totalKg)}: ${batch.naturalDays} dias de comida, " +
                    "de ${batch.start.format(shortFmt)} a ${batch.end.format(shortFmt)}. Mude o lote na aba Preparo."
            } else {
                "Um dia das duas: ${kg(cfg.dailyKg)} (" + cfg.dogs.joinToString(" + ") { "${it.name} ${g(it.kgPerDay)}" } + ")."
            },
            style = MaterialTheme.typography.bodySmall,
        )

        RecipeHero(recipe, cfg)
        RecipeIngredients(recipe, cfg, totalG, forBatch)
        RecipeComplements(recipe, cfg, totalG)

        Section("Variar faz bem") {
            Bullets(
                "Troque de receita pelo menos uma vez por semana: pode ser a cada 3 dias ou até todo dia.",
                "As receitas são exemplos. Dá para trocar os ingredientes por outros do mesmo grupo, " +
                    "mantendo as proporções da dieta.",
                "Os pesos são dos alimentos já cozidos: pese depois de cozinhar.",
            )
        }
    }
}

@Composable
private fun RecipeSourceHeader() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                "Receitas: sugestões de combinação da dieta cozida do Cachorro Verde, " +
                    "multiplicadas para a quantidade das suas cadelas.",
                style = MaterialTheme.typography.bodySmall,
            )
            LinkText("Fonte: ", SOURCE_URL, SOURCE_URL, MaterialTheme.typography.bodySmall)
        }
    }
}

/** Cabeçalho da receita: prato ilustrado sobre um fundo em degradê. */
@Composable
private fun RecipeHero(recipe: Recipe, cfg: Config) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(recipe.gradient)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            PlateArt(recipe, cfg, Modifier.size(150.dp))
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A2E24),
                )
                Text(recipe.summary, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4A3F35))
                FoodGroup.entries.forEach { group ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(group.color, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${group.label} ${num(group.share(cfg) * 100, 0)}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4A3F35),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tigela vista de cima: cada grupo é uma fatia do tamanho da sua proporção,
 * com o emoji do ingrediente principal do grupo.
 */
@Composable
private fun PlateArt(recipe: Recipe, cfg: Config, modifier: Modifier = Modifier) {
    val groups = FoodGroup.entries.map { it to it.share(cfg) }.filter { it.second > 0 }
    BoxWithConstraints(modifier) {
        val sizePx = with(LocalDensity.current) { maxWidth.toPx() }
        Canvas(Modifier.fillMaxSize().shadow(8.dp, CircleShape)) {
            val r = size.minDimension / 2
            val c = Offset(size.width / 2, size.height / 2)
            // Borda da tigela.
            drawCircle(Color(0xFFF7F3EE), r, c)
            drawCircle(Color(0xFFD9CFC3), r, c, style = Stroke(width = r * 0.04f))
            // Fatias de comida.
            val inner = r * 0.82f
            var start = -90f
            groups.forEach { (group, share) ->
                val sweep = (share * 360).toFloat()
                drawArc(
                    color = group.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(c.x - inner, c.y - inner),
                    size = Size(inner * 2, inner * 2),
                )
                // Linha clara entre as fatias.
                drawArc(
                    color = Color(0xFFF7F3EE),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(c.x - inner, c.y - inner),
                    size = Size(inner * 2, inner * 2),
                    style = Stroke(width = r * 0.03f),
                )
                start += sweep
            }
            // Brilho suave, para dar volume.
            drawCircle(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(c.x - inner * 0.35f, c.y - inner * 0.35f),
                    radius = inner,
                ),
                inner, c,
            )
        }
        // Emoji de cada grupo no meio da sua fatia.
        var start = -90.0
        groups.forEach { (group, share) ->
            val sweep = share * 360
            val mid = Math.toRadians(start + sweep / 2)
            start += sweep
            val emoji = recipe.items.firstOrNull { it.group == group }?.emoji ?: return@forEach
            // Fatias finas (vísceras) ganham o emoji mais perto da borda e menor.
            val thin = share < 0.1
            val radius = sizePx / 2 * (if (thin) 0.66 else 0.5)
            val emojiSize = if (thin) 16.sp else 26.sp
            val boxPx = sizePx * 0.22f
            val x = sizePx / 2 + radius * cos(mid) - boxPx / 2
            val y = sizePx / 2 + radius * sin(mid) - boxPx / 2
            val boxDp = with(LocalDensity.current) { boxPx.toDp() }
            Box(
                Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }.size(boxDp),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = emojiSize, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun RecipeIngredients(recipe: Recipe, cfg: Config, totalG: Double, forBatch: Boolean) {
    val scaled = recipe.scaled(cfg, totalG)
    Section("Ingredientes") {
        TableRow("", "Pesar cozido", "Comprar cru*", bold = true)
        HorizontalDivider()
        FoodGroup.entries.forEach { group ->
            val rows = scaled.filter { it.first.group == group }
            if (rows.isEmpty()) return@forEach
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Box(Modifier.size(10.dp).background(group.color, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${group.label} · ${weight(rows.sumOf { it.second })}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            rows.forEach { (item, cooked) ->
                val raw = item.rawGrams(cooked, cfg.riceFactor)
                val buy = if (item.unitGrams != null) {
                    val n = (cooked / item.unitGrams).roundToInt().coerceAtLeast(1)
                    "$n ${item.unitName}${if (n > 1) "s" else ""}"
                } else weight(raw)
                TableRow("${item.emoji} ${item.name}", weight(cooked), buy)
            }
        }
        HorizontalDivider()
        TableRow("Total", weight(totalG), "", bold = true)
        if (recipe.items.any { it.perDogPerDay != null }) {
            Text(
                "Ovo: 1 por cadela por dia, no lugar de 50 g de carne. O Cachorro Verde recomenda ovo " +
                    "1 a 2 vezes por semana (tudo bem 3); cães grandes podem comer até 2 por refeição. " +
                    if (forBatch) "Use esta receita só nesses dias, não no lote inteiro." else "",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (forBatch) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            "* Estimativa do Cachorro Verde: carnes e vísceras encolhem (uns 130 g crus para 100 g cozidos; " +
                "peixe encolhe ainda mais, arredonde para cima); tubérculos e vegetais pesam quase o mesmo; " +
                "grãos rendem ${num(cfg.riceFactor, 1)}x o peso cru (o fator do arroz nos Ajustes).",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun RecipeComplements(recipe: Recipe, cfg: Config, totalG: Double) {
    Section("Complementos") {
        Text("Obrigatórios", style = MaterialTheme.typography.labelLarge)
        Bullets(
            "Suplemento vitamínico-mineral: ${num(totalG * 3 / RECIPE_BASE_G, 0)} g de Food Dog " +
                "ou ${num(totalG * 2 / RECIPE_BASE_G, 0)} g de Nutroplus.",
            "Óleo: ${recipe.oil}, na dose do peso de cada cadela, por dia:",
        )
        cfg.dogs.forEach { dog ->
            val dose = vegetableOilDose(dog.weightKg) ?: "informe o peso nos Ajustes"
            Text("      ${dog.name}: $dose", style = MaterialTheme.typography.bodyMedium)
        }
        Text("Opcionais", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
        Bullets(
            "Iogurte natural ou kefir.",
            "Uma pitada de sal integral (sal marinho).",
            "Uma lâmina de alho fresco picadinho.",
            "Óleo de peixe (dose no cartão de cada cadela, na aba Preparo).",
        )
    }
}
