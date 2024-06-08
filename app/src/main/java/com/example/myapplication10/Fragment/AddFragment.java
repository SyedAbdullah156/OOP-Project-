package com.example.myapplication10.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication10.Model.PostModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.FragmentAddBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class AddFragment extends Fragment {

    FragmentAddBinding binding;
    Uri uri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    boolean postCancelButtonClicked;
    ProgressDialog dialog;

    public AddFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        postCancelButtonClicked = true;
        dialog = new ProgressDialog(getContext());
    }

    // This enables the postButton when user write text and disables the postButton when user doesn't writes text and also takes care of postCancelButton
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please Wait....");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        // Setting currentUser's username, profession and profileImage from the database to the fragment_add layout so that it appears already before the user make the post
        database.getReference().child("Users")
                        .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                            Picasso.get()
                                    .load(user.getProfileImage())
                                    .placeholder(R.drawable.default_profile_image)
                                    .into(binding.profileImage);
                            binding.name.setText(user.getName());
                            binding.profession.setText(user.getProfession());

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


        // Making postButton enabled and disabled by listening to the text change on the textview and postCancelButton
        binding.postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            // But this is not working when there is not any description is present and the image is removed because if the description is null and the image is removed after it this method is not called and hence after the image is also deleted both are gone but the postButton is not disabled now fo that we have to apply the logic that when the postCancelButton is clicked and the description is null the postButton should become disabled
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String description = binding.postDescription.getText().toString();
                // Description is not empty and the button is not clicked(postImage is there) OR description is empty and the button is not clicked(postImage is there) OR description is not empty and button is clicked(postImage is not there)
                if (((!description.isEmpty()) && postCancelButtonClicked == false) || (description.isEmpty() && postCancelButtonClicked == false) || (!(description.isEmpty()) && postCancelButtonClicked == true)) {
                    binding.postButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_enabled));
                    binding.postButton.setTextColor(getContext().getResources().getColor(R.color.white));
                    binding.postButton.setEnabled(true);
                } else if(description.isEmpty() && postCancelButtonClicked == true){ // If both description is empty and postImage is not present
                    binding.postButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_disabled));
                    binding.postButton.setTextColor(getContext().getResources().getColor(R.color.gray));
                    binding.postButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // When description is null and postCancelButton is pressed the postButton should become disable also it manages for postImage and cancelButton
        binding.postCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = binding.postDescription.getText().toString();

                binding.postImage.setVisibility(View.GONE);
                binding.postCancel.setVisibility(View.GONE);
                binding.postImage.setImageURI(null);
                postCancelButtonClicked = true;
                uri = null; // Reset the URI

                if(description.isEmpty()){
                    binding.postButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_disabled));
                    binding.postButton.setTextColor(getContext().getResources().getColor(R.color.gray));
                    binding.postButton.setEnabled(false);
                }
            }
        });

        // postImageSelector button when clicked open the gallery
        binding.postImageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        // Add post data including all things related to post are first stored in the storage and then into database using PostModel when postButton is clicked
        binding.postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();

                if(uri != null) {
                    final StorageReference reference = storage.getReference().child("posts")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child(new Date().getTime() + "");
                    reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    PostModel post = new PostModel();
                                    post.setPostImage(uri.toString());
                                    post.setPostedByUser(FirebaseAuth.getInstance().getUid());
                                    post.setPostDescription(binding.postDescription.getText().toString());
                                    post.setPostedATime(new Date().getTime());

                                    database.getReference().child("posts")
                                            .push()
                                            .setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    dialog.dismiss();
                                                    Toast.makeText(getContext(), "Posted Successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
                else
                {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Please select an Image for the Post", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }

    // This is the activity that is called when the user clicks on the image selector button on the addPost fragment layout
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Both postImage and postCancel are enabled when we click on the postImageSelector button
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                postCancelButtonClicked = false;
                uri = data.getData(); // This variable should remain global otherwise error will occur
                binding.postImage.setImageURI(uri);
                binding.postImage.setVisibility(View.VISIBLE);
                binding.postCancel.setVisibility(View.VISIBLE);

                // This is called when you add an image and there is no description available so the postButton is enabled
                binding.postButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_enabled));
                binding.postButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                binding.postButton.setEnabled(true);
            }
        } else {
            // Handle the case where the result is not RESULT_OK
            Log.e("ProfileFragment", "Result code is not OK: " + resultCode);
        }
    }
}