package jr.project.cloudbox.utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

import jr.project.cloudbox.CloudBoxApplication;
import jr.project.cloudbox.R;

public class DeviceNotificationUtility {

    Context context;

    int PROGRESS_MAX = 100;
    int PROGRESS_CURRENT = 0;

    ArrayList<Integer> uploadNotificationIds   = new ArrayList<>();
    ArrayList<Integer> downloadNotificationIds = new ArrayList<>();

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;

    public DeviceNotificationUtility(Context context) {
        this.context = context;
    }

    public void updateProgress(int progress, int id) {
        this.PROGRESS_CURRENT = progress;
        if (builder != null && notificationManager != null) {
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            builder.setOnlyAlertOnce(true);
            if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(id, builder.build());
        }
    }

    public void dismiss(int id) {
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
        if (uploadNotificationIds.contains(id)) {
            uploadNotificationIds.remove(getIndexOfNotification(id));
        }
    }

    public int getIndexOfNotification(int id) {
        int a = 0;
        for (int i = 0; i < uploadNotificationIds.size(); i++) {
            if (uploadNotificationIds.get(i) == id) {
                a = i;
            }
        }
        return a;
    }

    public void dismissAllUploads() {
        try {
            for (int i = 0; i < uploadNotificationIds.size(); i++) {
                NotificationManagerCompat.from(context).cancel(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showUploadNotification(String fileName, int id) {
        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, "26");
        builder.setContentTitle("Uploading " + fileName)
                .setContentText("Uploading in progress")
                .setSmallIcon(R.drawable.cloud_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(id, builder.build());
        if (!uploadNotificationIds.contains(id)){
            uploadNotificationIds.add(id);
        }
    }

    public void showUploadSuccessNotification(String fileName){
        createNotificationChannel();
        if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_UPLOAD_CHANNEL);
        builder.setContentTitle("Uploading Completed")
                .setContentText(""+fileName)
                .setSmallIcon(R.drawable.cloud_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify((int) TimeUtils.getTimestamp(), builder.build());
    }

    public void showUploadFailureNotification(String fileName){
        createNotificationChannel();
        if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_UPLOAD_CHANNEL);
        builder.setContentTitle("Uploading Failed")
                .setContentText(""+fileName)
                .setSmallIcon(R.drawable.cloud_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify((int) TimeUtils.getTimestamp(), builder.build());
    }

    // download
    public void showDownloadNotification(String fileName, int id){
        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, "26");
        builder.setContentTitle("Downloading " + fileName)
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.cloud_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(id, builder.build());
        if (!downloadNotificationIds.contains(id)){
            downloadNotificationIds.add(id);
        }
    }

    public void showDownloadSuccessNotification(String fileName){
        createNotificationChannel();
        if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_UPLOAD_CHANNEL);
        builder.setContentTitle("Download Completed")
                .setContentText(""+fileName)
                .setSmallIcon(R.drawable.cloud_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify((int) TimeUtils.getTimestamp(), builder.build());
    }

    public void showDownloadFailureNotification(String fileName){
        createNotificationChannel();
        if (ActivityCompat.checkSelfPermission(CloudBoxApplication.getAppContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_UPLOAD_CHANNEL);
        builder.setContentTitle("Download Failed")
                .setContentText(""+fileName)
                .setSmallIcon(R.drawable.cloud_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify((int) TimeUtils.getTimestamp(), builder.build());
    }


    private void createNotificationChannel() {
        CharSequence name = "CloudBoxNotificationChannel";
        String description = "Notifications from cloud box";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel( Constants.NOTIFICATION_UPLOAD_CHANNEL, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


}
