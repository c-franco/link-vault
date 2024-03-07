package com.example.linkvault.ui.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.linkvault.Constants;
import com.example.linkvault.LinkVaultBD;
import com.example.linkvault.MainActivity;
import com.example.linkvault.R;
import com.example.linkvault.databinding.FragmentSettingsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private View root;
    private LinkVaultBD dbHelper;

    private LinearLayout layout_language;
    private LinearLayout layout_export;
    private LinearLayout layout_import;
    private LinearLayout layout_delete;
    private Switch switch_darkmode;
    private TextView tv_version_number;
    private TextView tv_current_language_text;

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        setHasOptionsMenu(true);

        // Database setup
        dbHelper = new LinkVaultBD(getActivity());

        getComponents();
        setListeners();

        loadLanguage();
        loadDarkModeStatus();
        loadVersion();

        return root;
    }

    private void changeDarkModeStatus() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.KEY_DARK_MODE, switch_darkmode.isChecked());
        editor.apply();

        if(switch_darkmode.isChecked()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loadDarkModeStatus() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        boolean darkmode = preferences.getBoolean(Constants.KEY_DARK_MODE, false);
        switch_darkmode.setChecked(darkmode);
    }

    private void loadLanguage() {
        SharedPreferences preferences = this.getContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String languageCode = preferences.getString(Constants.KEY_LANGUAGE, Locale.getDefault().getLanguage());

        String language = "";

        if(languageCode.equals(Constants.ES_CODE)) {
            language = getString(R.string.language_spanish);
        }
        else if(languageCode.equals(Constants.EN_CODE)) {
            language = getString(R.string.language_english);
        }

        tv_current_language_text.setText(language);
    }

    private void loadVersion() {
        tv_version_number.setText(Constants.VERSION);
    }

    private void getComponents() {
        layout_language = root.findViewById(R.id.layout_language);
        layout_export = root.findViewById(R.id.layout_export);
        layout_import = root.findViewById(R.id.layout_import);
        layout_delete = root.findViewById(R.id.layout_delete);
        switch_darkmode = root.findViewById(R.id.switch_darkmode);
        tv_version_number = root.findViewById(R.id.tv_version_number);
        tv_current_language_text = root.findViewById(R.id.tv_current_language_text);
    }

    private void setListeners() {
        layout_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

        layout_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDataToCSV();
            }
        });

        layout_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        layout_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        switch_darkmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDarkModeStatus();
            }
        });
    }

    private void showLanguageDialog() {
        Dialog dialog = new Dialog(this.getContext());
        dialog.setContentView(R.layout.language_dialog);

        RadioGroup radioGroup = dialog.findViewById(R.id.rg_language_items);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        SharedPreferences preferences = this.getContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int lastCheckedId = 0;
        String languageCode = preferences.getString(Constants.KEY_LANGUAGE, Locale.getDefault().getLanguage());

        if(languageCode.equals(Constants.ES_CODE)) {
            lastCheckedId = R.id.rb_spanish;
        }
        else if(languageCode.equals(Constants.EN_CODE)) {
            lastCheckedId = R.id.rb_english;
        }

        radioGroup.check(lastCheckedId);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                SharedPreferences.Editor editor = preferences.edit();

                if (checkedId == R.id.rb_spanish) {
                    changeLanguage(Constants.ES_CODE);
                    editor.putString(Constants.KEY_LANGUAGE, Constants.ES_CODE);
                }
                else if (checkedId == R.id.rb_english) {
                    changeLanguage(Constants.EN_CODE);
                    editor.putString(Constants.KEY_LANGUAGE, Constants.EN_CODE);
                }

                refreshFragment();
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    private void refreshFragment() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.popBackStack();
        navController.navigate(R.id.navigation_settings);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshBottomNavView();
        }
    }

    private void changeLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = this.getActivity().getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void showDeleteDialog() {
        Dialog dialog = new Dialog(this.getContext());
        dialog.setContentView(R.layout.delete_item);

        Button cancelButton = dialog.findViewById(R.id.bt_cancel_delete);
        Button deleteButton = dialog.findViewById(R.id.bt_confirm_delete);
        TextView deleteText = dialog.findViewById(R.id.tv_delete_text);
        TextView subText = dialog.findViewById(R.id.tv_delete_text_message);

        subText.setVisibility(View.GONE);
        deleteText.setText(this.getContext().getString(R.string.text_delete_all_data));

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbHelper.deleteAllData();
                dialog.dismiss();
                Toast.makeText(v.getContext(), v.getContext().getString(R.string.text_data_deleted), Toast.LENGTH_SHORT).show();
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

    private void exportDataToCSV() {
        String csvData = dbHelper.exportToCSV();
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }

        File file = new File(Environment.getExternalStorageDirectory(), "linkvault_data.csv");
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file, true);

            outputStream.write(csvData.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.nav_toolbar);
            toolbar.setTitle(getString(R.string.title_settings));
        }

        MenuItem item_search = menu.findItem(R.id.item_search);
        MenuItem item_sort = menu.findItem(R.id.item_sort);

        if(item_search != null)
            item_search.setVisible(false);
        if(item_sort != null)
            item_sort.setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}