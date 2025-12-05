package com.hasandagasan.anidefteri;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.hasandagasan.anidefteri.classes.GetTypeFace;

public class editFragment extends Fragment {

    private static final String ARG_METIN = "metin";
    private static final String ARG_FAVORI = "favori";
    private static final String ARG_HATIRLATICI = "hatirlatici";
    private String orijinalMetin;
    private boolean favoriDurumu;
    private boolean hatirlaticiDurumu;
    private EditText editTextNot;
    private CheckBox checkboxFavori;
    private CheckBox checkboxHatirlatici;
    private Button btnKaydet;

    // Fragment'ı oluşturmak için kullanılan fabrika metodu
    public static editFragment newInstance(String metin, boolean favori, boolean hatirlaticiVar) {
        editFragment fragment = new editFragment();
        Bundle args = new Bundle();
        args.putString(ARG_METIN, metin);
        args.putBoolean(ARG_FAVORI, favori);
        args.putBoolean(ARG_HATIRLATICI, hatirlaticiVar); // YENİ
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orijinalMetin = getArguments().getString(ARG_METIN);
            favoriDurumu = getArguments().getBoolean(ARG_FAVORI);
            hatirlaticiDurumu = getArguments().getBoolean(ARG_HATIRLATICI); // YENİ
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        // View elemanlarını bağla
        editTextNot = view.findViewById(R.id.editMetin);
        checkboxFavori = view.findViewById(R.id.checkFavori);
        checkboxHatirlatici = view.findViewById(R.id.checkHatirlatici);
        btnKaydet = view.findViewById(R.id.kaydetButton);

        // Gelen verilerle arayüzü doldur
        editTextNot.setText(orijinalMetin);
        checkboxFavori.setChecked(favoriDurumu);
        checkboxHatirlatici.setChecked(hatirlaticiDurumu);

        // Kaydet butonunun tıklama olayını ayarla
        btnKaydet.setOnClickListener(view1 -> {

            String yeniMetin = editTextNot.getText().toString().trim();
            if (!yeniMetin.isEmpty()) {
                yeniMetin = yeniMetin.replaceAll("(?m)^[ \t]*\r?\n", "");
            }
            boolean yeniFavori = checkboxFavori.isChecked();
            boolean yeniHatirlatici = checkboxHatirlatici.isChecked();

            if (yeniMetin.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.please_enter_some_text), Toast.LENGTH_SHORT).show();
                return;
            }
            kaydetVeKapat(yeniMetin,yeniFavori,yeniHatirlatici);
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Arayüz oluşturulduktan sonra font ve renk ayarlarını uygula
        yenidenYukle();
    }

    private void kaydetVeKapat(String yeniMetin, boolean yeniFavori, boolean yeniHatirlatici) {
        try {

            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity == null) return;

            // 1. Önce metin ve favori durumunu her zaman güncelle
            mainActivity.guncelleNot(orijinalMetin, yeniMetin, yeniFavori);

            // 2. Hatırlatıcı durumunu kontrol et
            if (yeniHatirlatici) {
                // Eğer hatırlatıcı seçiliyse, hatırlatıcı diyalogunu göster.
                // Not metni güncellenmiş olabileceğinden 'yeniMetin'i gönderiyoruz.
                mainActivity.showReminderDialog(yeniMetin);
            } else {
                // Eğer hatırlatıcı seçili değilse, mevcut hatırlatıcıyı (varsa) sil.
                MainActivity.removeReminderFromJson(getContext(), yeniMetin);
            }

            mainActivity.mouseClickSound();

            // Fragment'ı kapat
            getActivity().getSupportFragmentManager().popBackStack();

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = getString(R.string.an_error_occurred);
            Toast.makeText(getContext(), errorMessage + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Font ve renk ayarlarını uygulayan metot
    public void yenidenYukle() {
        if (getContext() == null || getView() == null) return;

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);

        editTextNot.setHintTextColor(selectedColor);
        editTextNot.setTextColor(selectedColor);
        editTextNot.setTypeface(typeface);

        checkboxFavori.setTextColor(selectedColor);
        checkboxFavori.setTypeface(typeface);
        checkboxFavori.setText(getString(R.string.favorite));

        // YENİ: Hatırlatıcı CheckBox'ını da güncelle
        checkboxHatirlatici.setTextColor(selectedColor);
        checkboxHatirlatici.setTypeface(typeface);
        checkboxHatirlatici.setText(getString(R.string.reminder));

        btnKaydet.setTextColor(selectedColor);
        btnKaydet.setTypeface(typeface);
        btnKaydet.setText(getString(R.string.save));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ana ekrandaki butonların durumunu tekrar ayarla
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupButonlar();
        }
    }
}
