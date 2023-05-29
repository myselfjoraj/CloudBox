package jr.project.cloudbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import jr.project.cloudbox.CloudBoxApplication;
import jr.project.cloudbox.R;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.listeners.OnCBItemClick;
import jr.project.cloudbox.listeners.OnCBRecyclerClick;
import jr.project.cloudbox.listeners.OnProcessCompleted;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.Extras;

public class TransfersFragmentAdapter extends RecyclerView.Adapter<TransfersFragmentAdapter.MyViewHolder>{

    ArrayList<FileModel> fileModels;
    Context context;
    OnProcessCompleted processCompleted;
    OnCBRecyclerClick  cbItemClick;

    public TransfersFragmentAdapter(Context context){
        this.context = context;
    }

    public void setProgressCompletedListener(OnProcessCompleted processCompleted){
        this.processCompleted = processCompleted;
    }

    public void setOnCBClickListener(OnCBRecyclerClick clickListener){
        this.cbItemClick = clickListener;
    }

    public void setPaused(int position){
        this.fileModels.get(position).setPaused(true);
        notifyItemChanged(position);
    }

    public void setResumed(int position){
        this.fileModels.get(position).setPaused(false);
        notifyItemChanged(position);
    }

    public void removeProcess(int position){
        if (fileModels.size()>position) {
            this.fileModels.remove(position);
            notifyItemRemoved(position);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<FileModel> fm){
        this.fileModels = fm;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addToFileArray(ArrayList<FileModel> fileModels){
        this.fileModels = fileModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransfersFragmentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_progress_holder, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransfersFragmentAdapter.MyViewHolder holder, int position) {
        FileModel fModel = fileModels.get(position);

        //load icon
        Glide.with(context)
                .load(drawableIcon(fModel.getMimeType()))
                .into(holder.icon);

        //set play or pause icon
        int ic;
        if (fModel.isPaused()){
            ic = R.drawable.play_filled;
        }else {
            ic = R.drawable.pause_filled_icon;
        }
        Glide.with(context)
                .load(ic)
                .into(holder.playPause);

        if (fModel.isUpload()){
            holder.uDIV.setRotation(90);
        }else {
            holder.uDIV.setRotation(-90);
        }

        FileModel f = new CloudBoxOfflineDatabase(context).getCurrentFileProgress(fModel.getFileId());
        //file name
        holder.fileName.setText(fModel.getFileName());
        //file progress
        holder.progressBar.setProgress(f.getProgress());
        // size transferred
        holder.fileSizeTransfer.setText(setTransferText(f.getUploadedSize(),fModel.getFileSize()));
        // transfer progress and time
        holder.transferProgress.setText(
                setProgressText(f.getProgress(),f.getTimeLeft(),fModel.isPaused())
        );
        // notify progress completed
        if (f.getProgress() == 100){
            notifyProgressCompletedWithDelay(position);
        }

        holder.playPause.setOnClickListener(v -> {
            cbItemClick.onClick(position, Constants.BTN_PLAY_PAUSE);
        });

        holder.cancel.setOnClickListener(v->{
            cbItemClick.onClick(position,Constants.BTN_CANCEL);
        });

    }


    @Override
    public int getItemCount() {
        return fileModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView   playPause,icon,cancel,uDIV;
        ProgressBar progressBar;
        TextView    fileName,fileSizeTransfer,transferProgress;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            //iv
            playPause = itemView.findViewById(R.id.playPause);
            icon      = itemView.findViewById(R.id.icon);
            cancel    = itemView.findViewById(R.id.cancel);
            uDIV      = itemView.findViewById(R.id.uploadOrDownload);
            //progress
            progressBar = itemView.findViewById(R.id.progressBar);
            //tv
            fileName         = itemView.findViewById(R.id.fileName);
            fileSizeTransfer = itemView.findViewById(R.id.fileTransferSize);
            transferProgress = itemView.findViewById(R.id.transferDet);
        }
    }

    public String setTransferText(double upSize,String fileSize){
        return Extras.fileSizeWithUnitOnFloat(upSize)+"/"+
                Extras.fileSizeWithUnitOnFloat(Long.parseLong(fileSize));
    }

    public String setProgressText(int progress,String time,boolean paused){
        if (paused){
            return progress + "% ( Transfer Paused )";
        }
        else if (time.startsWith("Infinity") || time.startsWith("-Infinity")){
            return progress + "% ( Connecting ... )";
        } else {
            return progress + "% ( " + time + " left )";
        }
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

    @SuppressLint("NotifyDataSetChanged")
    public void notifyProgressCompletedWithDelay(int position){
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (processCompleted!=null){
                processCompleted.onComplete(position);
            }
        }, 2000);
    }
}
