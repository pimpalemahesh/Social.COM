package com.myinnovation.socom.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.myinnovation.socom.Adapter.FollowersAdapter;
import com.myinnovation.socom.Model.Follow;
import com.myinnovation.socom.Model.UserClass;
import com.myinnovation.socom.R;
import com.myinnovation.socom.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class  ProfileFragment extends Fragment {

    FragmentProfileBinding binding;

    ArrayList<Follow> list;
    String currentUid;
    FirebaseStorage storage;
    DatabaseReference reference;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(FirebaseAuth.getInstance().getUid() != null){
            currentUid = FirebaseAuth.getInstance().getUid();
        }
        storage = FirebaseStorage.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        binding.friendRv.showShimmerAdapter();

        list = new ArrayList<>();
        FollowersAdapter adapter = new FollowersAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.friendRv.setLayoutManager(linearLayoutManager);


        // setting followrs count
        reference
                .child("Users")
                .child(currentUid)
                .child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Follow follow = dataSnapshot.getValue(Follow.class);
                    if (follow != null && follow.getFollowedBy().equals(currentUid)) {
                        list.add(follow);
                    }
                }
                binding.friendRv.setAdapter(adapter);
                binding.friendRv.hideShimmerAdapter();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // setting cover photo image and profile using Picasso library
        // name profession followers also
        reference.child("Users")
                .child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserClass user = snapshot.getValue(UserClass.class);
                            if(user != null){
                                Picasso.get()
                                        .load(user.getCoverPhoto())
                                        .placeholder(R.drawable.ic_image)
                                        .into(binding.coverphoto);

                                Picasso.get()
                                        .load(user.getProfile_image())
                                        .placeholder(R.drawable.ic_user)
                                        .into(binding.profileImage);

                                if(snapshot.child("profile_image").exists()){
                                    binding.verifyAccount.setVisibility(View.VISIBLE);
                                } else{
                                    binding.verifyAccount.setVisibility(View.GONE);
                                }

                                binding.UserName.setText(user.getName());
                                binding.Profession.setText(user.getProfession());
                                binding.followers.setText(String.valueOf(user.getFollowerCount()));
                                binding.friend.setText(String.valueOf(user.getFriendCount()));
                                binding.posts.setText(String.valueOf(user.getPostCount()));


                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        // change Cover photo of user
        binding.changecoverphoto.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        // change Profile Image
        binding.profileImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 102);
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("File Uploading");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Uploaded : ");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);

        if (data != null && requestCode == 101 && data.getData() != null) {
            Uri uri = data.getData();
            binding.coverphoto.setImageURI(uri);

            final StorageReference storageReference = storage.getReference().child("cover_photo").child(currentUid);

            storageReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Cover Photo saved.", Toast.LENGTH_LONG).show();

                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> reference
                                        .child("Users")
                                        .child(currentUid)
                                        .child("coverPhoto").setValue(uri1.toString()))
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnProgressListener(snapshot -> {
                        int per = (int) ((100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                        pd.setMessage("Uploaded : " + per + "%");
                        pd.show();
                    });
        }

        if (data != null && requestCode == 102 && data.getData() != null) {
            Uri uri = data.getData();
            binding.profileImage.setImageURI(uri);

            final StorageReference storageReference = storage.getReference().child("profile_image").child(currentUid);

            storageReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Profile Photo saved.", Toast.LENGTH_LONG).show();

                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(uri12 -> {
                                    reference.child("Users").child(currentUid).child("profile_image").setValue(uri12.toString());
                                    binding.verifyAccount.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnProgressListener(snapshot -> {
                        int per = (int) ((100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                        pd.setMessage("Uploaded : " + per + "%");
                        pd.show();
                    });
        }
    }
}