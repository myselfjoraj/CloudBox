package jr.project.cloudbox.database;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import jr.project.cloudbox.R;

public class SharedPreferences {

    Context                                  context;
    android.content.SharedPreferences        pref;
    android.content.SharedPreferences.Editor editor;

    public SharedPreferences(Context context){
        this.context = context;
        pref         = context.getSharedPreferences("application",Context.MODE_PRIVATE);
        editor       = pref.edit();
    }

    //region Listener registering and unregistering methods
    public void registerListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void updateProfileName(String name){
        editor.putString("profileName",name);
        editor.apply();
    }

    public String getProfileName(){
        return pref.getString("profileName",""+getEmailId().substring(0,getEmailId().lastIndexOf("@")));
    }

    public void updateProfileImage(String url){
        editor.putString("profileImage",url);
        editor.apply();
    }

    public String getProfileImage(){
        return pref.getString("profileImage",null);
    }

    public double getUsedStorage(){
        return pref.getLong("usedStorageInBytes",0);
    }

    public static String getEmailId(){
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
    }

    public void setPasscodeForCB(String passcode){
        editor.putString("passcodeForCB",passcode);
        editor.apply();
    }

    public void removePasscodeForCB(){
        editor.remove("passcodeForCB");
        editor.apply();
    }

    public String getPasscodeForCB(){
        return pref.getString("passcodeForCB",null);
    }

    public void updateUsedStorage(long size){
        double usedStorage = getUsedStorage();
        editor.putLong("usedStorageInBytes", (long) (usedStorage+size));
        editor.apply();
        CloudBoxFirebaseDatabase.updateMyUsedSpace((long) (usedStorage+size));
    }
    public void updateRemovedStorage(long size){
        double usedStorage = getUsedStorage();
        editor.putLong("usedStorageInBytes", (long) (usedStorage-size));
        editor.apply();
        CloudBoxFirebaseDatabase.updateMyUsedSpace((long) (usedStorage-size));
    }

    public void updateUsedStorage(long size,String type){
        switch (type){
            case "image":
                setImageTotalSize(size);
                break;
            case "video":
                setVideoTotalSIze(size);
                break;
            case "audio":
                setAudioTotalSize(size);
                break;
            default:
                setOtherTotalSize(size);
                break;
        }
    }

    public void updateRemovedStorage(long size,String type){
        switch (type){
            case "image":
                long usedStorage = getImageTotalSize();
                editor.putLong("usedImageStorageInBytes",usedStorage-size);
                editor.apply();
                break;
            case "video":
                long usedStorage1 = getVideoTotalSIze();
                editor.putLong("usedVideoStorageInBytes",usedStorage1-size);
                editor.apply();
                break;
            case "audio":
                long usedStorage2 = getAudioTotalSize();
                editor.putLong("usedAudioStorageInBytes",usedStorage2+size);
                editor.apply();
                break;
            default:
                long usedStorage3 = getOtherTotalSize();
                editor.putLong("usedOtherFileStorageInBytes",usedStorage3+size);
                editor.apply();
                break;
        }
    }

    public void setImageTotalSize(long size){
        long usedStorage = getImageTotalSize();
        editor.putLong("usedImageStorageInBytes",usedStorage+size);
        editor.apply();
    }

    public long getImageTotalSize(){
        return pref.getLong("usedImageStorageInBytes",0);
    }

    public void setVideoTotalSIze(long size){
        long usedStorage = getVideoTotalSIze();
        editor.putLong("usedVideoStorageInBytes",usedStorage+size);
        editor.apply();
    }

    public long getVideoTotalSIze(){
        return pref.getLong("usedVideoStorageInBytes",0);
    }

    public void setAudioTotalSize(long size){
        long usedStorage = getAudioTotalSize();
        editor.putLong("usedAudioStorageInBytes",usedStorage+size);
        editor.apply();
    }

    public long getAudioTotalSize(){
        return pref.getLong("usedAudioStorageInBytes",0);
    }

    public void setOtherTotalSize(long size){
        long usedStorage = getOtherTotalSize();
        editor.putLong("usedOtherFileStorageInBytes",usedStorage+size);
        editor.apply();
    }

    public long getOtherTotalSize(){
        return pref.getLong("usedOtherFileStorageInBytes",0);
    }

    public void setCurrentTransferSpeed(String transfer){
        editor.putString("currentTransferSpeed",transfer);
        editor.apply();
    }

    public void setCurrentDownloadSpeed(String transfer){
        editor.putString("currentDownloadSpeed",transfer);
        editor.apply();
    }

    public String getCurrentTransferSpeed(){
        return pref.getString("currentTransferSpeed","0")+" MB/s";
    }

    public void setUploadedSize(long fileId,double size){
        editor.putString("currentTransferredSize-"+fileId,size+"");
        editor.apply();
    }

    public double getUploadedSize(long fileId){
        return Double.parseDouble(pref.getString("currentTransferredSize-"+fileId,"0"));
    }

    public void setUploadPreferenceOnMobileData(boolean a){
        editor.putBoolean("UploadPreferenceOnMobileData",a);
        editor.apply();
    }

    public void setUploadPreferenceOnWifi(boolean a){
        editor.putBoolean("UploadPreferenceOnWifi",a);
        editor.apply();
    }

    public boolean getUploadPreferenceOnMobileData(){
        return pref.getBoolean("UploadPreferenceOnMobileData",true);
    }

    public boolean getUploadPreferenceOnWifi(){
        return pref.getBoolean("UploadPreferenceOnWifi",true);
    }

    public void setDownloadPreferenceOnMobileData(boolean a){
        editor.putBoolean("DownloadPreferenceOnMobileData",a);
        editor.apply();
    }

    public void setDownloadPreferenceOnWifi(boolean a){
        editor.putBoolean("DownloadPreferenceOnWifi",a);
        editor.apply();
    }

    public boolean getDownloadPreferenceOnMobileData(){
        return pref.getBoolean("DownloadPreferenceOnMobileData",true);
    }

    public boolean getDownloadPreferenceOnWifi(){
        return pref.getBoolean("DownloadPreferenceOnWifi",true);
    }




}
