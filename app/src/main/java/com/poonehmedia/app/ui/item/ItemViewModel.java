package com.poonehmedia.app.ui.item;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.data.repository.CommentsRepository;
import com.poonehmedia.app.data.repository.ItemRepository;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;


@HiltViewModel
public class ItemViewModel extends BaseViewModel {

    private final ItemRepository repository;
    private final CommentsRepository commentsRepository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> postResponse = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> commentsReadMore = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentSectionVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> newCommentSectionVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<String> addCommentText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAddCommentLocked = new MutableLiveData<>();
    private final MutableLiveData<String> commentsCount = new MutableLiveData<>();
    private final MutableLiveData<List<Comment>> commentsData = new MutableLiveData<>();
    private final MutableLiveData<String> addCommentSubtitle = new MutableLiveData<>();
    private final Context context;
    private final SavedStateHandle savedStateHandle;
    private final PreferenceManager preferenceManager;
    private JsonObject addCommentObj = null;
    private String path;
    private boolean isDescription;

    @Inject
    public ItemViewModel(ItemRepository repository,
                         CommentsRepository commentsRepository,
                         DataController dataController,
                         PreferenceManager preferenceManager,
                         SavedStateHandle savedStateHandle,
                         RoutePersistence routePersistence,
                         @ApplicationContext Context context) {

        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.commentsRepository = commentsRepository;
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.savedStateHandle = savedStateHandle;
        this.context = context;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);
        isDescription = savedStateHandle.get("isDescription") != null;

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            extractResult(((JsonElement) navigationArgs.getData()));
        }

    }

    private void extractResult(JsonElement body) {
        try {
            JsonArray item = dataController.extractDataItemsAsJsonArray(body);
            data.postValue(item.get(0).getAsJsonObject());

            // comments
            JsonObject commentModule = dataController.extractModule(body, "JComments");
            if (!isDescription && commentModule != null) {
                JsonObject commentsReadMore = commentModule.get("params").getAsJsonObject().get("readmore").getAsJsonObject();
                this.commentsReadMore.postValue(commentsReadMore);

                JsonArray content = commentModule.get("content").getAsJsonArray();
                List<Comment> comments = dataController.bindCommentsJsonToObject(content);
                if (comments.size() > 0) {
                    commentsCount.postValue(commentModule.get("info").getAsJsonObject().get("total").getAsString());
                    commentSectionVisibility.setValue(true);
                    commentsData.postValue(comments);
                } else {
                    addCommentObj = commentModule.get("info").getAsJsonObject().get("addComment").getAsJsonObject();
                    commentsRepository.setAddCommentObj(addCommentObj);

                    if (addCommentObj == null) {
                        JsonObject lockedObj = commentModule.get("info").getAsJsonObject().get("commentsLocked").getAsJsonObject();
                        isAddCommentLocked.postValue(true);
                        addCommentSubtitle.postValue(lockedObj.get("text").getAsString());
                    } else {
                        addCommentText.postValue(addCommentObj.get("text").getAsString());
                        addCommentSubtitle.postValue(context.getString(R.string.comments_subtitle_article));
                        isAddCommentLocked.postValue(false);
                    }

                    newCommentSectionVisibility.postValue(true);
                }
            }

        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (ItemViewModel). data: " + body, e));
        }
    }

    public LiveData<String> getAddCommentSubtitle() {
        return addCommentSubtitle;
    }

    public LiveData<List<Comment>> getCommentsData() {
        return commentsData;
    }

    public LiveData<String> getCommentsCount() {
        return commentsCount;
    }

    public LiveData<Boolean> getCommentSectionVisibility() {
        return commentSectionVisibility;
    }

    public LiveData<Boolean> getNewCommentSectionVisibility() {
        return newCommentSectionVisibility;
    }

    public void fetchData() {
        requestData(repository.getData(path),
                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.e("items", throwable.getMessage());
                }
        );
    }

    public void postDataWithoutFile(JsonObject params) {
        requestData(
                repository.post(path, params),

                jsonElementResponse -> {
                    dataController.onSuccess(jsonElementResponse);
                    if (jsonElementResponse.isSuccessful()) {
                        JsonArray item = dataController.extractDataItemsAsJsonArray(jsonElementResponse);
                        postResponse.postValue(item.get(0).getAsJsonObject());
                    }
                },

                throwable -> {
                    dataController.onFailure(throwable);
                    Log.e("items", throwable.getMessage());
                }
        );
    }

    public LiveData<String> getAddCommentText() {
        return addCommentText;
    }

    public LiveData<Boolean> getAddCommentLocked() {
        return isAddCommentLocked;
    }

    public LiveData<JsonObject> getCommentsReadMore() {
        return commentsReadMore;
    }

    public LiveData<JsonObject> getData() {
        return data;
    }

    public JsonObject getAddCommentObj() {
        return addCommentObj;
    }

    public String getLanguage() {
        return repository.getLanguage();
    }

    public LiveData<JsonObject> getPostResponse() {
        return postResponse;
    }

    public void postDataWithFile(Map<String, File> files, JsonObject params, String mimeType) {
        String s = dataController.replaceSpecialCharacters(params.toString());
        JsonObject fixedParams = JsonParser.parseString(s).getAsJsonObject();
        requestData(
                repository.postDataWithFile(path, files, fixedParams, mimeType),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        JsonArray item = dataController.extractDataItemsAsJsonArray(response);
                        postResponse.postValue(item.get(0).getAsJsonObject());
                    }
                }
                , throwable -> {
                    dataController.onFailure(throwable);
                    Log.e("items", throwable.getMessage());
                }
        );
    }

    public void saveCurrentPageAsReturn() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("link", path);
        preferenceManager.saveReturn(jsonObject);
    }
}
