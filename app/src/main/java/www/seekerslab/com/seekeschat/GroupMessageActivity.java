package www.seekerslab.com.seekeschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//
public class GroupMessageActivity extends AppCompatActivity {

    Map<String, User> users = new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;


    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private RecyclerView recyclerView;

    PublisherAdView adView;

    List<ChatModel.Comment> comments = new ArrayList<>();

    int peopleCount = 0;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        adView = findViewById(R.id.groupmessageactivity_ad);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener(){

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("Message AD", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.d("Message AD", "onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d("Message AD", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d("Message AD", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.d("Message AD", "onAdClosed");
            }

        });


        destinationRoom = getIntent().getStringExtra("destinationRoom");
        uid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = findViewById(R.id.groupmessageactivity_edittext);

        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    users.put(item.getKey(),item.getValue(User.class));
                }
                init();

                recyclerView = findViewById(R.id.groupmessageactivity_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void init(){
        Button button = findViewById(R.id.groupmessageactivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timeStamp =  new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Boolean> map = (Map<String, Boolean>) dataSnapshot.getValue();

                                for(String item : map.keySet()){
                                    if(item.equals(uid)){
                                        continue;
                                    }
                                    sendGcm(users.get(item).pushToken);
                                }
                                editText.setText("");
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
    }

    void sendGcm(String pushToken){
        Gson gson = new Gson();

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = pushToken;
        notificationModel.notification.title = userName;
        notificationModel.notification.text =  editText.getText().toString();
        notificationModel.data.text = editText.getText().toString();
        notificationModel.data.title = userName;

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
        Request request = new Request.Builder().header("Context-Type", "application/json").addHeader("Authorization", "key=AAAAfxJlFzE:APA91bGiXT-KsyfDJAqJ3-FCEx7-fQrFeI5dDVfIXNCQet8i_dTDfBi9_oG-QMxFuigg7D3w9YGjmUwoAaWVwS6NwvTJzuK8vcwHrXgweNz27xWZSEWITtnnzsiM2ITCDb_XC6vjd9zc").url("https://gcm-http.googleapis.com/gcm/send").post(requestBody).build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(valueEventListener != null){
            databaseReference.removeEventListener(valueEventListener);
        }

        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter(){
            getMessageList();
        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments");


            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        String key = item.getKey();
                        ChatModel.Comment commentOrigin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment commentModify = item.getValue(ChatModel.Comment.class);
                        commentModify.readUsers.put(uid,true);

                        readUsersMap.put(key, commentModify);

                        comments.add(commentOrigin);
                    }
                    if(comments.size() != 0){
                        if(!comments.get(comments.size()-1).readUsers.containsKey(uid)){
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //메세지 갱신
                                    notifyDataSetChanged();

                                    recyclerView.scrollToPosition(comments.size()-1);
                                }
                            });
                        }else{
                            notifyDataSetChanged();

                            recyclerView.scrollToPosition(comments.size()-1);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new GroupmessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            GroupmessageViewHolder messageViewHolder = ((GroupmessageViewHolder)holder);

            //내가 보낸 메세지
            if(comments.get(position).uid.equals(uid)){
                messageViewHolder.textViewMessage.setText(comments.get(position).message);
                messageViewHolder.textViewMessage.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textViewMessage.setTextSize(15);
                messageViewHolder.messageitem_linearlayout_main.setGravity(Gravity.RIGHT);


                setReadCounter(position, messageViewHolder.textviewReadcounterLeft);

                Log.e("내가","보낸 메시지");
                //상대방이 보낸 메세지
            }else{
                Glide.with(holder.itemView.getContext()).load(users.get(comments.get(position).uid).profileImageUrl).centerCrop().into(messageViewHolder.imageview_profile);
                messageViewHolder.textview_name.setText(users.get(comments.get(position).uid).userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textViewMessage.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textViewMessage.setText(comments.get(position).message);
                messageViewHolder.textViewMessage.setTextSize(15);
                messageViewHolder.messageitem_linearlayout_main.setGravity(Gravity.LEFT);

                setReadCounter(position, messageViewHolder.textviewReadcounterRight);
                Log.e("상대가","보낸 메시지");
            }

            ((GroupmessageViewHolder) holder).imageview_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(holder.itemView.getContext(), ImageActivity.class);
                    intent.putExtra("imageurl", users.get(comments.get(position).uid).profileImageUrl);
                    intent.putExtra("name", users.get(comments.get(position).uid).userName);
                    intent.putExtra("position", position);

                    startActivity(intent);
                }
            });

            messageViewHolder.textViewMessage.setText(comments.get(position).message);
            messageViewHolder.textviewTimestamp.setText(comments.get(position).timeStamp);
        }
        void setReadCounter(final int position, final TextView textview){

            if(peopleCount == 0){
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();
                        int count = peopleCount - comments.get(position).readUsers.size();
                        if(count>0){
                            textview.setVisibility(View.VISIBLE);
                            textview.setText(String.valueOf(count));
                        }else{
                            textview.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }else{
                int count = peopleCount - comments.get(position).readUsers.size();
                if(count>0){
                    textview.setVisibility(View.VISIBLE);
                    textview.setText(String.valueOf(count));
                }else{
                    textview.setVisibility(View.GONE);
                }
            }


        }


        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class GroupmessageViewHolder extends RecyclerView.ViewHolder {

            public TextView textViewMessage;
            public TextView textview_name;
            public ImageView imageview_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout messageitem_linearlayout_main;
            public TextView textviewTimestamp;

            public TextView textviewReadcounterLeft;
            public TextView textviewReadcounterRight;


            public GroupmessageViewHolder(View view) {
                super(view);
                textViewMessage = view.findViewById(R.id.messageitem_textview_message);
                textview_name = view.findViewById(R.id.messageitem_textview_name);
                imageview_profile = view.findViewById(R.id.messageitem_imageview_profile);
                linearLayout_destination = view.findViewById(R.id.messageitem_linearlayout_destination);
                messageitem_linearlayout_main = view.findViewById(R.id.messageitem_linearlayout_main);
                textviewTimestamp = view.findViewById(R.id.messageitem_textview_timestamp);
                textviewReadcounterLeft = view.findViewById(R.id.messageitem_textview_readcounter_left);
                textviewReadcounterRight = view.findViewById(R.id.messageitem_textview_readcounter_right);
            }
        }
    }
}
