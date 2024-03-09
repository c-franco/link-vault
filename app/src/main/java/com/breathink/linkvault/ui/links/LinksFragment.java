package com.breathink.linkvault.ui.links;

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
import com.breathink.linkvault.databinding.FragmentLinksBinding;
import com.breathink.linkvault.models.Link;

import java.util.ArrayList;
import java.util.List;

public class LinksFragment extends Fragment {

    // region Variables

    private FragmentLinksBinding binding;
    private LinkVaultBD dbHelper;
    private LinksAdapter linksAdapter;
    private List<Link> linkList;

    // endregion

    // region View elements

    private TextView tv_empty_link_list;
    private static RecyclerView recyclerView_links;

    // endregion

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLinksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        startSetup();

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
            toolbar.setTitle(getString(R.string.title_links));
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

    private void startSetup() {
        // Database setup
        dbHelper = new LinkVaultBD(getActivity());

        // RecyclerView setup
        recyclerView_links = binding.getRoot().findViewById(R.id.recyclerView_links);
        recyclerView_links.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        tv_empty_link_list = binding.getRoot().findViewById(R.id.tv_empty_link_list);
        loadRecyclerViewData();
    }

    private void loadRecyclerViewData() {
        linkList = dbHelper.getAllLinks(false, false);
        linksAdapter = new LinksAdapter(linkList, (MainActivity) getActivity(), null, null);
        recyclerView_links.setAdapter(linksAdapter);
        tv_empty_link_list.setVisibility(linkList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void searchLinks(String newText) {
        List<Link> filteredList = filterLinks(linkList, newText);
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