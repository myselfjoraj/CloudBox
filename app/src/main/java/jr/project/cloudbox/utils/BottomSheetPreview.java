package jr.project.cloudbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import jr.project.cloudbox.R;

public class BottomSheetPreview {


    Context           context;
    BottomSheetDialog bottomSheetDialog;

    ProgressBar progress_bar;
    TextView sBegin;

    MediaPlayer mp = new MediaPlayer();

    public BottomSheetPreview(Context context){
        this.context      = context;
        bottomSheetDialog = new BottomSheetDialog(context);
    }

    public BottomSheetPreview setSheet(int type){
        if (type == Constants.BOTTOM_SHEET_PROCESS){
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_footprint);
            bottomSheetDialog.setCancelable(false);
        }else if (type == Constants.BOTTOM_SHEET_AUDIO){
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_audio_player);
            bottomSheetDialog.setCancelable(false);
        }
        return this;
    }

    @SuppressLint("SetTextI18n")
    public BottomSheetPreview setText(String text){
        if (bottomSheetDialog!=null){
            TextView sText = bottomSheetDialog.findViewById(R.id.textApply);
            assert sText != null;
            sText.setText(text+"");
        }
        return this;
    }

    public BottomSheetPreview audioSetUp(String text,String url){
        if (bottomSheetDialog!=null){
            TextView sText = bottomSheetDialog.findViewById(R.id.fileName);
            if (sText != null) {
                sText.setText(text + "");
            }

            sBegin = bottomSheetDialog.findViewById(R.id.onGoing);
            TextView sEnd   = bottomSheetDialog.findViewById(R.id.end);

            progress_bar = (ProgressBar) bottomSheetDialog
                    .findViewById(R.id.progressBar3);

            try{
                mp.setDataSource(url);
                observer = new MediaObserver();
                mp.prepare();
                mp.start();
                new Thread(observer).start();
            }catch(Exception e){e.printStackTrace();}

            mp.setOnCompletionListener(mp -> {
                observer.stop();
                progress_bar.setProgress(mp.getCurrentPosition());
                mp.stop();
                mp.reset();
            });

            ImageView btn_play_stop = bottomSheetDialog.findViewById(R.id.playPause);

            btn_play_stop.setOnClickListener(view -> {
                if(mp.isPlaying()) {
                    mp.pause();
                    btn_play_stop.setImageResource(R.drawable.play_filled);
                } else {
                    mp.start();
                    btn_play_stop.setImageResource(R.drawable.pause_filled_icon);
                }
            });

            sBegin.setText(milliSecondsToTimer(mp.getCurrentPosition()));
            sEnd.setText(milliSecondsToTimer(mp.getDuration()));

            bottomSheetDialog.findViewById(R.id.close).setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
            });

        }
        return this;
    }


    public void show(){
        if (bottomSheetDialog!=null && !bottomSheetDialog.isShowing()){
            bottomSheetDialog.show();
        }
    }

    public void dismiss(){
        if (bottomSheetDialog!=null && bottomSheetDialog.isShowing()){
            bottomSheetDialog.dismiss();
        }
    }

    private MediaObserver observer = null;

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                progress_bar.setProgress((int)((double)mp.getCurrentPosition() / (double)mp.getDuration()*100));
                sBegin.setText(milliSecondsToTimer(mp.getCurrentPosition()));
                try {
                    Thread.sleep(200);
                } catch (Exception ex) {

                }

            }
        }
    }

    private static String milliSecondsToTimer(long milliSeconds){
        String timerString="";
        String secondsString;
        int hours=(int)(milliSeconds/(1000*60*60));
        int minutes=(int)(milliSeconds % (1000*60*60))/(1000*60);
        int seconds=(int)((milliSeconds % (1000*60*60)) % (1000*60)/1000);
        if (hours>0){
            timerString=hours+":";
        }
        if (seconds<10){
            secondsString="0"+seconds;
        }else{
            secondsString=""+seconds;
        }
        timerString=timerString+minutes+":"+secondsString;
        return timerString;
    }

}
