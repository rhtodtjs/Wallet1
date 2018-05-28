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
import com.google.android.gms.tasks.OnSuccessListener;
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
public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private RecyclerView recyclerView;

    private String uid;
    private String chatRoomUid;

    private User destinationUser;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    PublisherAdView adView;

    int peopleCount = 0;

    private boolean is_first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        adView = findViewById(R.id.messageactivity_ad);
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

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //채팅을 요구하는 UID
        destinationUid = getIntent().getStringExtra("destinationUid");  //채팅을 당하는 아이디

        button = findViewById(R.id.messageactivity_button);
        editText = findViewById(R.id.messageactivity_edittext);
        recyclerView = findViewById(R.id.messageactivity_recyclerview);

        checkChatRoom();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid,true);
                chatModel.users.put(destinationUid, true);

                if(chatRoomUid == null){
                    button.setEnabled(false);
                    Log.d("chatRoomUid","null");
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                checkChatRoom();
                            }
                    });
                }else{
                    Log.d("chatRoomUid","Not Null");
                    if(editText.getText().toString() != null ||editText.getText().toString() !=  ""){
                        ChatModel.Comment comment = new ChatModel.Comment();
                        comment.uid = uid;
                        comment.message = editText.getText().toString();
                        comment.timeStamp = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());

                        Log.e("msg", comment.uid + " : " + comment.message);
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sendGcm();
                                editText.setText("");

                            }
                        });
                    }

                }



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

    void sendGcm(){
        Gson gson = new Gson();

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUser.pushToken;
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

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatModel chatModel = snapshot.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid) && chatModel.users.size() == 2){

                        chatRoomUid = snapshot.getKey();

                        Log.d("chatRoomUid", chatRoomUid + " : " + destinationUid);
                        button.setEnabled(true);

                        if(editText.getText().toString() != null ||editText.getText().toString() !=  "" && is_first){
                            ChatModel.Comment comment = new ChatModel.Comment();
                            comment.uid = uid;
                            comment.message = editText.getText().toString();
                            comment.timeStamp = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());

                            Log.e("msg", comment.uid + " : " + comment.message);
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    sendGcm();
                                    editText.setText("");

                                }
                            });
                        }

                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                        is_first = false;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;


        public RecyclerViewAdapter(){
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    destinationUser = dataSnapshot.getValue(User.class);

                    getMessageList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");


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

                        if(comments.size() > 0){
                            if(!comments.get(comments.size()-1).readUsers.containsKey(uid)){
                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            //내가 보낸 메세지
            if(comments.get(position).uid.equals(uid)){
                messageViewHolder.textViewMessage.setText(comments.get(position).message);
                messageViewHolder.textViewMessage.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textViewMessage.setTextSize(15);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageViewHolder.textViewMessage.getLayoutParams();
                params.gravity = Gravity.RIGHT;
                messageViewHolder.textViewMessage.setLayoutParams(params);
                messageViewHolder.messageitem_linearlayout_main.setGravity(Gravity.RIGHT);


                setReadCounter(position, messageViewHolder.textviewReadcounterLeft);

                Log.e("내가","보낸 메시지");
                //상대방이 보낸 메세지
            }else{
                Glide.with(holder.itemView.getContext()).load(destinationUser.profileImageUrl).centerCrop().into(messageViewHolder.imageview_profile);
                messageViewHolder.textview_name.setText(destinationUser.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textViewMessage.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textViewMessage.setText(comments.get(position).message);
                messageViewHolder.textViewMessage.setTextSize(15);
                messageViewHolder.messageitem_linearlayout_main.setGravity(Gravity.LEFT);

                setReadCounter(position, messageViewHolder.textviewReadcounterRight);
                Log.e("상대가","보낸 메시지");
            }

            messageViewHolder.textViewMessage.setText(comments.get(position).message);
            messageViewHolder.textviewTimestamp.setText(comments.get(position).timeStamp);

            ((MessageViewHolder) holder).imageview_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(holder.itemView.getContext(), ImageActivity.class);
                    intent.putExtra("imageurl", destinationUser.profileImageUrl);
                    intent.putExtra("name", destinationUser.userName);
                    intent.putExtra("position", position);

                    startActivity(intent);
                }
            });

        }

        void setReadCounter(final int position, final TextView textview){

            if(peopleCount == 0){
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();
                        int count = peopleCount - comments.get(position).readUsers.size();
                        if(count>0){
                            textview.setVisibility(View.VISIBLE);
                            textview.setText(String.valueOf(count));
                        }else{
                            textview.setVisibility(View.GONE);
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

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewMessage;
            public TextView textview_name;
            public ImageView imageview_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout messageitem_linearlayout_main;
            public TextView textviewTimestamp;

            public TextView textviewReadcounterLeft;
            public TextView textviewReadcounterRight;



            public MessageViewHolder(View view) {
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
