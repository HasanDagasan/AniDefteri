package com.hasandagasan.anidefteri.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;
import com.hasandagasan.anidefteri.FontAdapter;
import com.hasandagasan.anidefteri.MainActivity;
import com.hasandagasan.anidefteri.R;
import com.hasandagasan.anidefteri.editFragment;
import yuku.ambilwarna.AmbilWarnaDialog;

public class OptionMenuActions {
    private final MainActivity activity;
    public OptionMenuActions(MainActivity activity) {
        this.activity = activity;
    }
    public void yaziRengiSec(Context context) {
        int defaultColor = Color.BLACK;
        new AmbilWarnaDialog(context, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                activity.mouseClickSound();
                activity.kaydetVeUygula("selectedColor", color, null, null);
                activity.loadPreferences();
                activity.tumFragmentleriYenidenYukle();
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                activity.mouseClickSound();
            }
        }).show();
    }
    public void yaziTipiSec(Context context) {
        String[] fontList = {"Sans Serif", "AguDisplay", "Cookie", "DancingScript",
                "Lobster", "Play", "RubikVinyl", "VujahdayScript"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.select_font);

        FontAdapter adapter = new FontAdapter(context, fontList);
        builder.setAdapter(adapter, (dialog, which) -> {
            activity.mouseClickSound();
            String selectedFont = fontList[which];
            GetTypeFace typeFace = new GetTypeFace();
            typeFace.getTypefaceFromFontName(context, selectedFont);
            activity.kaydetVeUygula("selectedFont", null, selectedFont, null);
            activity.loadPreferences();
            activity.tumFragmentleriYenidenYukle();
        });

        builder.show();
    }
    public void sesAyarSec(Boolean sesAyar) {
        activity.kaydetVeUygula("sesAcik", null, null, sesAyar);
    }

    public void showOptionsDialogForNote(String metin, boolean isFavorite, boolean hatirlatici) {
        activity.mouseClickSound();

        boolean hatirlaticiVar = activity.hatirlaticiVarMi(metin);

        // --- DEĞİŞİKLİK 1: Metinleri strings.xml'den al ---
        String optionEditText = activity.getString(R.string.option_edit);
        String optionDeleteText = activity.getString(R.string.option_delete);
        String optionFavoriteText = isFavorite ? activity.getString(R.string.option_remove_favorite) : activity.getString(R.string.option_add_favorite);
        String optionReminderText = hatirlaticiVar ? activity.getString(R.string.option_edit_reminder) : activity.getString(R.string.option_add_reminder);

        final String[] options = {
                optionEditText,
                optionDeleteText,
                optionFavoriteText,
                optionReminderText
        };

        new AlertDialog.Builder(activity)
                .setTitle(R.string.options_dialog_title) // Başlığı da R.string ile ayarla
                .setItems(options, (dialog, item) -> {
                    String selectedOption = options[item];

                    // --- DEĞİŞİKLİK 2: Karşılaştırmaları metin değişkenleri ile yap ---
                    if (selectedOption.equals(optionEditText)) {
                        // --- Düzenleme işlemi ---
                        activity.mouseClickSound();
                        activity.getSupportFragmentManager().popBackStack(null, activity.getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
                        editFragment editFragment = com.hasandagasan.anidefteri.editFragment.newInstance(metin, isFavorite, hatirlatici);
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, editFragment)
                                .addToBackStack(null)
                                .commit();

                    } else if (selectedOption.equals(optionDeleteText)) {
                        // --- Silme işlemi ---
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.delete_confirmation_title)
                                .setMessage(R.string.delete_confirmation_message)
                                .setPositiveButton(R.string.yes, (d, w) -> {
                                    activity.mouseClickSound();
                                    activity.notuSil(metin);
                                    Toast.makeText(activity, R.string.deleted_toast, Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton(R.string.no, (d, w) -> activity.mouseClickSound())
                                .show();

                    } else if (selectedOption.equals(optionReminderText)) {
                        // --- Hatırlatma işlemi ---
                        activity.showReminderDialog(metin);

                    } else if (selectedOption.equals(optionFavoriteText)) {
                        // --- Favori işlemi ---
                        activity.mouseClickSound();
                        activity.toggleFavorite(metin, !isFavorite);
                    }
                })
                .show();
    }
}
