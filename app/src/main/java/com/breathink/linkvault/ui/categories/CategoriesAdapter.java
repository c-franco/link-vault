package com.breathink.linkvault.ui.categories;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.breathink.linkvault.LinkVaultBD;
import com.breathink.linkvault.MainActivity;
import com.breathink.linkvault.R;
import com.breathink.linkvault.models.Category;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Category> localDataSet;
    private MainActivity context;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_category_title;
        private final TextView tv_enter_category;
        private final TextView tv_more_icon_category;
        private final LinearLayout linearLayout;

        public ViewHolder(View view) {
            super(view);

            tv_category_title = view.findViewById(R.id.tv_category_title);
            tv_enter_category = view.findViewById(R.id.tv_enter_category);
            tv_more_icon_category = view.findViewById(R.id.tv_more_icon_category);
            linearLayout = view.findViewById(R.id.linear_layout_category);
        }

        public TextView getCategoryTextView() {
            return tv_category_title;
        }

    }

    public CategoriesAdapter(List<Category> dataSet, MainActivity mainActivity) {
        localDataSet = dataSet;
        context = mainActivity;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_category, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        Category category = localDataSet.get(position);

        viewHolder.getCategoryTextView().setText(category.title);

        viewHolder.tv_enter_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryLinks(v, category);
            }
        });

        viewHolder.tv_more_icon_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), viewHolder.tv_more_icon_category);
                popup.inflate(R.menu.options_menu);

                Menu menu = popup.getMenu();
                MenuItem deleteItem = menu.findItem(R.id.option_delete);
                deleteItem.setVisible(category.id != 1);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId() == R.id.option_edit) {
                            editCategory(category);
                        }
                        else if(item.getItemId() == R.id.option_delete) {
                            deleteCategory(v, category.id);
                        }
                        else if(item.getItemId() == R.id.option_share) {
                            shareCategory(v, category);
                        }

                        return false;
                    }
                });

                popup.show();
            }
        });

        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryLinks(v, category);
            }
        });
    }

    private void showCategoryLinks(View v, Category category) {

        Intent intent = new Intent(context, CategoryLinksActivity.class);
        intent.putExtra("categoryId", category.id);
        intent.putExtra("categoryTitle", category.title);

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilter(List<Category> newList) {
        localDataSet = newList;
        notifyDataSetChanged();
    }

    private void editCategory(Category category) {
        context.newCategoryDialog(false, true, category);
    }

    private void deleteCategory(View v, int id) {
        Dialog dialog = new Dialog(v.getContext());
        dialog.setContentView(R.layout.delete_item);

        Button cancelButton = dialog.findViewById(R.id.bt_cancel_delete);
        Button deleteButton = dialog.findViewById(R.id.bt_confirm_delete);
        TextView tv_delete_text = dialog.findViewById(R.id.tv_delete_text);
        TextView subText = dialog.findViewById(R.id.tv_delete_text_message);

        subText.setVisibility(View.VISIBLE);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        tv_delete_text.setText(v.getContext().getString(R.string.text_delete_category));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinkVaultBD dbHelper = new LinkVaultBD(v.getContext());

                dbHelper.deleteCategoryById(id);
                context.refreshRecyclerView();

                dialog.dismiss();
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

    private void shareCategory(View v, Category category) {

        LinkVaultBD dbHelper = new LinkVaultBD(v.getContext());
        List<String> urlList = dbHelper.getLinkUrlsByCategory(category.id);

        if(urlList != null && urlList.size() > 0) {
            String shareString = getUrlString(urlList, category);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);

            Intent clipboardIntent = new Intent(v.getContext(), MainActivity.class);
            clipboardIntent.setData(Uri.parse(shareString));

            Intent chooserIntent = Intent.createChooser(shareIntent, v.getContext().getString(R.string.item_copy));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { clipboardIntent });

            v.getContext().startActivity(chooserIntent);
        }
        else {
            Toast.makeText(v.getContext(), v.getContext().getString(R.string.text_no_links_in_category), Toast.LENGTH_SHORT).show();
        }
    }

    private String getUrlString(List<String> urlList, Category category) {

        StringBuilder urlString = new StringBuilder();
        urlString.append(category.title).append(": ").append("\n");

        for (String url : urlList) {
            urlString.append(url).append("\n");
        }

        return urlString.toString();
    }
}


