package com.poonehmedia.app.util.base;

import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;

import org.acra.ACRA;

public class GlideHelper {


    public static void setImage(ImageView view, String link, @NonNull RequestOptions ro) {
        try {
            if (link == null)
                return;

            if (!link.startsWith("http"))
                link = BuildConfig.baseUrl + link;

            CircularProgressDrawable cpd = new CircularProgressDrawable(view.getContext());
            cpd.setStrokeWidth(5f);
            cpd.setCenterRadius(30f);
            cpd.start();

            Glide.with(view.getContext())
                    .load(link)
                    .apply(ro)
                    .placeholder(cpd)
                    .error(R.drawable.ic_outline_image_24)
                    .error(RequestOptions.centerInsideTransform())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(view);

        } catch (Exception e) {
            Log.e("Glider", "Could not load image ");
            ACRA.getErrorReporter().handleException(new CrashReportException("An error occurred while glide was trying to load image with link: " + link, e));
        }
    }
}
