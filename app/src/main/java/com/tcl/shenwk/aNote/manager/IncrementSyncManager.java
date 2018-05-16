package com.tcl.shenwk.aNote.manager;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tcl.shenwk.aNote.data.ANoteDBManager;
import com.tcl.shenwk.aNote.data.CompareDBManager;
import com.tcl.shenwk.aNote.data.SyncDBManger;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.SyncItemEntity;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.network.CustomJsonRequest;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.network.UploadFileRequest;
import com.tcl.shenwk.aNote.task.DownloadTask;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IncrementSyncManager {
    private static final String TAG = "IncrementSyncManager";
    private String cookie;
    private Context context;
    private NetworkBase networkBase;
    private FinishListener finishListener;
    private long localUpdateCode;
    private long serverUpdateCode;
    private long serverLastUpdateTime;

    public IncrementSyncManager(Context context, String cookie, NetworkBase networkBase, FinishListener finishListener, long localUpdateCode, long serverUpdateCode, long serverLastUpdateTime) {
        this.cookie = cookie;
        this.context = context;
        this.networkBase = networkBase;
        this.finishListener = finishListener;
        this.localUpdateCode = localUpdateCode;
        this.serverUpdateCode = serverUpdateCode;
        this.serverLastUpdateTime = serverLastUpdateTime;
    }

    public void incrementSync() {
        // download server database
        try {
            new DownloadTask(
                    new URL(UrlSource.URL_SYNC_DOWNLOAD),
                    Constants.SERVER_DATABASE_NAME,
                    FileUtil.getUserDirPath(context) + File.separator + Constants.TEMP_DATABASE_NAME,
                    cookie,
                    new DownloadTask.OnFinishListener() {
                        @Override
                        public void onSuccess() {
                            Log.i(TAG, "incrementSync onSuccess: download server database success");
                            compareDifference();
                        }

                        @Override
                        public void onError(String err) {
                            Log.i(TAG, "incrementSync onError: download server database error");
                            if(finishListener != null){
                                finishListener.onError();
                            }
                        }
                    }
            ).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if(finishListener != null){
                finishListener.onError();
            }
        }
    }

    // incrementSync: check difference between local data and server data.
    private void compareDifference(){
        String serverDBPath = FileUtil.getUserDirPath(context) + File.separator + Constants.TEMP_DATABASE_NAME;
        CompareDBManager serverDB = new CompareDBManager(FileUtil.getUserDirPath(context) + File.separator + Constants.TEMP_DATABASE_NAME);
        CompareDBManager localDB;
        File localDBFile;
        if(FileUtil.isFileOrDirectoryExist(FileUtil.getUserDirPath(context) + File.separator + Constants.A_NOTE_DATA_DATABASE_NAME)){
            localDB = new CompareDBManager(FileUtil.getUserDirPath(context) + File.separator + Constants.A_NOTE_DATA_DATABASE_NAME);
            localDBFile = new File(FileUtil.getUserDirPath(context) + File.separator + Constants.A_NOTE_DATA_DATABASE_NAME);
        }else{
            localDB = new CompareDBManager(context.getDatabasePath(Constants.A_NOTE_DATA_DATABASE_NAME).getAbsolutePath());
            localDBFile = context.getDatabasePath(Constants.A_NOTE_DATA_DATABASE_NAME);
        }
        List<FileItem> uploadFiles = new ArrayList<>();
        int changeSum = 0;
        JSONArray deleteDirs = new JSONArray();
        JSONArray deleteFiles = new JSONArray();
        changeSum += checkNoteItems(localDB, serverDB, uploadFiles, deleteDirs);

        uploadFiles.add(new FileItem(localDBFile, Constants.SYNC_FILE_TYPE_DATABASE, ""));

        sendDeleteRequest(deleteDirs, deleteFiles);
        sendUploadRequest(uploadFiles);

        removeServerDB(serverDBPath);
        SyncDBManger.getInstance(context).cleanDeleteRecordTable();

        if(finishListener != null){
            finishListener.onSuccess((localUpdateCode > serverUpdateCode ? localUpdateCode : serverUpdateCode) + changeSum);
        }
    }

    // remove server database file after increment synchronization
    private void removeServerDB(String serverDBPath) {
        FileUtil.deleteFile(serverDBPath);
    }

    /**
     * check differences between server database and client database.
     * @param localDB       local database access manager.
     * @param serverDB      server database access manager.
     * @param uploadFiles   local upload files list.
     * @param deleteDirs    server delete directories list.
     * @return  differences sum between server database between client.
     */
    private int checkNoteItems(CompareDBManager localDB, CompareDBManager serverDB, List<FileItem> uploadFiles, JSONArray deleteDirs){
        int changeSum = 0;
        long syncTime = DateUtil.getInstance().getTime();
        List<SyncItemEntity> localNoteSyncItems = localDB.queryAllNoteSyncItem();
        List<SyncItemEntity> serverNoteSyncItems = serverDB.queryAllNoteSyncItem();
        String log = "";
        for(SyncItemEntity syncItemEntity : localNoteSyncItems){
            log += syncItemEntity.toString() + "  ";
        }
        Log.i(TAG, "checkNoteItems: local : " + log);
        log = "";
        for(SyncItemEntity syncItemEntity : serverNoteSyncItems){
            log += syncItemEntity.toString() + "  ";
        }
        Log.i(TAG, "checkNoteItems: server : " + log);
        List<SyncItemEntity>
                // situation 1
                serverOverlayLocal = new ArrayList<>(),
                // situation 2
                localOverlayServer = new ArrayList<>(),
                // situation 3, no data set should be operated
                // situation 4, contribute to S1 or S2
                // data set X, situation 5
                otherClientNewCreate = new ArrayList<>(),
                // data set Y, situation 6
                serverToDelete = new ArrayList<>(),
                // data set M, situation 7
                localNewCreate = new ArrayList<>(),
                // data set N, situation 8
                localToDelete = new ArrayList<>()
                        ;
        Iterator<SyncItemEntity> localIterator = localNoteSyncItems.iterator();
        Iterator<SyncItemEntity> serverIterator = serverNoteSyncItems.iterator();
        SyncItemEntity localItem = null;//localIterator.hasNext() ? localIterator.next() : null;
        SyncItemEntity serverItem = null;//serverIterator.hasNext() ? serverIterator.next() : null;
        while(localIterator.hasNext() || serverIterator.hasNext()){
            if(localIterator.hasNext() && !serverIterator.hasNext()){
                // local iterator has items, while server has not.
                // local iterator items belong to data set C
                while(localIterator.hasNext()){
                    localItem = localIterator.next();
                    if(localItem.getSyncRowId() == Constants.SYNC_ROW_ID_NO_ID){
                        // data set M, situation 7
                        localNewCreate.add(localItem);
                        Log.i(TAG, "checkNoteItems: serverIterator to end situation 7");
                    }else {
                        // data set N, situation 8
                        localToDelete.add(localItem);
                        Log.i(TAG, "checkNoteItems: serverIterator to end situation 8");
                    }
                    changeSum++;
                }
            }else if(!localIterator.hasNext() && serverIterator.hasNext()){
                // local iterator has no items, while server has.
                // server iterator items belong to data set B.
                while(serverIterator.hasNext()){
                    serverItem = serverIterator.next();
                    if(isLocalDelete(Constants.DELETE_ITEM_TYPE_NOTE, serverItem.getSyncRowId())){
                        // data set Y, situation 6
                        serverToDelete.add(serverItem);
                        Log.i(TAG, "checkNoteItems: localIterator to end situation 6");
                    }else{
                        // date set X, situation 5
                        otherClientNewCreate.add(serverItem);
                        Log.i(TAG, "checkNoteItems: localIterator to end situation 5");
                    }
                    changeSum++;
                }
            } else while(true){
                // both iterators have items
                if(serverItem == null) {
                    if (serverIterator.hasNext())
                        serverItem = serverIterator.next();
                    else break;
                }
                if(localItem == null){
                    if(localIterator.hasNext())
                        localItem = localIterator.next();
                    else break;
                }
                long localSyncRowId = localItem.getSyncRowId();
                long serverSyncRowId = serverItem.getSyncRowId();
                if(serverItem.getSyncRowId() == localItem.getSyncRowId()){
                    // data set A1 and data set A2
                    if (localItem.getModifyTime() < localItem.getLastUpdateTime()) {
                        // local item without modification
                        if (localItem.getLastUpdateTime() < serverItem.getLastUpdateTime()) {
                            // situation 1: server item is newer, overlay local item
                            Log.i(TAG, "checkNoteItems: situation 1");
                            serverOverlayLocal.add(serverItem);
                            changeSum++;
                        } else {
                            // situation 3:
                            // it is impossible for local lastUpdateTime to exceed server lastUpdateTime
                            // if they are equal, there is nothing to do.
                            Log.i(TAG, "checkNoteItems: situation 3");
                        }
                    } else {
                        // local item has been modified after last synchronization.
                        if (localItem.getLastUpdateTime() == serverItem.getLastUpdateTime()) {
                            // situation 2: local modification based on server version.
                            localOverlayServer.add(localItem);
                            changeSum++;
                            Log.i(TAG, "checkNoteItems: situation 2");
                        } else {
                            // situation 4:
                            // maybe conflict here
                            Log.i(TAG, "checkNoteItems: situation 4");
                            if(localItem.getModifyTime() > serverItem.getModifyTime()){
                                localOverlayServer.add(localItem);
                                Log.i(TAG, "checkNoteItems: conflict, local overlays server");
                            }else {
                                serverOverlayLocal.add(serverItem);
                                Log.i(TAG, "checkNoteItems: conflict, server overlays local");
                            }
                        }
                    }
                    // both items are consumed
                    localItem = null;
                    serverItem = null;
                } else if(localSyncRowId > serverSyncRowId){
                    // data set N, situation 8
                    localToDelete.add(localItem);
                    changeSum++;
                    Log.i(TAG, "checkNoteItems: situation 8");
                    // localItem is consumed
                    localItem = null;
                } else if(localSyncRowId == Constants.SYNC_ROW_ID_NO_ID){
                    // data set M, situation 7
                    localNewCreate.add(localItem);
                    changeSum++;
                    Log.i(TAG, "checkNoteItems: situation 7");
                    // localItem is consumed
                    localItem = null;
                } else{
                    // data set B
                    changeSum++;
                    if(isLocalDelete(Constants.DELETE_ITEM_TYPE_NOTE, serverItem.getSyncRowId())){
                        // data set Y, situation 6
                        serverToDelete.add(serverItem);
                        Log.i(TAG, "checkNoteItems: situation 6");
                    }else {
                        // data set X, situation 5
                        otherClientNewCreate.add(serverItem);
                        Log.i(TAG, "checkNoteItems: situation 5");
                    }
                    // serverItem is consumed
                    serverItem = null;
                }
            }
        }
//        while (true) {
//            if (localItem != null && localItem.getSyncRowId() == Constants.SYNC_ROW_ID_NO_ID) {
//                // localItem is new one, make server iterator reaches end
//                // data set C:
//                //      situation 5: client download from server.
//                //      situation 6: server delete.
//                if(serverItem != null) {
//                    serverToDelete.add(serverItem);
//                    changeSum++;
//                }
//                while (serverIterator.hasNext()) {
//                    serverItem = serverIterator.next();
//                    serverToDelete.add(serverItem);
//                    changeSum++;
//                }
//                // get local new created items.
//                localNewCreate.add(localItem);
//                changeSum++;
//                while(localIterator.hasNext()){
//                    localItem = localIterator.next();
//                    localNewCreate.add(localItem);
//                    changeSum++;
//                }
//                break;
//            } else if (serverItem != null && (localItem == null || localItem.getSyncRowId() < serverItem.getSyncRowId())) {
//                // server item does not match local, maybe has been deleted in local
//                serverToDelete.add(serverItem);
//                changeSum++;
//                if (serverIterator.hasNext()) {
//                    serverItem = serverIterator.next();
//                } else
//                    break;
//            } else if (localItem != null && (serverItem == null || localItem.getSyncRowId() > serverItem.getSyncRowId())) {
//                // local item does not match server, delete local item.
//                localToDelete.add(localItem);
//                changeSum++;
//                if (localIterator.hasNext()) {
//                    localItem = localIterator.next();
//                } else
//                    break;
//            } else if (localItem != null && serverItem != null && localItem.getSyncRowId() == serverItem.getSyncRowId()) {
//                // get the same syncRowId on both ends
//                // check which one is up to date.
//                if (localItem.getModifyTime() < localItem.getLastUpdateTime()) {
//                    // local item without modify
//                    if (localItem.getLastUpdateTime() < serverItem.getLastUpdateTime()) {
//                        // situation 1: server item is newer, overlay local item
//                        serverOverlayLocal.add(serverItem);
//                        changeSum++;
//                    } else {
//                        // situation 3:
//                        // it is impossible for local lastUpdateTime to exceed server lastUpdateTime
//                        // if they are equal, there is nothing to do.
//                    }
//                } else {
//                    // local item has been modified after last synchronization.
//                    if (localItem.getLastUpdateTime() == serverItem.getLastUpdateTime()) {
//                        // situation 2: local modification based on server version.
//                        localOverlayServer.add(localItem);
//                        changeSum++;
//                    } else {
//                        // situation 4:
//                        // maybe conflict here
//                    }
//                }
//                // get ready for next loop
//                if (localIterator.hasNext()) {
//                    localItem = localIterator.next();
//                } else
//                    break;
//                if (serverIterator.hasNext()) {
//                    serverItem = serverIterator.next();
//                } else
//                    break;
//            }
//        }
        localToDelete(localDB, localToDelete);
        serverOverlayLocal(localDB, serverDB, serverOverlayLocal, syncTime);
        serverToDelete(serverDB, serverToDelete, deleteDirs);
        otherClientNewCreate(serverDB, localDB, otherClientNewCreate);
        localOverlayServer(localDB, localOverlayServer, uploadFiles, syncTime);
        localNewSyncItem(localDB, serverDB.queryMaxSyncRowId(), localNewCreate, syncTime, uploadFiles);
        log = "";
        for(FileItem s : uploadFiles){
            log += s.relativePath + "/" + s.type + " || ";
        }
        Log.i(TAG, "checkNoteItems: uploadFiles " + log);
        localDB.close();
        serverDB.close();
        return changeSum;
    }

    // check whether a server item was deleted in local device.
    private boolean isLocalDelete(int deleteItemType, long syncRowId) {
        return SyncDBManger.getInstance(context).queryDeleteRecordId(deleteItemType, syncRowId) != Constants.SYNC_DELETE_ID_NO_ID;
    }

    // handle local note which need to be delete.
    private void localToDelete(CompareDBManager localDB, List<SyncItemEntity> localDelete){
        for(SyncItemEntity syncItemEntity : localDelete){
            NoteHandler.deleteNote(context, ANoteDBManager.getInstance(context).querySingleNoteRecordById(syncItemEntity.getItemId()));
        }
    }

    // handle newer note from server, overlay it on local database.
    private void serverOverlayLocal(CompareDBManager localDB, CompareDBManager serverDB, List<SyncItemEntity> serverOverlayLocal, long syncTime){
        for (SyncItemEntity syncItemEntity : serverOverlayLocal) {
            NoteEntity noteEntity = serverDB.querySingleWholeNoteById(syncItemEntity.getItemId());
            syncItemEntity.setLastUpdateTime(syncTime);
            if(noteEntity != null)
                FileUtil.deleteFile(FileUtil.getNoteContentPath(context, noteEntity.getNoteDirName()));
                localDB.updateWholeNoteById(noteEntity, syncItemEntity);
        }
    }

    // handle server note which need to be delete in server, along with a network request.
    private void serverToDelete(CompareDBManager serverDB, List<SyncItemEntity> serverDelete, JSONArray deleteDirs){
        for(SyncItemEntity syncItemEntity : serverDelete){
            NoteEntity noteEntity = serverDB.querySingleNoteContentPathById(syncItemEntity.getItemId());
            if(noteEntity != null)
                deleteDirs.put(noteEntity.getNoteDirName());
        }
    }

    // handle local new note to server, set the sync fields and add note file names to uploadFiles list.
    private void localNewSyncItem(CompareDBManager localDB, long maxSyncRowId, List<SyncItemEntity> localNewCreate, long syncTime, List<FileItem> uploadFiles){
        SyncItemEntity localItem;
        for(Iterator<SyncItemEntity> iterator = localNewCreate.iterator(); iterator.hasNext();){
            localItem = iterator.next();
            localItem.setSyncRowId(++maxSyncRowId);;
            localItem.setLastUpdateTime(syncTime);
            localDB.setSyncItem(localItem.getItemId(), localItem);
            NoteEntity noteEntity = ANoteDBManager.getInstance(context).querySingleNoteRecordById(localItem.getItemId());
            uploadFiles.add(
                    new FileItem(
                            new File(FileUtil.getUserDirPath(context) + File.separator + noteEntity.getNoteDirName() + File.separator + Constants.CONTENT_FILE_NAME),
                            Constants.SYNC_FILE_TYPE_CONTENT,
                            noteEntity.getNoteDirName()));
        }
    }

    // handle newer note in local, send local content file to server.
    private void localOverlayServer(CompareDBManager localDB, List<SyncItemEntity> localOverlayLocal, List<FileItem> uploadFiles, long syncTime){
        for(SyncItemEntity syncItemEntity : localOverlayLocal){
            NoteEntity noteEntity = ANoteDBManager.getInstance(context).querySingleNoteRecordById(syncItemEntity.getItemId());
            localDB.updateSyncItemLastUpdateTime(CompareDBManager.TABLE_CODE_NOTE, syncItemEntity.getSyncRowId(), syncTime);
            uploadFiles.add(
                    new FileItem(
                            new File(FileUtil.getUserDirPath(context) + File.separator + noteEntity.getNoteDirName() + File.separator + Constants.CONTENT_FILE_NAME),
                            Constants.SYNC_FILE_TYPE_CONTENT,
                            noteEntity.getNoteDirName()));
        }
    }

    // handle new note from other client which has been stored in server, store these items in local database.
    private void otherClientNewCreate(CompareDBManager serverDB, CompareDBManager localDB, List<SyncItemEntity> otherClientNewCreate){
        for(SyncItemEntity syncItemEntity : otherClientNewCreate) {
            NoteEntity noteEntity = serverDB.querySingleWholeNoteById(syncItemEntity.getItemId());
            localDB.insertSingleWholeNoteById(noteEntity, syncItemEntity);
        }
    }

    // send delete file request after comparision
    private void sendDeleteRequest(JSONArray deleteDirs, JSONArray deleteFiles){
        if(deleteFiles.length() == 0 && deleteDirs.length() == 0){
            Log.i(TAG, "sendDeleteRequest: no delete items");
            return;
        }
        JSONObject deleteRequestJSON = new JSONObject();
        try {
            Map<String, String> header = new HashMap<>();
            header.put(CustomJsonRequest.REQUEST_COOKIE, cookie);
            deleteRequestJSON.put(Constants.JSON_DELETE_DIRS, deleteDirs);
            deleteRequestJSON.put(Constants.JSON_DELETE_FILES, deleteFiles);
            CustomJsonRequest jsonObjectRequest = new CustomJsonRequest(
                    Request.Method.POST,
                    UrlSource.URL_SYNC_DELETE,
                    header,
                    deleteRequestJSON,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: send delete request successfully");
                            try {
                                int result = response.getInt(Constants.JSON_REQUEST_RESULT);
                                if(result == SyncManager.SYNC_RESULT_DELETE_SUCCESS){
                                    Log.i(TAG, "delete request onResponse: delete response successfully");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG, "delete request onErrorResponse: delete request error");
                        }
                    }
            );
            networkBase.addRequest(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendUploadRequest(List<FileItem> uploadFiles){
        if(uploadFiles.size() == 0){
            Log.i(TAG, "sendUploadRequest: no upload items");
            return;
        }
        Map<String, String> header = new HashMap<>();
        header.put(UploadFileRequest.REQUEST_COOKIE, cookie);
        for(FileItem fileItem : uploadFiles){
            UploadFileRequest uploadFileRequest = new UploadFileRequest(
                    UrlSource.URL_SYNC_UPLOAD,
                    header,
                    fileItem.type,
                    fileItem.file,
                    fileItem.relativePath,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: upload response");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG, "onErrorResponse: upload error");
                        }
                    }
            );
            networkBase.addRequest(uploadFileRequest);
        }
    }

    private class FileItem{
        File file;
        String type;
        String relativePath;

        FileItem(File file, String type, String relativePath) {
            this.file = file;
            this.type = type;
            this.relativePath = relativePath;
        }
    }

    private void sendUpdateCodeRequest(long updateCode, long updateTime){

    }

    public interface FinishListener{
        void onSuccess(long updateCode);
        void onError();
    }
}
