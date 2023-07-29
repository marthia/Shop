package com.poonehmedia.app.ui.item;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentItemBinding;
import com.poonehmedia.app.ui.adapter.CommentModuleAdapter;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.ui.FileUtils;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.poonehmedia.app.util.ui.UiComponents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ItemFragment extends BaseFragment {
    private final String TAG = getClass().getSimpleName();
    private final Map<String, File> uris = new HashMap<>();
    @Inject
    public DataController dataController;
    private FragmentItemBinding binding;
    private ItemViewModel viewModel;
    private ActivityResultLauncher<String> fileChooserLauncherMultiple;
    private ValueCallback<Uri[]> uriCallback;
    private boolean isUploading = false;
    private String currentKey = "";
    private ActivityResultLauncher<String> filePermissionRequest;
    private JsonObject params;
    private boolean isMultiple = false;
    private CommentModuleAdapter commentsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemBinding.inflate(inflater, container, false);

        initViews();

        subscribeData();

        initResultLaunchers();

        return binding.getRoot();
    }

    private void initViews() {

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        binding.setOnNewComment(v -> {
            if (preferenceManager.getUser().equals("")) {
                viewModel.saveCurrentPageAsReturn();
                navigator.navigate(requireActivity(), "index.php?option=com_users&view=login", this::handleDefaultNavigationState);
            } else if (viewModel.getAddCommentObj() != null)
                navigator.intrinsicNavigate(requireActivity(),
                        ItemFragmentDirections.actionAddEdit(""),
                        false);
        });

        binding.webView.subscribeFileChooserCommunication((isMultipleFiles, key) -> {
            currentKey = key;
            isMultiple = isMultipleFiles;
        });

        binding.webView.subscribeShowForm(() -> {
            setLoading(true);
            viewModel.fetchData();
        });

        binding.webView.subscribeProcessData((data, hasFile) -> {
            setLoading(true);
            params = data;

            if (hasFile) {
                viewModel.postDataWithFile(uris, params, "*/*");
                isUploading = true;
            } else
                viewModel.postDataWithoutFile(params);
        });

        binding.webView.subscribeNavigation(this::navigate);

        binding.webView.subscribeInternalNavigation(this::navigate);

        binding.webView.subscribeOpenFileChooser(callback -> {
            uriCallback = callback;
            openFileChooser();
        });


        // comments Adapter
        commentsAdapter = new CommentModuleAdapter();
        commentsAdapter.subscribeClick((item, position) -> {
            JsonObject value = viewModel.getCommentsReadMore().getValue();
            if (value != null)
                navigator.navigate(requireActivity(), value.get("link").getAsString(), this::handleDefaultNavigationState);
        });
        ItemSpaceDecoration commentsSpaceDecoration = new ItemSpaceDecoration(getContext(), 8);
        binding.rvComments.addItemDecoration(commentsSpaceDecoration);
        binding.rvComments.setAdapter(commentsAdapter);
    }

    private void navigate(String link) {
        navigator.navigate(requireActivity(), link, this::handleDefaultNavigationState);
    }

    private void initResultLaunchers() {
        filePermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                fileChooserLauncherMultiple.launch("*/*");
            } else {
                UiComponents.showSnack(requireActivity(), getString(R.string.cannot_continue_without_file_permission));
            }
        });

        fileChooserLauncherMultiple = registerForActivityResult(new CustomGetContent(), result -> {
                    if (result != null) {
                        Log.i("documentResult", result.toString());
                        uriCallback.onReceiveValue(result);
                        for (int i = 0; i < result.length; i++) {
                            String key = currentKey.replaceFirst("\\[]", "[" + i + "]");
                            File file = FileUtils.getFile(getContext(), result[i]);
                            uris.put(key, file);
                        }
                    } else uriCallback.onReceiveValue(result);
                }
        );
    }

    private void subscribeData() {

        setLoading(true);
        viewModel.resolveData();

        viewModel.getData().observe(getViewLifecycleOwner(), jsonObject -> {
            if (jsonObject != null && jsonObject.size() > 0) {
                setLoading(false);
                setContent(jsonObject);
            }
        });

        viewModel.getPostResponse().observe(getViewLifecycleOwner(), jsonObject -> {
            if (jsonObject != null) {
                setLoading(false);
                setContent(jsonObject);
                if (isUploading) {
                    uris.clear();
                }
            }
        });

        viewModel.getCommentsData().observe(getViewLifecycleOwner(), comments -> {
            commentsAdapter.submitList(comments);
        });
    }

    public void setContent(JsonObject content) {
        String htmlContent = dataController.generateHtmlContent(
                dataController.getWebViewStyles(getContext()),
                dataController.getWebViewJs(false),
                content.get("text").getAsString(),
                viewModel.getLanguage()
        );

        binding.webView.loadDataWithBaseURL(
                BuildConfig.baseUrl,
                htmlContent,
                "text/html",
                "UTF-8",
                ""
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

    private void setLoading(boolean state) {
        // make sure it's run on ui thread
        new Handler(Looper.getMainLooper()).post(() -> {
            if (state) {
                binding.getRoot().setAlpha(.4f);
            } else binding.getRoot().setAlpha(1f);

            binding.progress.setVisibility(state ? View.VISIBLE : View.GONE);
        });
    }

    private void openFileChooser() {
        int isAuthorized = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (isAuthorized != PackageManager.PERMISSION_GRANTED)
            filePermissionRequest.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        else
            fileChooserLauncherMultiple.launch("*/*");

    }

    public static class CustomGetContent extends ActivityResultContract<String, Uri[]> {

        @CallSuper
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull String input) {
            return new Intent(Intent.ACTION_GET_CONTENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    .setType(input);
        }

        @Nullable
        @Override
        public final SynchronousResult<Uri[]> getSynchronousResult(@NonNull Context context,
                                                                   @NonNull String input) {
            return null;
        }

        @Nullable
        @Override
        public final Uri[] parseResult(int resultCode, @Nullable Intent intent) {
            if (intent == null || resultCode != Activity.RESULT_OK) return null;
            List<Uri> uriList = new ArrayList<>();
            if (intent.getData() == null) {
                ClipData data = intent.getClipData();
                for (int i = 0; i < data.getItemCount(); i++) {
                    uriList.add(data.getItemAt(i).getUri());
                }
                return uriList.toArray(new Uri[data.getItemCount()]);
            } else {
                uriList.add(intent.getData());
                return uriList.toArray(new Uri[1]);
            }
        }
    }

}
