package com.mobile.groupchat4.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.groupchat4.R;
import com.mobile.groupchat4.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {

    private Context context;
    private ArrayList<ModelUser> userList;
    private String groupId, myGroupRole; //creator /admin/participants

    public AdapterParticipantAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_participants, parent, false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        //get data
        ModelUser modelUser = userList.get(position);
        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String image = modelUser.getImage();
        String uid = modelUser.getUid();

        //set Data
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try{

            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);

        }catch (Exception e){

            holder.avatarIv.setImageResource((R.drawable.ic_default_img));
        }

        checkIfAlreadyExists(modelUser, holder);


        //HANDLE CLICK
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CHeck if user already added or not
                // if Added: show remove-participants/make-admin/remove-admin option (Admin will not able to change role of creator
                // if not added, show add participant option
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    //user exists/not participant
                                    String hisPreviousRole = ""+dataSnapshot.child("role").getValue();

                                    //options to display in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose option");
                                    if(myGroupRole.equals("creator")){
                                        if(hisPreviousRole.equals("admin")){
                                            //im creator, he is admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //remove admin clicked
                                                        removeAdmin(modelUser);

                                                    }
                                                    else{
                                                        //remove participant clicked
                                                        removeParticipant(modelUser);
                                                    }

                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            //im creater, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //Make admin clicked
                                                        makeAdmin(modelUser);

                                                    }
                                                    else{
                                                        //remove participant clicked
                                                        removeParticipant(modelUser);
                                                    }

                                                }
                                            }).show();
                                        }
                                    }
                                    else if(myGroupRole.equals("admin")){
                                        if(hisPreviousRole.equals("creator")){
                                            //im admin, he is creator
                                            Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
                                        }
                                        else if(hisPreviousRole.equals("admin")){
                                            //im admin, he is admin too
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //remove admin clicked
                                                        removeAdmin(modelUser);

                                                    }
                                                    else{
                                                        //remove participant clicked
                                                        removeParticipant(modelUser);
                                                    }

                                                }
                                            }).show();

                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            //im admin, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //Make admin clicked
                                                        makeAdmin(modelUser);
                                                    }
                                                    else{
                                                        //remove participant clicked
                                                        removeParticipant(modelUser);
                                                    }

                                                }
                                            }).show();

                                        }
                                    }
                                }
                                else{
                                    //usder does not exists/ not a participant: add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //add user
                                                    addParticipant(modelUser);

                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();

                                                }
                                            }).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });

    }

    private void addParticipant(ModelUser modelUser) {
        //setup user data - add user to group
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String > hashMap = new HashMap<>();
        hashMap.put("uid", modelUser.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);

        //add that user in group>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //added successfully
                        Toast.makeText(context, "Added Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding user in group
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAdmin(ModelUser modelUser) {

        //setup data - change role
        HashMap<String, Object > hashMap = new HashMap<>();
        hashMap.put("role", "admin"); //roles are : participant/admin/creator

        //update role in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //made admin
                        Toast.makeText(context, "The user is admin now...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed making admin
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void removeParticipant(ModelUser modelUser) {
        //remove participant from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

        ref.child(groupId).child("Participants").child(modelUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //removed successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed remopved participant

                    }
                });
    }

    private void removeAdmin(ModelUser modelUser) {
        //setup data = remove admin - just change role
        HashMap<String, Object > hashMap = new HashMap<>();
        hashMap.put("role", "participant"); //roles are : participant/admin/creator

        //update role in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //made admin
                        Toast.makeText(context, "The user is no longer admin now...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed making admin
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkIfAlreadyExists(ModelUser modelUser, HolderParticipantAdd holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            //already exits
                            String hisRole = ""+dataSnapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }
                        else{
                            //doesn't exists
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{


        private ImageView avatarIv;
        private TextView nameTv, emailTv, statusTv;

        public HolderParticipantAdd(@NonNull View itemView){
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
