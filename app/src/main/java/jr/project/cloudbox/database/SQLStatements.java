package jr.project.cloudbox.database;

public class SQLStatements {

    /* TABLE CREATION STATEMENTS */

    public static String create_file_details_table =
            "CREATE TABLE FILE_DETAILS(" +
            "ID BLOB PRIMARY KEY," +
            "FILE_NAME VARCHAR," +
            "FILE_SIZE BLOB," +
            "FILE_TYPE VARCHAR," +
            "FILE_MIME_TYPE VARCHAR," +
            "FILE_URI VARCHAR," +
            "FILE_SESSION_URI VARCHAR," +
            "FILE_UPLOADED_URL VARCHAR," +
            "FILE_UPLOAD_TIME BLOB," +
            "FILE_FAVOURITE INTEGER," +
            "FILE_STATE VARCHAR" +
            " );";

    public static String create_file_process_table =
            "CREATE TABLE FILE_PROCESS(" +
                    "ID BLOB ," +
                    "STATE VARCHAR," +
                    "FINISHED_TIME BLOB," +
                    "PROCESS INT,"+
                    "UPLOADED_SIZE BLOB,"+
                    "TIME_LEFT VARCHAR,"+
                    "TYPE VARCHAR,"+
                    "S_ID BLOB PRIMARY KEY" +
                    " );";

    public static String create_recent_activity_table =
            "CREATE TABLE RECENT_ACTIVITY(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "FILE_ID BLOB," +
                    "ACTION VARCHAR," +
                    "TIME BLOB" +
                    " );";

    public static String create_file_trash_table =
            "CREATE TABLE FILE_TRASH(" +
                    "ID BLOB PRIMARY KEY," +
                    "FILE_NAME VARCHAR," +
                    "FILE_SIZE BLOB," +
                    "FILE_TYPE VARCHAR," +
                    "FILE_MIME_TYPE VARCHAR," +
                    "FILE_URI VARCHAR," +
                    "FILE_SESSION_URI VARCHAR," +
                    "FILE_UPLOADED_URL VARCHAR," +
                    "FILE_UPLOAD_TIME BLOB," +
                    "FILE_FAVOURITE INTEGER," +
                    "FILE_STATE VARCHAR" +
                    " );";

    public static String create_file_offline_table =
            "CREATE TABLE FILE_OFFLINE(" +
                    "ID BLOB PRIMARY KEY," +
                    "FILE_NAME VARCHAR," +
                    "FILE_SIZE BLOB," +
                    "FILE_TYPE VARCHAR," +
                    "FILE_MIME_TYPE VARCHAR," +
                    "FILE_URI VARCHAR," +
                    "FILE_SESSION_URI VARCHAR," +
                    "FILE_UPLOADED_URL VARCHAR," +
                    "FILE_UPLOAD_TIME BLOB," +
                    "FILE_FAVOURITE INTEGER," +
                    "FILE_STATE VARCHAR" +
                    " );";


}
