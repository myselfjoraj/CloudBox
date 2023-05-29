package jr.project.cloudbox.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Locale;

import jr.project.cloudbox.CloudBoxApplication;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.DeviceNotificationUtility;
import jr.project.cloudbox.utils.Extras;
import jr.project.cloudbox.utils.FileUtils;
import jr.project.cloudbox.utils.TimeUtils;

public class UploadService extends Service {

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

        String uri = intent.getExtras().getString("uri");

        double gb5 = 5000000000.00;
        if (mPref.getUsedStorage()<=gb5){
            if (Extras.isWifi(context) == Constants.NETWORK_MOBILE_DATA){
                if (mPref.getUploadPreferenceOnMobileData()){
                    if (uri!=null) {
                        startUpload(Uri.parse(uri));
                    }
                }else {
                    Toast.makeText(context, "You have disabled uploads on Mobile Data!", Toast.LENGTH_SHORT).show();
                }
            }else if (Extras.isWifi(context) == Constants.NETWORK_WIFI){
                if (mPref.getUploadPreferenceOnWifi()){
                    if (uri!=null) {
                        startUpload(Uri.parse(uri));
                    }
                }else {
                    Toast.makeText(context, "You have disabled uploads on Wifi!", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(context, "Please check your network and try again!", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(context, "Your 5GB quota is full!", Toast.LENGTH_SHORT).show();
        }


        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new CloudBoxOfflineDatabase(this).clearTransfersToFailure();
//        Toast.makeText(this, "cloud box upload service killed", Toast.LENGTH_SHORT).show();
    }


    boolean isInsertedProgressTable = false;
    public void startUpload(Uri uri){
        // user not logged error
        if (uid == null){
            Toast.makeText(CloudBoxApplication.getAppContext(), "Please login again!", Toast.LENGTH_SHORT).show();
            return;
        }
        // uri empty
        if (uri == null){
            Toast.makeText(CloudBoxApplication.getAppContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        long startTime = System.currentTimeMillis();

        FileUtils fileUtils = new FileUtils(CloudBoxApplication.getAppContext());

        String fileName  = fileUtils.getFileName(uri);
        String fileType  = fileUtils.getType(uri);
        String extension = fileUtils.getExtension(uri);

        long fileSize  = fileUtils.getSize(uri);
        long tMills    = Calendar.getInstance().getTimeInMillis();


        // insert the details on sqlite db
        mDatabase.insertFileDetails(
                tMills, fileName, fileSize,fileType, extension,String.valueOf(uri),tMills
        );

        // upload path
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference mReference = mStorage.getReference().child(uid).child(fileType).child(tMills+"-"+fileName);
        // init upload task
        UploadTask uploadTask = mReference.putFile(uri);
        CloudBoxApplication mApp = (CloudBoxApplication) getApplication();
        mApp.setUploadTask(tMills,uploadTask);

        mUtil.showUploadNotification(fileName, (int) tMills);

        uploadTask.addOnFailureListener(e -> {
            Toast.makeText(CloudBoxApplication.getAppContext(), "failed -> "+e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e("uploadTask",""+e);
            mUtil.updateProgress((int) 0,(int) tMills);
            mUtil.dismiss(12);
            mUtil.showUploadFailureNotification(fileName);
            isInsertedProgressTable = false;
            mApp.removeUploadTask(tMills);
        });

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            isInsertedProgressTable = false;
            if (taskSnapshot.getMetadata() == null ||
                    taskSnapshot.getMetadata().getReference() == null){
                //failed
                mDatabase.updateState(
                        tMills,
                        Constants.TRANSFER_FAILED,
                        TimeUtils.getTimestamp(),
                        Constants.TYPE_UPLOAD
                );
                return;
            }
            // retrieving download link
            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //success
                            String link = String.valueOf(task.getResult());
                            mDatabase.updateUploadedUrlInFileDetails(
                                    tMills, link
                            );
                            // add in recent activity
                            mDatabase.insertRecentActivity(tMills,Constants.ACTION_UPLOAD);
                            mDatabase.updateState(
                                    tMills,
                                    Constants.TRANSFER_COMPLETED,
                                    TimeUtils.getTimestamp(),
                                    Constants.TYPE_UPLOAD
                            );
                            mUtil.updateProgress((int) 100,(int) tMills);
                            mUtil.showUploadSuccessNotification(fileName);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                    .child("files")
                                    .child(uid)
                                    .child(tMills+"");
                            ref.child("id").setValue(tMills+"");
                            ref.child("fileName").setValue(fileName);
                            ref.child("fileSize").setValue(fileSize+"");
                            ref.child("fileUrl").setValue(link+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mPref.updateUsedStorage(fileSize);
                                        mPref.updateUsedStorage(fileSize,fileType);
                                    }
                                }
                            });

                        }else {
                            //failed
                            Log.d("uploadTask", "Upload failed");
                            Toast.makeText(CloudBoxApplication.getAppContext(), "something went wrong!", Toast.LENGTH_SHORT).show();
                            mDatabase.updateState(
                                    tMills,
                                    Constants.TRANSFER_FAILED,
                                    TimeUtils.getTimestamp(),
                                    Constants.TYPE_UPLOAD
                            );
                            mUtil.showUploadFailureNotification(fileName);
                        }
                        mUtil.dismiss((int) tMills);
                    });
            mApp.removeUploadTask(tMills);
        });


        uploadTask.addOnProgressListener(taskSnapshot -> {
            double transferred = taskSnapshot.getBytesTransferred();
            double total_byte  = taskSnapshot.getTotalByteCount();
            double progress    =  (100.0 * transferred) / total_byte;
            Log.d("uploadTask", "Upload is " + progress + "% done");

            // update session url
            mDatabase.updateSessionUrlInFileDetails(
                    tMills,String.valueOf(taskSnapshot.getUploadSessionUri())
            );
            if (!isInsertedProgressTable) {
                mDatabase.insertProgressTable(
                        tMills, tMills, Constants.TRANSFER_ONGOING,Constants.TYPE_UPLOAD
                );
                isInsertedProgressTable = true;
            }

            mUtil.updateProgress((int) progress,(int) tMills);
            long elapsedTime = System.currentTimeMillis() - startTime;
            double averageSpeed = (double) transferred / (elapsedTime / 1000.0);
            double estimatedTimeRemaining = (total_byte - transferred) / averageSpeed;

            String timeLeft =Extras.timeInUnit(estimatedTimeRemaining);
            mDatabase.updateFileProgress(tMills,
                    (int) progress,
                    taskSnapshot.getBytesTransferred(),
                    timeLeft);

            mPref.setUploadedSize(tMills,transferred);

        });

        uploadTask.addOnPausedListener(snapshot -> Log.e("mySpeed","paused->"+tMills));
    }

}
