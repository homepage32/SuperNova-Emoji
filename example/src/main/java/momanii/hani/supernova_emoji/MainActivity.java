package momanii.hani.supernova_emoji;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import hani.momanii.supernova_emoji_library.EmojiKeyboard;

public class MainActivity extends AppCompatActivity {
    EditText emojiconEditText, emojiconEditText2;
    TextView textView;
    ImageView emojiButton;
    ImageView submitButton;
    ViewGroup rootView;
    EmojiKeyboard emojIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.emoji_btn);
        submitButton = findViewById(R.id.submit_btn);
        emojiconEditText = findViewById(R.id.emojicon_edit_text);
        emojiconEditText2 = findViewById(R.id.emojicon_edit_text2);
        textView = findViewById(R.id.textView);
        emojIcon = new EmojiKeyboard(this, rootView);
        emojIcon.setKeyboardListener(new EmojiKeyboard.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                emojiButton.setImageResource(R.drawable.ic_keyboard_black_24dp);
            }

            @Override
            public void onKeyboardClose() {
                emojiButton.setImageResource(R.drawable.ic_insert_emoticon_black_24dp);
            }
        });
        emojIcon.showEmojiKeyboard(emojiconEditText);

        submitButton.setOnClickListener(v -> {
            String newText = emojiconEditText.getText().toString();
            textView.setText(newText);
        });
    }
}