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

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Lembrete diário para tirar do congelador a porção do dia seguinte: o Cachorro Verde
 * sugere descongelar uma porção por dia na parte de baixo da geladeira (12 a 18 horas).
 * Avisa também quando o dia seguinte é de ração ou de jejum.
 */
object Reminders {
    private const val CHANNEL = "lembretes"

    private fun pending(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** Agenda o próximo aviso, ou cancela se o lembrete estiver desligado. */
    fun schedule(context: Context, cfg: Config) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pending(context)
        am.cancel(pi)
        if (!cfg.reminderOn) return
        val now = ZonedDateTime.now()
        var at = now.toLocalDate().atStartOfDay(now.zone).plusMinutes(cfg.reminderMinutes.toLong())
        if (!at.isAfter(now)) at = at.plusDays(1)
        // Alarme inexato: não exige a permissão de alarme exato, e alguns minutos de folga não importam.
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toInstant().toEpochMilli(), pi)
    }

    /** Título e texto do aviso sobre o dia seguinte a [today]. */
    fun message(cfg: Config, today: LocalDate = LocalDate.now()): Pair<String, String> {
        val tomorrow = today.plusDays(1)
        val day = tomorrow.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd/MM", java.util.Locale.forLanguageTag("pt-BR")))
        return when (cfg.dayType(tomorrow)) {
            DayType.NATURAL -> {
                val recipe = cfg.recipeIndex(tomorrow)?.let { RECIPES[it] }
                "Descongelar a comida de amanhã" to
                    "Tire do congelador a porção de $day" +
                    (recipe?.let { " (${it.items.first().emoji} ${it.title})" } ?: "") +
                    ": ${kg(cfg.dailyKg)} para as duas. Deixe na parte de baixo da geladeira; " +
                    "descongela em 12 a 18 horas."
            }
            DayType.RACAO -> "Amanhã é dia de ração" to
                "Não precisa descongelar comida natural para $day."
            DayType.JEJUM -> "Amanhã é dia de jejum" to
                "Sem comida sólida em $day; não precisa descongelar."
        }
    }

    fun show(context: Context, cfg: Config) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Lembretes da comida", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val (title, text) = message(cfg)
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, n)
    }
}

/** Dispara o aviso do dia e agenda o próximo; depois de reiniciar o aparelho, só reagenda. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cfg = Storage.load(context)
        val reboot = intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (!reboot && cfg.reminderOn) Reminders.show(context, cfg)
        Reminders.schedule(context, cfg)
    }
}
