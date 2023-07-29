package com.poonehmedia.app.ui.player;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.R;
import com.poonehmedia.app.components.player.PlayerController;
import com.poonehmedia.app.components.player.VideoPlayer;
import com.poonehmedia.app.data.model.GalleryItem;
import com.poonehmedia.app.databinding.PageItemImageBinding;

import java.util.List;

import javax.inject.Inject;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private List<GalleryItem> items;
    private VideoPlayer player;
    private AudioManager mAudioManager;

    @Inject
    public GalleryAdapter() {
    }

    public void submitList(List<GalleryItem> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        PageItemImageBinding binding = PageItemImageBinding.inflate(inflater, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GalleryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void notifyOnDestroy() {
        if (player != null) {
            player.releasePlayer();
            player = null;
        }
    }

    public void notifyPageChanged() {
        if (player != null) {
            if (player.isPlaying())
                player.pausePlayer();
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PlayerController {
        private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        switch (focusChange) {
                            case AudioManager.AUDIOFOCUS_GAIN:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                                if (player != null)
                                    player.resumePlayer();
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            case AudioManager.AUDIOFOCUS_LOSS:
                                // Lost audio focus, probably "permanently"
                                // Lost audio focus, but will gain it back (shortly), so note whether
                                // playback should resume
                                if (player != null)
                                    player.pausePlayer();
                                break;
                        }
                    }
                };
        private final com.poonehmedia.app.databinding.PageItemImageBinding binding;
        private ImageButton mute, unMute, retry;

        public ViewHolder(@NonNull PageItemImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GalleryItem item) {
            String url = item.getImageUrlOrVideoThumb();
            binding.setUrl(url);

            if (item.getVideoLink() != null) {
                itemView.setOnClickListener(v -> {
                    binding.image.setVisibility(View.GONE);
                    binding.exoPlayer.setVisibility(View.VISIBLE);
                    initExoPlayer();
                    setSource(item.getVideoLink());

                });
            } else {
                binding.exoPlayer.setVisibility(View.GONE);
                binding.image.setVisibility(View.VISIBLE);
            }
            // binding.executePendingBindings();
        }

        private void initExoPlayer() {
            binding.progressBar.setVisibility(View.VISIBLE);
            mute = binding.exoPlayer.findViewById(R.id.btn_mute);
            unMute = binding.exoPlayer.findViewById(R.id.btn_unMute);
            retry = binding.exoPlayer.findViewById(R.id.retry_btn);

            //optional setting
            binding.exoPlayer.getSubtitleView().setVisibility(View.GONE);
            binding.exoPlayer.setControllerVisibilityListener(visibility -> {
                if (visibility == View.VISIBLE) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.exoPlayer.hideController();
                    }, 2000);
                }
            });

            mute.setOnClickListener(this);
            unMute.setOnClickListener(this);
            retry.setOnClickListener(this);
        }

        private void setSource(String url) {

            player = new VideoPlayer(binding.exoPlayer, itemView.getContext(), url, this);

            mAudioManager = (AudioManager) itemView.getContext().getSystemService(Context.AUDIO_SERVICE);

            player.seekToOnDoubleTap();

        }

        @Override
        public void onClick(View v) {
            int controllerId = v.getId();

            if (controllerId == R.id.btn_mute) {
                player.setMute(true);
            } else if (controllerId == R.id.btn_unMute) {
                player.setMute(false);
            } else if (controllerId == R.id.exo_rew) {
                player.seekToSelectedPosition(0, true);
            } else if (controllerId == R.id.retry_btn) {
                setSource(items.get(getAbsoluteAdapterPosition()).getVideoLink());
                showProgressBar(true);
                showRetryBtn(false);
            }
        }

        @Override
        public void setMuteMode(boolean mute) {
            if (player != null) {
                if (mute) {
                    this.mute.setVisibility(View.GONE);
                    unMute.setVisibility(View.VISIBLE);
                } else {
                    unMute.setVisibility(View.GONE);
                    this.mute.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void showProgressBar(boolean visible) {
            binding.progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        @Override
        public void showRetryBtn(boolean visible) {
            retry.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        @Override
        public void audioFocus() {
            mAudioManager.requestAudioFocus(
                    mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

}
