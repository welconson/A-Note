package com.tcl.shenwk.aNote.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.multiMediaInputSupport.CustomScrollingMovementMethod;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.ImeController;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.PermissionUtil;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.customSpan.AudioViewSpan;
import com.tcl.shenwk.aNote.view.customSpan.FileViewSpan;
import com.tcl.shenwk.aNote.view.customSpan.ImageViewSpan;
import com.tcl.shenwk.aNote.view.customSpan.VideoViewSpan;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;
import com.tcl.shenwk.aNote.view.fragment.TagRecordEditFragment;

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
    private View mEditLayout;
    private EditText mNoteContentText;
    private EditText mNoteTitle;
    private ImeController mImeController;
    private ImageButton mBackButton;
    private ImageButton mSaveButton;
    private ImageButton mAddImageButton;
    private ImageButton mAddAudioButton;
    private ImageButton mAddVideoButton;
    private ImageButton mAddFileButton;
    private ImageButton mTagEditor;
    private FloatingActionButton mEditNoteButton;
    private View mEditToolBar;
    private NoteEntity mNoteEntity;
    private boolean mIsModified;
    private boolean mIsNewNote;
    private List<ViewSpan> mViewSpans;
    private List<TagRecordEntity> mTagRecordEntries;

    private List<ActivityResult> unhandledActivityResults = new ArrayList<>();
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        final Intent intent = getIntent();
        mImeController = new ImeController(this);
        mIsModified = false;
        mViewSpans = new ArrayList<>();

        mEditLayout = findViewById(R.id.linearLayout);
        mEditToolBar = findViewById(R.id.edit_tool_bar);
        mBackButton = findViewById(R.id.back);
        mBackButton.setOnClickListener(backButtonOnClickListener);

        mSaveButton = findViewById(R.id.save);
        mSaveButton.setOnClickListener(saveButtonOnClickListener);

        mNoteContentText = findViewById(R.id.note_content);
        mNoteContentText.addTextChangedListener(textWatcher);
