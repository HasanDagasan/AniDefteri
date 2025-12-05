package com.hasandagasan.anidefteri;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String notMetni = intent.getStringExtra("NOT_METNI");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);
        String tekrarTipi = intent.getStringExtra("TEKRAR_TIPI");
        String kayitTarihiStr = intent.getStringExtra("KAYIT_TARIHI");
        String zamanFarkiBasligi = getZamanFarkiMetni(context, kayitTarihiStr);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        activityIntent.putExtra("OPEN_FRAGMENT", "HatirlatmaDetayFragment");
        activityIntent.putExtra("NOT_METNI", notMetni);
        activityIntent.putExtra("KAYIT_TARIHI", kayitTarihiStr);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "reminder_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(zamanFarkiBasligi)
                .setContentText(notMetni)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notMetni))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, builder.build());

        // TEKRARLI ALARMLARI YENİDEN KURMA
        if (tekrarTipi != null) {
            // String karşılaştırmaları için resources kullan
            String onceTekrar = context.getString(R.string.reminder_type_one_time);
            String gunlukTekrar = context.getString(R.string.reminder_type_daily);
            String aylikTekrar = context.getString(R.string.reminder_type_monthly);
            String yillikTekrar = context.getString(R.string.reminder_type_yearly);

            if (tekrarTipi.equals(onceTekrar)) {
                Log.d("Receiver", "Tek seferlik hatırlatıcı için static silme metodu çağrılıyor.");
                MainActivity.removeReminderFromJson(context, notMetni);

                Log.d("Receiver", "Tek seferlik hatırlatıcı silindi, MainActivity'e anons gönderiliyor.");
                Intent updateUIIntent = new Intent("REMINDER_DELETED_ACTION");
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUIIntent);

            } else {
                Calendar calendar = Calendar.getInstance();

                if (tekrarTipi.equals(gunlukTekrar)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                } else if (tekrarTipi.equals(aylikTekrar)) {
                    calendar.add(Calendar.MONTH, 1);
                } else if (tekrarTipi.equals(yillikTekrar)) {
                    calendar.add(Calendar.YEAR, 1);
                }

                ReminderScheduler.scheduleReminder(context, calendar, notMetni, tekrarTipi, kayitTarihiStr);
            }
        }
    }

    private String getZamanFarkiMetni(Context context, String kayitTarihiStr) {
        if (kayitTarihiStr == null) {
            return context.getString(R.string.notification_default_title);
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date kayitTarihi = format.parse(kayitTarihiStr);
            Date simdikiZaman = new Date();

            long farkMilisaniye = simdikiZaman.getTime() - kayitTarihi.getTime();
            long farkSaniye = TimeUnit.MILLISECONDS.toSeconds(farkMilisaniye);
            long farkDakika = TimeUnit.MILLISECONDS.toMinutes(farkMilisaniye);
            long farkSaat = TimeUnit.MILLISECONDS.toHours(farkMilisaniye);
            long farkGun = TimeUnit.MILLISECONDS.toDays(farkMilisaniye);

            if (farkSaniye < 60) {
                return context.getResources().getQuantityString(R.plurals.reminder_seconds_ago, (int)farkSaniye, farkSaniye);
            } else if (farkDakika < 60) {
                return context.getResources().getQuantityString(R.plurals.reminder_minutes_ago, (int)farkDakika, farkDakika);
            } else if (farkSaat < 24) {
                return context.getResources().getQuantityString(R.plurals.reminder_hours_ago, (int)farkSaat, farkSaat);
            } else if (farkGun < 7) {
                return context.getResources().getQuantityString(R.plurals.reminder_days_ago, (int)farkGun, farkGun);
            } else if (farkGun < 30) {
                long hafta = farkGun / 7;
                return context.getResources().getQuantityString(R.plurals.reminder_weeks_ago, (int)hafta, hafta);
            } else if (farkGun < 365) {
                long ay = farkGun / 30;
                return context.getResources().getQuantityString(R.plurals.reminder_months_ago, (int)ay, ay);
            } else {
                long yil = farkGun / 365;
                return context.getResources().getQuantityString(R.plurals.reminder_years_ago, (int)yil, yil);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return context.getString(R.string.notification_default_title);
        }
    }
}