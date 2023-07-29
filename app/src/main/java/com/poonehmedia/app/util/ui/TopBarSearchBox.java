package com.poonehmedia.app.util.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.poonehmedia.app.R;

public class TopBarSearchBox extends SearchView {


    public TopBarSearchBox(@NonNull Context context) {
        super(context);
        init(context, -1);
    }

    public TopBarSearchBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, -1);
    }

    public TopBarSearchBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, defStyleAttr);
    }

    private void init(Context context, int defStyleAttr) {

//        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context,
//                attrs, R.styleable.SearchView, defStyleAttr, 0);

        // default behaviour
        setIconified(false);
        setIconifiedByDefault(false);
        setBackgroundResource(R.drawable.round_corners_medium_shape_with_tint);

        // hint position
        setGravity(Gravity.CENTER_VERTICAL);

        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/iran_yekan_reqular_mobile_fa_num.ttf");
        CustomTypefaceSpan fontSpan = new CustomTypefaceSpan("", font);
        SpannableString hintSpannable = new SpannableString("  جستجو در فروشگاه");

        // set font on raw hint string
        hintSpannable.setSpan(fontSpan, 2, hintSpannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // set font on input text
        AutoCompleteTextView textView = findViewById(R.id.search_src_text);
        textView.setTypeface(font);
        setQueryHint(hintSpannable);
        textView.setThreshold(1);
        textView.setDropDownBackgroundResource(R.drawable.round_corners_medium_shape);

        //Get ImageView of icon
        ImageView searchViewIcon = findViewById(R.id.search_mag_icon);

        //Get parent of gathered icon
        ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
        //Remove it from the left...
        linearLayoutSearchView.removeView(searchViewIcon);
        //then put it back (to the right by default)
        linearLayoutSearchView.addView(searchViewIcon);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
            layoutParams.setMargins(100, 0, 0, 0);
            requestLayout();
        }
    }
}
