package jr.project.cloudbox.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.models.UserModel;
import jr.project.cloudbox.service.DownloadService;

public class IncomingFileIntent {

    Context context;
    BottomSheetDialog bottomSheetDialog;

    public IncomingFileIntent(Context context){
        this.context = context;
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_from_link_download);
    }

    public void execute(Uri data){
        String[] link = data.toString().split("/");
        if (link.length > 3 && link[3] != null && link[4] !=null){
            getUserDetails(link[3],link[4]);
        }
    }


    void getUserDetails(String uid,String fId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel uModel = snapshot.getValue(UserModel.class);
                    getFileDetailsFromIntent(uModel,uid,fId);
                }else {
                    Toast.makeText(context, "File not found in server!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    public void getFileDetailsFromIntent(UserModel u, String uid, String id){
        FirebaseDatabase.getInstance().getReference().child("files")
                .child(uid).child(id).child("fileName")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fName = snapshot.getValue(String.class);

                            FileModel fModel = new FileModel();
                            fModel.setFileId(Long.parseLong(id));
                            fModel.setFileName(fName);
                            fModel.setTimeStamp(Long.parseLong(id));

                            getFileSize(u,fModel,id);

                        }else {
                            Toast.makeText(context, "File not found in server!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
    }

    void getFileSize(UserModel u,FileModel fm,String id){
        FirebaseDatabase.getInstance().getReference().child("files")
                .child(u.getUid()).child(id).child("fileSize")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fSize = snapshot.getValue(String.class);
                            fm.setFileSize(fSize);
                            getFileUrl(u,fm,id);
                        }else {
                            Toast.makeText(context, "File not found in server!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
    }

    void getFileUrl(UserModel u,FileModel fm,String id){
        FirebaseDatabase.getInstance().getReference().child("files")
                .child(u.getUid()).child(id).child("fileUrl")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String furl = snapshot.getValue(String.class);
                            fm.setUrl(furl);
                            showIncomingFileIntentDeepLink(u,fm);
                        }else {
                            Toast.makeText(context, "File not found in server!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
    }

    @SuppressLint("SetTextI18n")
    public void showIncomingFileIntentDeepLink(UserModel uModel,FileModel f){

        ImageView close = bottomSheetDialog.findViewById(R.id.backBtn);
        ImageView profile = bottomSheetDialog.findViewById(R.id.imageView23);
        ImageView sharedI = bottomSheetDialog.findViewById(R.id.imgShared);

        TextView pName = bottomSheetDialog.findViewById(R.id.textView41);
        TextView pEmail = bottomSheetDialog.findViewById(R.id.textView42);
        TextView fName = bottomSheetDialog.findViewById(R.id.fileName);
        TextView fSize = bottomSheetDialog.findViewById(R.id.fileSize);
        TextView fTime = bottomSheetDialog.findViewById(R.id.fileTime);

        Button imp = bottomSheetDialog.findViewById(R.id.importFile);
        Button down = bottomSheetDialog.findViewById(R.id.download);

        CardView imgHolder = bottomSheetDialog.findViewById(R.id.cv);


        StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(f.getUrl());
        sr.getMetadata().addOnSuccessListener(storageMetadata -> {
            if (storageMetadata!=null) {
                String t = storageMetadata.getContentType();
                String type = t.substring(0, t.indexOf("/"));
                String mime = t.substring(t.indexOf("/") + 1);
                // file details
                if (type != null ){
                    if (Objects.equals(type, "image")){
                        if (f.getUrl() != null){
                            Glide.with(context)
                                    .load(f.getUrl())
                                    .placeholder(R.drawable.placeholder_image)
                                    .into(sharedI);
                        }else {
                            bottomSheetDialog.dismiss();
                        }
                    }
                    else if (Objects.equals(type, "video")){
                        if (f.getUrl() != null){
                            Glide.with(context)
                                    .load(R.drawable.placeholder_video)
                                    .into(sharedI);
                        }else {
                            bottomSheetDialog.dismiss();
                        }
                    }
                    else if (Objects.equals(type, "audio")){
                        if (f.getUrl() != null){
                            Glide.with(context)
                                    .load(R.drawable.placeholder_audio)
                                    .into(sharedI);
                        }else {
                            bottomSheetDialog.dismiss();
                        }
                    }else if (Objects.equals(type, "application")){
                        if (f.getUrl() != null){
                            Glide.with(context)
                                    .load(R.drawable.placeholder_document)
                                    .into(sharedI);
                        }else {
                            bottomSheetDialog.dismiss();
                        }
                    }else {
                        imgHolder.setVisibility(View.GONE);
                    }

                    imp.setOnClickListener(v -> addToMyFiles(f,type,mime));
                    down.setOnClickListener(v -> downloadFile(f,type,mime));
                }
            }
        });

        close.setOnClickListener(v -> bottomSheetDialog.dismiss());


        // file owner details
        String profileUrl = uModel.getProfileImage();
        Glide.with(context)
                .load(profileUrl)
                .placeholder(R.drawable.avatar)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(profile);

        pName.setText(uModel.getName());
        pEmail.setText(uModel.getEmail());


        fName.setText(f.getFileName()+"");
        fSize.setText(Extras.fileSizeWithUnitOnFloat(Double.parseDouble(f.getFileSize())));
        fTime.setText(TimeUtils.getDateFromTimeStamp(String.valueOf(f.getTimeStamp())));

        bottomSheetDialog.show();

    }

    void addToMyFiles(FileModel f,String type,String mime){
        CloudBoxOfflineDatabase mDatabase = new CloudBoxOfflineDatabase(context);
        long tMills = TimeUtils.getTimestamp();
        mDatabase.insertFileDetails(
                tMills,f.getFileName(), Long.parseLong(f.getFileSize()),type,mime,
                null, tMills
        );
        mDatabase.updateUploadedUrlInFileDetails(tMills,f.getUrl());
        mDatabase.insertRecentActivity(tMills,Constants.ACTION_UPLOAD);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("files")
                .child(FirebaseAuth.getInstance().getUid()).child(tMills+"");

        ref.child("id").setValue(tMills+"");
        ref.child("fileName").setValue(f.getFileName()+"");
        ref.child("fileSize").setValue(f.getFileSize()+"");
        ref.child("fileUrl").setValue(f.getUrl()+"");

        Toast.makeText(context, "File imported to your account successfully!", Toast.LENGTH_SHORT).show();

        if (bottomSheetDialog!=null && bottomSheetDialog.isShowing()){
            bottomSheetDialog.dismiss();
        }
    }

    void downloadFile(FileModel fm,String type,String mime){
        String fileUrl = fm.getUrl();
        long tMills = TimeUtils.getTimestamp();
        fm.setMimeType(mime);
        fm.setFileType(type);
        fm.setFileId(tMills);
        fm.setTimeStamp(tMills);
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fm.getUrl()));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fm.getFileName());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);

        Toast.makeText(context, "File downloading started. Please check your notification bar for details!", Toast.LENGTH_SHORT).show();

        if (bottomSheetDialog!=null && bottomSheetDialog.isShowing()){
            bottomSheetDialog.dismiss();
        }
    }



}
