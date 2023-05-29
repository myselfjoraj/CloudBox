package jr.project.cloudbox.utils;

import android.annotation.SuppressLint;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class TimeUtils {

    //get current date in dd mmmm yyyy
    public static String date(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        return formatter.format(date);
    }

    public static String DDmmYYYY(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String getStandardDateFromTimeStamp(String timeStamp){
        DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        long milliSeconds= Long.parseLong(timeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDateFromTimeStamp(String timeStamp){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        long milliSeconds= Long.parseLong(timeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String time(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static long getTimestamp(){
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getTimeFromTimeStamp(String timeStamp){
        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        long milliSeconds= Long.parseLong(timeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String setTimeTvForRecycler(long timeStamp){
        String rtTime = "time_tv";
        if (timeStamp>0){
            if (getStandardDateFromTimeStamp(timeStamp+"").equals(date())){
                rtTime = "uploaded on today at "+getTimeFromTimeStamp(timeStamp+"");
            }else {
                rtTime = "uploaded on "+getDateFromTimeStamp(timeStamp+"")
                        + " at "+getTimeFromTimeStamp(timeStamp+"");
            }
        }
        return rtTime;
    }

    public static String setTimeTvForHistoryRecycler(long timeStamp){
        String rtTime = "time_tv";
        if (timeStamp>0){
            if (getStandardDateFromTimeStamp(timeStamp+"").equals(date())){
                rtTime = ""+getTimeFromTimeStamp(timeStamp+"");
            }else {
                rtTime = ""+getDateFromTimeStamp(timeStamp+"");
            }
        }
        return rtTime;
    }

}