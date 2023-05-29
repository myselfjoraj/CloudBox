package jr.project.cloudbox.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.databinding.ActivityAdminUserProfileBinding;
import jr.project.cloudbox.models.UserModel;
import jr.project.cloudbox.service.UploadService;
import jr.project.cloudbox.utils.Extras;

public class AdminUserProfileActivity extends AppCompatActivity {

    ActivityAdminUserProfileBinding binding;
    ActivityResultLauncher<Intent> resultLauncher;
    boolean isBlocked = false;
    Uri sUri;
    String link;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserModel model = (UserModel) getIntent().getSerializableExtra("model");

        Glide.with(this)
                .load(model.getProfileImage())
                .placeholder(R.drawable.avatar)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage);

        binding.uid.setText ("UID     :   "+model.getUid());
        binding.used.setText("Used  :   "+ Extras.fileSizeWithUnitOnFloat(model.getUsed()));

        setResultLauncher();

        binding.nameField.setText(model.getName());
        binding.emailField.setText(model.getEmail());

        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> isBlocked = isChecked);

        binding.save.setOnClickListener(v -> saveChanges(model));

        binding.imageView27.setOnClickListener(v -> finish());

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            resultLauncher.launch(intent);
        });

        FirebaseDatabase.getInstance().getReference().child("users").child(model.getUid())
                .child("blocked").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String i = snapshot.getValue().toString();
                            binding.switch1.setChecked(i.equals("true"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    void saveChanges(UserModel m){

        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Updating Profile...");
        progressDialog.show();

        String name  = binding.nameField.getText()+"";
        String email = binding.emailField.getText()+"";

        if (sUri!=null){
            uploadPI(m.getUid(),name,email,progressDialog);
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(m.getUid());

        ref.child("name").setValue(name);
        ref.child("email").setValue(email);
        if (link!=null){
            ref.child("profileImage").setValue(link);
        }
        if (isBlocked){
            ref.child("blocked").setValue(true);
        }

        progressDialog.dismiss();
        Toast.makeText(this, "Profile updated successfully!",Toast.LENGTH_SHORT).show();
    }

    public void setResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Initialize result data
                    Intent data = result.getData();
                    // check condition
                    if (data != null) {
                        // Get uri
                        sUri = data.getData();
                        Glide.with(this)
                                .load(sUri)
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.profileImage);
                    }
                });
    }

    void uploadPI(String uid,String name,String email,ProgressDialog dialog){
        FirebaseStorage.getInstance().getReference().child("profileImages")
                .child(uid).putFile(sUri).addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot!=null) {
                        Objects.requireNonNull(Objects.requireNonNull
                                        (taskSnapshot.getMetadata()).getReference())
                                .getDownloadUrl().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        //success
                                        link = String.valueOf(task.getResult());
                                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                                .getReference()
                                                .child("users")
                                                .child(uid);

                                        ref.child("name").setValue(name);
                                        ref.child("email").setValue(email);
                                        if (link!=null){
                                            ref.child("profileImage").setValue(link);
                                        }
                                        if (isBlocked){
                                            ref.child("blocked").setValue(true);
                                        }

                                        dialog.dismiss();
                                    }
                                });
                    }
                }).addOnFailureListener(e -> dialog.dismiss());
    }
}