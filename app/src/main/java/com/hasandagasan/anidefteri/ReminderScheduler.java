package com.hasandagasan.anidefteri;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import java.util.Calendar;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Calendar calendar, String notMetni, String tekrarTipi, String kayitTarihi) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // --- DEĞİŞİKLİK 1 ---
                Toast.makeText(context, context.getString(R.string.exact_alarm_permission_needed), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }
        int alarmId = notMetni.hashCode();

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("NOT_METNI", notMetni);
        intent.putExtra("NOTIFICATION_ID", alarmId);
        intent.putExtra("TEKRAR_TIPI", tekrarTipi);
        intent.putExtra("KAYIT_TARIHI", kayitTarihi);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        boolean shouldSchedule = true;

        if ((calendar.getTimeInMillis() + 3500) <= System.currentTimeMillis()) {
            if (!tekrarTipi.equals(context.getString(R.string.reminder_type_one_time))) {
                // Tekrarlı alarm ise, bir sonraki periyoda ayarla
                if (tekrarTipi.equals(context.getString(R.string.reminder_type_daily))) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                } else if (tekrarTipi.equals(context.getString(R.string.reminder_type_monthly))) {
                    calendar.add(Calendar.MONTH, 1);
                } else if (tekrarTipi.equals(context.getString(R.string.reminder_type_yearly))) {
                    calendar.add(Calendar.YEAR, 1);
                }
                // --- DEĞİŞİKLİK 3 ---
                Toast.makeText(context, context.getString(R.string.past_date_set_to_next_period), Toast.LENGTH_SHORT).show();
            } else {
                // --- DEĞİŞİKLİK 4 ---
                Toast.makeText(context, context.getString(R.string.past_date_one_time_error), Toast.LENGTH_LONG).show();
                shouldSchedule = false;
            }
        }

        if (shouldSchedule) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("ReminderScheduler", "Alarm kuruldu: " + notMetni + " - Zaman: " + calendar.getTime());

            if (context instanceof MainActivity) {
                ((MainActivity) context).guncelleNotHatirlatici(notMetni, tekrarTipi, calendar);
                // --- DEĞİŞİKLİK 5 ---
                Toast.makeText(context, context.getString(R.string.reminder_set_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            MainActivity.removeReminderFromJson(context, notMetni);
        }
    }

    // cancelReminder metodu metin içermediği için aynı kalabilir.
    public static void cancelReminder(Context context, String notMetni) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int alarmId = notMetni.hashCode();

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d("ReminderScheduler", "Alarm iptal edildi: " + notMetni);
    }
}
