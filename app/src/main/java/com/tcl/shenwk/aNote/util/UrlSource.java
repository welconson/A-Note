package com.tcl.shenwk.aNote.util;

public interface UrlSource {
    String URL_TITLE = "http://10.0.2.2:3000";
    String URL_SIGN_UP = URL_TITLE + "/sign_up";
    String URL_SIGN_IN = URL_TITLE + "/login";
    String URL_SYNC_BUILD_SESSION = URL_TITLE + "/sync/build";
    String URL_SYNC_UPLOAD = URL_TITLE + "/sync/upload";
    String URL_SYNC_CANCEL_SESSION = URL_TITLE + "/sync/cancel";
    String URL_SYNC_FULL_DOWNLOAD = URL_TITLE + "/sync/full_download";
    String URL_SYNC_DOWNLOAD = URL_TITLE + "/sync/download";
}
