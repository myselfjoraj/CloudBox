package jr.project.cloudbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import jr.project.cloudbox.R;
import jr.project.cloudbox.activities.ImageViewerActivity;

public class Extras {

    public static String fileSizeWithUnit(long gFile){
        if (gFile>=0 && gFile<1024){
            return gFile+" B";
        }else if (gFile>=1024 && gFile<1048576){
            return gFile/1024+" KB";
        }else if (gFile>=1048576 && gFile<1073741824){
            return gFile/(1024*1024)+" MB";
        }else {
            return gFile/(1024*1024*1024)+" GB";
        }
    }

    public static String fileSizeWithUnit(double gFile){
        if (gFile>=0 && gFile<1024){
            return gFile+" B";
        }else if (gFile>=1024 && gFile<1048576){
            return gFile/1024+" KB";
        }else if (gFile>=1048576 && gFile<1073741824){
            return gFile/(1024*1024)+" MB";
        }else {
            return gFile/(1024*1024*1024)+" GB";
        }
    }
    @SuppressLint("DefaultLocale")
    public static String fileSizeWithUnitOnFloat(double gFile){
        if (gFile>=0 && gFile<1024){
            return String.format("%.2f", gFile)+" B";
        }else if (gFile>=1024 && gFile<1048576){
            double value = gFile/1024;
            return String.format("%.2f", value)+" KB";
        }else if (gFile>=1048576 && gFile<1073741824){
            double value = gFile/(1024*1024);
            return String.format("%.2f", value)+" MB";
        }else {
            double value = gFile/(1024*1024*1024);
            return String.format("%.2f", value)+" GB";
        }
    }

    public static String fileSize(long gFile){
        if (gFile>=0 && gFile<1024){
            return gFile+" B";
        }else if (gFile>=1024 && gFile<1048576){
            return  roundOff2(gFile/1024.0)+" KB";
        }else if (gFile>=1048576 && gFile<1073741824){
            return roundOff2(gFile/(1024.0*1024.0))+" MB";
        }else {
            return roundOff2(gFile/(1024.0*1024.0*1024.0))+" GB";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String timeInUnit(double time){
        if (time < 60){
            return String.format("%.0f", time)+" seconds";
        }else if (time>=60 && time<(60*60)){
            return String.format("%.0f", time/60)+" minutes";
        }else {
            return String.format("%.0f", time/(60*60))+" hours";
        }
    }

    public static double totalUsedPercent(long size){
        return (100.0 * size) / 5368709120.0;
    }

    public static double roundOff2(double i){
        return (double) Math.round(i * 100) / 100;
    }

    public static Bitmap retrieveVideoFrameFromVideo(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                try {
                    mediaMetadataRetriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bitmap;
    }

    public static byte[] retrieveThumbnailFromVideo(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                try {
                    mediaMetadataRetriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (bitmap != null) {
            return getBytes(bitmap);
        }else {
            return null;
        }
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static void loadThumbFromServer(Context ctx,String videoPath,ImageView iv){
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                try {
                    mediaMetadataRetriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (bitmap != null) {
            Glide.with(ctx)
                    .load(bitmap)
                    .placeholder(R.drawable.placeholder_image)
                    .into(iv);
        }
    }

    public static boolean urlExist(String url){
        if (!Extras.internetIsConnected()){
            return false;
        }
        if (url == null || url.length() == 0){
            return false;
        }
        URL u = null;
        int responseCode = 0;
        try {
            u = new URL(url);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("HEAD");
            responseCode = huc.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return HttpURLConnection.HTTP_OK == responseCode;
    }

    public static boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    public static int isWifi(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (internetIsConnected()) {
            if (isConnected) {
                boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                if (isWiFi){
                    return Constants.NETWORK_WIFI;
                }else {
                    return Constants.NETWORK_MOBILE_DATA;
                }
            }else {
                return Constants.NETWORK_NULL;
            }
        }else {
            return Constants.NETWORK_NULL;
        }
    }


}
