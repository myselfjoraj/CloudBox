package jr.project.cloudbox.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Objects;

public class StorageUtility {

    Context context;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth    mAuth   = FirebaseAuth.getInstance();

    public StorageUtility(Context context){
        this.context = context;
    }

    @SuppressLint("All")
    public void uploadDocuments(){
        String id = mAuth.getCurrentUser().getUid();
        if (id == null){
            Toast.makeText(context, "Please login again!", Toast.LENGTH_SHORT).show();
            return;
        }
        storage.getReference().child(id).child("files");
    }

    interface uploadListener{
        void progress(int progress);
    }


}
