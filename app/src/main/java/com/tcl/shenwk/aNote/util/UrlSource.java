package com.tcl.shenwk.aNote.util;

public interface UrlSource {
    String URL_HOST_TEST = "10.0.2.2";
    String URL_TITLE = "http://" + URL_HOST_TEST + ":3000";
    String URL_SIGN_UP = URL_TITLE + "/sign_up";
    String URL_SIGN_IN = URL_TITLE + "/login";
    String URL_SYNC_BUILD_SESSION = URL_TITLE + "/sync/build";
    String URL_SYNC_UPLOAD = URL_TITLE + "/sync/upload";
    String URL_SYNC_UPDATE = URL_TITLE + "/sync/update";
    String URL_SYNC_FULL_DOWNLOAD = URL_TITLE + "/sync/full_download";
    String URL_SYNC_DOWNLOAD = URL_TITLE + "/sync/download";
}
