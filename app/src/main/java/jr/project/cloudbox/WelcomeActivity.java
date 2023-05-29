package jr.project.cloudbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import jr.project.cloudbox.auth.LoginSelectionActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // transfer to login selection activity
        findViewById(R.id.getStarted).setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginSelectionActivity.class));
        });

        findViewById(R.id.terms).setOnClickListener(v -> {
            String url = "https://www.cloudbox.myselfjoraj.com/privacy-policy";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }
}