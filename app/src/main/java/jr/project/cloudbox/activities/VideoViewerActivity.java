package jr.project.cloudbox.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Objects;

import jr.project.cloudbox.R;
import jr.project.cloudbox.utils.Extras;

public class VideoViewerActivity extends AppCompatActivity {

    VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);

        Objects.requireNonNull(getSupportActionBar()).hide();

        String fName = getIntent().getStringExtra("name")+"";
        String url   = getIntent().getStringExtra("url")+"";

        if (URLUtil.isValidUrl(url)){
            if (!Extras.urlExist(url)){
                Toast.makeText(
                                this,
                                "File has been removed from server!",
                                Toast.LENGTH_SHORT
                        )
                        .show();
                finish();
            }
        }

        videoView = findViewById(R.id.videoView);
        TextView  fileName  = findViewById(R.id.fileName);
        ImageView backBtn   = findViewById(R.id.imageView20);

        fileName.setText(fName);

        backBtn.setOnClickListener(v -> finish());

        findViewById(R.id.progressCard).setVisibility(View.VISIBLE);

        initiateVideoView(videoView,url);

    }

    void initiateVideoView(VideoView videoView,String videoUrl){

        if (videoView == null || videoUrl==null){
            return;
        }

        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(Uri.parse(videoUrl));

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(VideoViewerActivity.this, "Error occurred on playing file!", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        });

        videoView.setOnPreparedListener(mp -> {
//            ViewGroup.LayoutParams lp = videoView.getLayoutParams();
//            float videoWidth = mp.getVideoWidth();
//            float videoHeight = mp.getVideoHeight() - h();
//            float viewWidth = videoView.getWidth();
//            lp.height = (int) (viewWidth * (videoHeight / videoWidth));
//            videoView.setLayoutParams(lp);
            //  if (!videoView.isPlaying()) optional
            findViewById(R.id.progressCard).setVisibility(View.GONE);
            videoView.start();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (videoView!=null && videoView.isPlaying()){
            videoView.stopPlayback();
            videoView = null;
        }
        finish();
    }

    int h(){
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (VideoViewerActivity.this.getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
}