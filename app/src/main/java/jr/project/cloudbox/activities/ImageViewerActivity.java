package jr.project.cloudbox.activities;

import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Objects;

import jr.project.cloudbox.R;
import jr.project.cloudbox.utils.Extras;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        Objects.requireNonNull(getSupportActionBar()).hide();

        String fileName;
        String imageUrl;

        fileName = getIntent().getStringExtra("fileName");
        imageUrl = getIntent().getStringExtra("imageUrl");

        if (URLUtil.isValidUrl(imageUrl)){
            if (!Extras.urlExist(imageUrl)){
                Toast.makeText(
                        this,
                        "File has been removed from server!",
                        Toast.LENGTH_SHORT
                        )
                        .show();
                finish();
            }
        }


        ImageView back = findViewById(R.id.backBtn);
        back.setOnClickListener(v -> finish());

        TextView fileTV =findViewById(R.id.fileName);

        ImageView photo = findViewById(R.id.displayImage);

        fileTV.setText(fileName);

        Glide.with(ImageViewerActivity.this)
                .load(imageUrl)
                .into(photo);


    }
}