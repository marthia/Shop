package com.poonehmedia.app.ui.comments;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.databinding.ListItemCommentAltBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

public class CommentsAdapter extends PagingDataAdapter<Comment, CommentsAdapter.ViewHolder> {

    private ClickProvider clickProvider;
    private ClickProvider navigationCallback;
    private ClickProvider onReply;

    public CommentsAdapter() {
        super(new Comment.CommentDiff());
    }

    public void subscribeClick(ClickProvider clickProvider) {
        this.clickProvider = clickProvider;
    }

    public void subscribeReply(ClickProvider clickProvider) {
        this.onReply = clickProvider;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemCommentAltBinding binding = ListItemCommentAltBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bind(item);
    }

    public void subscribeReport(ClickProvider navigationCallback) {
        this.navigationCallback = navigationCallback;
    }

    public void subscribeShowMessage(ClickProvider clickProvider) {

    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemCommentAltBinding binding;

        public ViewHolder(@NonNull ListItemCommentAltBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Comment item) {
            binding.setItem(item);


            binding.setDislikeClick(v -> {
//                if (item.isDislikeEnabled())
                clickProvider.onClick(item.getDislikeLink(), getAbsoluteAdapterPosition());
//                else onShowMessage.onClick(item, getAbsoluteAdapterPosition());
            });

            binding.setLikeClick(v -> {
//                if (item.isLikeEnabled())
                clickProvider.onClick(item.getLikeLink(), getAbsoluteAdapterPosition());
//                else onShowMessage.onClick(item, getAbsoluteAdapterPosition());
            });

            binding.setInappropriateClick(v -> {
                navigationCallback.onClick(item, getAbsoluteAdapterPosition());
            });

            binding.setReplyClick(v -> {
                onReply.onClick(item.getReplyLink(), getAbsoluteAdapterPosition());
            });

        }
    }
}
