package jr.project.cloudbox;

import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import jr.project.cloudbox.activities.PasscodeLockActivity;
import jr.project.cloudbox.activities.PermissionActivity;
import jr.project.cloudbox.admin.AdminActivity;
import jr.project.cloudbox.auth.LoginActivity;
import jr.project.cloudbox.database.SharedPreferences;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // initializing classes
        mAuth = FirebaseAuth.getInstance();

        // check for user logged in or not
        if (mAuth.getCurrentUser()!=null && mAuth.getUid()!=null){
            if (new SharedPreferences(SplashScreenActivity.this).getPasscodeForCB()!=null){
                startUnlockActivity();
            }else {
                checkPermission();
            }
        }else {
            startWelcomeActivity();
        }

    }

    public void checkPermission(){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                startMainActivity();
            }else {
                new Handler().postDelayed(() -> startActivity(new Intent(this, PermissionActivity.class)), 3000);
            }
        }else {
           if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                   != PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(this, new String[]
                       {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
           }else {
               startMainActivity();
           }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMainActivity();
            }
        }
    }

    public void startWelcomeActivity(){

        new Handler().postDelayed(() -> {
            Intent i=new Intent(SplashScreenActivity.this,WelcomeActivity.class);
            startActivity(i);
        }, 3000);
    }

    public void startUnlockActivity(){
        new Handler().postDelayed(() -> {
            Intent i=new Intent(SplashScreenActivity.this,PasscodeLockActivity.class);
            i.putExtra("value","unlock");
            startActivity(i);
        }, 3000);
    }

    public void startMainActivity(){
        new Handler().postDelayed(() -> {
            if(mAuth.getCurrentUser().getEmail().equals("admin@myselfjoraj.com")){
                Intent i = new Intent(SplashScreenActivity.this, AdminActivity.class);
                startActivity(i);
                finishAffinity();
            }else {
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);
                finishAffinity();
            }
        }, 5000);
    }

}