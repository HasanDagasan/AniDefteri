package com.hasandagasan.anidefteri;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hasandagasan.anidefteri.classes.HeartButton;
import java.util.ArrayList;

public class MyListAdapter extends ArrayAdapter<String> {

    private final MainActivity mainActivity;
    private final SparseBooleanArray selectedItems;
    private int textColor;
    private Typeface typeface;
    private OnFavoriteChangedListener favoriteChangedListener;

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged(int position, boolean isFavorite);
    }
    public MyListAdapter(@NonNull Context context, ArrayList<String> liste, int textColor, Typeface typeface, boolean singleSelectionMode) {
        // DEĞİŞİKLİK: Super constructor'a layout ID ve liste verildi.
        super(context, R.layout.list_item_layout, liste);
        // DEĞİŞİKLİK: MainActivity'yi context üzerinden alıyoruz.
        this.mainActivity = (MainActivity) context;
        this.textColor = textColor;
        this.typeface = typeface;
        this.selectedItems = new SparseBooleanArray();
    }
    public void setOnFavoriteChangedListener(OnFavoriteChangedListener listener) {
        this.favoriteChangedListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder deseni, listelerde performansı artırır.
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.textViewItem);
            holder.heartButton = convertView.findViewById(R.id.kalp);
            holder.hatirlaticiIcon = convertView.findViewById(R.id.hatirlaticiIconList); // İkonu bul
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String tamMetin = getItem(position);
        if (tamMetin == null) return convertView; // Null kontrolü

        boolean isFavorite = tamMetin.startsWith("★");
        String temizMetin = temizleVeMetniAl(tamMetin); // Yardımcı metot
        String gorunenMetin = isFavorite ? tamMetin.substring(1).trim() : tamMetin;

        holder.textView.setText(gorunenMetin);
        holder.textView.setTextColor(textColor);
        holder.textView.setTypeface(typeface);

        // DEĞİŞİKLİK: Kalp butonu rengi ve tıklama olayı
        holder.heartButton.setHeartColor(isFavorite ? textColor : Color.GRAY);
        holder.heartButton.setOnClickListener(v -> {
            // Favori durumunu tersine çevir
            if (favoriteChangedListener != null) {
                favoriteChangedListener.onFavoriteChanged(position, !isFavorite);
            }
        });

        // --- YENİ BÖLÜM: Hatırlatıcı ikonunu yönetme ---
        if (mainActivity.hatirlaticiVarMi(temizMetin)) {
            holder.hatirlaticiIcon.setVisibility(View.VISIBLE);
        } else {
            holder.hatirlaticiIcon.setVisibility(View.GONE);
        }
        // --- YENİ BÖLÜM SONU ---

        // Seçim modu için arkaplan rengi
        if (selectedItems.get(position, false)) {
            convertView.setBackgroundColor(0x33A9A9A9); // Yarı saydam gri
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }
    private String temizleVeMetniAl(String tamMetin) {
        if (tamMetin == null) return "";
        String temizMetin = tamMetin.startsWith("★") ? tamMetin.substring(1).trim() : tamMetin;
        return temizMetin.replaceAll("\\s*\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)$", "").trim();
    }
    private static class ViewHolder {
        TextView textView;
        HeartButton heartButton;
        ImageView hatirlaticiIcon;
    }
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyDataSetChanged();
    }
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
    public int getSelectedItemCount() {
        return selectedItems.size();
    }
    public ArrayList<Integer> getSelectedItems() {
        ArrayList<Integer> items = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
    public void updateTextColor(int newColor) {
        this.textColor = newColor;
        notifyDataSetChanged();
    }

    public void updateTypeface(Typeface newTypeface) {
        this.typeface = newTypeface;
        notifyDataSetChanged();
    }

    public void updateStyle(int newColor, Typeface newTypeface) {
        this.textColor = newColor;
        this.typeface = newTypeface;
        notifyDataSetChanged();
    }
}
