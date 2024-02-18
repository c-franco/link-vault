package com.example.linkvault;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.linkvault.databinding.ActivityMainBinding;
import com.example.linkvault.models.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_FIRST_TIME = "firstTime";

    private ActivityMainBinding binding;
    private LinkVaultBD dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        return true;
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

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_FIRST_TIME, true);
    }

    private void setFirstTime() {

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, false);
        editor.apply();
    }

    //endregion

    //region Other Methods

    private void createCategories() {

        if(isFirstTime()) {
            Category category1 = new Category("Categoría 1");
            Category category2 = new Category("Categoría 2");
            Category category3 = new Category("Categoría 3");
            Category category4 = new Category("Categoría 4");
            Category category5 = new Category("Categoría 5");
            Category category6 = new Category("Categoría 6");

            List<Category> categoryList = new ArrayList<>();
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
                Toast.makeText(MainActivity.this,"Upload a Video is clicked",Toast.LENGTH_SHORT).show();
            }
        });

        categoriesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(MainActivity.this,"Create a short is Clicked",Toast.LENGTH_SHORT).show();
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

    //endregion
}