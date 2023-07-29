package com.poonehmedia.app.util.ui;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

public class TextInput extends TextInputEditText {

    private Pattern pattern;

    public TextInput(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TextInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        pattern = Pattern.compile("^[a-zA-Z0-9$@$!,~\\\\/\\-'\"%*?&#^`;()\\n:-_. +]+$");
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        // no need to change anything if input type is already number
        if (getInputType() == InputType.TYPE_CLASS_NUMBER)
            return;

        // prevent loop
        if (lengthBefore == lengthAfter)
            return;

        String converted = replaceSpecialCharacters(text.toString());

        // return if text didn't change
        if (converted.equals(text.toString()))
            return;

        setText(converted);
        if (getText() != null)
            setSelection(getText().length());
    }

    /**
     * @param text any given string
     * @return replaces the given string special persian and arabic characters with the standard values
     * used in server to provide better ux in searching here and there. if no special characters
     * has been used no replacement will occur to avoid any ui complications.
     * @author Marthia
     */
    public String replaceSpecialCharacters(String text) {
        if (pattern.matcher(text).matches())
            return text;

        String d = text;
        // persian
        d = d.replace("۰", "0");
        d = d.replace("۱", "1");
        d = d.replace("۲", "2");
        d = d.replace("۳", "3");
        d = d.replace("۴", "4");
        d = d.replace("۵", "5");
        d = d.replace("۶", "6");
        d = d.replace("۷", "7");
        d = d.replace("۸", "8");
        d = d.replace("۹", "9");

        // arabic
        d = d.replace("٠", "0");
        d = d.replace("١", "1");
        d = d.replace("٢", "2");
        d = d.replace("٣", "3");
        d = d.replace("٤", "4");
        d = d.replace("٥", "5");
        d = d.replace("٦", "6");
        d = d.replace("٧", "7");
        d = d.replace("٨", "8");
        d = d.replace("٩", "9");

        d = d.replace("ي", "ی");
        d = d.replace("ك", "ک");

        return d;
    }

}
