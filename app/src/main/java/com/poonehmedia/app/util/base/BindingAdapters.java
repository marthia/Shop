package com.poonehmedia.app.util.base;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.components.fontAwesome.FontDrawable;
import com.poonehmedia.app.data.model.ModuleMetadata;
import com.poonehmedia.app.ui.interfaces.OnCustomClick;
import com.poonehmedia.app.util.ui.AndroidUtils;

import org.acra.ACRA;

public class BindingAdapters {

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        GlideHelper.setImage(view, url, RequestOptions.fitCenterTransform());
    }

    @BindingAdapter("circleCropImage")
    public static void setCircleImage(ImageView view, String url) {
        GlideHelper.setImage(view, url, new RequestOptions().circleCrop());
    }

    @BindingAdapter("gifImage")
    public static void setGifImage(ImageView view, int id) {
        Glide.with(view)
                .asDrawable()
                .load(id)
                .into(view);
    }

    @BindingAdapter("setPrice")
    public static void setPrice(TextView textView, String price) {

        if (price != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(price, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                textView.setText(Html.fromHtml(price), TextView.BufferType.SPANNABLE);
            }
        } else textView.setText("");
    }

    @BindingAdapter("order_status_background")
    public static void setBackground(MaterialTextView textView, boolean isSelected) {
        if (isSelected) {
            textView.setBackgroundTintList(
                    ColorStateList.valueOf(AndroidUtils.getAttr(textView.getContext(), R.attr.colorSecondary))
            );
        } else
            textView.setBackgroundTintList(AppCompatResources.getColorStateList(textView.getContext(), R.color.black_60));
    }

    @BindingAdapter("showHide")
    public static void showHide(View view, boolean b) {
        if (b) view.setVisibility(View.VISIBLE);
        else view.setVisibility(View.GONE);
    }

    @BindingAdapter(value = {"addressObject", "tag"})
    public static void bindAddress(TextView textView, JsonElement item, String tag) {
        for (int i = 0; i < item.getAsJsonArray().size(); i++) {
            JsonObject jsonObject = (JsonObject) item.getAsJsonArray().get(i);
            if (jsonObject.get("namekey").getAsString().equals(tag)) {
                textView.setText(jsonObject.get("value").getAsString());
                break;
            }
        }

    }

    @BindingAdapter("htmlText")
    public static void setCustomText(TextView textView, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            textView.setText(
                    Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        else
            textView.setText(
                    Html.fromHtml(text), TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter(value = {"addressObject", "firstName", "lastName"})
    public static void bindFullName(TextView textView, JsonElement item, String firstNameTag, String lastNameTag) {
        for (int i = 0; i < item.getAsJsonArray().size(); i++) {
            JsonObject jsonObject = (JsonObject) item.getAsJsonArray().get(i);
            if (jsonObject.get("namekey").getAsString().equals(firstNameTag)) {
                textView.setText(jsonObject.get("value").getAsString());
                continue;
            }
            if (jsonObject.get("namekey").getAsString().equals(lastNameTag)) {
                textView.append(" " + jsonObject.get("value").getAsString());
            }
        }
    }

    @BindingAdapter({"onClick", "item"})
    public static void customOnClick(View view, OnCustomClick click, JsonObject item) {
        view.setOnClickListener(v -> click.onClick(v, item));
    }

    @BindingAdapter({"item", "textField"})
    public static void setTitle(TextView view, JsonObject item, String field) {
        try {
            String text = item.get(field).getAsString();
            view.setText(text);

        } catch (Exception e) {
            Log.e("setTitle", e.getMessage());
        }
    }

    @BindingAdapter("icon")
    public static void setIcon(ImageView view, String fontAwesome) {
        try {
            if (fontAwesome != null) {
                FontDrawable drawable = new FontDrawable(view.getContext(), fontAwesome, true, false);
                drawable.setTextColor(Color.parseColor("#676767"));
                drawable.setTextSize(22);
                view.setImageDrawable(drawable);
            }

        } catch (Exception e) {
            Log.e("fontAwesome", "Couldn't inflate the font awesome icon");
        }
    }

    @BindingAdapter("orderTextColor")
    public static void setOrderStatusTextColor(TextView view, String status) {
        if (status.equals("confirmed") || status.equals("shipped"))
            view.setTextColor(Color.parseColor("#1CA45C"));
        else if (status.equals("cancelled")) view.setTextColor(Color.parseColor("#DA483B"));
        else view.setTextColor(Color.parseColor("#FFC718"));
    }

    @BindingAdapter("badgeItem")
    public static void setBadge(TextView textView, JsonObject item) {
        try {
            if (item != null) {
                textView.setText(item.get("badge_name").getAsString());

                Drawable background = textView.getBackground();
                background = DrawableCompat.wrap(background);
                DrawableCompat.setTint(background, Color.parseColor(item.get("badge_color").getAsString()));

                textView.setTextColor(Color.parseColor(item.get("badge_text_color").getAsString()));
            }

        } catch (Exception e) {
            Log.e("badge", e.getMessage());
        }
    }

    @BindingAdapter("isApproved")
    public static void changeIcon(ShapeableImageView imageView, String isApproved) {
        if (isApproved.equals("1")) {
            imageView.setImageResource(R.drawable.ic_round_check_24);
            imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#007a01")));
        } else {
            imageView.setImageResource(R.drawable.ic_round_close_24);
            imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#900000")));
        }
    }

    @BindingAdapter("setSelected")
    public static void setSelected(TextView textView, boolean b) {
        textView.setSelected(b);
    }

    @BindingAdapter({"item", "switchState"})
    public static void bindSwitch(SwitchMaterial switchMaterial, JsonObject item, String key) {
        switchMaterial.setChecked(item.get(key).getAsInt() == 1);
    }

    @BindingAdapter("copyToClipboard")
    public static void copyTextToClipBoard(TextView textView, String text) {
        textView.setOnClickListener(v -> {
            boolean success = AndroidUtils.copyToClipboard(textView.getContext(), text);
            if (success)
                Toast.makeText(textView.getContext(), R.string.copy_clipboard_success, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(textView.getContext(), R.string.copy_clipboard_failed, Toast.LENGTH_LONG).show();
        });
    }

    @BindingAdapter("cardStyle")
    public static void setCardStyle(MaterialCardView cardView, String type) {
        switch (type) {
            case "warning":
                cardView.setCardBackgroundColor(Color.parseColor("#4DFFAB00"));
                cardView.setStrokeColor(Color.parseColor("#FFAB00"));
                break;
            case "success":
                cardView.setCardBackgroundColor(Color.parseColor("#4D388E3C"));
                cardView.setStrokeColor(Color.parseColor("#388E3C"));
                break;
            case "info":
            case "notice":
                cardView.setCardBackgroundColor(Color.parseColor("#4D1976D2"));
                cardView.setStrokeColor(Color.parseColor("#1976D2"));
                break;
            case "error":
                cardView.setCardBackgroundColor(Color.parseColor("#4DD32F2F"));
                cardView.setStrokeColor(Color.parseColor("#D32F2F"));
                break;
        }
    }

    @BindingAdapter("cardSelection")
    public static void selectCardView(MaterialCardView cardView, Boolean isSelected) {
        if (isSelected == null)
            return;

        if (isSelected) {
            cardView.setStrokeWidth(cardView.getContext().getResources().getDimensionPixelSize(R.dimen.plane_01));
            cardView.setStrokeColor(AndroidUtils.getAttr(cardView.getContext(), R.attr.colorOnSurface));
        } else cardView.setStrokeWidth(0);
    }

    @BindingAdapter("textColor")
    public static void setTextColor(MaterialTextView textView, String type) {
        switch (type) {
            case "warning":
                textView.setTextColor(Color.parseColor("#FFAB00"));
                break;
            case "success":
                textView.setTextColor(Color.parseColor("#388E3C"));
                break;
            case "info":
            case "notice":
                textView.setTextColor(Color.parseColor("#1976D2"));
                break;
            case "error":
                textView.setTextColor(Color.parseColor("#D32F2F"));
                break;
        }
    }

    @BindingAdapter(value = {"optionalObj", "optionalKey"})
    public static void setOptionalText(MaterialTextView textView, JsonObject item, String key) {
        try {

            if (JsonHelper.isNotEmptyNorNull(item.get(key))) {
                textView.setText(item.get(key).getAsString());
            } else textView.setVisibility(View.GONE);

        } catch (Exception e) {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter(value = {"emptyObj", "emptyText"})
    public static void setEmptyText(MaterialTextView textView, JsonObject item, String key) {
        try {

            if (JsonHelper.isNotEmptyNorNull(item.get(key))) {
                textView.setText(item.get(key).getAsString());
            } else textView.setVisibility(View.INVISIBLE);

        } catch (Exception e) {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter("setTint")
    public static void setTint(MaterialTextView view, String color) {
        if (color == null || color.isEmpty())
            return;

        Drawable[] drawables = view.getCompoundDrawablesRelative();
        for (Drawable d : drawables) {
            if (d != null)
                d.setTint(Color.parseColor(color));
        }
    }

    @BindingAdapter("setTint")
    public static void setTint(MaterialTextView view, @ColorRes int resourceId) {
        if (resourceId == -1)
            return;

        Drawable[] drawables = view.getCompoundDrawablesRelative();
        for (Drawable d : drawables) {
            if (d != null)
                d.setTint(view.getContext().getResources().getColor(resourceId));
        }
    }


    @BindingAdapter("params")
    public static void updateViewSize(View view, ModuleMetadata params) {
        try {
            int width = Integer.parseInt(params.getThumbWidth());
            int height = Integer.parseInt(params.getThumbHeight());

            view.getLayoutParams().width = (int) AndroidUtils.getPixels(width, view.getContext());
        } catch (Exception e) {
            Log.e("sizeChange", "COULD NOT UPDATE LAYOUT PARAMS");
        }
    }

    @BindingAdapter({"optionalItem", "optionalKey", "optionalVisibility"})
    public static void setTextAndVisibility(MaterialTextView view, JsonObject item, String key, boolean visibility) {
        try {
            if (JsonHelper.isNotEmptyNorNull(item.get(key)) && visibility) {
                view.setText(item.get(key).getAsString());
            } else view.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("setTextAndVisibility", e.getMessage());
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter(value = {"optionalImage", "optionalKey", "optionalVisibility"}, requireAll = false)
    public static void setOptionalImage(ImageView image, Object url, String key, Boolean visibility) {
        boolean paramVisibility = true;
        if (visibility != null && visibility)
            paramVisibility = visibility;
        try {
            if (url == null || url instanceof JsonNull)
                image.setVisibility(View.GONE);
            else if (url instanceof JsonObject) {
                if (JsonHelper.isNotEmptyNorNull(((JsonObject) url).get(key == null ? "image" : key)) && paramVisibility) {
                    image.setVisibility(View.VISIBLE);
                    loadImage(image, ((JsonObject) url).get(key == null ? "image" : key).getAsString());
                } else image.setVisibility(View.GONE);
            } else if (url instanceof String) {
                if (!((String) url).isEmpty() && paramVisibility) {
                    image.setVisibility(View.VISIBLE);
                    loadImage(image, (String) url);
                } else image.setVisibility(View.GONE);
            } else if (url instanceof JsonPrimitive) {
                if (!((JsonPrimitive) url).getAsString().isEmpty() && paramVisibility) {
                    image.setVisibility(View.VISIBLE);
                    loadImage(image, ((JsonPrimitive) url).getAsString());
                } else image.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.i("optionalImage", e.getMessage());
        }
    }

    @BindingAdapter("isRecommend")
    public static void isRecommend(TextView view, boolean isRecommend) {

        if (isRecommend) {
            view.setText(R.string.comment_recommend);
            int green = view.getContext().getResources().getColor(R.color.green_color_badge);
            view.setTextColor(green);
            Drawable[] drawables = view.getCompoundDrawablesRelative();
            for (Drawable d : drawables) {
                if (d != null) {
                    d.setTint(green);
                }
            }

        } else {
            view.setText(R.string.comment_not_recommend);
            int red = view.getContext().getResources().getColor(R.color.red_color_badge);
            view.setTextColor(red);
            Drawable[] drawables = view.getCompoundDrawablesRelative();
            for (Drawable d : drawables) {
                if (d != null) {
                    d.setTint(red);
                }
            }
        }
    }

    @BindingAdapter("setIconStart")
    public static void setFontAwesomeIconStart(TextView textView, String fontAwesomeIcon) {
        try {
            FontDrawable drawable = new FontDrawable(textView.getContext(), fontAwesomeIcon, true, false);
            drawable.setTextColor(Color.parseColor("#80000000"));
            drawable.setTextSize(20);

            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
            textView.setCompoundDrawablePadding((int) AndroidUtils.getPixels(8, textView.getContext()));
        } catch (Exception ignore) {
        }
    }


    @BindingAdapter("setImageIcon")
    public static void setFontAwesomeImageIcon(ImageView imageView, String fontAwesomeIcon) {
        try {
            FontDrawable drawable = new FontDrawable(imageView.getContext(), fontAwesomeIcon, true, false);
            drawable.setTextColor(Color.parseColor("#80000000"));
            drawable.setTextSize(20);

            imageView.setImageDrawable(drawable);
        } catch (Exception ignore) {
        }
    }

    @BindingAdapter("setImageDrawable")
    public static void setDrawable(ImageView imageView, Drawable drawable) {
        try {
            imageView.setImageDrawable(drawable);
        } catch (Exception ignore) {
        }
    }


    @BindingAdapter("setImageRadius")
    public static void setImageShape(ShapeableImageView imageView, String borderRadius) {
        try {
            if (borderRadius == null || borderRadius.isEmpty())
                borderRadius = "16";

            ShapeAppearanceModel model = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, AndroidUtils.getPixels(Integer.parseInt(borderRadius), imageView.getContext()))
                    .build();

            imageView.setShapeAppearanceModel(model);
            imageView.refreshDrawableState();
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not apply ShapeAppearance", e));
        }
    }

}