package jr.project.cloudbox.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.TimeUtils;

public class CloudBoxOfflineDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "cloud_box_db";
    private static final int DATABASE_VERSION = 1;
    private static final String logName       = "DatabaseHelperClass";

    public CloudBoxOfflineDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public CloudBoxOfflineDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public CloudBoxOfflineDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(28)
    public CloudBoxOfflineDatabase(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
            super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //table creation
        db.execSQL(SQLStatements.create_file_details_table);
        db.execSQL(SQLStatements.create_file_process_table);
        db.execSQL(SQLStatements.create_recent_activity_table);
        db.execSQL(SQLStatements.create_file_trash_table);
        db.execSQL(SQLStatements.create_file_offline_table);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //development purpose
    public void create__if_not_created(){
        SQLiteDatabase db = this.getWritableDatabase();
        try {

            db.execSQL("DROP TABLE FILE_DETAILS");
            db.execSQL("DROP TABLE FILE_PROCESS");
            db.execSQL("DROP TABLE RECENT_ACTIVITY");


            db.execSQL(SQLStatements.create_file_details_table);
            db.execSQL(SQLStatements.create_file_process_table);
            db.execSQL(SQLStatements.create_recent_activity_table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void __crAndDr(){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DROP TABLE FILE_PROCESS");
            db.execSQL(SQLStatements.create_file_process_table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertNewUpload(){
//        SQLiteDatabase database=this.getWritableDatabase();
//        ContentValues cv=new ContentValues();
//        cv.put("UID",uid);
//        cv.put("PHONE",phone);
//        cv.put("NAME",name);
//        cv.put("STATUS",status);
//        cv.put("PROFILE_IMAGE",profileImage);
//        cv.put("EMAIL",email);
//        cv.put("TOKEN",token);
//        float r=database.insert("CONTACT_USERS",null,cv);
//        if (r==-1){
//            return "failed";
//        }else {
//            return "success";
//        }
    }

    public void insertFileDetails(long id,
                                    String file_name,
                                    long file_size,
                                    String file_type,
                                    String mime_type,
                                    String file_uri,
                                    long upload_time){

        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("ID",id);
        cv.put("FILE_NAME",file_name);
        cv.put("FILE_SIZE",file_size);
        cv.put("FILE_TYPE",file_type);
        cv.put("FILE_MIME_TYPE",mime_type);
        cv.put("FILE_URI",file_uri);
        cv.put("FILE_UPLOAD_TIME",upload_time);

        float r=database.insert(Constants.FILE_DETAILS_TABLE,null,cv);
        if (r==-1){
            Log.e(logName,"failed to INSERT in file details");
        }else {
            Log.e(logName,"INSERTED in file details");
        }
    }

    public void updateSessionUrlInFileDetails(long id, String url){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_SESSION_URI",url);
        float r=database.update(Constants.FILE_DETAILS_TABLE,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update session url in file details");
        }else {
            Log.e(logName,"updated session url in file details");
        }
    }

    public void updateUploadedUrlInFileDetails(long id, String url,byte[] image){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_UPLOADED_URL",url);
        cv.put("FILE_THUMBNAIL",image);
        float r=database.update(Constants.FILE_DETAILS_TABLE,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update url in file details");
        }else {
            Log.e(logName,"updated session url in file details");
        }
    }
    public void updateUploadedUrlInFileDetails(long id, String url){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_UPLOADED_URL",url);
        float r=database.update(Constants.FILE_DETAILS_TABLE,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update url in file details");
        }else {
            Log.e(logName,"updated session url in file details");
        }
    }

    public void insertRecentActivity(long id,String action){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_ID",id);
        cv.put("ACTION",action);
        cv.put("TIME", Calendar.getInstance().getTimeInMillis());
        float r=database.insert(Constants.RECENT_ACTIVITY_TABLE,null,cv);
        if (r==-1){
            Log.e(logName,"failed to INSERT in recent activity");
        }else {
            Log.e(logName,"INSERTED in recent activity");
        }
    }

    public ArrayList<FileModel> retrieveRecentActivity(){
        ArrayList<FileModel> fModel = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM RECENT_ACTIVITY ORDER BY ID DESC";
        Cursor cursor=database.rawQuery(qry,null);
        while (cursor.moveToNext()){
            FileModel f = getSuccessFileDetails(Long.parseLong(cursor.getString(1)));
            if (f!=null && f.getFileName()!=null) {
                f.setAction(cursor.getString(2));
                f.setTimeStamp(Long.parseLong(cursor.getString(3)));
                fModel.add(f);
            }
        }
        cursor.close();
        return fModel;
    }

    public FileModel getSuccessFileDetails(long fileId){
        FileModel f = new FileModel();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE ID="+fileId+" AND FILE_STATE= '"+Constants.TRANSFER_COMPLETED+"' LIMIT 1";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                f.setFileId(fileId);
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setMimeType(cr.getString(4));
                f.setFileType(cr.getString(3));
                f.setUrl(cr.getString(7));
                if (cr.getString(9 )!= null){
                    int fav = Integer.parseInt(cr.getString(9));
                    f.setFavourite(fav != 0);
                }

            }
        }
        cr.close();
        return f;
    }

    public FileModel getFileDetails(long fileId){
        FileModel f = new FileModel();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE ID="+fileId+" LIMIT 1";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));

            }
        }
        cr.close();
        return f;
    }

    public void insertProgressTable(long id,long sId,String state,String transType){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("ID",id);
        cv.put("S_ID",sId);
        cv.put("STATE",state);
        cv.put("TYPE",transType);
        float r=database.insert(Constants.FILE_PROCESS_TABLE,null,cv);
        if (r==-1){
            Log.e(logName,"failed to insert in process table");
        }else {
            Log.e(logName,"inserted in process table");
        }
    }

    public void updateState(long id, String state,long finishedTime,String type){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("STATE",state);
        cv.put("FINISHED_TIME",finishedTime);
        float r=database.update(Constants.FILE_PROCESS_TABLE,cv,"S_ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update url in file details");
        }else {
            Log.e(logName,"updated url in file details");
        }
        if (type.equals(Constants.TYPE_UPLOAD)) {
            ContentValues cv1 = new ContentValues();
            cv1.put("FILE_STATE", state);
            float r1 = database.update(Constants.FILE_DETAILS_TABLE, cv1, "ID=" + id, null);
            if (r1 == -1) {
                Log.e(logName, "failed to update STATE in file details");
            } else {
                Log.e(logName, "updated STATE in file details");
            }
        }
    }

    public void updateFileProgress(long id,int progress,double uploadedSize,String timeLeft){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("PROCESS",progress);
        cv.put("UPLOADED_SIZE",uploadedSize);
        cv.put("TIME_LEFT",timeLeft);
        float r=database.update(Constants.FILE_PROCESS_TABLE,cv,"S_ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update progress");
        }else {
            Log.e(logName,"updated progress");
        }
    }

    public ArrayList<FileModel> getCurrentTransferDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_PROCESS WHERE STATE='"+Constants.TRANSFER_ONGOING+"'";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = getFileDetails(Long.parseLong(cr.getString(0)));
                if (f!=null && f.getFileName()!=null) {
                    if (cr.getString(3)!=null) {
                        f.setProgress(Integer.parseInt(cr.getString(3)));
                    }
                    if (cr.getString(4)!=null) {
                        f.setUploadedSize(Double.parseDouble(cr.getString(4)));
                    }
                    if (cr.getString(6)!=null &&
                    cr.getString(6).equals(Constants.TYPE_UPLOAD)){
                        f.setUpload(true);
                    }
                    f.setTimeLeft(cr.getString(5));
                    f.setsId(cr.getString(7));
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public FileModel getCurrentFileProgress(long fileId){
        FileModel f = new FileModel();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_PROCESS WHERE STATE='"+Constants.TRANSFER_ONGOING+"' AND ID="+fileId+" LIMIT 1";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                if (cr.getString(3)!=null) {
                    f.setProgress(Integer.parseInt(cr.getString(3)));
                }
                if (cr.getString(4)!=null) {
                    f.setUploadedSize(Double.parseDouble(cr.getString(4)));
                }
                    f.setTimeLeft(cr.getString(5));
                if (cr.getString(6)!=null &&
                        cr.getString(6).equals(Constants.TYPE_UPLOAD)){
                    f.setUpload(true);
                }
                }
            }
        cr.close();
        return f;
    }

    public ArrayList<FileModel> getAllTransferDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_PROCESS WHERE STATE IS NOT'"+Constants.TRANSFER_ONGOING+"' " +
                "ORDER BY FINISHED_TIME DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = getFileDetails(Long.parseLong(cr.getString(0)));
                f.setState(cr.getString(1));
                f.setTimeStamp(Long.parseLong(cr.getString(2)));
                if (cr.getString(6)!=null &&
                        cr.getString(6).equals(Constants.TYPE_UPLOAD)){
                    f.setUpload(true);
                }
                f.setsId(cr.getString(7));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public void clearTransfersToFailure(){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("STATE",Constants.TRANSFER_FAILED);
        cv.put("FINISHED_TIME", TimeUtils.getTimestamp());
        float r=database.update(Constants.FILE_PROCESS_TABLE,cv,
                "STATE='"+Constants.TRANSFER_ONGOING+"'",null);
        float s=database.update(Constants.FILE_PROCESS_TABLE,cv,
                "STATE='"+Constants.TRANSFER_PAUSED+"'",null);
        if (r==-1 && s==-1){
            Log.e(logName,"failed to update failed details");
        }else {
            Log.e(logName,"updated failed details");
        }
    }

    public ArrayList<FileModel> getAllImageDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE FILE_TYPE = 'image' AND FILE_UPLOADED_URL IS NOT NULL ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public ArrayList<FileModel> getAllVideoDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE FILE_TYPE = 'video' AND FILE_UPLOADED_URL IS NOT NULL ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public ArrayList<FileModel> getAllAudioDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE FILE_TYPE = 'audio' AND FILE_UPLOADED_URL IS NOT NULL ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public ArrayList<FileModel> getAllApplicationDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE FILE_TYPE = 'application' AND FILE_UPLOADED_URL IS NOT NULL ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public ArrayList<FileModel> getAllFilesDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE FILE_UPLOADED_URL IS NOT NULL ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public ArrayList<FileModel> getAllFavouritesDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_DETAILS WHERE FILE_UPLOADED_URL IS NOT NULL AND FILE_FAVOURITE = 1 ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }


    public void insertFileTrash(FileModel model){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("ID",model.getFileId());
        cv.put("FILE_NAME",model.getFileName());
        cv.put("FILE_SIZE",model.getFileSize());
        cv.put("FILE_TYPE",model.getFileType());
        cv.put("FILE_MIME_TYPE",model.getMimeType());
        cv.put("FILE_URI",model.getUriVal());
        cv.put("FILE_SESSION_URI",model.getSessionUri());
        cv.put("FILE_UPLOADED_URL",model.getUrl());
        cv.put("FILE_UPLOAD_TIME",model.getTimeStamp());
        if (model.isFavourite()) {
            cv.put("FILE_FAVOURITE", 1);
        }else {
            cv.put("FILE_FAVOURITE", 0);
        }
        cv.put("FILE_STATE",model.getState());

        float r=database.insert(Constants.FILE_TRASH_TABLE,null,cv);
        if (r==-1){
            Log.e(logName,"failed to INSERT in trash");
        }else {
            Log.e(logName,"INSERTED in file trash");
            if (deleteRowFromId(Constants.FILE_DETAILS_TABLE,model.getFileId())){
                deleteRowFromRecentActivity(model.getFileId());
                Log.e(logName,"deleted from file details table");
            }
        }
    }

    public void insertFileDetails(FileModel model){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("ID",model.getFileId());
        cv.put("FILE_NAME",model.getFileName());
        cv.put("FILE_SIZE",model.getFileSize());
        cv.put("FILE_TYPE",model.getFileType());
        cv.put("FILE_MIME_TYPE",model.getMimeType());
        cv.put("FILE_URI",model.getUriVal());
        cv.put("FILE_SESSION_URI",model.getSessionUri());
        cv.put("FILE_UPLOADED_URL",model.getUrl());
        cv.put("FILE_UPLOAD_TIME",model.getTimeStamp());
        if (model.isFavourite()) {
            cv.put("FILE_FAVOURITE", 1);
        }else {
            cv.put("FILE_FAVOURITE", 0);
        }
        cv.put("FILE_STATE",model.getState());

        float r=database.insert(Constants.FILE_DETAILS_TABLE,null,cv);
        if (r==-1){
            Log.e(logName,"failed to INSERT in trash");
        }else {
            Log.e(logName,"INSERTED in file trash");
            if (deleteRowFromId(Constants.FILE_TRASH_TABLE,model.getFileId())){
                Log.e(logName,"deleted from file details table");
            }
        }
    }

    public ArrayList<FileModel> getAllTrashDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_TRASH ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public void insertOfflineFileDetails(FileModel model,String uri){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("ID",model.getFileId());
        cv.put("FILE_NAME",model.getFileName());
        cv.put("FILE_SIZE",model.getFileSize());
        cv.put("FILE_TYPE",model.getFileType());
        cv.put("FILE_MIME_TYPE",model.getMimeType());
        cv.put("FILE_URI",uri);
        cv.put("FILE_SESSION_URI",model.getSessionUri());
        cv.put("FILE_UPLOADED_URL",model.getUrl());
        cv.put("FILE_UPLOAD_TIME",model.getTimeStamp());
        if (model.isFavourite()) {
            cv.put("FILE_FAVOURITE", 1);
        }else {
            cv.put("FILE_FAVOURITE", 0);
        }
        cv.put("FILE_STATE",model.getState());

        float r=database.insert(Constants.FILE_OFFLINE_TABLE,null,cv);
        if (r==-1){
            Log.e(logName,"failed to INSERT in trash");
        }else {
            Log.e(logName,"INSERTED in file trash");
        }
    }

    public ArrayList<FileModel> getAllOfflineDetails(){
        ArrayList<FileModel> model = new ArrayList<>();
        SQLiteDatabase database=this.getWritableDatabase();
        String qry="SELECT * FROM FILE_OFFLINE ORDER BY ID DESC";
        Cursor cr = database.rawQuery(qry,null);
        if (cr.getCount()>0){
            while (cr.moveToNext()) {
                FileModel f = new FileModel();
                f.setFileId(Long.parseLong(cr.getString(0)));
                f.setFileName(cr.getString(1));
                f.setFileSize(cr.getString(2));
                f.setFileType(cr.getString(3));
                f.setMimeType(cr.getString(4));
                f.setUriVal(cr.getString(5));
                f.setSessionUri(cr.getString(6));
                f.setUrl(cr.getString(7));
                f.setTimeStamp(Long.parseLong(cr.getString(8)));
                f.setFavourite(cr.getString(9) != null &&
                        cr.getString(9).equals("1"));
                f.setState(cr.getString(10));
                if (f.getFileName() != null) {
                    model.add(f);
                }
            }
        }
        cr.close();
        return model;
    }

    public boolean deleteRowFromId(String table_name, long id){
        SQLiteDatabase database=this.getWritableDatabase();
        return database.delete(table_name,"ID"+ "="+id,null) > 0;
    }

    public boolean deleteRowFromSId(String table_name, long id){
        SQLiteDatabase database=this.getWritableDatabase();
        return database.delete(table_name,"S_ID"+ "="+id,null) > 0;
    }

    public void deleteRowFromRecentActivity(long id){
        SQLiteDatabase database=this.getWritableDatabase();
        database.delete(Constants.RECENT_ACTIVITY_TABLE,"FILE_ID"+ "="+id,null);
    }

    public void renameFile(long id, String name,String table_name){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_NAME",name);
        float r=database.update(table_name,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update");
        }else {
            Log.e(logName,"updated");
        }
    }
    public void renameFile(long id, String name){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_NAME",name);
        float r=database.update(Constants.FILE_DETAILS_TABLE,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update");
        }else {
            Log.e(logName,"updated");
        }
    }

    public void addToFavourites(long id){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_FAVOURITE",1);
        float r=database.update(Constants.FILE_DETAILS_TABLE,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update");
        }else {
            Log.e(logName,"updated");
        }
    }

    public void removeFavourites(long id){
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("FILE_FAVOURITE",0);
        float r=database.update(Constants.FILE_DETAILS_TABLE,cv,"ID="+id,null);
        if (r==-1){
            Log.e(logName,"failed to update");
        }else {
            Log.e(logName,"updated");
        }
    }

}
