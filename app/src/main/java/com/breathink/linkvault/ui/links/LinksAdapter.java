package com.breathink.linkvault.ui.links;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.breathink.linkvault.LinkVaultBD;
import com.breathink.linkvault.MainActivity;
import com.breathink.linkvault.R;
import com.breathink.linkvault.Utils;
import com.breathink.linkvault.models.Link;
import com.breathink.linkvault.ui.categories.CategoryLinksActivity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.ViewHolder> {

    public List<Link> localDataSet;
    private MainActivity context;
    private CategoryLinksActivity categoryLinksActivity;
    private PrivateLinksActivity privateLinksActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout linearLayout;
        private final TextView tv_link_title;
        private final TextView tv_link_url;
        private final TextView tv_goto_icon;
        private final TextView tv_more_icon;

        public ViewHolder(View view) {
            super(view);

            tv_link_title = view.findViewById(R.id.tv_link_title);
            tv_link_url = view.findViewById(R.id.tv_link_url);
            tv_goto_icon = view.findViewById(R.id.tv_goto_icon);
            tv_more_icon = view.findViewById(R.id.tv_more_icon);
            linearLayout = view.findViewById(R.id.linear_layout_link);
        }

        public TextView getLinkTextView() {
            return tv_link_title;
        }

        public TextView getUrlTextView() {
            return tv_link_url;
        }
    }

    public LinksAdapter(List<Link> dataSet, MainActivity mainActivity, CategoryLinksActivity _categoryLinksActivity, PrivateLinksActivity _privateLinksActivity) {
        localDataSet = dataSet;
        context = mainActivity;
        categoryLinksActivity = _categoryLinksActivity;
        privateLinksActivity = _privateLinksActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_link, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        Link link = localDataSet.get(position);

        viewHolder.getLinkTextView().setText(link.title);
        viewHolder.getUrlTextView().setText(link.url);

        viewHolder.tv_goto_icon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Utils.formatUrl(link.url)));
                v.getContext().startActivity(intent);
            }
        });

        viewHolder.tv_more_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), viewHolder.tv_more_icon);
                popup.inflate(R.menu.options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId() == R.id.option_edit) {
                            editLink(link);
                        }
                        else if(item.getItemId() == R.id.option_delete) {
                            deleteLink(v, link.id);
                        }
                        else if(item.getItemId() == R.id.option_share) {
                            shareLink(v, link.url);
                        }
                        else if(item.getItemId() == R.id.option_shortcut) {
                            createShortcut(v, link);
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
                Activity activity = null;

                if(context != null) {
                    activity = context;
                } else if(categoryLinksActivity != null) {
                    activity = categoryLinksActivity;
                } else if(privateLinksActivity != null) {
                    activity = privateLinksActivity;
                }

                ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("URL", link.url);
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(activity, v.getContext().getString(R.string.text_clipboard), Toast.LENGTH_SHORT).show();
            }
        });

        if(categoryLinksActivity != null) {
            viewHolder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    link.setSelected(!link.isSelected());
                    viewHolder.linearLayout.setBackgroundColor(link.isSelected() ?
                            categoryLinksActivity.getColor(R.color.select_color) : categoryLinksActivity.getColor(R.color.settings));

                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    private void editLink(Link link) {

        if(context != null) {
            context.newLinkDialog(link, true);
        } else if(categoryLinksActivity != null){
            categoryLinksActivity.newLinkDialog(link, true);
        } else if(privateLinksActivity != null){
            privateLinksActivity.newLinkDialog(link, true);
        }
    }

    private void deleteLink(View v, int id) {
        Dialog dialog = new Dialog(v.getContext());
        dialog.setContentView(R.layout.delete_item);

        Button cancelButton = dialog.findViewById(R.id.bt_cancel_delete);
        Button deleteButton = dialog.findViewById(R.id.bt_confirm_delete);
        TextView subText = dialog.findViewById(R.id.tv_delete_text_message);

        subText.setVisibility(View.GONE);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinkVaultBD dbHelper = new LinkVaultBD(v.getContext());

                dbHelper.deleteLinkById(id);

                if(context != null) {
                    context.refreshRecyclerView();
                } else if(categoryLinksActivity != null){
                    categoryLinksActivity.loadRecyclerViewData();
                } else if(privateLinksActivity != null){
                    privateLinksActivity.loadRecyclerViewData();
                }

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

    private void shareLink(View v, String url) {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);

        Intent clipboardIntent = new Intent(v.getContext(), MainActivity.class);
        clipboardIntent.setData(Uri.parse(url));

        Intent chooserIntent = Intent.createChooser(shareIntent, v.getContext().getString(R.string.item_copy));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { clipboardIntent });

        v.getContext().startActivity(chooserIntent);
    }

    private void createShortcut(View v, Link link) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(Utils.formatUrl(link.url)));

            ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                    .setShortLabel(link.title)
                    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_links_24dp))
                    .setIntent(browserIntent)
                    .build();

            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilter(List<Link> newList) {
        localDataSet = newList;
        notifyDataSetChanged();
    }
}

