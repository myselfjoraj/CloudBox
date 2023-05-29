package jr.project.cloudbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.activities.AudioViewerActivity;
import jr.project.cloudbox.activities.ImageViewerActivity;
import jr.project.cloudbox.activities.PdfViewerActivity;
import jr.project.cloudbox.activities.VideoViewerActivity;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.service.DownloadService;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.Extras;
import jr.project.cloudbox.utils.TimeUtils;

public class ListFilesAdapter extends RecyclerView.Adapter<ListFilesAdapter.MyViewHolder> {

    Context context;
    ArrayList<FileModel> fileArray;
    CloudBoxOfflineDatabase mDatabase;
    String mode;
    lastItem lastItemListener;

    public ListFilesAdapter(Context context){
        this.context = context;
    }
    public void setMode(String mode){
        this.mode = mode;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setArray(ArrayList<FileModel> fileModels){
        this.fileArray = fileModels;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<FileModel> filterList) {
        this.fileArray = filterList;
        notifyDataSetChanged();
    }

    public void setLastItemListener(lastItem lastItemListener){
        this.lastItemListener = lastItemListener;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        mDatabase = new CloudBoxOfflineDatabase(context);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        FileModel model = fileArray.get(holder.getAdapterPosition());

        //load icon
        Glide.with(context)
                .load(drawableIcon(model.getFileType()))
                .into(holder.icon);

        if (model.isFavourite()){
            holder.favIcon.setVisibility(View.VISIBLE);
        }else {
            holder.favIcon.setVisibility(View.GONE);
        }

        //setup name
        holder.fileName.setText(model.getFileName());
        holder.fileSize.setText(Extras.fileSizeWithUnit(Long.parseLong(model.getFileSize())));
        holder.fileTime.setText(TimeUtils.setTimeTvForRecycler(model.getTimeStamp()));
        holder.dash.setVisibility(View.VISIBLE);

        if (mode == null) {
            holder.moreBtn.setOnClickListener(v -> openOptionMenu(v, holder.getAdapterPosition()));
        }else {
            holder.moreBtn.setOnClickListener(v -> openOptionMenu2(v, holder.getAdapterPosition()));
        }

        holder.fileTap.setOnClickListener(v -> {
            if (mode!=null && mode.equals("offline")) {
                transferActivity2(holder.getAdapterPosition());
            }else {
                transferActivity(holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return fileArray.size();
    }

    public int drawableIcon(String type){
        int drawable;
        switch (type) {
            case "image":
                drawable = R.drawable.image_icon;
                break;
            case "video":
                drawable = R.drawable.video_icon;
                break;
            case "audio":
                drawable = R.drawable.audio_icon;
                break;
            default:
                drawable = R.drawable.document_icon;
                break;
        }
        return drawable;
    }

    public void transferActivity(int position){
        String type = fileArray.get(position).getFileType();
        String ext  = fileArray.get(position).getMimeType();

        String fileUrl = fileArray.get(position).getUrl();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent i = new Intent();
        boolean op = false;

        switch (type) {
            case "image":
                i.setClass(context, ImageViewerActivity.class);
                i.putExtra("fileName",fileArray.get(position).getFileName());
                i.putExtra("imageUrl",fileArray.get(position).getUrl());
                break;
            case "video":
                i.setClass(context, VideoViewerActivity.class);
                i.putExtra("name",fileArray.get(position).getFileName());
                i.putExtra("url",fileArray.get(position).getUrl());
                break;
            case "audio":
                i.setClass(context, AudioViewerActivity.class);
                i.putExtra("fileName",fileArray.get(position).getFileName());
                i.putExtra("fileUrl",fileArray.get(position).getUrl());
                break;
            default:
                if (ext.equals("pdf")) {
                    i.setClass(context, PdfViewerActivity.class);
                    i.putExtra("url",fileArray.get(position).getUrl());
                    i.putExtra("name",fileArray.get(position).getFileName());
                    i.putExtra("fileId",fileArray.get(position).getFileId());

                }else {
                    op = true;
                    Toast.makeText(context, "Please download the file to open.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        if (!op) {
            context.startActivity(i);
        }

    }

    public void transferActivity2(int position){
        String type = fileArray.get(position).getFileType();
        String ext  = fileArray.get(position).getMimeType();

        String fileUrl = fileArray.get(position).getUrl();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent i = new Intent();
        boolean op = false;

        switch (type) {
            case "image":
                i.setClass(context, ImageViewerActivity.class);
                i.putExtra("fileName",fileArray.get(position).getFileName());
                i.putExtra("imageUrl",fileArray.get(position).getUriVal());
                break;
            case "video":
                i.setClass(context, VideoViewerActivity.class);
                i.putExtra("name",fileArray.get(position).getFileName());
                i.putExtra("url",fileArray.get(position).getUriVal());
                break;
            case "audio":
                i.setClass(context, AudioViewerActivity.class);
                i.putExtra("fileName",fileArray.get(position).getFileName());
                i.putExtra("fileUrl",fileArray.get(position).getUriVal());
                break;
            default:
                if (ext.equals("pdf")) {
                    i.setClass(context, PdfViewerActivity.class);
                    i.putExtra("url",fileArray.get(position).getUriVal());
                    i.putExtra("name",fileArray.get(position).getFileName());
                    i.putExtra("fileId",fileArray.get(position).getFileId());

                }else {
                    op = true;
                    Toast.makeText(context, "Please download the file to downloads directory.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        if (!op) {
            context.startActivity(i);
        }

    }

    public void openOptionMenu(View v,final int position){
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.recent_recycler_drop_down, popup.getMenu());
        Menu menuOpts = popup.getMenu();

        if (fileArray.get(position).isFavourite()){
            menuOpts.getItem(5).setTitle("Remove Favourites");
        }else {
            menuOpts.getItem(5).setTitle("Add to favourites");
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Trash")){
                addToTrash(position);
            }else if (item.getTitle().equals("Rename")){
                showRenameDialog(position);
            }else if (item.getTitle().equals("Share via link")){
                copyLinkSheet(position);
            }else if (item.getTitle().equals("Download")){
                downloadFile(position,true);
            }else if (item.getTitle().equals("Make offline")){
                downloadFile(position,false);
            }else if (item.getTitle().equals("Add to favourites")){
                addOrRemoveFav(position,true);
            }else if (item.getTitle().equals("Remove Favourites")){
                addOrRemoveFav(position,false);
            }else if (item.getTitle().equals("Delete permanently")){
                deletePermanently(position);
            }
            return true;
        });
        popup.show();
    }

    public void openOptionMenu2(View v,final int position){
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        if (mode != null && mode.equals("offline")) {
            popup.getMenuInflater().inflate(R.menu.offline_list_drop_down, popup.getMenu());
        }else if (mode!= null && mode.equals("trash")) {
            popup.getMenuInflater().inflate(R.menu.trash_list_drop_down, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Rename")){
                showRenameDialog2(position);
            }else if (item.getTitle().equals("Remove from Trash")){
                mDatabase.insertFileDetails(fileArray.get(position));
                FirebaseDatabase.getInstance().getReference().child("files")
                        .child(FirebaseAuth.getInstance().getUid()).child(fileArray.get(position).getFileId()+"")
                        .child("trash").removeValue();
                fileArray.remove(position);
                notifyItemRemoved(position);
            }else if (item.getTitle().equals("Delete Permanently")){
                deletePermanently(position);
            }else if (item.getTitle().equals("Delete from here")){
                mDatabase.deleteRowFromId(Constants.FILE_OFFLINE_TABLE,fileArray.get(position).getFileId());
                File f = new File(fileArray.get(position).getUriVal());
                if (f.exists()){
                    f.delete();
                }
                fileArray.remove(position);
                notifyItemRemoved(position);
                if (position == 0 && lastItemListener != null){
                    lastItemListener.lastItem();
                }
            }
            return true;
        });
        popup.show();
    }

    public void addToTrash(int position){
        mDatabase.insertFileTrash(fileArray.get(position));
        FirebaseDatabase.getInstance().getReference().child("files")
                .child(FirebaseAuth.getInstance().getUid()).child(fileArray.get(position).getFileId()+"")
                .child("trash").setValue("true");
        notifyItemRangeRemoved(position,1);
        if (position == 0 && lastItemListener != null){
            lastItemListener.lastItem();
        }
    }

    public void showRenameDialog(int position){

        String fileUrl = fileArray.get(position).getUrl();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Rename File");

        // set the custom layout
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.et_dialog,null);
        builder.setView(customLayout);

        EditText editText = customLayout.findViewById(R.id.editText);

        editText.setText(fileArray.get(position).getFileName());

        builder.setPositiveButton(Html.fromHtml("<font color='#FF7F27'>OK</font>",Html.FROM_HTML_MODE_COMPACT), (dialog, which) -> {
            String fileName = editText.getText().toString();
            mDatabase.renameFile(fileArray.get(position).getFileId(),fileName);
            fileArray.get(position).setFileName(fileName);
            notifyItemChanged(position);
            FirebaseDatabase.getInstance().getReference()
                    .child("files")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(fileArray.get(position).getFileId()+"")
                    .child("fileName").setValue(fileName);
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#FF7F27'>CANCEL</font>",Html.FROM_HTML_MODE_COMPACT), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showRenameDialog2(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Rename File");

        // set the custom layout
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.et_dialog,null);
        builder.setView(customLayout);

        EditText editText = customLayout.findViewById(R.id.editText);

        editText.setText(fileArray.get(position).getFileName());

        builder.setPositiveButton(Html.fromHtml("<font color='#FF7F27'>OK</font>",Html.FROM_HTML_MODE_COMPACT), (dialog, which) -> {
            String fileName = editText.getText().toString();
            if (mode!=null && mode.equals("trash")) {
                mDatabase.renameFile(fileArray.get(position).getFileId(), fileName, Constants.FILE_TRASH_TABLE);
            }else {
                mDatabase.renameFile(fileArray.get(position).getFileId(), fileName, Constants.FILE_OFFLINE_TABLE);
            }
            fileArray.get(position).setFileName(fileName);
            notifyItemChanged(position);
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#FF7F27'>CANCEL</font>",Html.FROM_HTML_MODE_COMPACT), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void downloadFile(int position,boolean b){
        String fileUrl = fileArray.get(position).getUrl();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent dwIntent = new Intent(context, DownloadService.class);
        dwIntent.putExtra("model",fileArray.get(position));
        dwIntent.putExtra("downloadDir",b+"");
        context.startService(dwIntent);
    }

    @SuppressLint("SetTextI18n")
    public void copyLinkSheet(int position){
        FileModel fm = fileArray.get(position);
        String fileUrl = fileArray.get(position).getUrl();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        BottomSheetDialog b = new BottomSheetDialog(context);
        b.setContentView(R.layout.bottom_sheet_share_file_link);
        TextView fName = b.findViewById(R.id.fileName);
        TextView fSize = b.findViewById(R.id.fileSize);
        TextView fTime = b.findViewById(R.id.fileTime);
        ImageView ico = b.findViewById(R.id.fileIcon);
        assert fName != null;
        fName.setText(fm.getFileName());
        assert fSize != null;
        fSize.setText(Extras.fileSizeWithUnit(Long.parseLong(fm.getFileSize())));
        assert fTime != null;
        fTime.setText("created on "+ TimeUtils.getDateFromTimeStamp(
                String.valueOf(fm.getTimeStamp()))+" at "+
                TimeUtils.getTimeFromTimeStamp(String.valueOf(fm.getTimeStamp())
                ));
        //load icon
        assert ico != null;
        Glide.with(context)
                .load(drawableIco(fm.getMimeType()))
                .into(ico);

        LinearLayout in = b.findViewById(R.id.viewMain);

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch s = b.findViewById(R.id.shareSwitch);
        assert s != null;
        assert in != null;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("files")
                .child(FirebaseAuth.getInstance().getUid()).child(fm.getFileId()+"").
                child("share");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String sh = snapshot.getValue().toString();
                    s.setChecked(!sh.equals("false"));
                }else {
                    s.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});

        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                in.setVisibility(View.VISIBLE);
                ref.removeValue();
            }else {
                in.setVisibility(View.GONE);
                ref.setValue("false");
            }
        });

        EditText shLink = b.findViewById(R.id.etLink);
        String lk = "https://cloudbox.myselfjoraj.com/base?token="+
                FirebaseAuth.getInstance().getUid()+"&id="+
                fm.getFileId();
        assert shLink != null;
        shLink.setText(lk);

        Button sButton = b.findViewById(R.id.shareBtn);
        assert sButton != null;
        sButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Cloud Box Link Share");
            i.putExtra(Intent.EXTRA_TEXT, lk);
            context.startActivity(Intent.createChooser(i, "Cloud Box Link Share"));
        });


        b.show();
    }

