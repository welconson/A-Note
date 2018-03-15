package com.tcl.shenwk.aNote.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.ImeController;
import com.tcl.shenwk.aNote.multiMediaInputSupport.CustomMovementMethod;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.PermissionUtil;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.customSpan.AudioViewSpan;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for edit view to display, get input events from user.
 * Created by shenwk on 2018/1/25.
 */

public class EditNoteActivity extends AppCompatActivity{
    private static String TAG = "EditNoteActivity";
    //Action from calling, indicate the activity is started as adding note or modifying note
    public static String EDIT_TYPE_CREATE = "create";
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
    private NoteEntry mNoteEntry;
    private boolean mIsModified;
    private List<ViewSpan> viewSpans;

    private List<ActivityResult> unhandledActivityResults = new ArrayList<>();
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        final Intent intent = getIntent();
        mImeController = new ImeController(this);
        mIsModified = false;
        viewSpans = new ArrayList<>();

        mBackButton = findViewById(R.id.back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(EditNoteActivity.this, HomePageActivity.class);
                if(mIsModified) {
                    nextIntent.putExtra(Constants.ITEM_NOTE_ENTRY, mNoteEntry);
                    nextIntent.putExtra(Constants.ACTION_EDIT_NOTE, intent.getStringExtra(Constants.ACTION_EDIT_NOTE));
                    nextIntent.putExtra(Constants.ITEM_POSITION, intent.getIntExtra(
                            Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION));
                    nextIntent.putExtra(Constants.ACTION_TO_HOME_PAGE, Constants.HOME_PAGE_UPDATE_RESUME);
                    nextIntent.putExtra(Constants.ITEM_RESOURCE_ENTRY, getFirstResource());
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
                mNoteEntry.setNoteTitle(mNoteTitle.getText().toString());
                if(!NoteHandler.saveNote(mNoteEntry, EditNoteActivity.this,
                        mNoteContentText.getText(), viewSpans))
                    Log.i(TAG, "onClick: save note failed");
                Log.i(TAG, "onClick: save button onclick " + mNoteEntry.getNoteId());
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
                mIsModified = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNoteContentText.setMovementMethod(CustomMovementMethod.getInstance());
        int []location = new int [2];
        mNoteContentText.getLocationOnScreen(location);
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
                intent.addCategory(Intent.CATEGORY_OPENABLE);
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
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_AUDIO);
            }
        });

        int mode;
        String action_edit = intent.getStringExtra(Constants.ACTION_EDIT_NOTE);
        // to know this activity is used to create a new note or modify a exist note
        if(StringUtil.equal(EDIT_TYPE_MODIFY, action_edit)) {
            mode = MODE_PREVIEW;
            mNoteEntry = (NoteEntry) intent.getSerializableExtra(Constants.ITEM_NOTE_ENTRY);
            mNoteTitle.setText(mNoteEntry.getNoteTitle());
            mNoteContentText.setText(FileUtil.readFile(
                    FileUtil.getContentFileName(mNoteEntry.getNotePath())));
            inflateViewSpanWithResourceEntry(NoteHandler.getResourceDataById(
                    EditNoteActivity.this, mNoteEntry.getNoteId()));
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

    private ResourceDataEntry getFirstResource() {
        ResourceDataEntry resourceDataEntry = null;
        if(viewSpans.size() > 0){
            Editable editable = mNoteContentText.getText();
            int firstSpanStart = editable.length();
            for(ViewSpan viewSpan : viewSpans){
                int spanStart = editable.getSpanStart(viewSpan);
                if(firstSpanStart > spanStart){
                    firstSpanStart = spanStart;
                    resourceDataEntry = viewSpan.getResourceDataEntry();
                }
            }
        }
        return resourceDataEntry;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String requiredPermission = getRequiredPermission(requestCode);
        boolean isRequiringPermission = false;
        unhandledActivityResults.add(new ActivityResult(requestCode, resultCode, data));

        // schedule the permission thread task and span insert task
        if(requiredPermission != null && FileUtil.isUriPointToExternalStorage(
                EditNoteActivity.this ,data.getData())) {
            if(!PermissionUtil.checkPermission(EditNoteActivity.this, requiredPermission)) {
                // need to require permission, data will be handled
                // in permission requiring callback method.
                Log.i(TAG, "onActivityResult: requiring permission");
                PermissionUtil.requirePermission(EditNoteActivity.this, requiredPermission);
                isRequiringPermission = true;
            }
        }
        if(!isRequiringPermission) {
            // no permission requiring needed operation here
            Log.d(TAG, "onActivityResult: no need to require permission");
            mHandler.sendMessage(mHandler.obtainMessage(requestCode));
        }
    }

    private String getRequiredPermission(int requestCode) {
        String requiredPermission;
        switch (requestCode){
            case Constants.SELECT_AUDIO:
                requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            default:requiredPermission = null;
        }
        return requiredPermission;
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
     * Add new span into the ViewSpan list.
     * @param viewSpan viewSpan need to be added.
     */
    public void addSpan(ViewSpan viewSpan){
        if(viewSpan != null){
            viewSpans.add(viewSpan);
        }
    }

    public void removeSpan(ViewSpan viewSpan){
        if(viewSpan != null) {
            int index = 0;
            for(ViewSpan iterator : viewSpans) {
                if(iterator == viewSpan) {
                    viewSpans.remove(index);
                    break;
                }
                index++;
            }
        }
    }

    public void inflateViewSpanWithResourceEntry(List<ResourceDataEntry> resourceDataEntries){
        if(resourceDataEntries == null)
            return;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(mNoteContentText.getText());
        for(ResourceDataEntry resourceDataEntry : resourceDataEntries){
            LayoutInflater layoutInflater = getLayoutInflater();
            ViewSpan viewSpan;
            View view;
            int duration = -1;
            switch (resourceDataEntry.getDataType()){
                case Constants.RESOURCE_TYPE_IMAGE:
                    view = layoutInflater.inflate(R.layout.audio_span_layout, (ViewGroup) mNoteContentText.getParent(), false);
                    viewSpan = new AudioViewSpan(view, resourceDataEntry);
                    break;
                case Constants.RESOURCE_TYPE_AUDIO:
                    view = layoutInflater.inflate(R.layout.audio_span_layout, null, false);
                    viewSpan = new AudioViewSpan(view, resourceDataEntry);
                    duration = ((AudioViewSpan)viewSpan).getDuration();
                    break;
                case Constants.RESOURCE_TYPE_VIDEO:
                    view = layoutInflater.inflate(R.layout.audio_span_layout, (ViewGroup) mNoteContentText.getParent(), false);
                    viewSpan = new AudioViewSpan(view, resourceDataEntry);
                    break;
                    default:continue;
            }
            int editPosition = resourceDataEntry.getSpanStart() + Constants.AUDIO_SPAN_TAG.length();
            spannableStringBuilder.setSpan(viewSpan, resourceDataEntry.getSpanStart(),
                    editPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(duration != -1) {
                TextView textView = view.findViewById(R.id.audio_time);
                textView.setText(StringUtil.DurationFormat(duration));
                textView = view.findViewById(R.id.audio_title);
                textView.setText(viewSpan.getFileName());
            }
            addSpan(viewSpan);
        }
        mNoteContentText.setText(spannableStringBuilder);
        mNoteContentText.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PermissionUtil.PermissionRequestCode.REQUEST_STORAGE:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG, "onRequestPermissionsResult: storage permission granted");
                    mHandler.sendMessage(mHandler.obtainMessage(unhandledActivityResults.get(0).requestCode));
                }
                else {
                    Log.i(TAG, "onRequestPermissionsResult: storage permission not granted");
                    Toast.makeText(EditNoteActivity.this, Constants.TOAST_TEXT_WITHOUT_PERMISSION, Toast.LENGTH_SHORT).show();
                    unhandledActivityResults.remove(0);
                }
                break;
                default:
        }
    }

    public class ActivityResult{
        public int requestCode;
        public int resultCode;
        public final Intent data;

        ActivityResult(int requestCode, int resultCode, Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SELECT_AUDIO:
                    if(unhandledActivityResults.size() >0) {
                        selectAudio();
                    }
            }
        }
    };

    private void selectAudio(){
        ActivityResult activityResult = unhandledActivityResults.remove(0);
        Intent data = activityResult.data;
        if (data != null) {
            Uri uri = data.getData();
            String fileName = FileUtil.getFileNameFromURI(EditNoteActivity.this, uri, Constants.RESOURCE_TYPE_AUDIO);
            if (uri != null) {
                int spanStart = mNoteContentText.getSelectionEnd();
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.audio_span_layout, (ViewGroup) mNoteContentText.getParent(), false);

                // After AudioView initializing, it will get file name and resource data type.
                // There is no need to set it ourselves.
                ResourceDataEntry resourceDataEntry = new ResourceDataEntry();
                resourceDataEntry.setFileName(fileName);
                resourceDataEntry.setSpanStart(spanStart);
                AudioViewSpan audioViewSpan = new AudioViewSpan(view, uri, resourceDataEntry);
                String audioName = audioViewSpan.getFileName();
                Log.i(TAG, "onActivityResult: " + audioName);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(mNoteContentText.getText());
                spannableStringBuilder.insert(spanStart, Constants.AUDIO_SPAN_TAG);
                int editPosition = spanStart + Constants.AUDIO_SPAN_TAG.length();
                spannableStringBuilder.setSpan(audioViewSpan, spanStart,
                        editPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                TextView textView = view.findViewById(R.id.audio_time);
                textView.setText(StringUtil.DurationFormat(audioViewSpan.getDuration()));
                textView = view.findViewById(R.id.audio_title);
                textView.setText(audioName);
                mNoteContentText.setText(spannableStringBuilder);
                mNoteContentText.requestFocus();
                mNoteContentText.setSelection(editPosition);
                addSpan(audioViewSpan);
            }
        }

    }
}
