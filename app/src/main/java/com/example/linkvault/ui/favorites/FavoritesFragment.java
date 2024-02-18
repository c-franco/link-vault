package com.example.linkvault.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.linkvault.R;
import com.example.linkvault.databinding.FragmentFavoritesBinding;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}