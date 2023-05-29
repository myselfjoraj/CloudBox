package jr.project.cloudbox.database;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class CloudBoxFirebaseDatabase {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static void updateUserInfo(){

    }

    public static void updateMyUsedSpace(long space){

        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null){
            return;
        }
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(myUid).child("used").setValue(space);
    }

    public static void updateMyUsedSpace(long space,String type){

        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null){
            return;
        }
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(myUid).child(type);
        // get current used space
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    long current = snapshot.getValue(Long.class);
                    dbRef.setValue(current+space);
                }else {
                    dbRef.setValue(space);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

}
