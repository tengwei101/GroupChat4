package com.mobile.groupchat4.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mobile.groupchat4.R;
import com.mobile.groupchat4.models.ModelGroupChatList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.MyHolder> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        ModelGroupChatList model = groupChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.groupTitleTv.setText(groupTitle);
        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_cyan).into(holder.groupIconIv);

        }
        catch(Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_group_cyan);

        }

        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });


    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //ui views
        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
