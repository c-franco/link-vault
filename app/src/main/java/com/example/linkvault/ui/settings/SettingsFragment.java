package com.example.linkvault.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.linkvault.R;
import com.example.linkvault.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item_search = menu.findItem(R.id.item_search);
        MenuItem item_add = menu.findItem(R.id.item_add);
        MenuItem prueba1 = menu.findItem(R.id.prueba1);
        MenuItem prueba2 = menu.findItem(R.id.prueba2);

        if(item_search != null)
            item_search.setVisible(false);
        if(item_add != null)
            item_add.setVisible(false);
        if(prueba1 != null)
            prueba1.setVisible(false);
        if(prueba2 != null)
            prueba2.setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}