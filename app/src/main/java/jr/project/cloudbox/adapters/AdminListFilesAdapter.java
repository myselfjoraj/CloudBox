package jr.project.cloudbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.admin.AdminActivity;
import jr.project.cloudbox.admin.AdminAllFilesActivity;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.utils.Extras;
import jr.project.cloudbox.utils.TimeUtils;

public class AdminListFilesAdapter extends RecyclerView.Adapter<AdminListFilesAdapter.MyViewHolder>{
    Context context;
    ArrayList<FBaseModel> fm;

    public AdminListFilesAdapter(Context context){
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setArray(ArrayList<FBaseModel> fBaseModels){
        this.fm = fBaseModels;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<FBaseModel> filterList) {
        this.fm = filterList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FBaseModel model = fm.get(position);

        holder.fileName.setText(model.getFileName()+"");
        holder.fileSize.setText(Extras.fileSizeWithUnit(Long.parseLong(model.getFileSize())));
        holder.fileTime.setText(TimeUtils.setTimeTvForRecycler(Long.parseLong(model.getId())));

        holder.fileTap.setOnClickListener(v -> {
            String url = model.getFileUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        });

        holder.moreBtn.setOnClickListener(v -> openOptionMenuTransfer(holder.moreBtn,position));
    }

    public void openOptionMenuTransfer(View v,int position){
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.delete_drop_down, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Delete File")){
                String fileUrl = fm.get(position).getFileUrl();
                if (URLUtil.isValidUrl(fileUrl)){
                    if (!Extras.urlExist(fileUrl)){
                        Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                        notifyItemRangeRemoved(position,1);
                        return false;
                    }
                }
                FirebaseDatabase.getInstance().getReference().child("files")
                        .child(fm.get(position).getUid())
                        .child(fm.get(position).getId()+"").removeValue();
                FirebaseStorage.getInstance().getReferenceFromUrl(fm.get(position).getFileUrl()).delete();
                notifyItemRangeRemoved(position,1);
                Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return fm.size();
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
