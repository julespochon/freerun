package com.example.d_wen.freerun;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HistoryFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();


    private OnFragmentInteractionListener mListener;

    private ListView listView;
    private View fragmentView;
    private RecordingAdapter adapter;
    private MyFirebaseRecordingListener mFirebaseRecordingListener;
    private DatabaseReference databaseRef;
    private String idUser;


    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        listView = fragmentView.findViewById(R.id.myHistoryList);
        adapter = new RecordingAdapter(getActivity(), R.layout.row_history);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), "Exercise: " + ((TextView) view.findViewById(R.id
                        .exerciseType)).getText().toString() + " on " + ((TextView) view
                        .findViewById(R.id.exerciseDateTime)).getText().toString(), Toast
                        .LENGTH_SHORT).show();
            }
        });

        idUser = getActivity().getIntent().getExtras().getString(MyProfileFragment.USER_ID);

        return fragmentView;
    }

    private class RecordingAdapter extends ArrayAdapter<Recording> {
        private int row_layout;

        RecordingAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //Reference to the row View
            View row = convertView;

            if (row == null) {
                //Inflate it from layout
                row = LayoutInflater.from(getContext()).inflate(row_layout, parent, false);
            }

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale
                    .getDefault());
            ((TextView) row.findViewById(R.id.exerciseDateTime)).setText(formatter.format(new
                    Date(getItem(position).exerciseDateTime)));
            ((TextView) row.findViewById(R.id.exerciseDevice)).setText(getItem(position)
                    .exerciseSmartWatch ? "yes" : "no");
            ((TextView) row.findViewById(R.id.exerciseDevice2)).setText(getItem(position)
                    .exerciseHRbelt ? "yes" : "no");

            return row;
        }
    }

    public class Recording {
        protected long exerciseDateTime;
        protected boolean exerciseSmartWatch;
        protected boolean exerciseHRbelt;
    }

    private class MyFirebaseRecordingListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            adapter.clear();
            for (final DataSnapshot rec : dataSnapshot.getChildren()) {
                final Recording recording = new Recording();

                recording.exerciseDateTime = Long.parseLong(rec.child("datetime").getValue()
                        .toString());
                recording.exerciseSmartWatch = Boolean.parseBoolean(rec.child("use_watch")
                        .getValue().toString());
                recording.exerciseHRbelt = Boolean.parseBoolean(rec.child("use_belt")
                        .getValue().toString());
                adapter.add(recording);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v(TAG, databaseError.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                databaseRef.child("profiles").child(idUser).child("recordings").removeValue();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseRecordingListener = new MyFirebaseRecordingListener();
        databaseRef.child("profiles").child(idUser).child("recordings").addValueEventListener
                (mFirebaseRecordingListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        databaseRef.child("profiles").child(idUser).child("recordings").removeEventListener
                (mFirebaseRecordingListener);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
