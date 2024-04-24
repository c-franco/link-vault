package com.breathink.linkvault.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
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
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.breathink.linkvault.Constants;
import com.breathink.linkvault.LinkVaultBD;
import com.breathink.linkvault.MainActivity;
import com.breathink.linkvault.R;
import com.breathink.linkvault.databinding.FragmentSettingsBinding;
import com.breathink.linkvault.ui.links.PrivateLinksActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    // region Variables

    private FragmentSettingsBinding binding;
    private View root;
    private LinkVaultBD dbHelper;

    // endregion

    // region View elements

    private LinearLayout layout_language;
    private LinearLayout layout_private_links;
    private LinearLayout layout_export;
    private LinearLayout layout_import;
    private LinearLayout layout_delete;
    private LinearLayout layout_more_apps;
    private Switch switch_darkmode;
    private TextView tv_version_number;
    private TextView tv_current_language_text;

    // endregion

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

    // region Setup

    private void getComponents() {
        layout_language = root.findViewById(R.id.layout_language);
        layout_private_links = root.findViewById(R.id.layout_private_links);
        layout_export = root.findViewById(R.id.layout_export);
        layout_import = root.findViewById(R.id.layout_import);
        layout_delete = root.findViewById(R.id.layout_delete);
        layout_more_apps = root.findViewById(R.id.layout_more_apps);
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

        layout_private_links.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateUser();
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
                if(checkPerms()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("text/comma-separated-values");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    startActivityForResult(intent, 4321);
                }
            }
        });

        layout_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        layout_more_apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMoreApps();
            }
        });


        switch_darkmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDarkModeStatus();
            }
        });
    }

    // endregion

    // region Events

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("text/comma-separated-values");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(intent, 4321);
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_export_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (resultCode == getActivity().RESULT_OK) {
                openPrivateLinks();
            }
        }

        if (requestCode == 4321 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                importDataFromCSV(uri);
            }
        }
    }

    // endregion

    // region Load settings

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

    private void loadDarkModeStatus() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        boolean darkmode = preferences.getBoolean(Constants.KEY_DARK_MODE, false);
        switch_darkmode.setChecked(darkmode);
    }

    private void loadVersion() {
        tv_version_number.setText(Constants.VERSION);
    }

    // endregion

    // region Import / Export

    private void importDataFromCSV(Uri uri) {

        StringBuilder csvData = new StringBuilder();

        try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                csvData.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String[] csvLines = csvData.toString().split("\n");
            dbHelper.importData(csvLines);
            Toast.makeText(getContext(), getString(R.string.text_import_correct), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(getContext(), getString(R.string.error_import), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportDataToCSV() {
        String csvData = dbHelper.exportToCSV();
        if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Constants.CSV_NAME);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(csvData.getBytes());
                fos.close();
                Toast.makeText(this.getContext(), getString(R.string.text_export_correct)
                        + " " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this.getContext(), getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this.getContext(), getString(R.string.error_export_permission), Toast.LENGTH_SHORT).show();
        }
    }

    // endregion

    // region Permissions

    private boolean checkPerms() {
        int read = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if(read != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return false;
        } else {
            return true;
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private void authenticateUser() {
        KeyguardManager keyguardManager = requireContext().getSystemService(KeyguardManager.class);
        if (keyguardManager != null && keyguardManager.isKeyguardSecure()) {
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.text_confirm_identity))
                    .setSubtitle(getString(R.string.text_private_links))
                    .setNegativeButtonText(getString(R.string.text_cancel))
                    .build();

            BiometricPrompt biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            if (keyguardManager.isKeyguardSecure() && errorCode != 10 && errorCode != 13) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
                                    if (intent != null) {
                                        startActivityForResult(intent, 1234);
                                    }
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.text_wrong_version), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            openPrivateLinks();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                        }
                    });

            biometricPrompt.authenticate(promptInfo);
        } else {
            openPrivateLinks();
        }
    }

    // endregion

    // region Other methods

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

    private void openPrivateLinks() {
        Intent intent = new Intent(this.getContext(), PrivateLinksActivity.class);
        this.getContext().startActivity(intent);
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

    private void openMoreApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=pub:" + Constants.DEV_NAME));
        try{
            startActivity(intent);
        }
        catch(Exception e){
            intent.setData(Uri.parse("http://play.google.com/store/search?q=pub:" + Constants.DEV_NAME));
        }
    }

    // endregion

}