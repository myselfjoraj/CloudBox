package jr.project.cloudbox.crash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.utils.TimeUtils;

public class ExceptionDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_display);


        TextView exception_text = (TextView) findViewById(R.id.textView43);
        ImageView btnBack = findViewById(R.id.backBtn);
        Button send = findViewById(R.id.button2);
        exception_text.setText(getIntent().getExtras().getString("error"));

        btnBack.setOnClickListener(view -> intentData());

        send.setOnClickListener(v -> {
            String er = getIntent().getExtras().getString("error");
            FirebaseDatabase.getInstance().getReference().child("errors")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(String.valueOf(TimeUtils.getTimestamp())).setValue(er);
            intentData();
        });

    }

    @Override
    public void onBackPressed() {
        intentData();
    }

    public void intentData() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(ExceptionDisplay.this, SplashScreenActivity.class);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}