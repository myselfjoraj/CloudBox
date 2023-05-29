package jr.project.cloudbox.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.util.ArrayList;

import jr.project.cloudbox.R;
import jr.project.cloudbox.adapters.AdminListFilesAdapter;
import jr.project.cloudbox.databinding.ActivityAdminAllFilesBinding;
import jr.project.cloudbox.models.FBaseModel;
import jr.project.cloudbox.models.UserModel;

public class AdminAllFilesActivity extends AppCompatActivity {

    ActivityAdminAllFilesBinding binding;
    AdminListFilesAdapter adapter;
    ArrayList<FBaseModel> fBaseModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAllFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fBaseModels = (ArrayList<FBaseModel>) getIntent().getSerializableExtra("model");

        adapter = new AdminListFilesAdapter(this);
        adapter.setArray(fBaseModels);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        if (fBaseModels.size()>0){
            findViewById(R.id.noView).setVisibility(View.GONE);
        }else {
            findViewById(R.id.noView).setVisibility(View.VISIBLE);
        }

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


    }

    private void filter(String text) {
        ArrayList<FBaseModel> filteredList = new ArrayList<>();
        if (text == null){
            adapter.filterList(fBaseModels);
            return;
        }
        for (FBaseModel item : fBaseModels) {
            if (item.getFileName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        }
    }
}