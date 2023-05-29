package jr.project.cloudbox.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import jr.project.cloudbox.R;
import jr.project.cloudbox.SplashScreenActivity;
import jr.project.cloudbox.adapters.UsersListAdapter;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.models.UserModel;
import jr.project.cloudbox.utils.Constants;

public class AdminActivity extends AppCompatActivity {

    ArrayList<UserModel> users;
    UsersListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        users = new ArrayList<>();

        adapter = new UsersListAdapter(this);

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    users.add(user);
                }
                adapter.addUsersArray(users);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});

        RecyclerView recyclerView = findViewById(R.id.usersRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.uploadPlusTop).setOnClickListener(v -> {
            openOptionMenuTransfer(findViewById(R.id.uploadPlusTop));
        });

        EditText et = findViewById(R.id.search);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String search = s.toString();
                if (search.length()>0){
                    filter(search);
                }else {
                    filter(null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}});




    }

    private void filter(String text) {
        ArrayList<UserModel> filteredList = new ArrayList<>();
        if (text == null){
            adapter.filterList(users);
            return;
        }
        for (UserModel item : users) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        }
    }

    public void openOptionMenuTransfer(View v){
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.logout_drop_down, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Log Out")){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AdminActivity.this, SplashScreenActivity.class));
            }
            return true;
        });
        popup.show();
    }
}