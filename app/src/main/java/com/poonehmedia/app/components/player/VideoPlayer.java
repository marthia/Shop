package com.poonehmedia.app.components.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;


public class VideoPlayer {

    private final String TAG = getClass().getSimpleName();
    private final Context context;
    private final PlayerController playerController;

    private final PlayerView playerView;
    private final String videoSource;
    private SimpleExoPlayer exoPlayer;
    private DefaultTrackSelector trackSelector;
    private int widthOfScreen;
    private ComponentListener componentListener;

    public VideoPlayer(PlayerView playerView,
                       Context context,
                       String videoSource,
                       PlayerController mView) {

        this.playerView = playerView;
        this.context = context;
        this.playerController = mView;
        this.videoSource = videoSource;
        initializePlayer();

    }

    /******************************************************************
     initialize ExoPlayer
     ******************************************************************/
    private void initializePlayer() {
        playerView.requestFocus();

        componentListener = new ComponentListener();

        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                context,
                100 * 1024 * 1024,
                5 * 1024 * 1024);

        trackSelector = new DefaultTrackSelector(context);

        exoPlayer = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build();

        playerView.setPlayer(exoPlayer);
        playerView.setKeepScreenOn(true);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.addListener(componentListener);
        //build mediaSource depend on video type (Regular, HLS, DASH, etc)
        MediaSource mediaSource = buildMediaSource(videoSource, cacheDataSourceFactory);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
    }

    /******************************************************************
     building mediaSource depend on stream type and caching
     ******************************************************************/
    private MediaSource buildMediaSource(String videoSource, CacheDataSourceFactory cacheDataSourceFactory) {
        Uri source = Uri.parse(videoSource);
        @C.ContentType int type = Util.inferContentType(source);
        switch (type) {
            case C.TYPE_SS:
                Log.d(TAG, "buildMediaSource() C.TYPE_SS = [" + C.TYPE_SS + "]");
                return new SsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(source);

            case C.TYPE_DASH:
                Log.d(TAG, "buildMediaSource() C.TYPE_DASH = [" + C.TYPE_DASH + "]");
                return new DashMediaSource.Factory(cacheDataSourceFactory).createMediaSource(source);

            case C.TYPE_HLS:
                Log.d(TAG, "buildMediaSource() C.TYPE_HLS = [" + C.TYPE_HLS + "]");
                return new HlsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(source);

            case C.TYPE_OTHER:
                Log.d(TAG, "buildMediaSource() C.TYPE_OTHER = [" + C.TYPE_OTHER + "]");
                return new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(source);

            default: {
                throw new IllegalStateException("Unsupported type: " + source);
            }
        }
    }

    public void pausePlayer() {
        if (exoPlayer != null)
            exoPlayer.pause();
    }

    public void resumePlayer() {
        if (exoPlayer != null)
            exoPlayer.play();

    }

    public boolean isPlaying() {
        if (exoPlayer != null)
            return exoPlayer.isPlaying();
        return false;
    }

    public void releasePlayer() {
        if (exoPlayer == null)
            return;

        playerView.setPlayer(null);
        exoPlayer.release();
        exoPlayer.removeListener(componentListener);
        exoPlayer = null;

    }

    /************************************************************
     mute, unMute
     ***********************************************************/
    public void setMute(boolean mute) {
        float currentVolume = exoPlayer.getVolume();
        if (currentVolume > 0 && mute) {
            exoPlayer.setVolume(0);
            playerController.setMuteMode(true);
        } else if (!mute && currentVolume == 0) {
            exoPlayer.setVolume(1);
            playerController.setMuteMode(false);
        }
    }

    /***********************************************************
     manually select stream quality
     ***********************************************************/
    public void setSelectedQuality(Activity activity) {

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo;

        if (trackSelector != null) {
            mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

            if (mappedTrackInfo != null) {

                int rendererIndex = 0; // renderer for video
                int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
                boolean allowAdaptiveSelections =
                        rendererType == C.TRACK_TYPE_VIDEO
                                || (rendererType == C.TRACK_TYPE_AUDIO
                                && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                                == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);


                Pair<AlertDialog, MyTrackSelectionView> dialogPair =
                        MyTrackSelectionView.getDialog(activity, trackSelector,
                                rendererIndex,
                                exoPlayer.getVideoFormat().bitrate);
                dialogPair.second.setShowDisableOption(false);
                dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
                dialogPair.second.animate();
                Log.d(TAG, "dialogPair.first.getListView()" + dialogPair.first.getListView());
                dialogPair.first.show();

            }

        }
    }

    /***********************************************************
     double tap event and seekTo
     ***********************************************************/
    public void seekToSelectedPosition(int hour, int minute, int second) {
        long playbackPosition = (hour * 3600 + minute * 60 + second) * 1000;
        exoPlayer.seekTo(playbackPosition);
    }

    public void seekToSelectedPosition(long millisecond, boolean rewind) {
        if (rewind) {
            exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 15000);
            return;
        }
        exoPlayer.seekTo(millisecond * 1000);
    }

    public void seekToOnDoubleTap() {
        getWidthOfScreen();
        final GestureDetector gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {

                        float positionOfDoubleTapX = e.getX();

                        if (positionOfDoubleTapX < widthOfScreen / 2)
                            exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 5000);
                        else
                            exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 5000);

                        Log.d(TAG, "onDoubleTap(): widthOfScreen >> " + widthOfScreen +
                                " positionOfDoubleTapX >>" + positionOfDoubleTapX);
                        return true;
                    }
                });

        playerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void getWidthOfScreen() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        widthOfScreen = metrics.widthPixels;
    }

    /***********************************************************
     Listeners
     ***********************************************************/
    private class ComponentListener implements EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(TAG, "onPlayerStateChanged: playWhenReady: " + playWhenReady + " playbackState: " + playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    playerController.showProgressBar(false);
                    playerController.showRetryBtn(true);
                    break;
                case Player.STATE_BUFFERING:
                    playerController.showProgressBar(true);
                    break;
                case Player.STATE_READY:
                    playerController.showProgressBar(false);
                    playerController.audioFocus();
                    break;
                case Player.STATE_ENDED:
                    playerController.showProgressBar(false);
//                    playerController.videoEnded();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            playerController.showProgressBar(false);
            playerController.showRetryBtn(true);
        }
    }

}