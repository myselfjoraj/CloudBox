package jr.project.cloudbox.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.cloudbox.CloudBoxApplication;
import jr.project.cloudbox.adapters.RecentActivityAdapter;
import jr.project.cloudbox.adapters.TransfersFragmentAdapter;
import jr.project.cloudbox.database.CloudBoxOfflineDatabase;
import jr.project.cloudbox.database.SharedPreferences;
import jr.project.cloudbox.databinding.FragmentTaskBinding;
import jr.project.cloudbox.listeners.OnCBRecyclerClick;
import jr.project.cloudbox.models.FileModel;
import jr.project.cloudbox.service.UploadService;
import jr.project.cloudbox.utils.Constants;
import jr.project.cloudbox.utils.Extras;
import jr.project.cloudbox.utils.TimeUtils;

public class TaskFragment extends Fragment {

    public TaskFragment() {
        // Required empty public constructor
    }

    FragmentTaskBinding      binding;
    CloudBoxOfflineDatabase  mDatabase;
    ArrayList<FileModel>     fileHistoryArray, fileTransferArray;
    UploadTask               uploadTask;
    FirebaseStorage          mStorage;
    FirebaseAuth             mAuth;
    RecentActivityAdapter    adapter_all_files;
    TransfersFragmentAdapter adapter_transfers;
    SharedPreferences        mPref;
    CloudBoxApplication      mApp;
    android.content.SharedPreferences.OnSharedPreferenceChangeListener listener;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTaskBinding.inflate(inflater, container, false);

        mDatabase = new CloudBoxOfflineDatabase(requireContext());
        mStorage  = FirebaseStorage.getInstance();
        mAuth     = FirebaseAuth.getInstance();
        mPref     = new SharedPreferences(requireContext());

        adapter_all_files   = new RecentActivityAdapter(requireContext());
        adapter_transfers   = new TransfersFragmentAdapter(requireContext());
        fileHistoryArray    = new ArrayList<>();
        fileTransferArray   = new ArrayList<>();

        mApp = (CloudBoxApplication) requireActivity().getApplication();

        binding.transferHistoryRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.transferHistoryRecycler.setAdapter(adapter_all_files);

        binding.currentUploadsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.currentUploadsRecycler.setAdapter(adapter_transfers);

        // Toast.makeText(getContext(), "->"+mDatabase.getAllTransferDetails().size(), Toast.LENGTH_SHORT).show();
        setUpTransferHistoryRecycler();
        setUpCurrentTransferRecycler();

        listener = (sharedPreferences, key) -> {
            if (key.startsWith("currentTransferredSize")){
                setUpCurrentTransferRecycler();
                for (int i = 0; i<fileTransferArray.size();i++){
                    String sId =  fileTransferArray.get(i).getsId()+"";
                    String keyId = key.replace("currentTransferredSize-","");
                    if (sId.equals(keyId)){
                        adapter_transfers.notifyItemChanged(i);
                    }
                }

            }
        };

        adapter_transfers.setProgressCompletedListener(position -> {
            setUpCurrentTransferRecycler();
            setUpTransferHistoryRecycler();
        });

        adapter_transfers.setOnCBClickListener((position, view) -> {
            long fileId = fileTransferArray.get(position).getFileId();
            UploadTask uploadTask1 = mApp.getUploadTask(fileId);
            if (view == Constants.BTN_PLAY_PAUSE){
                if (uploadTask1.isPaused()){
                    adapter_transfers.setResumed(position);
                }else {
                    uploadTask1.pause();
                    adapter_transfers.setPaused(position);
                }
            }else if (view == Constants.BTN_CANCEL){
                uploadTask1.cancel();
                String a = Constants.TYPE_DOWNLOAD;
                if (fileTransferArray.get(position).isUpload()){
                    a = Constants.TYPE_UPLOAD;
                }
                mDatabase.updateState(
                        fileId,
                        Constants.TRANSFER_CANCELLED,
                        TimeUtils.getTimestamp(),a
                );
                adapter_transfers.removeProcess(position);
                if (fileTransferArray.size()>position) {
                    Toast.makeText(requireContext(),
                            "File upload cancelled for " +
                            fileTransferArray.get(position).getFileName(),
                            Toast.LENGTH_SHORT
                    ).show();
                    fileTransferArray.remove(position);
                }
                mApp.removeUploadTask(fileId);
                setUpCurrentTransferRecycler();
            }
        });

        binding.etTransfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String search = s.toString();
                if (search.length()>0){
                    binding.cUploadCard.setVisibility(View.GONE);
                    binding.noTransfersView.setVisibility(View.GONE);
                    filter(search);
                }else {
                    filter(null);
                    binding.cUploadCard.setVisibility(View.VISIBLE);
                    binding.noTransfersView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return binding.getRoot();
    }

    private void filter(String text) {
        ArrayList<FileModel> filteredList = new ArrayList<>();
        if (text == null){
            adapter_all_files.filterList(fileHistoryArray);
            return;
        }
        for (FileModel item : fileHistoryArray) {
            if (item.getFileName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter_all_files.filterList(filteredList);
        }
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

    public void setUpTransferHistoryRecycler(){
        if (fileHistoryArray.size()>0) {
            fileHistoryArray.clear();
        }

        fileHistoryArray = mDatabase.getAllTransferDetails();

        if (fileHistoryArray.size() == 0){
            binding.transferHistoryRecycler.setVisibility(View.GONE);
            binding.noUploads.setVisibility(View.VISIBLE);
        }else {
            binding.transferHistoryRecycler.setVisibility(View.VISIBLE);
            binding.noUploads.setVisibility(View.GONE);

            adapter_all_files.isAllTransferTask(true);
            adapter_all_files.addToRecentAdapter(fileHistoryArray);
        }
    }

    public void setUpCurrentTransferRecycler(){
        if (fileTransferArray.size()>0) {
            fileTransferArray.clear();
        }
        fileTransferArray = mDatabase.getCurrentTransferDetails();
        /*
        ArrayList<Integer> ar = new ArrayList<>();
        for (int i = 0 ; i<fileTransferArray.size();i++){
            UploadTask up = mApp.getUploadTask(fileTransferArray.get(i).getFileId());
            if (up != null){
                if (up.isPaused()) {
                    fileTransferArray.get(i).setPaused(true);
                }
            }else {
                ar.add(i);
            }
        }

        for (int i = 0;i<ar.size();i++){
            mDatabase.updateState(
                    fileTransferArray.get(i).getFileId(),
                    Constants.TRANSFER_FAILED,
                    TimeUtils.getTimestamp()
            );
            fileTransferArray.remove(i);
        }
        */
        if (fileTransferArray.size() == 0){
            binding.currentUploadsRecycler.setVisibility(View.GONE);
            binding.noTransfersView.setVisibility(View.VISIBLE);
        }else {
            binding.currentUploadsRecycler.setVisibility(View.VISIBLE);
            binding.noTransfersView.setVisibility(View.GONE);
            adapter_transfers.addToFileArray(fileTransferArray);
        }
    }





}