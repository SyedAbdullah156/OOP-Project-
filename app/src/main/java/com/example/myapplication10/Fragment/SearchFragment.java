package com.example.myapplication10.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication10.Adapter.UserAdapter;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.databinding.FragmentSearchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    ArrayList<UserInfoModel> list = new ArrayList<>();
    ArrayList<UserInfoModel> filteredList = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase database;
    FragmentSearchBinding binding;
    UserAdapter adapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        /*-----------------------------------------------------------------------------------------------------
        Using UserAdapter for displaying the user_rv_sample so that users can be displayed in the search section
        ------------------------------------------------------------------------------------------------------*/
        adapter = new UserAdapter(getContext(), filteredList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.userRV.setLayoutManager(linearLayoutManager);
        binding.userRV.setAdapter(adapter);

        // This addValueEventListener is called every time whenever a change in the database "Users" section occurs it keeps users up to date
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // This prevents from repeating users in the search fragment as one of the users is followed by clicking on the follow button
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserInfoModel user = dataSnapshot.getValue(UserInfoModel.class);
                    user.setUserId(dataSnapshot.getKey());

                    // Not adding the currentUser to the user list in the search fragment
                    if ((!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid()))) {
                        list.add(user);
                    }
                }
                // Initially show all users
                filteredList.clear();
                filteredList.addAll(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Add TextWatcher to filter users based on search input
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the user list as the text changes
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        });

        return binding.getRoot();
    }

    private void filterUsers(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            for (UserInfoModel user : list) {
                if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}