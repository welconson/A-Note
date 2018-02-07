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
import android.widget.EditText;
import android.widget.ImageButton;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.EditNoteHandler;
import com.tcl.shenwk.aNote.util.ImeController;
import com.tcl.shenwk.aNote.multiMediaInputSupport.CustomMovementMethod;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.customSpan.CustomImageSpan;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Activity for edit view to display, get input events from user.
 * Created by shenwk on 2018/1/25.
 */

public class EditNoteActivity extends AppCompatActivity{
    private static String TAG = "EditNoteActivity";
    //Action from calling, indicate the activity is started as adding note or modifying note
    public static String EDIT_TYPE_ADD = "add";
    public static String EDIT_TYPE_MODIFY = "modify";
    //mode value
    public static int MODE_EDIT = 0;
    public static int MODE_PREVIEW = 1;
    private int mMode = MODE_PREVIEW;
    private EditText mNoteContentText;
    private EditText mNoteTitle;
    private ImeController mImeController;
    private ImageButton mBackButton;
    private ImageButton mSaveButton;
    private ImageButton mAddImageButton;
    private long mNoteId = Constants.NO_NOTE_ID;
    private NoteEntry mNoteEntry;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        mImeController = new ImeController(this);

        mBackButton = findViewById(R.id.back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditNoteActivity.super.onBackPressed();
            }
        });

        mSaveButton = findViewById(R.id.save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeSetting(MODE_PREVIEW);
                mNoteContentText.clearFocus();
                mNoteTitle.clearFocus();
                mNoteEntry.setNoteContent(mNoteContentText.getText().toString());
                mNoteEntry.setNoteTitle(mNoteTitle.getText().toString());
                if(!EditNoteHandler.saveNote(mNoteEntry, EditNoteActivity.this))
                    Log.i(TAG, "onClick: save note failed");
                Log.i(TAG, "onClick: save button onclick " + mNoteId);
            }
        });

        mNoteContentText = findViewById(R.id.note_content);
        mNoteContentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "onTextChanged: " + mNoteContentText.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNoteContentText.setMovementMethod(CustomMovementMethod.getInstance());

        mNoteTitle = findViewById(R.id.note_title);
        mNoteTitle.requestFocus();

        //set keyboard listener
        View editArea = findViewById(R.id.edit_area);
        editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: mNoteContentText.hasFocus() " + mNoteContentText.hasFocus());
                if(mMode == MODE_PREVIEW){
                    modeSetting(MODE_EDIT);
                }
                if(mNoteContentText.hasFocus()){
                    //toggle soft keyboard as the content text has got focused
                    mImeController.toggleSoftInput();
                }
                else {
                    //request for focus and show soft keyboard
                    mNoteContentText.requestFocus();
                    mImeController.showSoftInput(mNoteContentText);
                }
            }
        });
        //set keyboard listener end

        //set add image button listener
        mAddImageButton = findViewById(R.id.add_image_button);
        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        int mode;
        Intent intent = getIntent();
        String action_edit = intent.getStringExtra(Constants.ACTION_EDIT_NOTE);
        if(StringUtil.equal(EDIT_TYPE_MODIFY, action_edit)) {
            mode = MODE_PREVIEW;
            mNoteEntry = ANoteDBManager.getInstance(EditNoteActivity.this).querySingleNoteRecordById(
                    intent.getLongExtra(Constants.EDIT_NOTE_ID_NAME, Constants.NO_NOTE_ID));
            mNoteTitle.setText(mNoteEntry.getNoteTitle());
        }
        else {
            mode = MODE_EDIT;
            mNoteEntry = new NoteEntry();
        }
        modeSetting(mode);
        if(mMode == MODE_EDIT){
            EditNoteActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            mNoteTitle.requestFocus();
//            There is no use to show or hide soft input in onCreate method
//            mImeController.showSoftInput(mNoteTitle);
        }
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

    /**
     *
     * @param mode set edit activity to pointed mode.
     *             edit mode: show save button......
     *             preview mode: show back button......
     */
    private void modeSetting(int mode){
        if(mode == MODE_EDIT){
            mMode = mode;
            mSaveButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.INVISIBLE);
            mNoteContentText.setEnabled(true);
            mNoteTitle.setEnabled(true);
        }
        else if(mode == MODE_PREVIEW){
            mMode = mode;
            mBackButton.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.INVISIBLE);
            mNoteContentText.setEnabled(false);
            mNoteTitle.setEnabled(false);
        }
        else Log.i(TAG, "modeSetting: invalid mode");
    }
}