    public int drawableIco(String type){
        int drawable;
        switch (type) {
            case "image":
                drawable = R.drawable.cloud_fragment_image;
                break;
            case "video":
                drawable = R.drawable.cloud_fragment_video;
                break;
            case "audio":
                drawable = R.drawable.cloud_fragment_audio;
                break;
            default:
                drawable = R.drawable.cloud_fragment_other;
                break;
        }
        return drawable;
    }

    public void addOrRemoveFav(int position,boolean isAdd){
        if (isAdd){
            mDatabase.addToFavourites(fileArray.get(position).getFileId());
            fileArray.get(position).setFavourite(true);
        }else {
            mDatabase.removeFavourites(fileArray.get(position).getFileId());
            fileArray.get(position).setFavourite(false);
        }
        notifyItemChanged(position);
    }

    public void deletePermanently(int position){
        mDatabase.insertFileTrash(fileArray.get(position));
        mDatabase.deleteRowFromId(Constants.FILE_TRASH_TABLE,fileArray.get(position).getFileId());
        String fileUrl = fileArray.get(position).getUrl();
        String fileType = fileArray.get(position).getFileType();
        long fileSize = Long.parseLong(fileArray.get(position).getFileSize());
        long fileTime = fileArray.get(position).getTimeStamp();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                notifyItemRangeRemoved(position,1);
                return;
            }
        }
        SharedPreferences sPref = new SharedPreferences(context);
        FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl).delete();
        FirebaseDatabase.getInstance().getReference().child("files")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(fileTime+"").removeValue();
        sPref.updateRemovedStorage(fileSize);
        sPref.updateRemovedStorage(fileSize,fileType);
        if (fileArray.size()>position){
            fileArray.remove(position);
            notifyItemRemoved(position);
        }else {
            if (fileArray.size()>=position) {
                fileArray.remove(position);
            }
            notifyDataSetChanged();
        }
        if (position == 0 && lastItemListener != null){
            lastItemListener.lastItem();
        }
    }

    public interface lastItem{
        void lastItem();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView icon,moreBtn,favIcon;
        TextView fileName,fileSize,fileTime,dash;
        LinearLayout fileTap;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // iv
            icon    = itemView.findViewById(R.id.icon);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            favIcon = itemView.findViewById(R.id.favIcon);
            // tv
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileTime = itemView.findViewById(R.id.fileTime);
            dash     = itemView.findViewById(R.id.textView30);
            //ll
            fileTap  = itemView.findViewById(R.id.fileTap);
        }
    }
}
