package jr.project.cloudbox.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.activities.ListFilesActivity;
import jr.project.cloudbox.activities.PasscodeLockActivity;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.databinding.FragmentFilesBinding;
import jr.project.cloudbox.databinding.FragmentHomeBinding;
import jr.project.cloudbox.databinding.FragmentSettingsBinding;
import jr.project.cloudbox.service.UploadService;
import jr.project.cloudbox.utils.Extras;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    FragmentSettingsBinding binding;
    SharedPreferences mPref;
    ActivityResultLauncher<Intent> resultLauncher;
    Uri profileUri;
    String profileImageLink,uid;
    BottomSheetDialog bottomSheetDialog;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater,container,false);

        mPref = new SharedPreferences(requireContext());
        uid   = FirebaseAuth.getInstance().getUid();

        binding.usedStorage.setText(
                Extras.fileSize((long) mPref.getUsedStorage())+" of 5 GB used"
        );

        binding.progressBar.setProgress((int) Extras.totalUsedPercent((long) mPref.getUsedStorage()));

        binding.profileName.setText(mPref.getProfileName());
        binding.emailId.setText(SharedPreferences.getEmailId());

        if (mPref.getProfileImage()!=null){
            Glide.with(requireContext())
                    .load(mPref.getProfileImage())
                    .placeholder(R.drawable.avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImg);
        }

        setResultLauncher();

        binding.accountSettings.setOnClickListener(v -> showAccountSettingsBottomSheet());

        binding.trash.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","trash");
            startActivity(intent);
        });

        binding.offlineFiles.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","offline");
            startActivity(intent);
        });


        binding.passcodeSetUp.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PasscodeLockActivity.class);
            if (mPref.getPasscodeForCB()!=null){
                intent.putExtra("value","exist");
            }else {
                intent.putExtra("value","null");
            }
            startActivity(intent);
        });

        binding.signOut.setOnClickListener(v -> showSignOutDialog());

        binding.transferPreference.setOnClickListener(v -> showTransferPreferenceDialog());

        binding.privacyPolicy.setOnClickListener(v -> {
            String url = "https://www.cloudbox.myselfjoraj.com/privacy-policy";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });






        return binding.getRoot();
    }

    public void showSignOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
        builder.setTitle("Sign Out?")
                .setMessage("Are you sure you want to sign out of Cloud Box?")
                .setPositiveButton("NO", (dialog, which) -> { })
                .setNegativeButton("YES", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    ((ActivityManager) requireContext()
                            .getSystemService(Context.ACTIVITY_SERVICE))
                            .clearApplicationUserData();
                    startActivity(new Intent(requireContext(), SplashScreenActivity.class));
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void showAccountSettingsBottomSheet(){
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_account_edit);

        ImageView pImage  = bottomSheetDialog.findViewById(R.id.profileImage);
        EditText  nameBox = bottomSheetDialog.findViewById(R.id.nameField);
        Button    save    = bottomSheetDialog.findViewById(R.id.save);

        assert nameBox != null;
        nameBox.setText(mPref.getProfileName());

        if (mPref.getProfileImage()!=null) {
            assert pImage != null;
            Glide.with(requireContext())
                    .load(mPref.getProfileImage())
                    .apply(RequestOptions.circleCropTransform())
                    .into(pImage);
        }

        if (profileUri!=null){
            assert pImage != null;
            Glide.with(requireContext())
                    .load(profileUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(pImage);
        }

        Objects.requireNonNull(pImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                // Initialize intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // set type
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Launch intent
                resultLauncher.launch(intent);
            }
        });

        assert save != null;
        save.setOnClickListener(v -> {

            String name = nameBox.getText().toString();
            if (name.length() == 0){
                Toast.makeText(getContext(),
                        "Please enter your name!", Toast.LENGTH_SHORT
                ).show();
                return;
            }else if (name.length() > 22 ){
                Toast.makeText(getContext(),
                        "Your name exceeds the character limit of 22!", Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (profileUri!=null) {
                FirebaseStorage.getInstance().getReference().child("profileImages")
                        .child(uid).putFile(profileUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(task -> {
                                        profileImageLink = String.valueOf(task.getResult());
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                                                .child("profileImage").setValue(profileImageLink);
                                        mPref.updateProfileImage(profileImageLink);
                                    });
                        });
            }

            FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                    .child("name").setValue(name);
            mPref.updateProfileName(name);

            //re setting views
            binding.profileName.setText(mPref.getProfileName());
            if (mPref.getProfileImage()!=null){
                Glide.with(requireContext())
                        .load(mPref.getProfileImage())
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.profileImg);
            }

            bottomSheetDialog.dismiss();
        });


        bottomSheetDialog.show();
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
                        profileUri = data.getData();
                        showAccountSettingsBottomSheet();
                    }
                });
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public void showTransferPreferenceDialog(){
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_transfer_preference);

        Switch uMobile = bottomSheetDialog.findViewById(R.id.uploadMobile);
        assert uMobile != null;
        uMobile.setChecked(mPref.getUploadPreferenceOnMobileData());
        uMobile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPref.setUploadPreferenceOnMobileData(isChecked);
        });

        Switch uWifi = bottomSheetDialog.findViewById(R.id.uploadWifi);
        assert uWifi != null;
        uWifi.setChecked(mPref.getUploadPreferenceOnWifi());
        uWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPref.setUploadPreferenceOnWifi(isChecked);
        });

        Switch dMobile = bottomSheetDialog.findViewById(R.id.downloadMobile);
        assert dMobile != null;
        dMobile.setChecked(mPref.getDownloadPreferenceOnMobileData());
        dMobile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPref.setDownloadPreferenceOnMobileData(isChecked);
        });

        Switch dWifi = bottomSheetDialog.findViewById(R.id.downloadWifi);
        assert dWifi != null;
        dWifi.setChecked(mPref.getDownloadPreferenceOnWifi());
        dWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPref.setDownloadPreferenceOnWifi(isChecked);
        });

        bottomSheetDialog.show();
    }

}