package com.mobile.groupchat4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.groupchat4.adapters.AdapterParticipantAdd;
import com.mobile.groupchat4.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private  String myGroupRole;

    private FirebaseAuth firebaseAuth;

    private ActionBar actionBar;
    private ImageView groupIconIv;
    private TextView groupDescriptionTv, participantsTv, createdByTv, editGroupTv, addParticipantTv, leaveGroupTv;

    private RecyclerView participantsRv;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantAdd adapterParticipantAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        groupId = getIntent().getStringExtra("groupId");

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        groupDescriptionTv = findViewById(R.id.groupDescriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);

        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupInfo();

        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if user is participant/admin: leave group
                //if user is creator: delete group
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to delete group permenanatly";
                    positiveButtonTitle = "DELETE";
                }
                else{
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to leave group permenanatly";
                    positiveButtonTitle = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(myGroupRole.equals("creator")){
                                    //im creator of group: delete group
                                    deleteGroup();
                                }
                                else{
                                    //im participants/admin: leave group
                                    leaveGroup();
                                }

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });



    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //group left successfully
                        Toast.makeText(GroupInfoActivity.this, "Group left successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to leave group
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //group deleted succesfully
                        Toast.makeText(GroupInfoActivity.this, "Group deleted successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete group
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        
                    }
                });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){

                    //get group info
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    //convert time stamp to dd/mm/yyyy hh:mm am/pm
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                    loadCreatorInfo(dateTime, createdBy);

                    //set group info
                    actionBar.setTitle(groupTitle);
                    groupDescriptionTv.setText(groupDescription);

                    try{

                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_cyan).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.ic_group_cyan);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void loadCreatorInfo(String dateTime, String createdBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    createdByTv.setText("Created by " + name + " on " +dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String currentUid = ds.child("uid").getValue(String.class);

                            // Check if the UID matches the current user's UID
                            if (firebaseAuth.getUid().equals(currentUid)) {
                                myGroupRole = "" + ds.child("role").getValue();
                                actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + " (" + myGroupRole + ")");

                                if (myGroupRole.equals("participant")) {
                                    editGroupTv.setVisibility(View.GONE);
                                    addParticipantTv.setVisibility(View.GONE);
                                    leaveGroupTv.setText("Leave Group");
                                } else if (myGroupRole.equals("admin")) {
                                    editGroupTv.setVisibility(View.GONE);
                                    addParticipantTv.setVisibility(View.VISIBLE);
                                    leaveGroupTv.setText("Leave Group");
                                } else if (myGroupRole.equals("creator")) {
                                    editGroupTv.setVisibility(View.VISIBLE);
                                    addParticipantTv.setVisibility(View.VISIBLE);
                                    leaveGroupTv.setText("Delete Group");
                                }
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadParticipants() {
        userList = new ArrayList<>();
        final List<String> uidList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uidList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // get uid from Group > Participants
                    String uid = "" + ds.child("uid").getValue();
                    uidList.add(uid);
                }

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelUser modelUser = ds.getValue(ModelUser.class);
                            if (uidList.contains(modelUser.getUid())) {
                                userList.add(modelUser);
                            }
                        }
                        // adapter
                        adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this, userList, groupId, myGroupRole);
                        // set adapter
                        participantsRv.setAdapter(adapterParticipantAdd);
                        participantsTv.setText("Participants (" + userList.size() + ")");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}