package com.tcl.shenwk.aNote.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.model.EditNoteHandler;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.ImeController;
import com.tcl.shenwk.aNote.multiMediaInputSupport.CustomMovementMethod;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.customSpan.AudioViewSpan;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;
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
    private ImageButton mAddAudioButton;
    private long mNoteId = Constants.NO_NOTE_ID;
    private NoteEntry mNoteEntry;
    private boolean mIsModified;

    private int contentInsideXOffset;
    private int contentInsideYOffset;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        final Intent intent = getIntent();
        mImeController = new ImeController(this);
        mIsModified = false;

        mBackButton = findViewById(R.id.back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(EditNoteActivity.this, HomePageActivity.class);
                if(mIsModified) {
                    nextIntent.putExtra(Constants.ITEM_ENTRY, mNoteEntry);
                    nextIntent.putExtra(Constants.ACTION_EDIT_NOTE, intent.getStringExtra(Constants.ACTION_EDIT_NOTE));
                    nextIntent.putExtra(Constants.ITEM_POSITION, intent.getIntExtra(
                            Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION));
                    nextIntent.putExtra(Constants.ACTION_TO_HOME_PAGE, Constants.HOME_PAGE_UPDATE_RESUME);
                } else {
                    nextIntent.putExtra(Constants.ACTION_TO_HOME_PAGE, Constants.HOME_PAGE_NORMAL_RESUME);
                }
                EditNoteActivity.this.startActivity(nextIntent);
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
        contentInsideXOffset = 1;
        mNoteContentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "onTextChanged: " + mNoteContentText.getText());
                mIsModified = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNoteContentText.setMovementMethod(CustomMovementMethod.getInstance());
        int []location = new int [2];
        mNoteContentText.getLocationOnScreen(location);
        contentInsideXOffset = location[0] + mNoteContentText.getPaddingLeft();
        contentInsideYOffset = location[1] + mNoteContentText.getPaddingTop();
        mNoteTitle = findViewById(R.id.note_title);
        mNoteTitle.requestFocus();

        //set keyboard listener
        View editArea = findViewById(R.id.edit_area);
        editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == MODE_PREVIEW){
                    modeSetting(MODE_EDIT);
                }
                if (mNoteContentText.hasFocus()) {
                    //toggle soft keyboard as the content text has got focused
                    mImeController.toggleSoftInput();
                } else {
                    //request for focus and show soft keyboard
                    mNoteContentText.requestFocus();
                    mImeController.showSoftInput(mNoteContentText);
                }
            }
        });
        //set keyboard listener end

        //set add image button listener

        //add buttons onClick listener settings
        mAddImageButton = findViewById(R.id.add_image_button);
        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_IMAGE);
            }
        });
        mAddAudioButton = findViewById(R.id.add_audio_button);
        mAddAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_AUDIO);
            }
        });

        int mode;
        String action_edit = intent.getStringExtra(Constants.ACTION_EDIT_NOTE);
        if(StringUtil.equal(EDIT_TYPE_MODIFY, action_edit)) {
            mode = MODE_PREVIEW;
            mNoteEntry = (NoteEntry) intent.getSerializableExtra(Constants.ITEM_ENTRY);
            mNoteTitle.setText(mNoteEntry.getNoteTitle());
            mNoteContentText.setText(mNoteEntry.getNoteContent());
        }
        else {
            mode = MODE_EDIT;
            mIsModified = true;
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
        //handle select image result
        if(requestCode == Constants.SELECT_IMAGE){
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
        //handle select audio result
        else if(requestCode == Constants.SELECT_AUDIO) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String audioName = getFileNameFromURI(EditNoteActivity.this, uri);
                    Log.i(TAG, "onActivityResult: " + audioName);
//                    Log.i(TAG, "onActivityResult: isReadable = " + FileUtil.isExternalStorageReadable()
//                    + " isWritable = " + FileUtil.isExternalStorageWritable());
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(EditNoteActivity.this, uri);
                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    Log.i(TAG, "onActivityResult: durationStr = " + durationStr);


                    LayoutInflater layoutInflater = getLayoutInflater();
                    View view = layoutInflater.inflate(R.layout.audio_span_layout, (ViewGroup) mNoteContentText.getParent(), false);
                    AudioViewSpan audioViewSpan = new AudioViewSpan(view,contentInsideXOffset, contentInsideYOffset);
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(mNoteContentText.getText());
                    spannableStringBuilder.insert(mNoteContentText.getSelectionEnd(), Constants.AUDIO_SPAN_TAG);
                    int editPosition = mNoteContentText.getSelectionEnd() + Constants.AUDIO_SPAN_TAG.length();
                    spannableStringBuilder.setSpan(audioViewSpan, mNoteContentText.getSelectionEnd(),
                            editPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    TextView textView = view.findViewById(R.id.audio_time);
                    textView.setText(StringUtil.DurationFormat(durationStr));
                    textView = view.findViewById(R.id.audio_title);
                    textView.setText(audioName);
                    mNoteContentText.setText(spannableStringBuilder);
                    mNoteContentText.requestFocus();
                    mNoteContentText.setSelection(editPosition);
                }

                Log.i(TAG, "onActivityResult: requestCode = SELECT_AUDIO");
            }
        } else super.onActivityResult(requestCode, resultCode, data);
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

    /**
     *
     * @param context activity context.
     * @param contentUri uri with the content.
     * @return real path of the uri.
     */
    public String getFileNameFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        String ret = null;
        String[] proj = { MediaStore.Audio.Media.DISPLAY_NAME };
        cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
        if(cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            cursor.moveToFirst();
            ret = cursor.getString(column_index);
            cursor.close();
            Log.i(TAG, "getFileNameFromURI: try " + ret);
            return ret;
        }
        return null;
    }
}
