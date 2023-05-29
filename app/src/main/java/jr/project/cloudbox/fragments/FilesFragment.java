package jr.project.cloudbox.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.cloudbox.activities.ListFilesActivity;
import jr.project.cloudbox.activities.PdfViewerActivity;
import jr.project.cloudbox.R;
import jr.project.cloudbox.adapters.ShortImageFragmentAdapter;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.databinding.FragmentFilesBinding;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.utils.Extras;

public class FilesFragment extends Fragment {

    public FilesFragment() {
        // Required empty public constructor
    }

    FragmentFilesBinding      binding;
    SharedPreferences         mPref;
    CloudBoxOfflineDatabase   mDatabase;
    ArrayList<FileModel>      imageArray,videoArray,audioArray,otherArray;
    ShortImageFragmentAdapter imageFragmentAdapter,videoFragmentAdapter,audioFragmentAdapter,
                              othersFragmentAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFilesBinding.inflate(inflater,container,false);

        requireActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mPref     = new SharedPreferences(requireContext());
        mDatabase = new CloudBoxOfflineDatabase(requireContext());

        imageFragmentAdapter  = new ShortImageFragmentAdapter(requireContext());
        videoFragmentAdapter  = new ShortImageFragmentAdapter(requireContext());
        audioFragmentAdapter  = new ShortImageFragmentAdapter(requireContext());
        othersFragmentAdapter = new ShortImageFragmentAdapter(requireContext());

        imageArray = mDatabase.getAllImageDetails();
        videoArray = mDatabase.getAllVideoDetails();
        audioArray = mDatabase.getAllAudioDetails();
        otherArray = mDatabase.getAllApplicationDetails();

        // current file distributed sizes
        populateCurrentFileSizeStatus();

        // visibility for views
        populateViewsForRecycler();

        // listen for file size changes
        mPref.registerListener((sharedPreferences, key) -> {
            if (key.startsWith("used")){
                populateCurrentFileSizeStatus();
            }
        });

        // view all files
        binding.topAll.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","files");
            startActivity(intent);
        });


        binding.allImages.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","image");
            startActivity(intent);
        });

        binding.allVideos.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","video");
            startActivity(intent);
        });

        binding.allAudios.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","audio");
            startActivity(intent);
        });

        binding.allOthers.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","application");
            startActivity(intent);
        });


        //images
        binding.imagesRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false)
        );
        binding.imagesRecycler.setAdapter(imageFragmentAdapter);
        imageFragmentAdapter.setArray(imageArray);
        binding.viewAllImages.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","image");
            startActivity(intent);
        });

        //videos
        binding.videosRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false)
        );
        binding.videosRecycler.setAdapter(videoFragmentAdapter);
        videoFragmentAdapter.setArray(videoArray);
        binding.viewAllVideo.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","video");
            startActivity(intent);
        });

        //audios
        binding.audiosRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false)
        );
        binding.audiosRecycler.setAdapter(audioFragmentAdapter);
        audioFragmentAdapter.setArray(audioArray);
        binding.viewAllAudio.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","audio");
            startActivity(intent);
        });

        //others
        binding.othersRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false)
        );
        binding.othersRecycler.setAdapter(othersFragmentAdapter);
        othersFragmentAdapter.setArray(otherArray);
        binding.viewAllOther.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFilesActivity.class);
            intent.putExtra("type","application");
            startActivity(intent);
        });

        if (mPref.getUsedStorage() == 0){
            binding.noItemsLayout.setVisibility(View.VISIBLE);
        }else {
            binding.noItemsLayout.setVisibility(View.GONE);
        }

        binding.editTextTextPersonName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String search = s.toString();
                if (search.length()>0){
                    Intent intent = new Intent(getContext(), ListFilesActivity.class);
                    intent.putExtra("type","files");
                    intent.putExtra("txt",search);
                    binding.editTextTextPersonName.setText("");
                    startActivity(intent);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mPref!=null && binding!=null && mPref.getUsedStorage() == 0){
            binding.noItemsLayout.setVisibility(View.VISIBLE);
            binding.imagesToolBar.setVisibility(View.GONE);
            binding.imagesRecycler.setVisibility(View.GONE);
            binding.videoToolBar.setVisibility(View.GONE);
            binding.videosRecycler.setVisibility(View.GONE);
            binding.audioToolBar.setVisibility(View.GONE);
            binding.audiosRecycler.setVisibility(View.GONE);
            binding.otherToolBar.setVisibility(View.GONE);
            binding.othersRecycler.setVisibility(View.GONE);
        }else {
            if (binding!=null) {
                binding.noItemsLayout.setVisibility(View.GONE);
            }
        }
    }

    public void populateCurrentFileSizeStatus(){
        binding.imagesSize.setText(Extras.fileSize(mPref.getImageTotalSize()));
        binding.videoSize.setText(Extras.fileSize(mPref.getVideoTotalSIze()));
        binding.audioSize.setText(Extras.fileSize(mPref.getAudioTotalSize()));
        binding.otherSize.setText(Extras.fileSize(mPref.getOtherTotalSize()));
    }

    public void populateViewsForRecycler(){
        // if image exists
        if (imageArray.size()>0){
            binding.imagesToolBar.setVisibility(View.VISIBLE);
            binding.imagesRecycler.setVisibility(View.VISIBLE);
        }else {
            binding.imagesToolBar.setVisibility(View.GONE);
            binding.imagesRecycler.setVisibility(View.GONE);
        }

        // if video exists
        if (videoArray.size()>0){
            binding.videoToolBar.setVisibility(View.VISIBLE);
            binding.videosRecycler.setVisibility(View.VISIBLE);
        }else {
            binding.videoToolBar.setVisibility(View.GONE);
            binding.videosRecycler.setVisibility(View.GONE);
        }

        // if audio exists
        if (audioArray.size()>0){
            binding.audioToolBar.setVisibility(View.VISIBLE);
            binding.audiosRecycler.setVisibility(View.VISIBLE);
        }else {
            binding.audioToolBar.setVisibility(View.GONE);
            binding.audiosRecycler.setVisibility(View.GONE);
        }

        // if other exists
        if (otherArray.size()>0){
            binding.otherToolBar.setVisibility(View.VISIBLE);
            binding.othersRecycler.setVisibility(View.VISIBLE);
        }else {
            binding.otherToolBar.setVisibility(View.GONE);
            binding.othersRecycler.setVisibility(View.GONE);
        }

    }

    public void showFolderCreationBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_create_folder);

        Button ok = bottomSheetDialog.findViewById(R.id.okBtn);
        Button cancel = bottomSheetDialog.findViewById(R.id.cancelBtn);

        Objects.requireNonNull(ok).setOnClickListener(v -> createFolder(bottomSheetDialog));

        Objects.requireNonNull(cancel).setOnClickListener(v -> bottomSheetDialog.cancel());

        bottomSheetDialog.show();
    }

    public void createFolder(BottomSheetDialog dialog){

    }
}