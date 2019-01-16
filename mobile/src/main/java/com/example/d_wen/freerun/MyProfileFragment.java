package com.example.d_wen.freerun;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.support.constraint.Constraints.TAG;


public class MyProfileFragment extends Fragment {


private static final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private static final DatabaseReference profileGetRef=database.getReference("profiles");


    public static final String USER_ID = "USER_ID";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String USER_PROFILE = "USER_PROFILE";

    private static final int EDIT_PROFILE_INFO = 1;

    private String groupName;
    private String groupNameJoin;


    private String userID;
    private Profile userProfile;
    private View fragmentView;
    private boolean changePassword = false;

    private FirebaseAuth mAuth;

    private OnFragmentInteractionListener mListener;

    public MyProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        Intent intent = getActivity().getIntent();
        userID = intent.getExtras().getString(USER_ID);
        readUserProfile();


        Button createGroupButton = fragmentView.findViewById(R.id.createGroupeButton);
        createGroupButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                EditText groupNameEditText = fragmentView.findViewById(R.id.editTextGroup);
                groupName = groupNameEditText.getText().toString();

                 final DatabaseReference groupeGetRef=database.getReference("Groupes/"+groupName);
                 final DatabaseReference groupeRef = groupeGetRef;
                 profileGetRef.child(userID).runTransaction(new Transaction.Handler(){

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData
                    mutableData) {

                        mutableData.child("groupe").setValue(groupName);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    }
                });

                groupeRef.runTransaction(new Transaction.Handler() {



                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData
                                                                    mutableData) {


                        mutableData.child("group_name").setValue(groupName);
                        mutableData.child("participants").child(userProfile.username);
                        mutableData.child("Participants").child(userProfile.username).child("score_each_km").setValue("");
                        mutableData.child("Participants").child(userProfile.username).child("score_in _tot").setValue("");

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    }
                });
            }
        });

       /* Button joinGroupButton = fragmentView.findViewById(R.id.joinButton);
        joinGroupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View va){
                EditText groupNameEditText = fragmentView.findViewById(R.id.editTextGroupJoin);
                groupNameJoin = groupNameEditText.getText().toString();



                DatabaseReference groupeJoinGetRef=database.getReference("Groupes/"+groupNameJoin);
                DatabaseReference groupeJoinRef = groupeJoinGetRef.push();

                groupeJoinRef.runTransaction(new Transaction.Handler() {


                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData
                                                                    mutableData) {
                        mutableData.child("participants").child(userProfile.username);
                        mutableData.child("participants").child(userProfile.username).child("score_in _tot").setValue("");
                        mutableData.child("Participants").child(userProfile.username).child("score_each_km").setValue("");
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    }
               });




                // ...

            }
        });*/

        return fragmentView;
    }





    /*public void findGroup(final String monGroupe){
        groupeGetRef.orderByKey().addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                if (monGroupe==dataSnapshot.getValue().toString()){

                    groupeGetRef.runTransaction(new Transaction.Handler() {


                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData
                                                                        mutableData) {
                            mutableData.child(monGroupe).child("participants").child("Participant1").setValue(userProfile.username);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        }
                    });
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            // ...
        });
    }
*/
    private void readUserProfile() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");
        profileRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user_db=dataSnapshot.child("username").getValue(String.class);
                int height_db=dataSnapshot.child("height").getValue(int.class);
                float weight_db=dataSnapshot.child("weight").getValue(float.class);
                String photo=dataSnapshot.child("photo").getValue(String.class);

                userProfile=new Profile();
                userProfile.username=user_db;
                userProfile.height=height_db;
                userProfile.weight=weight_db;
                userProfile.photoPath=photo;

                setUserImageAndProfileInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_my_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_user:
                changePassword = false;
                Intent intentEditProfile = new Intent(getActivity(), RegisterActivity.class);
                intentEditProfile.putExtra(USER_ID, userID);
                intentEditProfile.putExtra(CHANGE_PASSWORD, changePassword);
                startActivityForResult(intentEditProfile, EDIT_PROFILE_INFO);
                break;
            case R.id.edit_password:
                changePassword = true;
                Intent intentEditPassword = new Intent(getActivity(), RegisterActivity.class);
                intentEditPassword.putExtra(USER_ID, userID);
                intentEditPassword.putExtra(CHANGE_PASSWORD, changePassword);
                startActivity(intentEditPassword);
                break;
            case R.id.logout:
                mAuth.signOut();
                Intent intentLogout = new Intent(getActivity(),LoginActivity.class);
                startActivity(intentLogout);
        }

        return super.onOptionsItemSelected(item);
    }

   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_INFO && resultCode == AppCompatActivity.RESULT_OK) {
            userProfile = (Profile) data.getSerializableExtra(USER_PROFILE);
            if (userProfile != null) {
                setUserImageAndProfileInfo();
            }
        }
    }

    private void setUserImageAndProfileInfo() {
        //  Reference to an image file in Firebase Storage
        StorageReference storageRef=FirebaseStorage.getInstance().getReferenceFromUrl
                (userProfile.photoPath);
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (isAdded()) {
                    final Bitmap selectedImage=BitmapFactory.decodeByteArray(bytes, 0, bytes
                            .length);
                    ImageView imageView = fragmentView.findViewById(R.id.userImageValue);
                    imageView.setImageBitmap(selectedImage);
                }
            }
        });

        TextView usernameTextView=fragmentView.findViewById(R.id.usernameValue);
        usernameTextView.setText(userProfile.username);

        TextView heightTextView=fragmentView.findViewById(R.id.heightValue);
        heightTextView.setText(String.valueOf(userProfile.height));

        TextView weightTextView=fragmentView.findViewById(R.id.weightValue);
        weightTextView.setText(String.valueOf(userProfile.weight));
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener=(OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener=null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
