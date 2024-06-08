package com.example.myapplication10.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Model.CommentModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.CommentRvSampleBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {

    Context context;
    ArrayList<CommentModel> list;

    public CommentAdapter(Context context, ArrayList<CommentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.comment_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        CommentModel comment = list.get(position);

        // Setting up comment Time
        String time = TimeAgo.using(comment.getCommentAt());
        holder.binding.commentTime.setText(time);

        // Setting up the commentText for this specific comment
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(comment.getCommentedByUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                            Picasso.get()
                                    .load(user.getProfileImage())
                                    .placeholder(R.drawable.default_profile_image)
                                    .into(holder.binding.profileImage);
                            holder.binding.userComment.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " " + comment.getCommentBody()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        CommentRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CommentRvSampleBinding.bind(itemView);
        }
    }
}
