package com.example.d_wen.freerun;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.example.d_wen.freerun.MyProfileFragment.USER_PROFILE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LeaderboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View fragmentView;
    private Query query;
    private String userID;

    private Profile scoreProfile;
    private Profile scoreChamp;

    public static final String USER_ID = "USER_ID";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LeaderboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LeaderboardFragment newInstance(String param1, String param2) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sortUserScrore();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        Intent intent=getActivity().getIntent();
        userID = intent.getExtras().getString(USER_ID);
        getUserProfile();


        return fragmentView;

    }

    public void sortUserScrore(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference scoresRef = database.getReference("profiles");
        scoresRef.orderByChild("total_score").limitToLast(3).addChildEventListener(new ChildEventListener() {
            int i =0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                if (i==2) {
                    Profile champ1 = dataSnapshot.getValue(Profile.class);
                /*System.out.println(dataSnapshot.getKey() + " was " + champ.weight + " meters tall.");
                float weight_db = dataSnapshot.getValue(float.class);

                    scoreChamp = new Profile();
                    scoreChamp.weight = weight_db;*/

                    TextView scoreChamp1 = fragmentView.findViewById(R.id.textViewScoreChamp1);
                    scoreChamp1.setText(String.valueOf(champ1.total_score));
                    TextView nameChamp1 = fragmentView.findViewById(R.id.textViewChamp1);
                    nameChamp1.setText(champ1.username);
                    i=i+1;
                }
                if (i==1) {
                    Profile champ2 = dataSnapshot.getValue(Profile.class);
                /*System.out.println(dataSnapshot.getKey() + " was " + champ.weight + " meters tall.");
                float weight_db = dataSnapshot.getValue(float.class);

                    scoreChamp = new Profile();
                    scoreChamp.weight = weight_db;*/

                    TextView scoreChamp2 = fragmentView.findViewById(R.id.textViewScoreChamp2);
                    scoreChamp2.setText(String.valueOf(champ2.total_score));
                    TextView nameChamp2 = fragmentView.findViewById(R.id.textViewChamp2);
                    nameChamp2.setText(champ2.username);
                    i=i+1;
                }


                if (i==0) {
                    Profile champ3 = dataSnapshot.getValue(Profile.class);
                /*System.out.println(dataSnapshot.getKey() + " was " + champ.weight + " meters tall.");
                float weight_db = dataSnapshot.getValue(float.class);

                    scoreChamp = new Profile();
                    scoreChamp.weight = weight_db;*/

                    TextView scoreChamp3 = fragmentView.findViewById(R.id.textViewScoreChamp3);
                    scoreChamp3.setText(String.valueOf(champ3.total_score));
                    TextView nameChamp3 = fragmentView.findViewById(R.id.textViewChamp3);
                    nameChamp3.setText(champ3.username);
                    i=i+1;
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


      private void getUserProfile() {
          final FirebaseDatabase database = FirebaseDatabase.getInstance();
          final DatabaseReference profileRef = database.getReference("profiles");
          profileRef.child(userID).addValueEventListener(new ValueEventListener() {
             @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 String user_db = dataSnapshot.child("username").getValue(String.class);
                 double weight_db = dataSnapshot.child("total_score").getValue(double.class);

                 scoreProfile = new Profile();
                 scoreProfile.username = user_db;
                 scoreProfile.total_score = weight_db;


                 TextView playerName=fragmentView.findViewById(R.id.textViewPlayer);
                 playerName.setText(scoreProfile.username);

                 TextView playerWeight=fragmentView.findViewById(R.id.textViewScorePlayer);
                 playerWeight.setText(String.valueOf(scoreProfile.total_score));
             }
            @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

                }

        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
