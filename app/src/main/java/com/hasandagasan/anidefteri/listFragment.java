package com.hasandagasan.anidefteri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.hasandagasan.anidefteri.classes.GetTypeFace;
import com.hasandagasan.anidefteri.classes.OptionMenuActions;
import java.util.ArrayList;
import java.util.Collections;

public class listFragment extends Fragment {

    public listFragment() {}
    ListView listView;
    ArrayList<String> liste;
    ArrayList<String> tumListe;
    MyListAdapter adapter;
    TextView textView;
    private androidx.appcompat.view.ActionMode actionMode;
    private int aktifFiltre = 0;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageButton filterButton;
    private View topBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        listView = view.findViewById(R.id.listView);
        textView = view.findViewById(R.id.textViewSozYok);
        filterButton = view.findViewById(R.id.filterButton);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        topBar = view.findViewById(R.id.topBar);

        MainActivity mainActivity = (MainActivity) getActivity();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);
        textView.setTypeface(typeface);
        textView.setTextColor(selectedColor);

        searchButton.setEnabled(false);
        searchButton.setClickable(false);
        searchButton.setFocusable(false);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterButton.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.mouseClickSound();
            showFilterDialog();
        });

        // --- ActionMode Callback'i Geri Ekliyoruz ---
        androidx.appcompat.view.ActionMode.Callback actionModeCallback = new androidx.appcompat.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, android.view.Menu menu) {
                // Sağlanan menüyü inflate ediyoruz (şişiriyoruz)
                mode.getMenuInflater().inflate(R.menu.multiple_selection, menu);
                topBar.setVisibility(View.GONE); // Arama çubuğunu gizle
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
                // Üst boşluğu (marginTop) 100dp olarak ayarla
                params.topMargin = dpToPx(50); // dp değerini pixel'e çeviriyoruz
                listView.setLayoutParams(params);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, android.view.Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, android.view.MenuItem item) {
                // Menüdeki silme butonuna tıklandığında
                if (item.getItemId() == R.id.action_delete) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.deletion_confirmation_title))
                            .setMessage(getString(R.string.deletion_confirmation))
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                if (mainActivity != null) mainActivity.mouseClickSound();
                                secilenOgeleriSil();
                                mode.finish(); // Silme işleminden sonra modu bitir
                            })
                            .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                                if (mainActivity != null) mainActivity.mouseClickSound();
                            })
                            .show();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                if (adapter != null) {
                    adapter.clearSelection();
                }
                actionMode = null;

                // Arama çubuğunu göster
                topBar.setVisibility(View.VISIBLE);

                // --- YENİ KOD BAŞLANGICI ---
                // ListView'in layout parametrelerini al
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
                // Üst boşluğu (marginTop) tekrar 0 yap
                params.topMargin = 0;
                listView.setLayoutParams(params);
            }
        };

        if (getArguments() != null) {
            liste = getArguments().getStringArrayList("liste");
            tumListe = new ArrayList<>(liste);
            adapter = new MyListAdapter(getContext(), liste, selectedColor, typeface, true);

            adapter.setOnFavoriteChangedListener((position, isFavorite) -> {
                if (position >= liste.size()) return;
                String metin = liste.get(position);
                String temizMetin = temizleVeMetniAl(metin);

                if (mainActivity != null) {
                    mainActivity.toggleFavorite(temizMetin, isFavorite);
                }

                guncelleTumListe(metin, isFavorite);
                performSearch(searchEditText.getText().toString());
            });
            listView.setAdapter(adapter);

            if (liste == null || liste.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
            }

            // --- Tıklama Olayını Çoklu Seçim Modu İçin Güncelliyoruz ---
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                if (actionMode == null) {
                    // Normal modda tek tıklama
                    showOptionsDialog(position);
                } else {
                    // Seçim modunda tek tıklama (öğeyi seç/seçimi kaldır)
                    adapter.toggleSelection(position);
                    updateActionModeTitle();
                }
            });

            // --- Uzun Tıklama İle Seçim Modunu Başlatmayı Geri Ekliyoruz ---
            listView.setOnItemLongClickListener((parent, view1, position, id) -> {
                if (actionMode == null) {
                    actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(actionModeCallback);

                    if (mainActivity != null) mainActivity.setActionMode(actionMode);
                    adapter.toggleSelection(position);
                    updateActionModeTitle();
                }
                return true;
            });
        }
        return view;
    }

    private void performSearch(String query) {
        ArrayList<String> sourceList = new ArrayList<>();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        switch (aktifFiltre) {
            case 0: sourceList.addAll(tumListe); break;
            case 1:
                for (String item : tumListe) { if (item.startsWith("★")) sourceList.add(item); }
                break;
            case 2:
                for (String item : tumListe) {
                    if (mainActivity.hatirlaticiVarMi(temizleVeMetniAl(item))) sourceList.add(item);
                }
                break;
        }

        ArrayList<String> searchResult = new ArrayList<>();
        if (query.trim().isEmpty()) {
            searchResult.addAll(sourceList);
        } else {
            for (String item : sourceList) {
                if (item.toLowerCase().contains(query.toLowerCase())) searchResult.add(item);
            }
        }

        liste.clear();
        liste.addAll(searchResult);
        if(adapter!=null) adapter.notifyDataSetChanged();

        textView.setVisibility(liste.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    private void showFilterDialog() {
        final CharSequence[] filterOptions = {getString(R.string.no_filter), getString(R.string.favorite), getString(R.string.reminder_sentences)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.select_filter);
        builder.setSingleChoiceItems(filterOptions, aktifFiltre, (dialog, which) -> {
            aktifFiltre = which;
            performSearch(searchEditText.getText().toString());
            dialog.dismiss();
            if (getActivity() != null) ((MainActivity) getActivity()).mouseClickSound();
        });
        builder.show();
    }
    private void secilenOgeleriSil() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null || adapter == null) return;

        ArrayList<Integer> secilenPozisyonlar = adapter.getSelectedItems();
        Collections.sort(secilenPozisyonlar, Collections.reverseOrder());

        for (int pozisyon : secilenPozisyonlar) {
            if (pozisyon < liste.size()) {
                String silinecekMetin = liste.get(pozisyon);
                String temizMetin = temizleVeMetniAl(silinecekMetin);
                mainActivity.notuSil(temizMetin);
                tumListe.remove(silinecekMetin);
            }
        }
        performSearch(searchEditText.getText().toString());

        int silinenSayi = secilenPozisyonlar.size();
        String mesaj = getResources().getQuantityString(R.plurals.items_deleted, silinenSayi, silinenSayi);
        Toast.makeText(getContext(), mesaj, Toast.LENGTH_SHORT).show();
    }

    private void updateActionModeTitle() {
        if (actionMode != null) {
            int secilenSayisi = adapter.getSelectedItemCount();
            if (secilenSayisi > 0) {
                String baslik = getResources().getQuantityString(R.plurals.items_selected, secilenSayisi, secilenSayisi);
                actionMode.setTitle(baslik);
            } else {
                actionMode.finish();
            }
        }
    }
    private void guncelleTumListe(String eskiMetin, boolean yeniFavoriDurumu) {
        for (int i = 0; i < tumListe.size(); i++) {
            String item = tumListe.get(i);
            String temizItemMetin = temizleVeMetniAl(item.startsWith("★") ? item.substring(1).trim() : item);
            String temizEskiMetinSadece = temizleVeMetniAl(eskiMetin.startsWith("★") ? eskiMetin.substring(1).trim() : eskiMetin);

            if (temizItemMetin.equals(temizEskiMetinSadece)) {
                String tarihKismi = "";
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)").matcher(item);
                if (m.find()) {
                    tarihKismi = "\n" + m.group(0);
                }

                if (yeniFavoriDurumu) {
                    tumListe.set(i, "★ " + temizItemMetin + tarihKismi);
                } else {
                    tumListe.set(i, temizItemMetin + tarihKismi);
                }
                break;
            }
        }
    }

    private void showOptionsDialog(int position) {
        if (position >= liste.size()) return;
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;
        String secilenMetin = liste.get(position);
        boolean isFavorite = secilenMetin.startsWith("★");
        String temizMetin = temizleVeMetniAl(secilenMetin);
        boolean hatirlaticiVar = mainActivity.hatirlaticiVarMi(temizMetin);
        OptionMenuActions optionMenu = new OptionMenuActions(mainActivity);
        optionMenu.showOptionsDialogForNote(temizMetin, isFavorite, hatirlaticiVar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupButonlar();
        }
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }

    }

    private String temizleVeMetniAl(String tamMetin) {
        return tamMetin.replaceAll("\\s*\\(.*?\\)$", "").replace("★", "").trim();
    }

    public void yenidenYukle() {
        // 1. Context veya View yoksa hiçbir şey yapma (Fragment yok edilmiş olabilir)
        if (getContext() == null || getView() == null) return;

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        // --- YENİ BÖLÜM: DİL DEĞİŞİKLİĞİ İÇİN METİNLERİ YENİLE ---
        // 2. XML'den gelen ve dil değişikliğinden etkilenen tüm metinleri yeniden ata.
        textView.setText(R.string.no_sentence_to_list_found); // "Söz bulunamadı" metnini strings.xml'den almalı
        searchEditText.setHint(R.string.search_in_list); // Arama kutusunun "hint" metnini güncelle

        // Filtreleme diyalog metinleri ve ActionMode başlıkları gibi diğer metinler
        // zaten her açıldığında `getString()` ile yüklendiği için genellikle
        // anlık güncellemeye ihtiyaç duymazlar. Ancak ana arayüzdekiler önemlidir.
        // -------------------------------------------------------------

        // 3. Veri listesini MainActivity'den al
        this.tumListe = new ArrayList<>(mainActivity.gosterilecekListe);

        // 4. Mevcut arama ve filtre durumuna göre listeyi yenile
        performSearch(searchEditText != null ? searchEditText.getText().toString() : "");

        // 5. Yazı tipi ve rengi gibi stil ayarlarını al
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);

        // 6. Adapter'ı güncelle veya yeniden oluştur
        if (adapter != null) {
            // Sadece stili ve veriyi güncellemek, listener'ları korur.
            adapter.updateStyle(selectedColor, typeface); // Bu metod adapter.notifyDataSetChanged() içermeli
        } else {
            // Adapter ilk kez oluşturuluyorsa...
            adapter = new MyListAdapter(getContext(), this.liste, selectedColor, typeface, true);
            adapter.setOnFavoriteChangedListener((position, isFavorite) -> {
                if (position >= liste.size()) return;
                String metin = liste.get(position);
                String temizMetin = temizleVeMetniAl(metin);

                if (mainActivity != null) {
                    mainActivity.toggleFavorite(temizMetin, isFavorite);
                }

                guncelleTumListe(metin, isFavorite);
                performSearch(searchEditText.getText().toString());
            });
            listView.setAdapter(adapter);
        }

        // 7. Diğer stil elemanlarını güncelle
        textView.setTypeface(typeface);
        textView.setTextColor(selectedColor);
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
