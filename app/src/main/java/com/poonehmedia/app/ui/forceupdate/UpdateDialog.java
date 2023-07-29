package com.poonehmedia.app.ui.forceupdate;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.AppParams;
import com.poonehmedia.app.databinding.DialogUpdateBinding;
import com.poonehmedia.app.modules.navigation.NavigationHelper;
import com.poonehmedia.app.util.base.DeviceInfoManager;

public class UpdateDialog extends Dialog {

    private final FragmentActivity context;
    private final NavigationHelper navigator;
    private final DeviceInfoManager deviceInfoManager;
    private final AppParams appParams;
    private DialogUpdateBinding binding;


    public UpdateDialog(@NonNull FragmentActivity context,
                        NavigationHelper navigationHelper,
                        DeviceInfoManager deviceInfoManager,
                        AppParams appParams) {
        super(context);
        this.context = context;
        navigator = navigationHelper;
        this.deviceInfoManager = deviceInfoManager;
        this.appParams = appParams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogUpdateBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setCancelable(false);

        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        init();
    }

    private void init() {
        String currentVersion = deviceInfoManager.getAppVersion();
        binding.currentVersion.setText(currentVersion);
        binding.updateVersion.setText(String.valueOf(appParams.getAppVersion()));

        binding.buttonUpdate.setOnClickListener(v -> {
            String link = BuildConfig.baseUrl +
                    "index.php?option=com_rppamspro&task=download.lastversion&apppack=" +
                    deviceInfoManager.getPackageName();

            navigator.openUrl(link);
        });

        if (appParams.isForceUpdate()) binding.buttonClose.setText(R.string.button_shut_down);

        binding.buttonClose.setOnClickListener(v -> {
            if (appParams.isForceUpdate())
                navigator.closeApp(context);
            else
                dismiss();
        });

    }
}
