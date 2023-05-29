package jr.project.cloudbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.fragments.FilesFragment;
import jr.project.cloudbox.fragments.HomeFragment;
import jr.project.cloudbox.fragments.SettingsFragment;
import jr.project.cloudbox.fragments.TaskFragment;
import jr.project.cloudbox.service.UploadService;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.IncomingFileIntent;

public class MainActivity extends AppCompatActivity {

    ImageView home,files,transfers,settings;
    ActivityResultLauncher<Intent> resultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        home      = findViewById(R.id.home);
        files     = findViewById(R.id.files);
        transfers = findViewById(R.id.transfers);
        settings  = findViewById(R.id.settings);

        setResultLauncher();

        home.setOnClickListener(v -> changeFragment(new HomeFragment()));

        files.setOnClickListener(v -> changeFragment(new FilesFragment()));

        transfers.setOnClickListener(v -> changeFragment(new TaskFragment()));

        settings.setOnClickListener(v -> changeFragment(new SettingsFragment()));

        findViewById(R.id.plusAdd).setOnClickListener(v -> showFileTypeSelectionSheet());


        if (FirebaseAuth.getInstance().getUid() != null) {
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getUid()).child("blocked")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String t = Objects.requireNonNull(snapshot.getValue()).toString();
                                if (t.equals("true")) {
                                    showBlockedDialog();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }


        // Get the deep link intent
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        if (FirebaseAuth.getInstance().getUid() != null &&
                Intent.ACTION_VIEW.equals(action) && data != null) {
            new IncomingFileIntent(MainActivity.this).execute(data);
        }


    }

    public void changeFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction =
                MainActivity.this
                .getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
    }

    public void showFileTypeSelectionSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_files_upload_selection);

        LinearLayout browseFile    = bottomSheetDialog.findViewById(R.id.browseFiles);
        LinearLayout browseGallery = bottomSheetDialog.findViewById(R.id.browseGallery);
        LinearLayout browseVideo   = bottomSheetDialog.findViewById(R.id.browseVideo);
        LinearLayout browseAudio   = bottomSheetDialog.findViewById(R.id.browseAudio);

        assert browseFile != null;
        browseFile.setOnClickListener(v -> {
            selectFiles(Constants.SELECT_ALL_FILES);
            bottomSheetDialog.dismiss();
        });

        assert browseGallery != null;
        browseGallery.setOnClickListener(v -> {
            selectFiles(Constants.SELECT_IMAGE);
            bottomSheetDialog.dismiss();
        });

        assert browseVideo != null;
        browseVideo.setOnClickListener(v -> {
            selectFiles(Constants.SELECT_VIDEO);
            bottomSheetDialog.dismiss();
        });

        /*
        assert shareApp != null;
        shareApp.setOnClickListener(v -> {
            String lk = "Get access to always free 5 GB secure cloud storage.\n" +
                    "Download CloudBox Now : https://cloudbox.myselfjoraj.com/download";
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Cloud Box Link Share");
            i.putExtra(Intent.EXTRA_TEXT, lk);
            startActivity(Intent.createChooser(i, "Cloud Box Link Share"));
            bottomSheetDialog.dismiss();
        });
         */

        assert browseAudio != null;
        browseAudio.setOnClickListener(v -> {
            selectFiles(Constants.SELECT_AUDIO);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        //gallery
        if (requestCode == 12) {
            if (data != null) {
                Uri contentURI = data.getData();
                Intent uploadIntent = new Intent(MainActivity.this, UploadService.class);
                uploadIntent.putExtra("uri",contentURI.toString());
                MainActivity.this.startService(uploadIntent);
            }
        }
    }




    private void selectFiles(int fileType) {
        String fileMime;

        switch (fileType){
            case Constants.SELECT_PDF:
                fileMime = "application/pdf";
                break;
            case Constants.SELECT_IMAGE:
                fileMime = "image/*";
                break;
            case Constants.SELECT_VIDEO:
                fileMime = "video/*";
                break;
            case Constants.SELECT_AUDIO:
                fileMime = "audio/*";
                break;
            default:
                fileMime = "*/*";
                break;
        }

        // Initialize intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // set type
        intent.setType(fileMime);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Launch intent
        resultLauncher.launch(intent);
    }

    // Initialize result launcher
    public void setResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Initialize result data
                    Intent data = result.getData();
                    // check condition
                    if (data != null) {
                        // Get uri
                        Uri sUri = data.getData();
                        Intent uploadIntent = new Intent(MainActivity.this, UploadService.class);
                        uploadIntent.putExtra("uri",sUri.toString());
                        startService(uploadIntent);
                    }
                });
    }

    public void showBlockedDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Blocked")
                .setMessage("You are blocked from Cloud Box application by the administrator. Please write a mail to support@myselfjoraj.com to know the reasons.")
                .setCancelable(false)
                .setNegativeButton("YES", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getUid() == null){
            startActivity(new Intent(MainActivity.this,SplashScreenActivity.class));
            finishAffinity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new CloudBoxOfflineDatabase(this).clearTransfersToFailure();
//        Toast.makeText(this, "cloud box upload service killed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        new CloudBoxOfflineDatabase(this).clearTransfersToFailure();
    }
}