package jr.project.cloudbox.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import jr.project.cloudbox.R;
import jr.project.cloudbox.adapters.ListFilesAdapter;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.databinding.ActivityListFilesBinding;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.FileModel;

public class ListFilesActivity extends AppCompatActivity {

    ActivityListFilesBinding binding;
    ArrayList<FileModel> fileArray;
    String type;
    CloudBoxOfflineDatabase mDatabase;
    ListFilesAdapter filesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = new CloudBoxOfflineDatabase(ListFilesActivity.this);

        type = getIntent().getStringExtra("type");

        String text = getIntent().getStringExtra("txt");

        setTitleBar();
        loadFileArrayFromDatabase();

        filesAdapter = new ListFilesAdapter(ListFilesActivity.this);

        if (type.equals("offline")){
            filesAdapter.setMode("offline");
        }else if (type.equals("trash")){
            filesAdapter.setMode("trash");
        }

        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        ListFilesActivity.this, LinearLayoutManager.VERTICAL,false
                )
        );

        binding.recyclerView.setAdapter(filesAdapter);

        if (fileArray!=null){
            filesAdapter.setArray(fileArray);
        }

        if (fileArray!=null && fileArray.size()>0){
            binding.noView.setVisibility(View.GONE);
        }else {
            binding.noView.setVisibility(View.VISIBLE);
        }

        filesAdapter.setLastItemListener(() -> {
            binding.noView.setVisibility(View.VISIBLE);
        });

        binding.backBtn.setOnClickListener(v -> finish());

        binding.searchStorageFiles.addTextChangedListener(new TextWatcher() {
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

        if (text!=null && text.length()>0){
            binding.searchStorageFiles.setText(text);
            filter(text);
            binding.searchStorageFiles.requestFocus();
        }

    }

    private void filter(String text) {
        ArrayList<FileModel> filteredList = new ArrayList<>();
        if (text == null){
            filesAdapter.filterList(fileArray);
            return;
        }
        for (FileModel item : fileArray) {
            if (item.getFileName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            filesAdapter.filterList(filteredList);
        }
    }

    public void loadFileArrayFromDatabase(){
        switch (type) {
            case "image":
                fileArray = mDatabase.getAllImageDetails();
                break;
            case "video":
                fileArray = mDatabase.getAllVideoDetails();
                break;
            case "audio":
                fileArray = mDatabase.getAllAudioDetails();
                break;
            case "application":
                fileArray = mDatabase.getAllApplicationDetails();
                break;
            case "files":
                fileArray = mDatabase.getAllFilesDetails();
                break;
            case "trash":
                fileArray = mDatabase.getAllTrashDetails();
                break;
            case "offline":
                fileArray = mDatabase.getAllOfflineDetails();
                break;
            case "favourites":
                fileArray = mDatabase.getAllFavouritesDetails();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void setTitleBar(){
        if (type == null){
            return;
        }
        binding.favIconAct.setVisibility(View.GONE);
        switch (type) {
            case "image":
                binding.titleName.setText("Images");
                break;
            case "video":
                binding.titleName.setText("Videos");
                break;
            case "audio":
                binding.titleName.setText("Audios");
                break;
            case "application":
                binding.titleName.setText("Files");
                break;
            case "trash":
                binding.titleName.setText("Trash Bin");
                break;
            case "offline":
                binding.titleName.setText("Offline Files");
                break;
            case "favourites":
                binding.titleName.setText("Favourites");
                binding.favIconAct.setVisibility(View.VISIBLE);
                break;
            default:
                binding.titleName.setText("All Files");
                break;
        }
    }
}