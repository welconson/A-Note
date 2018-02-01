package com.tcl.shenwk.aNote.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.Util.ImeController;
import com.tcl.shenwk.aNote.multiMediaInputSupport.CustomMovementMethod;
import com.tcl.shenwk.aNote.view.Constants;
import com.tcl.shenwk.aNote.view.customSpan.CustomImageSpan;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by shenwk on 2018/1/25.
 */

public class EditNoteActivity extends AppCompatActivity{
    private static String TAG = "EditNoteActivity";
    private EditText mNoteContentText;
    private EditText mNoteTitle;
    private ImeController mImeController;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        mImeController = new ImeController(this);

        ImageButton imageButton = findViewById(R.id.back);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditNoteActivity.super.onBackPressed();
            }
        });
        mNoteContentText = findViewById(R.id.note_content);
        mNoteContentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNoteContentText.setMovementMethod(CustomMovementMethod.getInstance());

        mNoteTitle = findViewById(R.id.note_title);
        mNoteTitle.requestFocus();

        //set soft input mode
        EditNoteActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //set soft input mode

        //set keyboard listener
        View editArea = findViewById(R.id.edit_area);
        editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteContentText.requestFocus();
                mImeController.toggleSoftInput();

            }
        });
        //set keyboard listener end

        //set add image button listener
        imageButton = findViewById(R.id.add_image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            InputStream inputStream = null;
            Bitmap bitmap = null;
            if(data != null) {
                Uri uri = data.getData();
                if(uri != null) {
                    try {
                        inputStream = getContentResolver().openInputStream(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        Drawable drawable = new BitmapDrawable(bitmap);
                        int width = drawable.getIntrinsicWidth();
                        int height = drawable.getIntrinsicHeight();
                        drawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);
                        if (bitmap != null) {
                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(mNoteContentText.getText());
                            ImageSpan imageSpan = new ImageSpan(drawable);
                            CustomImageSpan customImageSpan = new CustomImageSpan(drawable);
                            spannableStringBuilder.insert(mNoteContentText.getSelectionEnd(), Constants.IMAGE_SPAN_TAG);
                            int editPosition = mNoteContentText.getSelectionEnd() + Constants.IMAGE_SPAN_TAG.length();
                            spannableStringBuilder.setSpan(customImageSpan, mNoteContentText.getSelectionEnd(),
                                    editPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            mNoteContentText.setText(spannableStringBuilder);
                            mNoteContentText.requestFocus();
                            mNoteContentText.setSelection(editPosition);
                            //get image from editable test
//                    Editable editable = mNoteContentText.getEditableText();
//                    int start = editable.getSpanStart(imageSpan);
//                    if(start != -1){
//                        ImageSpan[] spans = editable.getSpans(start, start + Constants.IMAGE_SPAN_TAG.length(), ImageSpan.class);
//                        mNoteContentText.setBackground(spans[0].getDrawable());
//                    }
                            //get image from editable test end
                        }
                    }
                }
            }
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }
}
