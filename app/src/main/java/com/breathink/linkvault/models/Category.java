package com.breathink.linkvault.models;

import java.util.Locale;

public class Category {

    public int id;
    public String title;
    public String timestamp;

    public Category() { }

    public Category(String title) {
        this.title = title;
    }

    public String toCSV() {
        return String.format(Locale.getDefault(), "%d,%s,%s", id, title, timestamp);
    }
}
