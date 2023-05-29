package jr.project.cloudbox;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import jr.project.cloudbox.crash.CrashHandler;

public class CloudBoxApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;


    private final HashMap<Long, UploadTask>       mUploadTasks = new HashMap<>();
    private final HashMap<Long, FileDownloadTask> mFileTasks   = new HashMap<>();

    public void setUploadTask(Long id, UploadTask uploadTask) {
        mUploadTasks.put(id, uploadTask);
    }

    public void setDownloadTask(Long id, FileDownloadTask downloadTask) {
        mFileTasks.put(id, downloadTask);
    }

    public UploadTask getUploadTask(Long id) {
        return mUploadTasks.get(id);
    }

    public FileDownloadTask getDownloadTask(Long id){
        return mFileTasks.get(id);
    }

    public void removeUploadTask(Long id){
        mUploadTasks.remove(id);
    }
    public void removeDownloadTask(Long id){
        mFileTasks.remove(id);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CloudBoxApplication.context = getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static Context getAppContext() {
        return CloudBoxApplication.context;
    }
}
