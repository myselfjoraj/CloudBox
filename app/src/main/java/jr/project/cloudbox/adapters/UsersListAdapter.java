package jr.project.cloudbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import jr.project.cloudbox.R;
import jr.project.cloudbox.admin.AdminAllFilesActivity;
import jr.project.cloudbox.admin.AdminUserProfileActivity;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.models.UserModel;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.MyViewHolder> {

    ArrayList<UserModel> users = new ArrayList<>();
    Context context;

    @SuppressLint("NotifyDataSetChanged")
    public void addUsersArray(ArrayList<UserModel> mModel){
        this.users = mModel;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<UserModel> filterList) {
        this.users = filterList;
        notifyDataSetChanged();
    }

    public UsersListAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        UserModel user = users.get(position);

        Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(holder.profileImage);

        holder.name.setText(user.getName()+"");
        holder.uid.setText("UID : "+user.getUid());
        holder.email.setText(user.getEmail()+"");

        holder.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserProfileActivity.class);
            intent.putExtra("model",user);
            context.startActivity(intent);
        });

        holder.cross.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserProfileActivity.class);
            intent.putExtra("model",user);
            context.startActivity(intent);
        });

        holder.tap.setOnClickListener(v -> getFilesArray(user.getUid()));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profileImage,cross;
        TextView name,email,uid;
        LinearLayout tap;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // iv
            profileImage = itemView.findViewById(R.id.profilePic);
            cross        = itemView.findViewById(R.id.cross);
            // tv
            name = itemView.findViewById(R.id.name);
            uid  = itemView.findViewById(R.id.uid);
            email= itemView.findViewById(R.id.email);
            //ll
            tap  = itemView.findViewById(R.id.tap);
        }
    }

    void getFilesArray(String uid){

        FirebaseDatabase.getInstance().getReference().child("files").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<FBaseModel> fm = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FBaseModel model = snapshot.getValue(FBaseModel.class);
                            model.setUid(uid);
                            fm.add(model);
                        }
                        Intent intent = new Intent(context, AdminAllFilesActivity.class);
                        intent.putExtra("model",fm);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