//        mNoteContentText.setMovementMethod(CustomMovementMethod.getInstance());
        mNoteContentText.setMovementMethod(CustomScrollingMovementMethod.getInstance());
        mNoteTitle = findViewById(R.id.note_title);
        mNoteTitle.addTextChangedListener(textWatcher);

        //set keyboard listener
        View editArea = findViewById(R.id.edit_area);

        // now this is a invalid listener, there must be one child view
        // have intercepted the MotionEvents from it.
        editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: edit area");
                if (mMode == MODE_PREVIEW) {
                    modeSetting(MODE_EDIT);
                }
                else if (mNoteContentText.hasFocus()) {
                    //toggle soft keyboard as the content text has got focused
                    mImeController.toggleSoftInput();
                }

            }
        });
        //add buttons onClick listener settings
        mAddImageButton = findViewById(R.id.add_image_button);
        mAddImageButton.setOnClickListener(addImageButtonOnClickListener);
        mAddAudioButton = findViewById(R.id.add_audio_button);
        mAddAudioButton.setOnClickListener(addAudioButtonOnClickListener);
        mAddVideoButton = findViewById(R.id.add_video_button);
        mAddVideoButton.setOnClickListener(addVideoButtonOnClickListener);
        mAddFileButton = findViewById(R.id.add_file_button);
        mAddFileButton.setOnClickListener(addFileButtonOnClickListener);
        mEditNoteButton = findViewById(R.id.edit_note_button);
        mEditNoteButton.setOnClickListener(editNoteButtonOnClickListener);
        mTagEditor = findViewById(R.id.tag_editor);
        mTagEditor.setOnClickListener(tagEditorOnClickListener);

        int mode;
        String action_edit = intent.getStringExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE);
        // to know this activity is used to create a new note or modify a exist note
        if(StringUtil.equal(EDIT_TYPE_MODIFY, action_edit)) {
            mode = MODE_PREVIEW;
            mNoteEntity = (NoteEntity) intent.getSerializableExtra(Constants.ITEM_NOTE_entity);
            mNoteTitle.setText(mNoteEntity.getNoteTitle());
            mNoteContentText.setText(FileUtil.readFile(
                    FileUtil.getContentFileName(mNoteEntity.getNotePath())));
            inflateViewSpanWithResourceentity(NoteHandler.getResourceDataById(
                    EditNoteActivity.this, mNoteEntity.getNoteId()));
            mIsNewNote = false;
            mTagRecordEntries = ANoteDBManager.getInstance(EditNoteActivity.this).
                    queryAllTagRecordByNoteId(mNoteEntity.getNoteId());
        }
        else {
            mode = MODE_EDIT;
            mNoteEntity = new NoteEntity();
            long withTagId = intent.getLongExtra(Constants.WITH_TAG_ID, Constants.NO_TAG_ID);
            if( withTagId != Constants.NO_TAG_ID) {
                mTagRecordEntries = new ArrayList<>();
                TagRecordEntity tagRecordEntity = new TagRecordEntity();
                tagRecordEntity.status = TagRecordEntity.NEW_CREATE;
                tagRecordEntity.setTagId(withTagId);
                mTagRecordEntries.add(tagRecordEntity);
            }
            mIsNewNote = true;
        }
        modeSetting(mode);
        if(mMode == MODE_EDIT){
            EditNoteActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            mNoteTitle.requestFocus();
//            There is no use to show or hide soft input in onCreate method
//            mImeController.showSoftInput(mNoteTitle);
        }
    }

    private ResourceDataEntity getFirstResource() {
        ResourceDataEntity resourceDataEntity = null;
        if(mViewSpans.size() > 0){
            Editable editable = mNoteContentText.getText();
            int firstSpanStart = editable.length();
            for(ViewSpan viewSpan : mViewSpans){
                int spanStart = editable.getSpanStart(viewSpan);
                if(firstSpanStart > spanStart){
                    firstSpanStart = spanStart;
                    resourceDataEntity = viewSpan.getResourceDataentity();
                }
            }
        }
        return resourceDataEntity;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String requiredPermission = getRequiredPermission(requestCode);
        boolean isRequiringPermission = false;
        unhandledActivityResults.add(new ActivityResult(requestCode, resultCode, data));

        // schedule the permission thread task and span insert task
        if(requiredPermission != null && data != null && FileUtil.isUriPointToExternalStorage(
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
            mHandler.sendMessage(mHandler.obtainMessage(Constants.MESSAGE_ON_ACTIVITY_RESULT));
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
            mNoteContentText.setFocusableInTouchMode(true);
            mNoteTitle.setFocusableInTouchMode(true);
            mNoteContentText.requestFocus();
            mNoteContentText.setSelection(mNoteContentText.getEditableText().length());
            mEditNoteButton.setVisibility(View.INVISIBLE);
            mEditToolBar.setVisibility(View.VISIBLE);
        }
        else if(mode == MODE_PREVIEW){
            mMode = mode;
            mBackButton.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.INVISIBLE);
            mEditNoteButton.setVisibility(View.VISIBLE);
            mNoteContentText.setFocusableInTouchMode(false);
            mNoteTitle.setFocusableInTouchMode(false);
            mNoteTitle.clearFocus();
            mNoteContentText.clearFocus();
            mImeController.hideSoftInput(mNoteContentText);
            mEditToolBar.setVisibility(View.INVISIBLE);
        }
        else Log.i(TAG, "modeSetting: invalid mode");
    }

    /**
     * Add new span into the ViewSpan list.
     * @param viewSpan viewSpan need to be added.
     */
    public void addSpan(ViewSpan viewSpan){
        if(viewSpan != null){
            mViewSpans.add(viewSpan);
        }
    }

    public void removeSpan(ViewSpan viewSpan){
        if(viewSpan != null) {
            int index = 0;
            for(ViewSpan iterator : mViewSpans) {
                if(iterator == viewSpan) {
                    mViewSpans.remove(index);
                    break;
                }
                index++;
            }
        }
    }

    public void inflateViewSpanWithResourceentity(List<ResourceDataEntity> resourceDataEntries){
        if(resourceDataEntries == null)
            return;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(mNoteContentText.getText());
        for(ResourceDataEntity resourceDataEntity : resourceDataEntries){
            LayoutInflater layoutInflater = getLayoutInflater();
            ViewSpan viewSpan;
            View view;
            int duration = -1;
            switch (resourceDataEntity.getDataType()){
                case Constants.RESOURCE_TYPE_IMAGE:
                    view = layoutInflater.inflate(R.layout.image_span_layout, (ViewGroup) mNoteContentText.getParent(), false);
                    viewSpan = new ImageViewSpan(view, resourceDataEntity);
                    break;
                case Constants.RESOURCE_TYPE_AUDIO:
                    view = layoutInflater.inflate(R.layout.audio_span_layout, null, false);
                    viewSpan = new AudioViewSpan(view, resourceDataEntity);
                    duration = ((AudioViewSpan)viewSpan).getDuration();
                    break;
                case Constants.RESOURCE_TYPE_VIDEO:
                    view = layoutInflater.inflate(R.layout.video_span_layout, (ViewGroup) mNoteContentText.getParent(), false);
                    viewSpan = new VideoViewSpan(view, resourceDataEntity);
                    break;
                case Constants.RESOURCE_TYPE_FILE:
                    view = layoutInflater.inflate(R.layout.file_span_layout, (ViewGroup) mNoteContentText.getParent(), false);
                    viewSpan = new FileViewSpan(view, resourceDataEntity);
                    break;
                    default:continue;
            }
            int editPosition = resourceDataEntity.getSpanStart() + Constants.AUDIO_SPAN_TAG.length();
            spannableStringBuilder.setSpan(viewSpan, resourceDataEntity.getSpanStart(),
                    editPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(duration != -1) {
                TextView textView = view.findViewById(R.id.duration);
                textView.setText(StringUtil.DurationFormat(duration));
                textView = view.findViewById(R.id.resource_name);
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
                case Constants.MESSAGE_ON_ACTIVITY_RESULT:
                    handleActivityResult();
                    break;
            }
            mImeController.showSoftInput(mNoteContentText);
        }
    };

    private void handleActivityResult(){
        ActivityResult activityResult = unhandledActivityResults.remove(0);
        Intent data = activityResult.data;
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                createViewSpanForUriResource(activityResult.requestCode, uri);
            }
        }
    }

    private void createViewSpanForUriResource(int type, Uri uri){
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        ViewSpan viewSpan;
        switch (type){
            case Constants.RESOURCE_TYPE_IMAGE: {
                view = layoutInflater.inflate(R.layout.image_span_layout, (ViewGroup) mNoteContentText.getParent(), false);

                ResourceDataEntity resourceDataEntity = new ResourceDataEntity();
                viewSpan = new ImageViewSpan(view, uri, resourceDataEntity);

                break;
            }
            case Constants.RESOURCE_TYPE_AUDIO: {
                view = layoutInflater.inflate(R.layout.audio_span_layout, (ViewGroup) mNoteContentText.getParent(), false);

                ResourceDataEntity resourceDataEntity = new ResourceDataEntity();

                viewSpan = new AudioViewSpan(view, uri, resourceDataEntity);
                TextView textView = view.findViewById(R.id.duration);
                textView.setText(StringUtil.DurationFormat(((AudioViewSpan)viewSpan).getDuration()));
                textView = view.findViewById(R.id.resource_name);
                textView.setText(viewSpan.getFileName());

                break;
            }
            case Constants.RESOURCE_TYPE_VIDEO:{
                view = layoutInflater.inflate(R.layout.video_span_layout, (ViewGroup) mNoteContentText.getParent(), false);

                ResourceDataEntity resourceDataEntity = new ResourceDataEntity();

                viewSpan = new VideoViewSpan(view, uri, resourceDataEntity);
                TextView textView = view.findViewById(R.id.duration);
                textView.setText(StringUtil.DurationFormat(((VideoViewSpan)viewSpan).getDuration()));
                textView = view.findViewById(R.id.resource_name);
                textView.setText(viewSpan.getFileName());

                break;
            }
            case Constants.RESOURCE_TYPE_FILE:{
                view = layoutInflater.inflate(R.layout.file_span_layout, (ViewGroup) mNoteContentText.getParent(), false);

                ResourceDataEntity resourceDataEntity = new ResourceDataEntity();

                viewSpan = new FileViewSpan(view, uri, resourceDataEntity);
                TextView textView = view.findViewById(R.id.duration);
                textView.setText("File");
                textView = view.findViewById(R.id.resource_name);
                textView.setText(viewSpan.getFileName());

                break;
            }
            default:
                viewSpan = null;
                Log.i(TAG, "createViewSpanForUriResource: view span type error");
        }
        if(viewSpan != null) {
            int spanStart = mNoteContentText.getSelectionEnd();
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(mNoteContentText.getText());
            spannableStringBuilder.insert(spanStart, Constants.RESOURCE_TAG);
            int editPosition = spanStart + Constants.RESOURCE_TAG.length();
            spannableStringBuilder.setSpan(viewSpan, spanStart,
                    editPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mNoteContentText.setText(spannableStringBuilder);
            mNoteContentText.requestFocus();
            mNoteContentText.setSelection(editPosition);
            addSpan(viewSpan);
        }
    }

    public List<TagRecordEntity> getNoteTagRecord(){
        return mTagRecordEntries;
    }

    //listener callback definitions
    private View.OnClickListener saveButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mIsNewNote && mNoteContentText.getEditableText().length() == 0 &&
                    mNoteTitle.getEditableText().length() == 0){
                Toast.makeText(EditNoteActivity.this, Constants.TOAST_NEW_NOTE_WITH_NOTHING, Toast.LENGTH_SHORT).show();
            }
            else if(mIsModified) {
                mNoteEntity.setNoteTitle(mNoteTitle.getText().toString());
                if (NoteHandler.saveNote(mNoteEntity, EditNoteActivity.this,
                        mNoteContentText.getText(), mViewSpans, mTagRecordEntries)){
                    // Only when it is a new note, we will save tag records there, or
                    // we will do it when choosing tags is just done.
                    if(mIsNewNote){
                        mIsNewNote = false;
                        NoteHandler.saveTagRecord(EditNoteActivity.this,
                                mNoteEntity.getNoteId(), mTagRecordEntries);
                    }
                } else Log.i(TAG, "onClick: save note failed");
                Log.i(TAG, "onClick: save button onclick " + mNoteEntity.getNoteId());
            }
            modeSetting(MODE_PREVIEW);
        }
    };

    private View.OnClickListener addImageButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_IMAGE);
        }
    };

    private View.OnClickListener addAudioButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_AUDIO);
        }
    };

    private View.OnClickListener addVideoButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_VIDEO);
        }
    };

    private View.OnClickListener addFileButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, null), Constants.SELECT_FILE);
        }
    };

    private View.OnClickListener editNoteButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            modeSetting(MODE_EDIT);
        }
    };

    private View.OnClickListener backButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent nextIntent = new Intent();
            if(mIsModified) {
                Intent intent = getIntent();
                nextIntent.putExtra(Constants.ITEM_NOTE_entity, mNoteEntity);
                nextIntent.putExtra(Constants.ITEM_POSITION, intent.getIntExtra(
                        Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION));
            }
            setResult(RESULT_OK, nextIntent);
            finish();
        }
    };

    private View.OnClickListener tagEditorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = getFragmentManager();
//            fragmentManager.beginTransaction()
//                    .add(R.id.edit_note_layout, new TagRecordEditFragment())
//                    .commit();
            TagRecordEditFragment tagRecordEditFragment = new TagRecordEditFragment();
            tagRecordEditFragment.setExitCallback(tagSelectDoneCallback);
            tagRecordEditFragment.show(fragmentManager, "test");
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.i(TAG, "onTextChanged: " + s);
            mIsModified = true;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TagRecordEditFragment.ExitCallback tagSelectDoneCallback = new TagRecordEditFragment.ExitCallback() {
        @Override
        public void onTagSelectDone(List<TagRecordEntity> tagRecordEntries) {
            if(mTagRecordEntries == null) {
                mTagRecordEntries = tagRecordEntries;
            }
            else {
                mTagRecordEntries.clear();
                mTagRecordEntries = tagRecordEntries;
            }
            if(!mIsNewNote){
                NoteHandler.saveTagRecord(EditNoteActivity.this,
                        mNoteEntity.getNoteId(), tagRecordEntries);
            }
        }
    };
}
