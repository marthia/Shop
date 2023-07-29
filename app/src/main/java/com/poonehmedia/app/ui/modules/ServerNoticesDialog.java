package com.poonehmedia.app.ui.modules;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.JsonObject;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.DialogServerNoticeBinding;
import com.poonehmedia.app.util.base.DataController;


public class ServerNoticesDialog extends Dialog {

    private final JsonObject content;
    private final DataController dataController;
    private final String lang;
    private final Consumer<String> navigator;
    private final Context context;
    private DialogServerNoticeBinding binding;

    public ServerNoticesDialog(@NonNull Context context,
                               JsonObject content,
                               DataController dataController,
                               String lang,
                               Consumer<String> navigator) {
        super(context);
        this.context = context;
        this.content = content;
        this.dataController = dataController;
        this.lang = lang;
        this.navigator = navigator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogServerNoticeBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setCancelable(false);

        // transparent background
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);

        // limit the vertical height
        setMaxHeight();
        init();
    }

    private void setMaxHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();

        ((FragmentActivity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        int MAX_HEIGHT = (displaymetrics.heightPixels * 85) / 100;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        int dialogWidth = lp.width;
        getWindow().setLayout(dialogWidth, MAX_HEIGHT);
    }

    private void init() {

        binding.webView.subscribeInternalNavigation(navigator::accept);

        setContent(content);
    }

    public void setContent(JsonObject content) {
        String htmlContent = dataController.generateHtmlContent(
                dataController.getWebViewStyles(getContext()),
                dataController.getWebViewJs(false),
                content.get("text").getAsString(),
                lang
        );

        binding.webView.loadDataWithBaseURL(
                BuildConfig.baseUrl,
                htmlContent,
                "text/html",
                "UTF-8",
                ""
        );
    }

}
