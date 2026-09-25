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

import android.content.Context
import java.time.LocalDate

object Storage {
    private const val FILE = "config"

    fun load(context: Context): Config {
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val def = Config()
        fun d(key: String, fallback: Double) = p.getString(key, null)?.toDoubleOrNull() ?: fallback
        return Config(
            dogs = def.dogs.mapIndexed { i, dog ->
                Dog(
                    p.getString("dog_${i}_name", null) ?: dog.name,
                    d("dog_${i}_kg", dog.kgPerDay),
                    // 0 era o valor salvo antes de o peso ter padrão: trata como não informado.
                    d("dog_${i}_weight", 0.0).takeIf { it > 0 } ?: dog.weightKg,
                    p.getString("dog_${i}_birth", null)?.let(LocalDate::parse) ?: dog.birth,
                    p.getString("dog_${i}_adult_size", null)
                        ?.let { n -> AdultSize.entries.firstOrNull { it.name == n } } ?: dog.adultSize,
                    p.getBoolean("dog_${i}_neutered", dog.neutered),
                )
            },
            pctCarne = d("pct_carne", def.pctCarne),
            pctVisceras = d("pct_visceras", def.pctVisceras),
            pctVegetais = d("pct_vegetais", def.pctVegetais),
            pctCarbo = d("pct_carbo", def.pctCarbo),
            priceCarbo = d("price_carbo", def.priceCarbo),
            priceVegetais = d("price_vegetais", def.priceVegetais),
            priceVisceras = d("price_visceras", def.priceVisceras),
            meats = def.meats.mapIndexed { i, m -> Meat(m.name, d("meat_${i}_price", m.pricePerKg)) },
            riceFactor = d("rice_factor", def.riceFactor),
            racaoEvery = p.getInt("racao_every", def.racaoEvery),
            jejumEvery = p.getInt("jejum_every", def.jejumEvery),
            cycleStart = p.getString("cycle_start", null)?.let(LocalDate::parse)
                ?: def.cycleStart.also { save(context, def) },
        )
    }

    fun save(context: Context, c: Config) {
        val e = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        c.dogs.forEachIndexed { i, dog ->
            e.putString("dog_${i}_name", dog.name)
            e.putString("dog_${i}_kg", dog.kgPerDay.toString())
            e.putString("dog_${i}_weight", dog.weightKg.toString())
            e.putString("dog_${i}_birth", dog.birth?.toString())
            e.putString("dog_${i}_adult_size", dog.adultSize.name)
            e.putBoolean("dog_${i}_neutered", dog.neutered)
        }
        e.putString("pct_carne", c.pctCarne.toString())
        e.putString("pct_visceras", c.pctVisceras.toString())
        e.putString("pct_vegetais", c.pctVegetais.toString())
        e.putString("pct_carbo", c.pctCarbo.toString())
        e.putString("price_carbo", c.priceCarbo.toString())
        e.putString("price_vegetais", c.priceVegetais.toString())
        e.putString("price_visceras", c.priceVisceras.toString())
        c.meats.forEachIndexed { i, m -> e.putString("meat_${i}_price", m.pricePerKg.toString()) }
        e.putString("rice_factor", c.riceFactor.toString())
        e.putInt("racao_every", c.racaoEvery)
        e.putInt("jejum_every", c.jejumEvery)
        e.putString("cycle_start", c.cycleStart.toString())
        e.apply()
    }
}
