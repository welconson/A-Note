package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.util.Log;

import com.tcl.shenwk.aNote.data.ANoteDBManager;
import com.tcl.shenwk.aNote.data.DataProvider;
import com.tcl.shenwk.aNote.data.SyncDBManger;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.RandomUtil;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Note operation handler, include editing note and managing note.
 * Created by shenwk on 2018/2/2.
 */

public class NoteHandler {
    private static String TAG = "NoteHandler";
    /**
     * The method handlers all note data, if there is no error all data writing successfully,
     * or nothing will be modified.
     * @param noteEntity     note content entity holder, together with note record
     *                  information content and attachments inside note.
     * @param context   context used to operate on files.
     * @param editable  content of note.
     * @return  true if no error, or false and do nothing.
     */
    public static boolean saveNote(NoteEntity noteEntity, Context context, Editable editable,
                                   List<ViewSpan> viewSpans){
        long noteId = noteEntity.getNoteId();
        boolean ret = true;
        boolean isNewRecord = false;
        // if it is a new note, create a directory for it
        if(noteId == Constants.NO_NOTE_ID) {
            // new note record
            // if it is a new note, create a directory fot it.
            isNewRecord = true;
            String noteDirName = createNoteDirName(context);
            if(noteDirName == null){
                ret = false;
            }
            else noteEntity.setNoteDirName(noteDirName);
        }
        //if directory created error, ignore next step.
        if(ret) {
            if (FileUtil.writeFile(context, editable.toString(),
                    FileUtil.getNoteContentPath(context, noteEntity.getNoteDirName()))) {
                //if it is a new note, insert, or update it.
                if (isNewRecord) {
                    noteId = ANoteDBManager.getInstance(context).insertNoteRecord(noteEntity);
                    // if insert note record error, delete note directory
                    if (noteId == Constants.NO_NOTE_ID) {
                        Log.i(TAG, "saveNote: insert note record to database error");
                        FileUtil.deleteDirectoryAndFiles(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()));
                        ret = false;
                    }
                } else
                    ANoteDBManager.getInstance(context).updateNoteRecord(noteEntity, ANoteDBManager.UpdateFlagTable.UPDATE_ALL);

                // if note record does not exist, ignore next step
                if (ret) {
                    DataProvider.getInstance(context).updateNoteEntity();
                    noteEntity.setNoteId(noteId);
                    String resourceDir = createNoteResourceDirectory(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()));
                    if(resourceDir == null)
                        ret = false;
                    // if resource directory does not exist, ignore next step
                    if (ret){
                        List<String> resourcePathList = new ArrayList<>();
                        for (ViewSpan viewSpan : viewSpans) {
                            ResourceDataEntity resourceDataEntity = viewSpan.getResourceDataEntity();
                            int spanStart = editable.getSpanStart(viewSpan);
                            // if the ViewSpan still remain in TextView
                            if(spanStart == -1) {
                                // if the ViewSpan do not exist in TextView and saved before,
                                // delete the formal unused resource data here.
                                if(viewSpan.getFilePath() != null)
                                    deleteResourceData(context, resourceDataEntity);
                            }else {
                                resourceDataEntity.setSpanStart(spanStart);
                                // set resource entity with the valid note id
                                resourceDataEntity.setNoteId(noteId);
                                String resourcePath = saveResourceData(context, noteEntity.getNoteDirName(),
                                        resourceDataEntity, viewSpan.getResourceDataUri());
                                if (resourcePath != null) {
                                    resourcePathList.add(resourcePath);
                                }
                                // once there is a file saving occurs error,
                                // delete all new files created in this part.
                                else {
                                    for (String resourcePathToDelete : resourcePathList) {
                                        FileUtil.deleteFile(resourcePathToDelete);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // if write content file error, delete the note directory
            else {
                Log.i(TAG, "saveNote: write note content error");
                if(noteId != Constants.NO_NOTE_ID)
                    FileUtil.deleteDirectoryAndFiles(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()));
                ret = false;
            }
        }
        Log.i(TAG, "saveNote: save content file, id = " + noteId);
        return ret;
    }

    public static List<ResourceDataEntity> getResourceDataById(Context context, long noteId){
        return ANoteDBManager.getInstance(context).queryAllResourceDataByNoteId(noteId);
    }

    public static NoteEntity getSingleNote(Context context, long noteId){
        return ANoteDBManager.getInstance(context).querySingleNoteRecordById(noteId);
    }

    public static void removeNote(Context context, NoteEntity noteEntity){
        ANoteDBManager.getInstance(context).updateNoteRecord(noteEntity,
                ANoteDBManager.UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED);
        deleteNote(context, noteEntity);
    }

    public static void deleteNote(Context context, NoteEntity noteEntity){
        SyncDBManger.getInstance(context).checkDeleteRecord(context, noteEntity.getNoteId(), Constants.DELETE_ITEM_TYPE_NOTE);
        ANoteDBManager.getInstance(context).deleteNoteRecord(noteEntity.getNoteId());
        ANoteDBManager.getInstance(context).deleteTagRecordByNoteId(noteEntity.getNoteId());
        ANoteDBManager.getInstance(context).deleteResourceDataByNoteId(noteEntity.getNoteId());
        FileUtil.deleteDirectoryAndFiles(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()));
    }


    private static String createNoteDirName(Context context){
        String dirPath = context.getFilesDir().getAbsolutePath() + File.separator
                + LoginManager.userFolder;
        String directoryName;
        do{
            directoryName = RandomUtil.randomString(Constants.NOTE_DIRECTORY_LENGTH);
        }while (FileUtil.isFileOrDirectoryExist(dirPath + File.separator + directoryName));
        if(!FileUtil.createDir(dirPath + File.separator + directoryName)) {
            Log.i(TAG, "createNoteDirName: new note create directory error");
            return null;
        }
        return directoryName;
    }

    /**
     * if note resource directory exists, do nothing, or create a new directory.
     * @param notePath  the note path which the resource corresponding to.
     * @return  if exists or successfully created, return the path, or false.
     */
    private static String createNoteResourceDirectory(String notePath){
        String resourceDir = notePath + File.separator
                + Constants.RESOURCE_DIR;
        if(!FileUtil.isFileOrDirectoryExist(resourceDir )){
            if (!FileUtil.createDir(resourceDir)) {
                Log.i(TAG, "saveNote: create resource data directory failed");
                return null;
            }
        }
        return resourceDir;
    }

    /**
     * Save resource file, and render a random name for the resource file.
     * @param context       save uri file need context.
     * @param noteDirName   resource directory of the note.
     * @param resourceDataEntity     resource data entity to be saved.
     * @param resourceUri   if a new resource file, the uri will be a valid value.
     * @return  the resource file path if successfully, or null.
     */
    private static String saveResourceData(Context context, String noteDirName,
                                           ResourceDataEntity resourceDataEntity, Uri resourceUri){
        String resourceRelativePath = resourceDataEntity.getResourceRelativePath();
        // since ViewSpan's subclass have been restricted,
        // between filePath and there is and only one not null.
        if(resourceDataEntity.getResourceRelativePath() == null) {
            // resource data is new
            // first get a path for it
            resourceRelativePath = generateResourceDataPath(context, noteDirName);
            resourceDataEntity.setResourceRelativePath(resourceRelativePath);

            if (FileUtil.saveFileOfUri(context, resourceUri,
                    FileUtil.getResourcePath(context, resourceRelativePath))) {
                Log.i(TAG, "saveNote: save resource data successfully");
                if (ANoteDBManager.getInstance(context).insertResourceData(resourceDataEntity) == -1) {
                    FileUtil.deleteFile(FileUtil.getResourcePath(context, resourceRelativePath));
                    resourceRelativePath = null;
                    Log.i(TAG, "saveNote: insert resource data record failed");
                }
            } else {
                resourceRelativePath = null;
                Log.i(TAG, "saveNote: save resource data failed");
            }
        }
        // resource data record exist, so we just update inside text position information
        else {
            ANoteDBManager.getInstance(context).updateResourceData(resourceDataEntity);
        }
        return FileUtil.getResourcePath(context, resourceRelativePath);
    }

    private static String generateResourceDataPath(Context context, String noteDirName){
        String prefixDir = noteDirName + File.separator + Constants.RESOURCE_DIR;
        String resourceRelativePath;
        do {
            resourceRelativePath = prefixDir + File.separator + RandomUtil.randomString(Constants.RESOURCE_FILE_NAME_LENGTH);
        }while(FileUtil.isFileOrDirectoryExist(context.getFilesDir() + File.separator + resourceRelativePath));
        return resourceRelativePath;
    }

    private static void deleteResourceData(Context context, ResourceDataEntity resourceDataEntity){
        if(resourceDataEntity != null && resourceDataEntity.getResourceRelativePath() != null){
            ANoteDBManager.getInstance(context).deleteResourceData(resourceDataEntity.getResourceId());
            if (FileUtil.deleteFile(FileUtil.getResourcePath(context, resourceDataEntity.getResourceRelativePath()))) {
                Log.i(TAG, "deleteResourceData delete data file: successfully");
            }
            else Log.i(TAG, "deleteResourceData delete data file: failed");
        }
    }

    public static void saveTagRecord(Context context, long noteId, List<TagRecordEntity>  tagRecordEntries){
        if(tagRecordEntries == null)
            return;
        Iterator<TagRecordEntity> tagRecordEntityIterator= tagRecordEntries.iterator();
        while(tagRecordEntityIterator.hasNext()){
            TagRecordEntity tagRecordEntity = tagRecordEntityIterator.next();
            switch (tagRecordEntity.status){
                case TagRecordEntity.NEW_CREATE:
                    tagRecordEntity.setNoteId(noteId);
                    long ret = ANoteDBManager.getInstance(context).insertTagRecord(tagRecordEntity);
                    if(ret != Constants.NO_TAG_RECORD_ID) {
                        tagRecordEntity.setTagRecordId(ret);
                        tagRecordEntity.status = TagRecordEntity.NORMAL;
                    }
                    break;
                case TagRecordEntity.TO_DELETE:
                    ANoteDBManager.getInstance(context).deleteTagRecord(tagRecordEntity.getTagRecordId());
                    tagRecordEntityIterator.remove();
                    break;
            }
            tagRecordEntity.setNoteId(noteId);

        }
    }

    public static void setNoteHasArchived(Context context, NoteEntity noteEntity){
        if(noteEntity == null)
            return;
        ANoteDBManager.getInstance(context).updateNoteRecord(noteEntity, ANoteDBManager.UpdateFlagTable.UPDATE_HAS_ARCHIVED);
    }

    public static void setNoteIsLabelDiscard(Context context, NoteEntity noteEntity){
        if(noteEntity == null)
            return;
        ANoteDBManager.getInstance(context).updateNoteRecord(noteEntity, ANoteDBManager.UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED);
    }
}
