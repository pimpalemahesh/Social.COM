package com.myinnovation.socom.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myinnovation.socom.Activity.CommentActivity;
import com.myinnovation.socom.Model.Notification;
import com.myinnovation.socom.Model.UserClass;
import com.myinnovation.socom.R;
import com.myinnovation.socom.databinding.SampleNotificationBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.myviewholder> {

    ArrayList<Notification> list;
    Context context;

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_notification, parent, false);
        return new myviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        Notification notification = list.get(position);

        String type = notification.getType();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserClass user = snapshot.getValue(UserClass.class);
                        String text = TimeAgo.using(notification.getNotificationAt());
                        if (user != null) {
                            Picasso.get()
                                    .load(user.getProfile_image())
                                    .placeholder(R.drawable.ic_user)
                                    .into(holder.binding.profileImage);


                            holder.binding.time.setText(text);

                            switch (type) {
                                case "like":
                                    holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " liked your post"));
                                    break;
                                case "comment":
                                    holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " Commented on your post"));
                                    break;
                                case "follow":
                                    holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " start following you"));
                                    break;
                                default:
                                    holder.binding.notification.setText(R.string.you_have_notification);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.openNotification.setOnClickListener(v -> {

            if (!type.equals("follow")) {
                FirebaseDatabase.getInstance().getReference()
                        .child("notification")
                        .child(notification.getPostedBy())
                        .child(notification.getNotificationId())
                        .child("checkOpen")
                        .setValue(true);

                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", notification.getPostId());
                intent.putExtra("postedBy", notification.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        });

        Boolean checkOpen = notification.isCheckOpen();
        if (checkOpen.equals(true)) {
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class myviewholder extends RecyclerView.ViewHolder {

        SampleNotificationBinding binding;

        public myviewholder(@NonNull View itemView) {
            super(itemView);
            binding = SampleNotificationBinding.bind(itemView);
        }
    }
}
