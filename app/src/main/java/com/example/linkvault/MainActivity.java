package com.example.linkvault;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.linkvault.databinding.ActivityMainBinding;
import com.example.linkvault.models.Category;
import com.example.linkvault.models.Link;
import com.example.linkvault.ui.categories.CategoriesFragment;
import com.example.linkvault.ui.favorites.FavoritesFragment;
import com.example.linkvault.ui.links.LinksFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LinkVaultBD dbHelper;
    private AutoCompleteTextView auto_complete_textView;
    private SearchView searchView;

    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Singleton.getInstance().setMainActivityInstance(this);

        // Database setup
        dbHelper = new LinkVaultBD(MainActivity.this);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.nav_toolbar);
        setSupportActionBar(toolbar);

        // Navigation setup
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Desactivar modo noche por defecto
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setListeners();
        createCategories();
    }

    //region Events

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.item_search);
        searchView = (SearchView) searchMenuItem.getActionView();

        Fragment currentFragment = getCurrentFragment();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (currentFragment instanceof LinksFragment) {
                    LinksFragment linksFragment = (LinksFragment) currentFragment;
                    linksFragment.searchLinks(newText);
                }
                else if (currentFragment instanceof CategoriesFragment) {
                    CategoriesFragment categoriesFragment = (CategoriesFragment) currentFragment;
                    categoriesFragment.searchCategories(newText);
                }
                else if (currentFragment instanceof FavoritesFragment) {
                    FavoritesFragment favoritesFragment = (FavoritesFragment) currentFragment;
                    favoritesFragment.searchLinks(newText);
                }

                return false;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                menu.findItem(R.id.item_sort).setVisible(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                menu.findItem(R.id.item_sort).setVisible(false);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_sort) {
            showSortOptionsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        binding.fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    //endregion

    // region Shared Preferences

    private boolean isFirstTime() {

        SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(Constants.KEY_FIRST_TIME, true);
    }

    private void setFirstTime() {

        SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.KEY_FIRST_TIME, false);
        editor.apply();
    }

    //endregion

    // region Dialogs

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet);

        LinearLayout linksLayout = dialog.findViewById(R.id.layout_links);
        LinearLayout categoriesLayout = dialog.findViewById(R.id.layout_categories);
        ImageView cancelButton = dialog.findViewById(R.id.cancel_button);

        linksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                newLinkDialog(null, false);
            }
        });

        categoriesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                newCategoryDialog(false, false, null);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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
                    Toast.makeText(MainActivity.this, getString(R.string.error_title_link_empty), Toast.LENGTH_SHORT).show();
                }
                else if(!validString(url)) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_url_empty), Toast.LENGTH_SHORT).show();
                }
                else if(!validUrl(url)) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_url_not_valid), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, getString(R.string.error_db_link), Toast.LENGTH_SHORT).show();
                    }

                    dialog.dismiss();
                    refreshRecyclerView();
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
                    Toast.makeText(MainActivity.this, getString(R.string.error_title_category_empty), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, getString(R.string.error_db_category), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    refreshRecyclerView();
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

    private void showSortOptionsDialog() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.sort_dialog);

        RadioGroup radioGroup = dialog.findViewById(R.id.rg_sort_items);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        Fragment currentFragment = getCurrentFragment();
        SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        int lastCheckedId = 0;

        if (currentFragment instanceof LinksFragment) {
            lastCheckedId = preferences.getInt(Constants.KEY_SORT_LINK, R.id.rb_title);
        }
        else if (currentFragment instanceof CategoriesFragment) {
            lastCheckedId = preferences.getInt(Constants.KEY_SORT_CAT, R.id.rb_title);
        }
        else if (currentFragment instanceof FavoritesFragment) {
            lastCheckedId = preferences.getInt(Constants.KEY_SORT_FAV, R.id.rb_title);
        }
        radioGroup.check(lastCheckedId);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                SharedPreferences.Editor editor = preferences.edit();

                if (currentFragment instanceof LinksFragment) {
                    editor.putInt(Constants.KEY_SORT_LINK, checkedId);
                }
                else if (currentFragment instanceof CategoriesFragment) {
                    editor.putInt(Constants.KEY_SORT_CAT, checkedId);
                }
                else if (currentFragment instanceof FavoritesFragment) {
                    editor.putInt(Constants.KEY_SORT_FAV, checkedId);
                }

                editor.apply();

                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                refreshRecyclerView();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    //endregion

    //region Other Methods

    private void createCategories() {

        if(isFirstTime()) {
            Category category0 = new Category(getString(R.string.default_category0));
            Category category1 = new Category(getString(R.string.default_category1));
            Category category2 = new Category(getString(R.string.default_category2));
            Category category3 = new Category(getString(R.string.default_category3));
            Category category4 = new Category(getString(R.string.default_category4));
            Category category5 = new Category(getString(R.string.default_category5));
            Category category6 = new Category(getString(R.string.default_category6));

            List<Category> categoryList = new ArrayList<>();
            categoryList.add(category0);
            categoryList.add(category1);
            categoryList.add(category2);
            categoryList.add(category3);
            categoryList.add(category4);
            categoryList.add(category5);
            categoryList.add(category6);

            for (Category category : categoryList) {
                dbHelper.addNewCategory(category);
            }

            setFirstTime();
        }
    }

    private void loadCategories() {

        List<String> categoryTitles = dbHelper.getAllCategoryTitles();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_item, categoryTitles);

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

    private Fragment getCurrentFragment() {

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

        return currentFragment;
    }

    public void refreshRecyclerView() {
        Fragment currentFragment = getCurrentFragment();

        if (currentFragment instanceof LinksFragment) {
            LinksFragment linksFragment = (LinksFragment) currentFragment;
            linksFragment.OnCreatedLinkListener();
        }
        else if (currentFragment instanceof CategoriesFragment) {
            CategoriesFragment categoriesFragment = (CategoriesFragment) currentFragment;
            categoriesFragment.OnCreatedLinkListener();
        }
        else if (currentFragment instanceof FavoritesFragment) {
            FavoritesFragment favoritesFragment = (FavoritesFragment) currentFragment;
            favoritesFragment.OnCreatedLinkListener();
        }
    }

    public MainActivity getMainActivity() {
        return MainActivity.this;
    }

    //endregion
}