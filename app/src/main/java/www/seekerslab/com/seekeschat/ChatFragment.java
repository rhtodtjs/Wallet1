package www.seekerslab.com.seekeschat;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

//
public class ChatFragment extends Fragment{

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();
        private List<String> keys = new ArrayList<>();

        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                        keys.add(item.getKey());
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;

            String destinationUid = null;


            //챗방에 있는 유저들 체크
            for(String user : chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid);

                    FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User userModel = dataSnapshot.getValue(User.class);

                            Glide.with(customViewHolder.itemView.getContext()).load(userModel.profileImageUrl).centerCrop().into(customViewHolder.imageView);


                            customViewHolder.textviewTitle.append(userModel.userName + " ");

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }




            //메세지를 내림차순으로 정렬 후 마지막 메세지를 메세지의 키 값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.<String>reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            if(commentMap.keySet().toArray().length>0){
                String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                customViewHolder.textviewLastMessage.setText(chatModels.get(position).comments.get(lastMessageKey).message);

                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                customViewHolder.textviewTimestamp.setText(chatModels.get(position).comments.get(lastMessageKey).timeStamp);
            }
            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = null;
                    if(chatModels.get(position).users.size() > 2){
                        intent = new Intent(view.getContext(), GroupMessageActivity.class);
                        intent.putExtra("destinationRoom", keys.get(position));
                        Log.d("destinationRoom", keys.get(position));
                    }else{
                        intent = new Intent(view.getContext(), MessageActivity.class);
                        intent.putExtra("destinationUid", destinationUsers.get(position));
                    }


                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            });

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textviewTitle;
            public TextView textviewLastMessage;
            public TextView textviewTimestamp;

            public CustomViewHolder(View view) {
                super(view);

                imageView = view.findViewById(R.id.chatitem_imageview);
                textviewTitle = view.findViewById(R.id.chatitem_textview_title);
                textviewLastMessage = view.findViewById(R.id.chatitem_textview_lastmessage);
                textviewTimestamp = view.findViewById(R.id.chatitem_textview_timestamp);
            }
        }
    }
}
