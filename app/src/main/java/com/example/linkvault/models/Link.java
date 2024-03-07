package com.example.linkvault.models;

import java.util.Date;
import java.util.Locale;

public class Link {

    public int id;
    public String url;
    public String title;
    public int idCategory;
    public boolean isFavorite;
    public boolean isPrivate;
    public boolean isSelected;
    public String timestamp;

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String toCSV() {
        return String.format(Locale.getDefault(), "%d,%s,%s,%d,%d,%d,%s",
                id, url, title, idCategory, isFavorite ? 1 : 0, isPrivate ? 1 : 0, timestamp);
    }
}
