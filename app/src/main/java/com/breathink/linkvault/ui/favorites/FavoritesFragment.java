package com.breathink.linkvault.ui.favorites;

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

import com.breathink.linkvault.LinkVaultBD;
import com.breathink.linkvault.MainActivity;
import com.breathink.linkvault.R;
import com.breathink.linkvault.databinding.FragmentFavoritesBinding;
import com.breathink.linkvault.models.Link;
import com.breathink.linkvault.ui.links.LinksAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private LinkVaultBD dbHelper;
    private LinksAdapter linksAdapter;
    private List<Link> favoritesList;

    private TextView tv_empty_favorites_list;
    private static RecyclerView recyclerView_favorites;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        // Database setup
        dbHelper = new LinkVaultBD(getActivity());

        // RecyclerView setup
        recyclerView_favorites = binding.getRoot().findViewById(R.id.recyclerView_favorites);
        recyclerView_favorites.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        tv_empty_favorites_list = binding.getRoot().findViewById(R.id.tv_empty_favorites_list);
        loadRecyclerViewData();

        return root;
    }

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
            toolbar.setTitle(getString(R.string.title_favorites));
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
        favoritesList = dbHelper.getFavoriteLinks();
        linksAdapter = new LinksAdapter(favoritesList, (MainActivity) getActivity(), null, null);
        recyclerView_favorites.setAdapter(linksAdapter);
        tv_empty_favorites_list.setVisibility(favoritesList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void searchLinks(String newText) {
        List<Link> filteredList = filterLinks(favoritesList, newText);
        linksAdapter.setFilter(filteredList);
    }

    private List<Link> filterLinks(List<Link> originalList, String query) {
        List<Link> filteredList = new ArrayList<>();

        if (originalList != null) {
            String lowerCaseQuery = query.toLowerCase();

            for (Link link : originalList) {
                if (link.title.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(link);
                }
                else if (link.url.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(link);
                }
            }
        }

        return filteredList;
    }

    //endregion
}