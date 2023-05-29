package jr.project.cloudbox.activities;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;

import jr.project.cloudbox.R;
import jr.project.cloudbox.databinding.ActivityAudioViewerBinding;
import jr.project.cloudbox.utils.Extras;

public class AudioViewerActivity extends AppCompatActivity {

    ActivityAudioViewerBinding binding;
    MediaPlayer mp = new MediaPlayer();

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressCard.setVisibility(View.VISIBLE);
        binding.linearLayout11.setVisibility(View.GONE);

       Glide.with(AudioViewerActivity.this)
                .load(R.drawable.spinning_disk)
                .into((ImageView) findViewById(R.id.cdPlay));

       String fileName = getIntent().getStringExtra("fileName")+"";
       String fileUrl  = getIntent().getStringExtra("fileUrl");

        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(
                                this,
                                "File has been removed from server!",
                                Toast.LENGTH_SHORT
                        )
                        .show();
                finish();
            }
        }


        binding.backBtn.setOnClickListener(v -> finish());

        binding.fileName.setText(fileName);



        try {
            mp.reset();
            mp.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mp.setDataSource(fileUrl);
            mp.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

            }
        });

        mp.setOnPreparedListener(mp -> {
            binding.progressCard.setVisibility(View.GONE);
            binding.linearLayout11.setVisibility(View.VISIBLE);
            binding.begin.setText(milliSecondsToTimer(mp.getCurrentPosition()));
            binding.end.setText(milliSecondsToTimer(mp.getDuration()));
            binding.seekBar.setMax(mp.getDuration());
            mp.start();
            updateSeekBar();
        });


        binding.playPause.setOnClickListener(view -> {
            if(mp.isPlaying()) {
                mp.pause();
                binding.playImage.setImageResource(R.drawable.play_filled);
            } else {
                mp.start();
                binding.playImage.setImageResource(R.drawable.pause_filled_icon);
                updateSeekBar();
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // No action needed
            }
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // */

        binding.repeat.setOnClickListener(v -> repeatPlay(fileUrl));

        binding.stop.setOnClickListener(v -> {
            if (mp!=null){
                if (mp.isPlaying()){
                    mp.pause();
                    binding.playImage.setImageResource(R.drawable.play_filled);
                }
                mp.reset();
            }
        });

    }

    final Handler mHandler = new Handler();
    private void updateSeekBar() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mp != null) {
                    int mCurrentPosition = mp.getCurrentPosition();
                    binding.seekBar.setProgress(mCurrentPosition);
                    binding.begin.setText(milliSecondsToTimer(mCurrentPosition));
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp!=null){
            if (mp.isPlaying()){
                mp.pause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mp!=null){
            if (mp.isPlaying()){
                mp.pause();
                mp.reset();
            }
        }
    }

    void repeatPlay(String fileUrl){
        try {
            mp.reset();
            mp.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mp.setDataSource(fileUrl);
            mp.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



//    void initiateAudio(String text,String url){
//
//        TextView sText = findViewById(R.id.fileName);
//        if (sText != null) {
//            sText.setText(text + "");
//        }
//
//        sBegin = binding.begin;
//        TextView sEnd   = binding.end;
//
//
//
//        mp.setOnCompletionListener(mp -> {
//           // observer.stop();
//            binding.seekBar.setProgress(mp.getCurrentPosition());
//            mp.stop();
//            mp.reset();
//        });
//
//        ImageView btn_play_stop = findViewById(R.id.playImage);
//
//        btn_play_stop.setOnClickListener(view -> {
//            if(mp.isPlaying()) {
//                mp.pause();
//                btn_play_stop.setImageResource(R.drawable.play_filled);
//            } else {
//                mp.start();
//                btn_play_stop.setImageResource(R.drawable.pause_filled_icon);
//            }
//        });
//
//        sBegin.setText(milliSecondsToTimer(mp.getCurrentPosition()));
//        sEnd.setText(milliSecondsToTimer(mp.getDuration()));
//
//    }


//    private class MediaObserver implements Runnable {
//        private AtomicBoolean stop = new AtomicBoolean(false);
//
//        public void stop() {
//            stop.set(true);
//        }
//
//        @Override
//        public void run() {
//            while (!stop.get()) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        binding.seekBar.setProgress((int)((double)mp.getCurrentPosition() / (double)mp.getDuration()*100));
//                        binding.begin.setText(milliSecondsToTimer(mp.getCurrentPosition()));
//                    }
//                });
//                try {
//                    Thread.sleep(200);
//                } catch (Exception ex) {
//
//                }
//
//            }
//        }
//    }

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