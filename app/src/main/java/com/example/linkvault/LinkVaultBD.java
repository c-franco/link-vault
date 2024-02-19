package com.example.linkvault;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.linkvault.models.Category;
import com.example.linkvault.models.Link;

import java.util.ArrayList;
import java.util.List;

public class LinkVaultBD extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "LinksVault";
    private static final int DB_VERSION = 1;
    public static final String LINKS_TABLE = "links";
    public static final String CATEGORIES_TABLE = "categories";
    public static final String ID_COL = "id";
    public static final String TITLE_COL = "title";
    public static final String URL_COL = "url";
    public static final String ID_CATEGORY_COL = "idCategory";
    public static final String IS_FAVORITE_COL = "isFavorite";
    public static final String IS_PRIVATE_COL = "isPrivate";

    public LinkVaultBD(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase bd) {

        bd.execSQL("create table " + CATEGORIES_TABLE + " (" +
                ID_COL + " integer PRIMARY KEY AUTOINCREMENT, " +
                TITLE_COL + " varchar(20))");

        bd.execSQL("create table " + LINKS_TABLE + " (" +
                ID_COL + " integer PRIMARY KEY AUTOINCREMENT, " +
                URL_COL + " text, " +
                TITLE_COL + " varchar(50), " +
                ID_CATEGORY_COL + " integer, " +
                IS_FAVORITE_COL + " bool, " +
                IS_PRIVATE_COL + " bool, " +
                " CONSTRAINT fk_CategoryLink FOREIGN KEY (idCategory) REFERENCES categories (id) ON DELETE CASCADE ON UPDATE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase bd, int oldVersion, int newVersion) {

    }

    // region Create

    public void addNewLink(Link link) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(URL_COL, link.url);
        values.put(TITLE_COL, link.title);
        values.put(ID_CATEGORY_COL, link.idCategory);
        values.put(IS_FAVORITE_COL, link.isFavorite);
        values.put(IS_PRIVATE_COL, link.isPrivate);

        db.insert(LINKS_TABLE, null, values);
        db.close();
    }

    public void addNewCategory(Category category) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TITLE_COL, category.title);

        db.insert(CATEGORIES_TABLE, null, values);
        db.close();
    }

    // endregion

    // region Category querys

    public List<String> getAllCategoryTitles() {
        List<String> categoryTitles = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(CATEGORIES_TABLE, new String[]{TITLE_COL}, null, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String categoryTitle = cursor.getString(cursor.getColumnIndex(TITLE_COL));
                    categoryTitles.add(categoryTitle);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryTitles;
    }

    @SuppressLint("Range")
    public int getCategoryId(String selectedCategory) {
        int categoryId = 0;

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(CATEGORIES_TABLE, new String[]{ID_COL}, TITLE_COL + "=?", new String[]{selectedCategory}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                categoryId = cursor.getInt(cursor.getColumnIndex(ID_COL));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryId;
    }

    // endregion
}
