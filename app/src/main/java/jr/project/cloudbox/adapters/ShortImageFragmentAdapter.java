package jr.project.cloudbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import jr.project.cloudbox.MainActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.activities.AudioViewerActivity;
import jr.project.cloudbox.activities.ImageViewerActivity;
import jr.project.cloudbox.activities.PdfViewerActivity;
import jr.project.cloudbox.activities.VideoViewerActivity;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.utils.Extras;

public class ShortImageFragmentAdapter extends RecyclerView.Adapter<ShortImageFragmentAdapter.MyViewHolder>{

    Context context;
    ArrayList<FileModel> fileModels;

    public ShortImageFragmentAdapter(Context context){
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setArray(ArrayList<FileModel> fileModels){
        this.fileModels = fileModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_holder, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FileModel f = fileModels.get(position);

        if (f.getFileType().equals("image")){
            holder.fName.setVisibility(View.GONE);
            Glide.with(context)
                    .load(f.getUriVal())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.iv);
            if (f.getUrl()!=null){
                Glide.with(context)
                        .load(f.getUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(holder.iv);
            }
        }else if (f.getFileType().equals("video")){
            holder.fName.setText(f.getFileName()+"");
            holder.fName.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(R.drawable.placeholder_video)
                    .into(holder.iv);
        }else if (f.getFileType().equals("audio")){
            holder.fName.setText(f.getFileName()+"");
            holder.fName.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(R.drawable.placeholder_audio)
                    .into(holder.iv);
        }else {
            holder.fName.setText(f.getFileName()+"");
            holder.fName.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(R.drawable.placeholder_document)
                    .into(holder.iv);
        }

        holder.iv.setOnClickListener(v -> {
            transferActivity(position);
        });
    }

    @Override
    public int getItemCount() {
        return fileModels.size();
    }

    public void transferActivity(int position){
        ArrayList<FileModel> fileArray = fileModels;
        String type = fileArray.get(position).getFileType();
        String ext  = fileArray.get(position).getMimeType();

        String fileUrl = fileArray.get(position).getUrl();
        if (URLUtil.isValidUrl(fileUrl)){
            if (!Extras.urlExist(fileUrl)){
                Toast.makeText(context, "File has been removed from server!", Toast.LENGTH_SHORT).show();
                notifyItemRangeRemoved(position,1);
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


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView iv;
        TextView fName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.image);
            fName = itemView.findViewById(R.id.fileName);
        }
    }
}
