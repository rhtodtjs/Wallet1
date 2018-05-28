package www.seekerslab.com.seekeschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//
public class SelectFriendActivity extends AppCompatActivity {

    ChatModel chatModel = new ChatModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        RecyclerView recyclerView = findViewById(R.id.selectfriendactivity_recyclerview);
        recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button button = findViewById(R.id.selectfriendactivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.users.put(myUid, true);



                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        Intent intent = new Intent();
                        intent.putExtra("destinationRoom", databaseReference.getKey());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });


            }
        });
    }

    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<User> users;

        public SelectFriendRecyclerViewAdapter(){
            users = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    users.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        User user = snapshot.getValue(User.class);
                        if(user.uid.equals(myUid)) continue;

                        users.add(snapshot.getValue(User.class));

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            Glide.with(holder.itemView.getContext()).load(users.get(position).profileImageUrl).centerCrop().into(((CustomViewHolder)holder).imageView);


            ((CustomViewHolder)holder).textView.setText(users.get(position).userName);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   ((CustomViewHolder) holder).checkBox.setChecked(!(((CustomViewHolder) holder).checkBox.isChecked()));

                }
            });
            if(users.get(position).comment != null){
                ((CustomViewHolder) holder).textViewComment.setText(users.get(position).comment);
            }

            ((CustomViewHolder) holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //체크된 상태
                    if(b){
                        chatModel.users.put(users.get(position).uid, true);

                        //체크 취소 상태
                    }else{
                        chatModel.users.remove(users.get(position));

                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView;
            public TextView textViewComment;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textViewComment = view.findViewById(R.id.frienditem_textview_comment);
                checkBox = view.findViewById(R.id.frienditem_checkbox);
            }
        }
    }
}
