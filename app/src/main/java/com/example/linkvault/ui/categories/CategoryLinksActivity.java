package com.example.linkvault.ui.categories;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkvault.LinkVaultBD;
import com.example.linkvault.MainActivity;
import com.example.linkvault.R;
import com.example.linkvault.Singleton;
import com.example.linkvault.models.Category;
import com.example.linkvault.models.Link;
import com.example.linkvault.ui.links.LinksAdapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryLinksActivity extends AppCompatActivity {

    private MainActivity mainActivity;
    private LinkVaultBD dbHelper;
    private LinksAdapter linksAdapter;
    private List<Link> linkList;
    public int categoryId;
    public String categoryTitle;
    private String selectedCategory;

    private AutoCompleteTextView auto_complete_textView;

    private TextView tv_empty_link_list_category;
    private static RecyclerView recyclerView_links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_links);

        mainActivity = Singleton.getInstance().getMainActivityInstance();
        if (mainActivity != null) {
            mainActivity.getMainActivity();
        }

        Intent intent = getIntent();
        categoryId = intent.getIntExtra("categoryId", 1);
        categoryTitle = intent.getStringExtra("categoryTitle");

        Toolbar toolbar = findViewById(R.id.nav_toolbar);
        toolbar.setTitle(categoryTitle);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Database setup
        dbHelper = new LinkVaultBD(this);

        // RecyclerView setup
        recyclerView_links = findViewById(R.id.recyclerView_links_in_category);
        recyclerView_links.setLayoutManager(new LinearLayoutManager(this));
        tv_empty_link_list_category = findViewById(R.id.tv_empty_link_list_category);
        loadRecyclerViewData();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void newLinkDialog(Link link, boolean edit) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.link_detail);

        Button cancelButton = dialog.findViewById(R.id.bt_cancel_link);
        Button createButton = dialog.findViewById(R.id.bt_create_link);
        TextView dialogTitle = dialog.findViewById(R.id.tv_create_link);
        TextView createCategory = dialog.findViewById(R.id.bt_create_category_inlink);
        TextView titleText = dialog.findViewById(R.id.et_title_link);
        TextView urlText = dialog.findViewById(R.id.et_url_link);
        CheckBox isFavorite = dialog.findViewById(R.id.cb_favorite);
        CheckBox isPrivate = dialog.findViewById(R.id.cb_private);
        auto_complete_textView = dialog.findViewById(R.id.auto_complete_textView);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        int id = 0;
        auto_complete_textView.setText(getString(R.string.default_category0));

        if(edit) {
            dialogTitle.setText(getString(R.string.text_modify_link));
            isFavorite.setText((R.string.text_add_favorite));
            isPrivate.setText((R.string.text_add_private));
            cancelButton.setText(R.string.text_cancel);
            createButton.setText(getString(R.string.text_update));

            selectedCategory = dbHelper.getCategoryTitle(link.idCategory);

            id = link.id;
            titleText.setText(link.title);
            urlText.setText(link.url);
            auto_complete_textView.setText(selectedCategory);
            isFavorite.setChecked(link.isFavorite);
            isPrivate.setChecked(link.isPrivate);
        }

        loadCategories();

        int finalId = id;
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = titleText.getText().toString();
                String url = urlText.getText().toString();

                if(!validString(title)) {
                    Toast.makeText(CategoryLinksActivity.this, getString(R.string.error_title_link_empty), Toast.LENGTH_SHORT).show();
                }
                else if(!validString(url)) {
                    Toast.makeText(CategoryLinksActivity.this, getString(R.string.error_url_empty), Toast.LENGTH_SHORT).show();
                }
                else if(!validUrl(url)) {
                    Toast.makeText(CategoryLinksActivity.this, getString(R.string.error_url_not_valid), Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        int idCategory = dbHelper.getCategoryId(selectedCategory);

                        Link link = new Link();
                        link.title = title;
                        link.url = url;
                        link.idCategory = idCategory;
                        link.isFavorite = isFavorite.isChecked();
                        link.isPrivate = isPrivate.isChecked();

                        if(edit) {
                            link.id = finalId;
                            dbHelper.updateLink(link);
                        }
                        else
                            dbHelper.addNewLink(link);

                    } catch (Exception ex) {
                        Toast.makeText(CategoryLinksActivity.this, getString(R.string.error_db_link), Toast.LENGTH_SHORT).show();
                    }

                    dialog.dismiss();
                    loadRecyclerViewData();
                }
            }
        });

        createCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCategoryDialog(true, false, null);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    public void newCategoryDialog(boolean parent, boolean edit, Category category) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.category_detail);

        Button cancelButton = dialog.findViewById(R.id.bt_cancel_category);
        Button createButton = dialog.findViewById(R.id.bt_create_category);
        TextView titleText = dialog.findViewById(R.id.et_title_category);
        TextView dialogTitle = dialog.findViewById(R.id.tv_create_category);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        int id = 0;

        if(edit) {
            dialogTitle.setText(getString(R.string.text_modify_category));
            cancelButton.setText(R.string.text_cancel);
            createButton.setText(getString(R.string.text_update));

            id = category.id;
            titleText.setText(category.title);
        }

        int finalId = id;
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = titleText.getText().toString();

                if(!validString(title)) {
                    Toast.makeText(CategoryLinksActivity.this, getString(R.string.error_title_category_empty), Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        Category category = new Category();
                        category.title = title;

                        if (edit) {
                            category.id = finalId;
                            dbHelper.updateCategory(category);
                        }
                        else {
                            dbHelper.addNewCategory(category);
                        }

                    } catch (Exception ex) {
                        Toast.makeText(CategoryLinksActivity.this, getString(R.string.error_db_category), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    loadRecyclerViewData();
                }
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                if(parent)
                    loadCategories();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    public void loadRecyclerViewData() {
        linkList = dbHelper.getLinksByCategoryId(categoryId);
        linksAdapter = new LinksAdapter(linkList, null, CategoryLinksActivity.this);
        recyclerView_links.setAdapter(linksAdapter);
        tv_empty_link_list_category.setVisibility(linkList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadCategories() {

        List<String> categoryTitles = dbHelper.getAllCategoryTitles();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CategoryLinksActivity.this, R.layout.list_item, categoryTitles);

        auto_complete_textView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        auto_complete_textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (String) parent.getItemAtPosition(position);
            }
        });
    }

    public boolean validUrl(String url) {

        Pattern patron = Pattern.compile("^(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?$");
        Matcher mat = patron.matcher(url);

        return mat.matches();
    }

    public boolean validString(String string) {

        Pattern patron = Pattern.compile("^(?!\\s*$).+");

        Matcher mat = patron.matcher(string);

        return mat.matches();
    }
}



