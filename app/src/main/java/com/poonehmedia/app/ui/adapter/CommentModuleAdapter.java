package com.poonehmedia.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.databinding.ListItemCommentModuleAltBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

public class CommentModuleAdapter extends ListAdapter<Comment, CommentModuleAdapter.ViewHolder> {

    private ClickProvider clickProvider;

    public CommentModuleAdapter() {
        super(new Comment.CommentDiff());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListItemCommentModuleAltBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent,
                        false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bind(item);
    }

    public void subscribeClick(ClickProvider clickProvider) {

        this.clickProvider = clickProvider;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemCommentModuleAltBinding binding;

        public ViewHolder(ListItemCommentModuleAltBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Comment item) {
            binding.setItem(item);
            itemView.setOnClickListener(v -> clickProvider.onClick(item, getAbsoluteAdapterPosition()));
            // binding.executePendingBindings();
        }
    }
}
