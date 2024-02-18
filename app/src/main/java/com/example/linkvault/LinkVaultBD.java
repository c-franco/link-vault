package com.example.linkvault;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.linkvault.models.Category;
import com.example.linkvault.models.Link;

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
}
