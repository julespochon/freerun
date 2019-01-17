package com.example.d_wen.freerun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import static com.example.d_wen.freerun.MyProfileFragment.USER_ID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RunPreparationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RunPreparationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunPreparationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final String TAG = this.getClass().getSimpleName();

    public static final String RECORDIND_ID = "recID";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private int aimedPace = 0;
    private int plannedDistance = 0;
    private String recordingKeySaved;

    private static final int BLE_CONNECTION = 1;
    private String deviceAddress;
    private Switch switchHRbelt;
    private SwitchBeltOnCheckedChangeListener switchBeltOnCheckedChangeListener;

    public RunPreparationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RunPreparationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RunPreparationFragment newInstance(String param1, String param2) {
        RunPreparationFragment fragment = new RunPreparationFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_run_preparation,
                container, false);

        // BLE connection
        switchBeltOnCheckedChangeListener = new SwitchBeltOnCheckedChangeListener();
        switchHRbelt = fragmentView.findViewById(R.id.switchHRBelt);

        EditText paceEntry = fragmentView.findViewById(R.id.pace);
        EditText distanceEntry = fragmentView.findViewById(R.id.distance);
        final TextView runDuration = fragmentView.findViewById(R.id.calculatedTime);

        paceEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    aimedPace = 0;
                }
                else {
                    int number = Integer.parseInt(s.toString());
                    aimedPace = number;
                }
                int hours = aimedPace * plannedDistance / 60; //since both are ints, you get an int
                int minutes = aimedPace * plannedDistance % 60;
                runDuration.setText(hours + ":" + minutes);
            }
        });

        distanceEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    plannedDistance = 0;
                }
                else {
                    int number = Integer.parseInt(s.toString());
                    plannedDistance = number;
                }
                int hours = aimedPace * plannedDistance / 60; //since both are ints, you get an int
                int minutes = aimedPace * plannedDistance % 60;
                runDuration.setText(hours + ":" + minutes);
            }
        });


        Button startRunButton = fragmentView.findViewById(R.id.runButton);
        startRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getIntent();
                final String userID = intent.getExtras().getString(USER_ID);

                final FirebaseDatabase database = FirebaseDatabase
                        .getInstance();
                final DatabaseReference profileGetRef = database.getReference
                        ("profiles");
                final DatabaseReference recordingRef = profileGetRef.child
                        (userID).child("recordings").push();

                final Switch useWatchSwitch = fragmentView.findViewById(R.id.switchWatch);
                final Switch useHearRateBelt = fragmentView.findViewById(R.id.switchHRBelt);
                final Switch useVocalCoach = fragmentView.findViewById(R.id.switchVocalCoach);
                final Switch runForGroup = fragmentView.findViewById(R.id.switchGroup);

                recordingRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        mutableData.child("datetime").setValue(System.currentTimeMillis());
                        mutableData.child("aimed_pace").setValue(aimedPace);
                        mutableData.child("planned_distance").setValue(plannedDistance);
                        mutableData.child("use_vocal_coach").setValue(useVocalCoach.isChecked());
                        mutableData.child("use_watch").setValue(useWatchSwitch.isChecked());
                        mutableData.child("use_belt").setValue(useHearRateBelt.isChecked());

                        recordingKeySaved = recordingRef.getKey();
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b,
                                           @Nullable DataSnapshot dataSnapshot) {
                        Toast.makeText(getContext(), "Activity entry started",
                                Toast.LENGTH_SHORT);

                        if (useWatchSwitch.isChecked()){
                            startWearActivity();
                        }

                        Intent intentStartRunning = new Intent(getActivity(), RunActivity.class);
                        intentStartRunning.putExtra(USER_ID, userID);
                        intentStartRunning.putExtra(RECORDIND_ID, recordingKeySaved);
                        intentStartRunning.putExtra(RunActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
                        intentStartRunning.putExtra(RunActivity.RUN_FOR_GROUP, runForGroup.isChecked());
                        startActivity(intentStartRunning);
                    }
                });

            }
        });
        return fragmentView;
    }

    private void startWearActivity() {
        Log.d(TAG, "Entered smart watch hr reading");
        Intent intentStartRec = new Intent(getActivity(), WearService.class);
        intentStartRec.setAction(WearService.ACTION_SEND.STARTACTIVITY.name());
        intentStartRec.putExtra(WearService.ACTIVITY_TO_START, BuildConfig
                .W_runningrecordingactivity);
        getActivity().startService(intentStartRec);
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

    private class SwitchBeltOnCheckedChangeListener implements CompoundButton
            .OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                Intent intent = new Intent(getActivity(), DeviceScanActivity.class);
                startActivityForResult(intent, BLE_CONNECTION);
            } else {
                compoundButton.setText(R.string.hr_belt);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        switchHRbelt.setOnCheckedChangeListener
                (switchBeltOnCheckedChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        switchHRbelt.setOnCheckedChangeListener(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Switch switchBelt = fragmentView.findViewById(R.id.switchHRBelt);
        if (requestCode == BLE_CONNECTION && resultCode == Activity.RESULT_OK) {
            String deviceName = data.getStringExtra(RunActivity.EXTRAS_DEVICE_NAME);
            deviceAddress = data.getStringExtra(RunActivity.EXTRAS_DEVICE_ADDRESS);

            switchBelt.setText(deviceName);
        } else {
            switchBelt.setChecked(false);
            switchBelt.setText(R.string.hr_belt);
        }
    }
}
