package jr.project.cloudbox.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Calendar;

import jr.project.cloudbox.CloudBoxApplication;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.DeviceNotificationUtility;
import jr.project.cloudbox.utils.Extras;
import jr.project.cloudbox.utils.TimeUtils;

public class DownloadService extends Service {

    SharedPreferences         mPref;
    CloudBoxOfflineDatabase   mDatabase;
    DeviceNotificationUtility mUtil;
    String  uid;
    Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        context   = CloudBoxApplication.getAppContext();
        uid       = FirebaseAuth.getInstance().getUid();
        mPref     = new SharedPreferences(context);
        mDatabase = new CloudBoxOfflineDatabase(context);
        mUtil     = new DeviceNotificationUtility(context);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean isDownloadDir = Boolean.parseBoolean(intent.getExtras().getString("downloadDir"));
        FileModel fm = (FileModel) intent.getSerializableExtra("model");

        if (Extras.isWifi(context) == Constants.NETWORK_MOBILE_DATA){
            if (mPref.getDownloadPreferenceOnMobileData()){
                if (fm.getUrl()!=null) {
                    startDownload(fm,isDownloadDir);
                }
            }else {
                Toast.makeText(context, "You have disabled downloads on Mobile Data!", Toast.LENGTH_SHORT).show();
            }
        }else if (Extras.isWifi(context) == Constants.NETWORK_WIFI){
            if (mPref.getDownloadPreferenceOnWifi()){
                if (fm.getUrl()!=null) {
                    startDownload(fm,isDownloadDir);
                }
            }else {
                Toast.makeText(context, "You have disabled downloads on Wifi!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(context, "Please check your network and try again!", Toast.LENGTH_SHORT).show();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new CloudBoxOfflineDatabase(this).clearTransfersToFailure();
    }

    boolean isInsertedProgressTable = false;
    public void startDownload(FileModel fm,boolean isDownloadsDirectory){
        // user not logged error
        if (uid == null){
            Toast.makeText(CloudBoxApplication.getAppContext(), "Please login again!", Toast.LENGTH_SHORT).show();
            return;
        }
        // uri empty
        if (fm == null){
            Toast.makeText(CloudBoxApplication.getAppContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }


        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fm.getUrl());

        long fileId = fm.getFileId();
        long tMills = Calendar.getInstance().getTimeInMillis();
        long startTime = System.currentTimeMillis();

        File f = new File(getFilesDir().getAbsolutePath()+"/download");
        if (!f.exists()){
            f.mkdirs();
        }
        String fName = storageRef.getName();
        String fileName = fName.substring(fName.indexOf("-") + 1);
        File file;
        if (isDownloadsDirectory){
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fm.getUrl()));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fm.getFileName());
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadManager.enqueue(request);
            Toast.makeText(context, "Look notification for download details.", Toast.LENGTH_SHORT).show();
            return;
        }else {
            file = new File(getFilesDir().getAbsolutePath() + "/download/" +
                    fileName);
        }
        FileDownloadTask fileTask = storageRef.getFile(file);
        CloudBoxApplication mApp = (CloudBoxApplication) getApplication();
        mApp.setDownloadTask(fileId,fileTask);

        mUtil.showDownloadNotification(fileName, (int) fileId);

        fileTask.addOnProgressListener(taskSnapshot -> {
            double transferred = taskSnapshot.getBytesTransferred();
            double total_byte  = taskSnapshot.getTotalByteCount();
            double progress    =  (100.0 * transferred) / total_byte;
            Log.d("DownloadTask", "Download is " + progress + "% done");

            if (!isInsertedProgressTable) {
                mDatabase.insertProgressTable(
                        fileId,tMills, Constants.TRANSFER_ONGOING,Constants.TYPE_DOWNLOAD
                );
                isInsertedProgressTable = true;
            }

            mUtil.updateProgress((int) progress,(int) fileId);
            long elapsedTime = System.currentTimeMillis() - startTime;
            double averageSpeed = (double) transferred / (elapsedTime / 1000.0);
            double estimatedTimeRemaining = (total_byte - transferred) / averageSpeed;

            String timeLeft = Extras.timeInUnit(estimatedTimeRemaining);
            mDatabase.updateFileProgress(tMills,
                    (int) progress,
                    taskSnapshot.getBytesTransferred(),
                    timeLeft);
            mPref.setUploadedSize(tMills,transferred);

        });

        fileTask.addOnSuccessListener(taskSnapshot -> {
            // add in recent activity
            mDatabase.updateState(
                    tMills,
                    Constants.TRANSFER_COMPLETED,
                    TimeUtils.getTimestamp(),
                    Constants.TYPE_DOWNLOAD
            );
            mApp.removeDownloadTask(fileId);
            if (!isDownloadsDirectory){
                mDatabase.insertOfflineFileDetails(fm,file.getAbsolutePath());
            }
            mUtil.dismiss((int) fileId);
            mUtil.showDownloadSuccessNotification(fileName);
            isInsertedProgressTable = false;
        });

        fileTask.addOnFailureListener(e -> {
            Toast.makeText(CloudBoxApplication.getAppContext(), "failed -> "+e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e("DownloadTask",""+e);
            mDatabase.updateState(
                    tMills,
                    Constants.TRANSFER_FAILED,
                    TimeUtils.getTimestamp(),
                    Constants.TYPE_DOWNLOAD
            );
            isInsertedProgressTable = false;
            mApp.removeDownloadTask(fileId);
            mUtil.dismiss((int) fileId);
            mUtil.showDownloadFailureNotification(fileName);
        });


    }
}
