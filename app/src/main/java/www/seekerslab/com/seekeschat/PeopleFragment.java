package www.seekerslab.com.seekeschat;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

//
public class PeopleFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        FloatingActionButton fab = view.findViewById(R.id.peoplefragment_floatingbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(view.getContext(), SelectFriendActivity.class), 20);

            }
        });

        final SharedPreferences pref = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        /*SharedPreferences.Editor edit = pref.edit();
        edit.putString("pw", password.getText().toString());
        edit.commit();*/

        if(pref.getInt("people", 0)!=1){
            TapTargetView.showFor((Activity) getContext(),                 // `this` is an Activity
                    TapTarget.forView(view.findViewById(R.id.peoplefragment_floatingbutton), "단체 대화방 만들기", "친구들을 초대해 보세요!")
                            // All options below are optional
                            .outerCircleColor(R.color.green)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.white)   // Specify a color for the target circle
                            .titleTextSize(20)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.red)  // Specify the color of the description text
                            .textColor(R.color.blue)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(false)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                            // .icon(Drawable)                     // Specify a custom drawable to draw as the target
                            .targetRadius(60),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);      // This call is optional
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putInt("people", 1);
                            edit.commit();

                        }
                    });
        }



        return view;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull ActivityResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 20 && resultCode == Activity.RESULT_OK){
            Intent intent = new Intent(getContext(), GroupMessageActivity.class);
            intent.putExtra("destinationRoom", data.getStringExtra("destinationRoom"));
            ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getContext(), R.anim.fromright, R.anim.toleft);
            startActivity(intent, activityOptions.toBundle());
        }
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        ArrayList<User> users;

        public PeopleFragmentRecyclerViewAdapter(){
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            Glide.with(holder.itemView.getContext()).load(users.get(position).profileImageUrl).centerCrop().into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(holder.itemView.getContext(), ImageActivity.class);
                    intent.putExtra("imageurl", users.get(position).profileImageUrl);
                    intent.putExtra("name", users.get(position).userName);
                    intent.putExtra("position", position);

                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.scale, R.anim.scaleback);
                }
            });


            ((CustomViewHolder)holder).textView.setText(users.get(position).userName);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", users.get(position).uid);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            });
            if(users.get(position).comment != null){
                ((CustomViewHolder) holder).textViewComment.setText(users.get(position).comment);
            }



        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView;
            public TextView textViewComment;
            public TextView textViewStatus;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textViewComment = view.findViewById(R.id.frienditem_textview_comment);
                textViewStatus = view.findViewById(R.id.frienditem_textview_status);

            }
        }
    }
}
