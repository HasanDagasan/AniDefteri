package com.hasandagasan.anidefteri;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HatirlatmaDetayFragment extends Fragment {

    private String notMetni;
    private String kayitTarihi; // Format: "2025-11-14 23:31"

    // Fragment'ı oluştururken verileri almak için standart yöntem
    public static HatirlatmaDetayFragment newInstance(String notMetni, String kayitTarihi) {
        HatirlatmaDetayFragment fragment = new HatirlatmaDetayFragment();
        Bundle args = new Bundle();
        args.putString("NOT_METNI", notMetni);
        args.putString("KAYIT_TARIHI", kayitTarihi);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notMetni = getArguments().getString("NOT_METNI");
            kayitTarihi = getArguments().getString("KAYIT_TARIHI");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hatirlatma_detay, container, false);

        // XML'deki bileşenleri bul
        TextView tvTesvik = view.findViewById(R.id.tvTesvik);
        TextView tvNotMetni = view.findViewById(R.id.tvNotMetni);
        TextView tvKayitTarihiGoster = view.findViewById(R.id.tvKayitTarihi);

        // Bileşenlere verileri ata
        tvNotMetni.setText(notMetni);

        // Teşvik cümlesini ve tarih formatını ayarla
        if (kayitTarihi != null && !kayitTarihi.isEmpty()) {
            try {
                // Gelen "yyyy-MM-dd HH:mm" formatını Date nesnesine çevir
                SimpleDateFormat gelenFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date tarihObj = gelenFormat.parse(kayitTarihi);

                // Ekranda gösterilecek "dd.MM.yyyy HH:mm" formatına çevir
                SimpleDateFormat gosterimFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                tvKayitTarihiGoster.setText(gosterimFormat.format(tarihObj));

                // Teşvik cümlesini oluştur
                tvTesvik.setText(getGecenSureMesaji(tarihObj,getContext()));

            } catch (ParseException e) {
                e.printStackTrace();
                tvKayitTarihiGoster.setText(kayitTarihi); // Hata olursa orijinalini göster
                tvTesvik.setVisibility(View.GONE); // Hata olursa teşviki gizle
            }
        } else {
            tvTesvik.setVisibility(View.GONE);
            tvKayitTarihiGoster.setText(R.string.no_knowledge_of_history);
        }
        return view;
    }

    private String getGecenSureMesaji(Date baslangicTarihi, Context context) {
        long farkMillis = new Date().getTime() - baslangicTarihi.getTime();
        long gunFarki = TimeUnit.MILLISECONDS.toDays(farkMillis);

        if (gunFarki >= 365) {
            long yil = gunFarki / 365;
            return context.getString(R.string.memory_years_ago, yil);
        } else if (gunFarki >= 30) {
            long ay = gunFarki / 30;
            return context.getString(R.string.memory_months_ago, ay);
        } else if (gunFarki > 0) {
            return context.getString(R.string.memory_days_ago, gunFarki);
        } else {
            long saatFarki = TimeUnit.MILLISECONDS.toHours(farkMillis);
            if (saatFarki > 0) {
                return context.getString(R.string.memory_hours_ago, saatFarki);
            } else {
                long dakikaFarki = TimeUnit.MILLISECONDS.toMinutes(farkMillis);
                return context.getString(R.string.memory_minutes_ago, dakikaFarki);
            }
        }
    }
}
