package jr.project.cloudbox.utils;

import java.security.PublicKey;

public class Constants {

    // selection request code
    public static final int SELECT_ALL_FILES = 221;
    public static final int SELECT_PDF       = 222;
    public static final int SELECT_IMAGE     = 223;
    public static final int SELECT_VIDEO     = 224;
    public static final int SELECT_AUDIO     = 225;
    public static final int SELECT_DOCUMENTS = 226;


    // actions
    public static final String ACTION_UPLOAD    = "ACTION_UPLOAD";
    public static final String ACTION_OPEN      = "ACTION_OPEN";
    public static final String ACTION_DELETE    = "ACTION_DELETE";
    public static final String ACTION_FAVOURITE = "ACTION_FAVOURITE";
    public static final String ACTION_DOWNLOAD  = "ACTION_DOWNLOAD";

    public static final String TYPE_UPLOAD   = "TYPE_UPLOAD";
    public static final String TYPE_DOWNLOAD = "TYPE_DOWNLOAD";

    // table name
    public static final String FILE_DETAILS_TABLE      = "FILE_DETAILS";
    public static final String FILE_PROCESS_TABLE      = "FILE_PROCESS";
    public static final String RECENT_ACTIVITY_TABLE   = "RECENT_ACTIVITY";
    public static final String FILE_TRASH_TABLE        = "FILE_TRASH";
    public static final String FILE_OFFLINE_TABLE      = "FILE_OFFLINE";

    // transfer state
    public static final String TRANSFER_COMPLETED = "TRANSFER_COMPLETED";
    public static final String TRANSFER_PAUSED    = "TRANSFER_PAUSED";
    public static final String TRANSFER_CANCELLED = "TRANSFER_CANCELLED";
    public static final String TRANSFER_FAILED    = "TRANSFER_FAILED";
    public static final String TRANSFER_ONGOING   = "TRANSFER_ONGOING";

    // notification
    public static String NOTIFICATION_UPLOAD_CHANNEL = "26";

    // task button
    public static final int BTN_PLAY_PAUSE = 0;
    public static final int BTN_CANCEL     = 1;

    // passcode
    public static int PASSCODE_CHANGE = 0;
    public static int PASSCODE_RETYPE = 1;
    public static int PASSCODE_UNLOCK = 2;
    public static int PASSCODE_SETUP  = 3;

    //b sheet
    public static int BOTTOM_SHEET_PROCESS = 1;
    public static int BOTTOM_SHEET_AUDIO   = 2;

    //network
    public static int NETWORK_MOBILE_DATA = 1;
    public static int NETWORK_WIFI        = 2;
    public static int NETWORK_NULL        = 3;

}
