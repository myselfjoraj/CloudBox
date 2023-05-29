package jr.project.cloudbox.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.models.UserModel;
import jr.project.cloudbox.utils.BottomSheetPreview;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.Extras;

public class LoginSelectionActivity extends AppCompatActivity{

    GoogleSignInClient mGoogleSignInClient;
    BottomSheetPreview bPreview;
    FirebaseAuth firebaseAuth;

    private static final int RC_SIGN_IN = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_selection);

        // proceed to enter email
        findViewById(R.id.loginNow).setOnClickListener(v -> {
            startActivity(new Intent(LoginSelectionActivity.this, LoginActivity.class));
        });

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("757412481545-ocp5d8uhamavi29mqtfbgo7e1mna4mom.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();

        // proceed with google auth
        findViewById(R.id.signInGoogle).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        bPreview = new BottomSheetPreview(LoginSelectionActivity.this);
        bPreview.setSheet(Constants.BOTTOM_SHEET_PROCESS)
                .setText("    Registering your account in Cloud Box ...");

    }


    void throwDataToDatabase(String email, String name){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null){
            Toast.makeText(this, "Authentication unsuccessful!", Toast.LENGTH_SHORT).show();
            bPreview.dismiss();
            return;
        }
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DatabaseReference dbRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(uid);
        // insert data
         dbRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (!snapshot.exists()){
                     dbRef.child("email").setValue(email);
                     dbRef.child("name").setValue(name);
                     dbRef.child("uid").setValue(uid);
                     dbRef.child("used").setValue(0);
                     new SharedPreferences(LoginSelectionActivity.this).updateProfileName(name);
                 }
                 checkFootPrint();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {}});
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            bPreview.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            FirebaseGoogleAuth(account);
        } catch (ApiException e) {
            Log.w("LoginGoogle", "signInResult:failed code=" + e.getStatusCode());
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount account) {
        //check if account is null
        if (account != null)
        {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(LoginSelectionActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Toast.makeText(LoginSelectionActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                        bPreview.dismiss();
                    }
                }
            });
        }
        else{
            Toast.makeText(LoginSelectionActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
            bPreview.dismiss();
        }
    }

    private void updateUI()
    {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null)
        {
            String personName = account.getDisplayName();
            String personEmail = account.getEmail();
            throwDataToDatabase(personEmail,personName);
        }else {
            Toast.makeText(this, "Authentication unsuccessful!", Toast.LENGTH_SHORT).show();
            bPreview.dismiss();
        }

    }

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    void checkFootPrint(){

        SharedPreferences sPref = new SharedPreferences(LoginSelectionActivity.this);

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
                            startActivity(new Intent(LoginSelectionActivity.this, SplashScreenActivity.class));
                            finishAffinity();
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
                                Log.e("hi-cbox",""+id);
                                if (fBaseModel.getId()!=null) {
                                    fBaseModels.add(fBaseModel);
                                }
                            }
                            if (fBaseModels.size()>0) {
                                updateInSQLite(fBaseModels, () -> {
                                    bPreview.dismiss();
                                    startActivity(new Intent(LoginSelectionActivity.this, SplashScreenActivity.class));
                                    finishAffinity();
                                });
                            }else {
                                bPreview.dismiss();
                                startActivity(new Intent(LoginSelectionActivity.this, SplashScreenActivity.class));
                                finishAffinity();
                            }
                        }else {
                            bPreview.dismiss();
                            startActivity(new Intent(LoginSelectionActivity.this, SplashScreenActivity.class));
                            finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
    }

    private void updateInSQLite(ArrayList<FBaseModel> fBaseModels, LoginActivity.Done done) {
        CloudBoxOfflineDatabase mDatabase = new CloudBoxOfflineDatabase(LoginSelectionActivity.this);
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
                    String type = t.substring(0,t.indexOf("/"));
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
                    }else {
                        mDatabase.insertFileDetails(
                                finalFileId, f.getFileName(), finalFileSize, type, mime,
                                null, finalFileId
                        );
                        mDatabase.updateUploadedUrlInFileDetails(finalFileId, f.getFileUrl());
                    }
                    SharedPreferences sPref = new SharedPreferences(LoginSelectionActivity.this);
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

}