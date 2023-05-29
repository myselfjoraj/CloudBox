package jr.project.cloudbox.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.utils.BottomSheetPreview;
import jr.project.cloudbox.utils.CloudBoxUtils;
import jr.project.cloudbox.utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    EditText name, email, password;
    FirebaseAuth mAuth;
    BottomSheetPreview bPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.nameTypeField);
        email = findViewById(R.id.emailTypeField);
        password = findViewById(R.id.passwordTypeField);

        bPreview = new BottomSheetPreview(RegisterActivity.this);

        findViewById(R.id.signIn).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        findViewById(R.id.loginNow).setOnClickListener(v -> initiateRegistration());

        bPreview.setSheet(Constants.BOTTOM_SHEET_PROCESS)
                .setText("    Registering your account in Cloud Box ...");

    }

    void initiateRegistration(){
        String mName = name.getText().toString();
        String mEmail = email.getText().toString();
        String mPassword = password.getText().toString();

        if (mName.length() == 0){
            Toast.makeText(this, "Please enter a valid name!", Toast.LENGTH_SHORT).show();
        }else if (mName.length() < 5){
            Toast.makeText(this, "Your profile name should be greater than 5 characters!", Toast.LENGTH_SHORT).show();
        }else if (mEmail.length() == 0){
            Toast.makeText(this, "Please enter a valid e-mail!", Toast.LENGTH_SHORT).show();
        }else if (!CloudBoxUtils.isEmailValid(mEmail)){
            Toast.makeText(this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show();
        }else if (mPassword.length() == 0){
            Toast.makeText(this, "Please enter a password!", Toast.LENGTH_SHORT).show();
        }else if (mPassword.length()<6){
            Toast.makeText(this, "Your password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
        }else {
            initiateFBaseRegistration(mEmail,mPassword,mName);
        }
    }

    void initiateFBaseRegistration(String email, String password, String name){
        bPreview.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        throwDataToDatabase(email,name);
                    } else {
                        bPreview.dismiss();
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void throwDataToDatabase(String email, String name){
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference dbRef =FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(uid);
        // insert data
        dbRef.child("email").setValue(email);
        dbRef.child("name").setValue(name);
        dbRef.child("uid").setValue(uid);
        dbRef.child("used").setValue(0);
        new SharedPreferences(RegisterActivity.this).updateProfileName(name);
        sendVerificationEmail();
    }

    void sendVerificationEmail(){
        Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FirebaseAuth.getInstance().signOut();
                        showSuccess();
                    }else {
                        Toast.makeText(this, "You are already registered!",
                                Toast.LENGTH_SHORT).show();
                    }
                    bPreview.dismiss();
                });
    }

    public void showSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Verify your e-mail")
                .setMessage("Please verify your " +
                        "e-mail address with verification link send to your mail from cloud box.Please " +
                        "check your spam folder if link not found in your inbox.")
                .setCancelable(false)
                .setNegativeButton("OK", (dialog, which) -> {
                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}