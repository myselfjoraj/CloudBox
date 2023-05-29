package jr.project.cloudbox.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.admin.AdminActivity;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.models.UserModel;
import jr.project.cloudbox.utils.BottomSheetPreview;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.Extras;

public class LoginActivity extends AppCompatActivity {

    EditText mailTyped, passwordTyped;
    FirebaseAuth mAuth;
    BottomSheetPreview bPreview;

    SharedPreferences sPref ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mailTyped     = findViewById(R.id.emailTypeField);
        passwordTyped = findViewById(R.id.passwordTypeField);

        TextView ab = findViewById(R.id.textView6);


        mAuth = FirebaseAuth.getInstance();
        sPref = new SharedPreferences(LoginActivity.this);

        bPreview = new BottomSheetPreview(LoginActivity.this);

        bPreview.setSheet(Constants.BOTTOM_SHEET_PROCESS)
                .setText("    Setting up your account in this device ...");

        findViewById(R.id.signUp).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        findViewById(R.id.loginNow).setOnClickListener(v -> {
            String email    = mailTyped.getText().toString();
            String password = passwordTyped.getText().toString();
                if (Extras.internetIsConnected()) {
                    initiateLogin(email, password);
                }else {
                    Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
        });



    }

    void initiateLogin(String email, String password){

        if (email.length() == 0){
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() == 0){
            Toast.makeText(this, "Please enter a valid password!", Toast.LENGTH_SHORT).show();
            return;
        }

        bPreview.show();

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    bPreview.dismiss();
                    if (task.isSuccessful()){
                        if(Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()
                        || email.equals("admin@myselfjoraj.com")) {
                            bPreview.setText("    Updating your account on this device ...");
                            bPreview.show();
                            checkFootPrint();
                        }else {
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Please verify your " +
                                            "e-mail address with verification link send to your email address.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(LoginActivity.this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    void checkFootPrint(){
        // get User Profile
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(Objects.requireNonNull(mAuth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel model = snapshot.getValue(UserModel.class);
                            assert model != null;
                            sPref.updateProfileName(model.getName());
                            sPref.updateProfileImage(model.getProfileImage());
                            sPref.updateUsedStorage((long) model.getUsed());
                            // get files
                            checkFilePrints();
                        }else {
                            bPreview.dismiss();
                            transferActivity();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
    }

    void checkFilePrints(){
        ArrayList<FBaseModel> fBaseModels = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("files")
                .child(Objects.requireNonNull(mAuth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                FBaseModel fBaseModel = new FBaseModel();
                                String id    = snapshot1.child("id").getValue(String.class);
                                String fName = snapshot1.child("fileName").getValue(String.class);
                                String fSize = snapshot1.child("fileSize").getValue(String.class);
                                String fUrl  = snapshot1.child("fileUrl").getValue(String.class);
                                String trash = snapshot1.child("trash").getValue(String.class);

                                fBaseModel.setTrsh(trash!=null);

                                fBaseModel.setId(id);
                                fBaseModel.setFileName(fName);
                                fBaseModel.setFileSize(fSize);
                                fBaseModel.setFileUrl(fUrl);
                                Log.e("hi-cbox",""+id+" ->"+trash);
                                if (fBaseModel.getId()!=null) {
                                    fBaseModels.add(fBaseModel);
                                }
                            }
                            if (fBaseModels.size()>0) {
                                updateInSQLite(fBaseModels, () -> {
                                    bPreview.dismiss();
                                    transferActivity();
                                });
                            }else {
                                bPreview.dismiss();
                                transferActivity();
                            }
                        }else {
                            bPreview.dismiss();
                            transferActivity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
    }

    private void updateInSQLite(ArrayList<FBaseModel> fBaseModels, Done done) {
        CloudBoxOfflineDatabase mDatabase = new CloudBoxOfflineDatabase(LoginActivity.this);
        for (Iterator<?> i = fBaseModels.iterator(); i.hasNext();){

            FBaseModel f = (FBaseModel) i.next();

            if (!Extras.urlExist(f.getFileUrl())){
                break;
            }

            StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(f.getFileUrl());
            if (f.getId()==null){
                break;
            }
            long fileId,fileSize = 0;
            fileId = (long) Double.parseDouble(f.getId());
            fileSize = (long) Double.parseDouble(f.getFileSize());
            if (fileId == 0 || fileSize == 0){
                break;
            }
            long finalFileId = fileId;
            long finalFileSize = fileSize;
            sr.getMetadata().addOnSuccessListener(storageMetadata -> {
                if (storageMetadata!=null) {
                    String t = storageMetadata.getContentType();
                    String type = t.substring(0, t.indexOf("/"));
                    String mime = t.substring(t.indexOf("/") + 1);
                    ////
                    if (f.isTrsh()) {
                        FileModel fileModel = new FileModel();
                        fileModel.setFileId(finalFileId);
                        fileModel.setFileName(f.getFileName());
                        fileModel.setFileSize(String.valueOf(finalFileSize));
                        fileModel.setFileType(type);
                        fileModel.setMimeType(mime);
                        fileModel.setTimeStamp(finalFileId);
                        fileModel.setUrl(f.getFileUrl());
                        mDatabase.insertFileTrash(fileModel);
                    } else {
                        mDatabase.insertFileDetails(
                                finalFileId, f.getFileName(), finalFileSize, type, mime,
                                null, finalFileId
                        );
                        mDatabase.updateUploadedUrlInFileDetails(finalFileId, f.getFileUrl());
                    }
                    sPref.updateUsedStorage(finalFileSize,type);
                }

            });
            if(!i.hasNext()) {
                done.completed();
            }
        }
        done.completed();

    }

    public interface Done{
        void completed();
    }

    void transferActivity(){
        Intent intent = new Intent(LoginActivity.this, SplashScreenActivity.class);
        intent.putExtra("extra","login");
        startActivity(intent);
        finishAffinity();
    }

}