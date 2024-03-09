package com.breathink.linkvault;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.breathink.linkvault.models.Category;
import com.breathink.linkvault.models.Link;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class LinkVaultBD extends SQLiteOpenHelper {

    // region Static fields

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
    public static final String TIMESTAMP_COL = "timestamp";

    // endregion

    // region Variables

    private Context context;

    // endregion

    public LinkVaultBD(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase bd) {

        bd.execSQL("create table " + CATEGORIES_TABLE + " (" +
                ID_COL + " integer PRIMARY KEY AUTOINCREMENT, " +
                TITLE_COL + " varchar(20), " +
                TIMESTAMP_COL + " datetime)");

        bd.execSQL("create table " + LINKS_TABLE + " (" +
                ID_COL + " integer PRIMARY KEY AUTOINCREMENT, " +
                URL_COL + " text, " +
                TITLE_COL + " varchar(50), " +
                ID_CATEGORY_COL + " integer, " +
                IS_FAVORITE_COL + " bool, " +
                IS_PRIVATE_COL + " bool, " +
                TIMESTAMP_COL + " datetime, " +
                " CONSTRAINT fk_CategoryLink FOREIGN KEY (idCategory) REFERENCES categories (id) ON DELETE CASCADE ON UPDATE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase bd, int oldVersion, int newVersion) {

    }

    // region Get

    @SuppressLint("Range")
    public List<Link> getAllLinks(boolean privateLinks, boolean export) {
        List<Link> linkList = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int sortOption = preferences.getInt(Constants.KEY_SORT_LINK, 0);

        String selection;
        String[] selectionArgs;

        if(export) {
            selection = null;
            selectionArgs = null;
        }
        else {
            selection = IS_PRIVATE_COL + " = ?";
            selectionArgs = privateLinks ? new String[]{"1"} : new String[]{"0"};
        }

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(LINKS_TABLE, new String[]{ID_COL, URL_COL, TITLE_COL, ID_CATEGORY_COL, IS_FAVORITE_COL, IS_PRIVATE_COL, TIMESTAMP_COL},
                     selection, selectionArgs, null, null, getSortClause(sortOption))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Link link = new Link();
                    link.id = cursor.getInt(cursor.getColumnIndex(ID_COL));
                    link.url = cursor.getString(cursor.getColumnIndex(URL_COL));
                    link.title = cursor.getString(cursor.getColumnIndex(TITLE_COL));
                    link.idCategory = cursor.getInt(cursor.getColumnIndex(ID_CATEGORY_COL));
                    link.isFavorite = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE_COL)) > 0;
                    link.isPrivate = cursor.getInt(cursor.getColumnIndex(IS_PRIVATE_COL)) > 0;
                    link.timestamp = cursor.getString(cursor.getColumnIndex(TIMESTAMP_COL));

                    linkList.add(link);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return linkList;
    }

    @SuppressLint("Range")
    public List<Link> getFavoriteLinks() {
        List<Link> linkList = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int sortOption = preferences.getInt(Constants.KEY_SORT_FAV, 0);

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(LINKS_TABLE, new String[]{ID_COL, URL_COL, TITLE_COL, ID_CATEGORY_COL, IS_FAVORITE_COL, IS_PRIVATE_COL},
                     IS_FAVORITE_COL + "=? AND " + IS_PRIVATE_COL + " = ?", new String[]{"1", "0"}, null, null, getSortClause(sortOption))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Link link = new Link();
                    link.id = cursor.getInt(cursor.getColumnIndex(ID_COL));
                    link.url = cursor.getString(cursor.getColumnIndex(URL_COL));
                    link.title = cursor.getString(cursor.getColumnIndex(TITLE_COL));
                    link.idCategory = cursor.getInt(cursor.getColumnIndex(ID_CATEGORY_COL));
                    link.isFavorite = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE_COL)) > 0;
                    link.isPrivate = cursor.getInt(cursor.getColumnIndex(IS_PRIVATE_COL)) > 0;

                    linkList.add(link);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return linkList;
    }

    @SuppressLint("Range")
    public List<String> getLinkUrlsByCategory(int categoryId) {
        List<String> urlList = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(LINKS_TABLE, new String[]{URL_COL},
                     ID_CATEGORY_COL + "=?", new String[]{String.valueOf(categoryId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    urlList.add(cursor.getString(cursor.getColumnIndex(URL_COL)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlList;
    }

    @SuppressLint("Range")
    public List<Link> getLinksByCategoryId(int categoryId, boolean privateLinks, boolean delete) {
        List<Link> links = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int sortOption = preferences.getInt(Constants.KEY_SORT_LINK, 0);

        String selection;
        String[] selectionArgs;

        if(delete) {
            selection = ID_CATEGORY_COL + "=?";
            selectionArgs = new String[]{String.valueOf(categoryId)};
        }
        else {
            selection = ID_CATEGORY_COL + "=? AND " + IS_PRIVATE_COL + "=?";
            selectionArgs = privateLinks ? new String[]{String.valueOf(categoryId), "1"}
                    : new String[]{String.valueOf(categoryId), "0"};
        }

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(LINKS_TABLE, new String[]{ID_COL, URL_COL, TITLE_COL, ID_CATEGORY_COL, IS_FAVORITE_COL, IS_PRIVATE_COL},
                     selection, selectionArgs, null, null, getSortClause(sortOption))) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Link link = new Link();
                    link.id = cursor.getInt(cursor.getColumnIndex(ID_COL));
                    link.url = cursor.getString(cursor.getColumnIndex(URL_COL));
                    link.title = cursor.getString(cursor.getColumnIndex(TITLE_COL));
                    link.idCategory = cursor.getInt(cursor.getColumnIndex(ID_CATEGORY_COL));
                    link.isFavorite = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE_COL)) > 0;
                    link.isPrivate = cursor.getInt(cursor.getColumnIndex(IS_PRIVATE_COL)) > 0;

                    links.add(link);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return links;
    }

    public List<String> getAllCategoryTitles() {
        List<String> categoryTitles = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int sortOption = preferences.getInt(Constants.KEY_SORT_CAT, 0);

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(CATEGORIES_TABLE, new String[]{TITLE_COL},
                     null, null, null, null, getSortClause(sortOption))) {

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
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int sortOption = preferences.getInt(Constants.KEY_SORT_CAT, 0);

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(CATEGORIES_TABLE, new String[]{ID_COL,TITLE_COL,TIMESTAMP_COL},
                     null, null, null, null, getSortClause(sortOption))) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Category category = new Category();
                    category.id = cursor.getInt(cursor.getColumnIndex(ID_COL));;
                    category.title = cursor.getString(cursor.getColumnIndex(TITLE_COL));
                    category.timestamp = cursor.getString(cursor.getColumnIndex(TIMESTAMP_COL));
                    categories.add(category);

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
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

    @SuppressLint("Range")
    public String getCategoryTitle(int categoryId) {
        String categoryTitle = "";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(CATEGORIES_TABLE, new String[]{TITLE_COL}, ID_COL + "=?", new String[]{String.valueOf(categoryId)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                categoryTitle = cursor.getString(cursor.getColumnIndex(TITLE_COL));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryTitle;
    }

    private boolean categoryExist(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {ID_COL};
        String selection = TITLE_COL + "=?";
        String[] selectionArgs = {title};

        try (Cursor cursor = db.query(CATEGORIES_TABLE, columns, selection, selectionArgs, null, null, null)) {
            return cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    // endregion

    // region Create

    public void addNewLink(Link link) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(URL_COL, link.url);
        values.put(TITLE_COL, link.title);
        values.put(ID_CATEGORY_COL, link.idCategory);
        values.put(IS_FAVORITE_COL, link.isFavorite);
        values.put(IS_PRIVATE_COL, link.isPrivate);
        values.put(TIMESTAMP_COL, Utils.getCurrentTimestamp());

        db.insert(LINKS_TABLE, null, values);
        db.close();
    }

    public void addNewCategory(Category category) {

        if(categoryExist(category.title)) {
            Toast.makeText(this.context, this.context.getString(R.string.error_db_repeated_category), Toast.LENGTH_SHORT).show();
        }
        else {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TITLE_COL, category.title);
            values.put(TIMESTAMP_COL, Utils.getCurrentTimestamp());

            db.insert(CATEGORIES_TABLE, null, values);
            db.close();
        }
    }

    // endregion

    // region Update

    public void updateLink(Link link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(URL_COL, link.url);
        values.put(TITLE_COL, link.title);
        values.put(ID_CATEGORY_COL, link.idCategory);
        values.put(IS_FAVORITE_COL, link.isFavorite);
        values.put(IS_PRIVATE_COL, link.isPrivate);

        String whereClause = ID_COL + "=?";
        String[] whereArgs = {String.valueOf(link.id)};

        db.update(LINKS_TABLE, values, whereClause, whereArgs);
        db.close();
    }

    public void updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TITLE_COL, category.title);

        String whereClause = ID_COL + "=?";
        String[] whereArgs = {String.valueOf(category.id)};

        db.update(CATEGORIES_TABLE, values, whereClause, whereArgs);
        db.close();
    }

    public void moveLinkToCategory(Link link, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_CATEGORY_COL, categoryId);

        String whereClause = ID_COL + "=?";
        String[] whereArgs = {String.valueOf(link.id)};

        db.update(LINKS_TABLE, values, whereClause, whereArgs);
        db.close();
    }

    // endregion

    // region Delete

    public void deleteLinkById(int linkId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(linkId)};

        db.delete(LINKS_TABLE, selection, selectionArgs);

        db.close();
    }

    public void deleteCategoryById(int categoryId) {

        List<Link> linksToDelete = getLinksByCategoryId(categoryId, false, true);

        for (Link link : linksToDelete) {
            deleteLinkById(link.id);
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(categoryId)};

        db.delete(CATEGORIES_TABLE, selection, selectionArgs);
        db.close();
    }

    public void deleteAllData() {
        deleteAllLinks();
        deleteAllCategories();
    }

    public void deleteAllLinks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LINKS_TABLE, null, null);
        db.close();
    }

    public void deleteAllCategories() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + CATEGORIES_TABLE + " WHERE " + ID_COL + " <> 1");
        db.close();
    }

    // endregion

    // region Import / Export

    public String exportToCSV() {
        StringBuilder csvData = new StringBuilder();

        List<Category> categories = getAllCategories();
        csvData.append("ID,TITLE,TIMESTAMP\n");
        for (Category category : categories) {
            csvData.append(category.toCSV()).append("\n");
        }

        List<Link> links = getAllLinks(false, true);
        csvData.append("\nID,URL,TITLE,ID_CATEGORY,IS_FAVORITE,IS_PRIVATE,TIMESTAMP\n");
        for (Link link : links) {
            csvData.append(link.toCSV()).append("\n");
        }

        return csvData.toString();
    }

    public void importData(String[] csvLines) {
        if (csvLines.length > 1) {

            Dictionary categoryIds = new Hashtable();
            boolean categories = true;
            for (int i = 1; i < csvLines.length; i++) {

                if(Objects.equals(csvLines[i], "") && categories) {
                    categories = false;
                    i += 2;
                }

                String[] values = csvLines[i].split(",");

                if(categories) {

                    Category category = new Category();
                    category.title = values[1];
                    category.timestamp = values[2];

                    if(!categoryExist(category.title)){
                        addNewCategory(category);
                    }

                    int categoryId = getCategoryId(category.title);
                    categoryIds.put(values[0], categoryId);
                }
                else {

                    int categoryId = (int) categoryIds.get(values[3]);

                    Link link = new Link();
                    link.url = values[1];
                    link.title = values[2];
                    link.idCategory = categoryId;
                    link.isFavorite = Objects.equals(values[4], "1");
                    link.isPrivate = Objects.equals(values[5], "1");
                    link.timestamp = values[6];

                    addNewLink(link);
                }
            }
        }
    }

    // endregion

    // region Other methods

    private String getSortClause(int sortOption) {

        if(sortOption == R.id.rb_newest){
            return TIMESTAMP_COL + " DESC";
        }
        else if(sortOption == R.id.rb_oldest){
            return TIMESTAMP_COL + " ASC";
        }
        else {
            return TITLE_COL + " ASC";
        }
    }

    // endregion

}
