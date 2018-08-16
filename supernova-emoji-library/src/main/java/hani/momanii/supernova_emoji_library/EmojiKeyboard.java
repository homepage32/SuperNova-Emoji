/*
 * Copyright 2016 Hani Al Momani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hani.momanii.supernova_emoji_library;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hani.momanii.supernova_emoji_library.EmojiconGridView;
import hani.momanii.supernova_emoji_library.EmojiconsPopup;
import hani.momanii.supernova_emoji_library.R;
import hani.momanii.supernova_emoji_library.emoji.Emojicon;

public class EmojiKeyboard implements View.OnFocusChangeListener {
    private EmojiconsPopup popup;
    private Context context;
    private ImageView emojiButton;
    private int keyBoardIcon = R.drawable.ic_keyboard_black_24dp;
    private int smileyIcon = R.drawable.ic_insert_emoticon_black_24dp;
    private EmojiKeyboardListener keyboardListener;
    private List<EditText> emojiconEditTextList = new ArrayList<>();
    private EditText emojiconEditText;

    /**
     * Constructor
     *
     * @param ctx              The context of current activity.
     * @param rootView         The top most layout in your view hierarchy. The difference of this
     *                         view and the screen height will be used to calculate the keyboard
     *                         height.
     * @param emojiconEditText The Id of EditText.
     * @param emojiButton      The Id of ImageButton used to open Emoji
     */
    public EmojiKeyboard(Context ctx, ViewGroup rootView, EditText emojiconEditText,
                         ImageView emojiButton) {
        this.emojiButton = emojiButton;
        this.context = ctx;
        addEditTextList(emojiconEditText);
        this.popup = new EmojiconsPopup(rootView, ctx);
    }

    public void addEditTextList(EditText... emojiconEditText) {
        Collections.addAll(emojiconEditTextList, emojiconEditText);
        for (EditText editText : emojiconEditText) {
            editText.setOnFocusChangeListener(this);
        }
    }


    /**
     * Constructor
     *
     * @param ctx              The context of current activity.
     * @param rootView         The top most layout in your view hierarchy. The difference of this
     *                         view and the screen height will be used to calculate the keyboard
     *                         height.
     * @param emojiconEditText The Id of EditText.
     * @param emojiButton      The Id of ImageButton used to open Emoji
     * @param iconPressedColor The color of icons on tab
     * @param tabsColor        The color of tabs background
     * @param backgroundColor  The color of emoji background
     */
    public EmojiKeyboard(Context ctx, ViewGroup rootView, EditText emojiconEditText,
                         ImageView emojiButton, String iconPressedColor, String tabsColor,
                         String backgroundColor) {
        addEditTextList(emojiconEditText);
        this.emojiButton = emojiButton;
        this.context = ctx;
        this.popup = new EmojiconsPopup(rootView, ctx, iconPressedColor,
                tabsColor, backgroundColor);
    }

    public void ShowEmojIcon() {
        if (emojiconEditText == null)
            emojiconEditText = emojiconEditTextList.get(0);
        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, smileyIcon);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup
                .OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
                if (keyboardListener != null)
                    keyboardListener.onKeyboardOpen();
            }

            @Override
            public void onKeyboardClose() {
                if (keyboardListener != null)
                    keyboardListener.onKeyboardClose();
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojicon == null) {
                    return;
                }

                int start = emojiconEditText.getSelectionStart();
                int end = emojiconEditText.getSelectionEnd();
                if (start < 0) {
                    emojiconEditText.append(emojicon.getEmoji());
                } else {
                    emojiconEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup
                .OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiconEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        showForEditText();
    }

    private void showForEditText() {

        emojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (emojiconEditText == null)
                    emojiconEditText = emojiconEditTextList.get(0);
                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, keyBoardIcon);
                    }

                    //else, open the text keyboard first and immediately after that show the
                    // emoji popup
                    else {
                        emojiconEditText.setFocusableInTouchMode(true);
                        emojiconEditText.requestFocus();
                        final InputMethodManager inputMethodManager = (InputMethodManager)
                                context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager
                                .SHOW_IMPLICIT);
                        popup.showAtBottomPending();
                        changeEmojiKeyboardIcon(emojiButton, keyBoardIcon);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else {
                    popup.dismiss();
                }


            }
        });
    }


    public void closeEmojIcon() {
        if (popup != null && popup.isShowing())
            popup.dismiss();

    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            if (view instanceof EditText) {
                emojiconEditText = (EditText) view;
            }
        }
    }


    public interface EmojiKeyboardListener {
        void onKeyboardOpen();

        void onKeyboardClose();
    }

    public void setEmojiKeyboardListener(EmojiKeyboardListener listener) {
        this.keyboardListener = listener;
    }    private int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private static final String KEY_KEYBOARD_HEIGHT = "emoji_library_keyboard_height";

    private static int getKeyboardHeight(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KEYBOARD_HEIGHT, -1);
    }

    public static void initialize(final View rootView) {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Context context = rootView.getContext();
            Rect r = new Rect();
            Resources res = rootView.getResources();

            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = getUsableScreenHeight(rootView);
            int heightDifference = screenHeight - (r.bottom - r.top);
            int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");

            if (resourceId > 0) {
                heightDifference -= res.getDimensionPixelSize(resourceId);
            }

            if (heightDifference > 100) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_KEYBOARD_HEIGHT, heightDifference).apply();
            }
        });
    }

    private static int getUsableScreenHeight(View rootView) {
        DisplayMetrics metrics = new DisplayMetrics();

        WindowManager windowManager = (WindowManager) rootView.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        return metrics.heightPixels;
    }

}
