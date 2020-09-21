package com.example.pgflatfinder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.zip.Inflater;

public class ProfileFragment extends Fragment {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    TextView display_name,mobile_number,email;
    Button edit_btn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mMainView = inflater.inflate(R.layout.fragment_profile, container, false);
        final  String mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        display_name = mMainView.findViewById(R.id.display_name);
        mobile_number = mMainView.findViewById(R.id.mobile_number);
        email = mMainView.findViewById(R.id.email);
        edit_btn = mMainView.findViewById(R.id.edit_details);


        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                display_name.setText(snapshot.child("users").child(mCurrentUser).child("name").getValue().toString());
                mobile_number.setText(snapshot.child("users").child(mCurrentUser).child("mobile").getValue().toString());
                email.setText(snapshot.child("users").child(mCurrentUser).child("email").getValue().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRootRef.keepSynced(true);

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),EditPage.class);
                startActivity(intent);
            }
        });

        return mMainView;
    }
}