package www.seekerslab.com.seekeschat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

//


public class AccountFragment extends Fragment {

    AlertDialog dialog;

    ImageView imageView;
    String imageUrl = null;
    private static final int PICK_FROM_ALBUM = 200;
    Uri imageUri;
    DatabaseReference mDatabase;
    TextView textView;

    TextView email;

    Button testBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_account, container, false);
        imageView = view.findViewById(R.id.fragmentaccount_imageview);
        textView = view.findViewById(R.id.fragmentaccount_textview_name);
        email = view.findViewById(R.id.fragmentaccount_textview_email);
        textView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        testBtn = view.findViewById(R.id.fragmentaccount_btn_test);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), TestActivity.class);
                startActivity(intent);

            }
        });


        FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageUrl = (String)dataSnapshot.getValue();
                Glide.with(view.getContext()).load(imageUrl).centerCrop().into(imageView);
                Log.d("imageUrl", imageUrl);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        startActivityForResult(intent,  PICK_FROM_ALBUM);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button button = view.findViewById(R.id.accountfragment_button_comment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(getContext(),"comment", "");
            }
        });

        return view;
    }

    void showDialog(final Context context, String type, String pw){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_comment, null);
        TextView info= view.findViewById(R.id.dialog_comment_textview);

       switch (type){
           case "comment":

               info.setText("상태 메세지를 설정해 주세요");
               final EditText editText = view.findViewById(R.id.commentdialog_edittext);
               editText.setInputType(InputType.TYPE_CLASS_TEXT);
               builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                       Map<String, Object> stringObjectMap = new HashMap<>();

                       String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                       stringObjectMap.put("comment", editText.getText().toString());
                       FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(stringObjectMap);

                       dialog.dismiss();
                   }
               }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                   }
               });
               break;


       }

        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull ActivityResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                if (resultCode == Activity.RESULT_OK) {
                    //imageView.setImageURI(data.getData());
                    Glide.with(getContext()).load(data.getData()).centerCrop().into(imageView);
                    imageUri = data.getData();


                    FirebaseStorage.getInstance().getReference().child("userImages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            String imageUrl = task.getResult().getDownloadUrl().toString();


                            mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profileImageUrl").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });

                        }
                    });

                }
                break;

            }
        }
    }



}
