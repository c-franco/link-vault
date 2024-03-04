package com.example.linkvault.ui.categories;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkvault.LinkVaultBD;
import com.example.linkvault.MainActivity;
import com.example.linkvault.R;
import com.example.linkvault.databinding.FragmentCategoriesBinding;
import com.example.linkvault.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private FragmentCategoriesBinding binding;
    private LinkVaultBD dbHelper;
    private CategoriesAdapter categoriesAdapter;
    private List<Category> categoryList;

    private TextView tv_empty_category_list;
    private static RecyclerView recyclerView_categories;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        // Database setup
        dbHelper = new LinkVaultBD(getActivity());

        // RecyclerView setup
        recyclerView_categories = binding.getRoot().findViewById(R.id.recyclerView_categories);
        recyclerView_categories.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        tv_empty_category_list = binding.getRoot().findViewById(R.id.tv_empty_category_list);
        loadRecyclerViewData();

        return root;
    }

    // region Events

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.nav_toolbar);
            toolbar.setTitle(getString(R.string.title_categories));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void OnCreatedLinkListener() {
        loadRecyclerViewData();
    }

    // endregion

    // region Other methods

    private void loadRecyclerViewData() {
        categoryList = dbHelper.getAllCategories();
        categoriesAdapter = new CategoriesAdapter(categoryList, (MainActivity) getActivity());
        recyclerView_categories.setAdapter(categoriesAdapter);
        tv_empty_category_list.setVisibility(categoryList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void searchCategories(String newText) {
        List<Category> filteredList = filterLinks(categoryList, newText);
        categoriesAdapter.setFilter(filteredList);
    }

    private List<Category> filterLinks(List<Category> originalList, String query) {
        List<Category> filteredList = new ArrayList<>();

        if (originalList != null) {
            String lowerCaseQuery = query.toLowerCase();

            for (Category category : originalList) {
                if (category.title.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(category);
                }
            }
        }

        return filteredList;
    }

    //endregion
}