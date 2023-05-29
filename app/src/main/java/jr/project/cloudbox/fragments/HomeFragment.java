package jr.project.cloudbox.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import jr.project.cloudbox.activities.ListFilesActivity;
import jr.project.cloudbox.adapters.RecentActivityAdapter;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.databinding.FragmentHomeBinding;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.service.UploadService;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.Extras;
import jr.project.cloudbox.utils.FileUtils;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    FragmentHomeBinding            binding;
    UploadTask                     uploadTask;
    ActivityResultLauncher<Intent> resultLauncher;
    CloudBoxOfflineDatabase        mDatabase;
    FirebaseStorage                mStorage;
    StorageReference               mReference;
    SharedPreferences              mPref;
    FileUtils                      fileUtils;
    String                         uid;
    RecentActivityAdapter          adapter;
    ArrayList<FileModel>           fileArray;

    android.content.SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);

        mStorage  = FirebaseStorage.getInstance();
        uid       = FirebaseAuth.getInstance().getUid();
        fileUtils = new FileUtils(getContext());
        mDatabase = new CloudBoxOfflineDatabase(getContext());
        mPref     = new SharedPreferences(requireContext());
        adapter   = new RecentActivityAdapter(getContext());
        fileArray = new ArrayList<>();

        /*  set up text views  */

        // set used storage
        binding.usedStorageTV.setText(
                Extras.fileSize((long) mPref.getUsedStorage())
        );

        //set recycler view
        binding.recentFilesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recentFilesRecycler.setAdapter(adapter);

        try {
            setUpRecentActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // listening for shared preference changes
        listener = (sharedPreferences, key) -> {
            if (key.equals("usedStorageInBytes")){
                // set used storage
                binding.usedStorageTV.setText(
                        Extras.fileSize((long) mPref.getUsedStorage())
                );
                //set recent activity
                setUpRecentActivity();
            }
        };

        setResultLauncher();

        binding.uploadPlusTop.setOnClickListener(v -> selectFiles(Constants.SELECT_ALL_FILES));

        binding.favTop.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), ListFilesActivity.class);
            i.putExtra("type","favourites");
            startActivity(i);
        });

        binding.fileSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
//                bottomSheetDialog.setContentView(R.layout.bottom_sheet_audio_player);
//                bottomSheetDialog.show();
                //startActivity(new Intent(getContext(), AdminActivity.class));
            }
        });

        binding.searchStorageFiles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String search = s.toString();
                if (search.length()>0){
                    binding.cardView6.setVisibility(View.GONE);
                    filter(search);
                }else {
                    filter(null);
                    binding.cardView6.setVisibility(View.VISIBLE);
                }
            }
        });

        return binding.getRoot();

    }




    @Override
    public void onResume() {
        super.onResume();
        // register for sPref changes
        mPref.registerListener(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister for sPref changes
        mPref.unregisterListener(listener);
    }

    private void selectFiles(int fileType) {
        String fileMime;

        switch (fileType){
            case Constants.SELECT_PDF:
                fileMime = "application/pdf";
                break;
            case Constants.SELECT_IMAGE:
                fileMime = "image/*";
                break;
            default:
                fileMime = "*/*";
                break;
        }

        // Initialize intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // set type
        intent.setType(fileMime);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Launch intent
        resultLauncher.launch(intent);
    }

    // Initialize result launcher
    @SuppressLint("WrongConstant")
    public void setResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Initialize result data
                    Intent data = result.getData();
                    // check condition
                    if (data != null) {
                        // Get uri
                        Uri sUri = data.getData();
//                        requireContext().getContentResolver()
//                                .getPersistedUriPermissions();
                        //uploadFile(sUri);
                        Intent uploadIntent = new Intent(getContext(), UploadService.class);
                        uploadIntent.putExtra("uri",sUri.toString());
                        requireContext().startService(uploadIntent);
                    }
                });
    }

    private void setUpRecentActivity(){
        fileArray.clear();
        fileArray = mDatabase.retrieveRecentActivity();
        if (fileArray.size()>0){
            binding.noItemsLayout.setVisibility(View.GONE);
            binding.recentFilesRecycler.setVisibility(View.VISIBLE);
            //setting adapter
            adapter.addToRecentAdapter(fileArray);
            // set progress of storage used
            binding.progressBar.setProgress((int) Extras.totalUsedPercent((long) mPref.getUsedStorage()));
        }else {
            binding.recentFilesRecycler.setVisibility(View.GONE);
            binding.noItemsLayout.setVisibility(View.VISIBLE);
        }

    }

    private void filter(String text) {
        ArrayList<FileModel> filteredList = new ArrayList<>();
        if (text == null){
            adapter.filterList(fileArray);
            return;
        }
        for (FileModel item : fileArray) {
            if (item.getFileName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        }
    }

   // uploadTask = mStorageRef.putFile(localFile,new StorageMetadata.Builder().build(), sessionUri);

}