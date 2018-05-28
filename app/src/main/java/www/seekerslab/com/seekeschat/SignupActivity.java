package www.seekerslab.com.seekeschat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

//
public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private ImageView profile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String seekersChatBackground = mFirebaseRemoteConfig.getString("SeekersChat_background");

        getWindow().setStatusBarColor(Color.parseColor(seekersChatBackground));

        profile = findViewById(R.id.signupactivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,  PICK_FROM_ALBUM);
            }
        });

        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.signupactivity_imageview_profile), "프로필 이미지 선택", "회원가입시 필수 입력사항 입니다")
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
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                       // .icon(Drawable)                     // Specify a custom drawable to draw as the target
                        .targetRadius(60),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        startActivityForResult(intent,  PICK_FROM_ALBUM);
                    }
                });

        email = findViewById(R.id.signupactivity_edittext_email);
        name = findViewById(R.id.signupactivity_edittext_name);
        password = findViewById(R.id.signupactivity_edittext_password);
        signup = findViewById(R.id.signupactivity_button_signup);

        signup.setBackgroundColor(Color.parseColor(seekersChatBackground));

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup.setEnabled(false);
                if(email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null || imageUri == null){
                    signup.setEnabled(true);
                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        final String  uid = task.getResult().getUser().getUid();
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();

                        task.getResult().getUser().updateProfile(userProfileChangeRequest);

                        FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                String imageUrl = task.getResult().getDownloadUrl().toString();

                                User user = new User();
                                user.userName = name.getText().toString();
                                user.profileImageUrl = imageUrl;
                                user.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                user.balance = 10000;

                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        signup.setEnabled(true);
                                        SignupActivity.this.finish();
                                    }
                                });


                            }
                        });

                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            Glide.with(this).load(data.getData()).centerCrop().into(profile);
            imageUri = data.getData();
        }
    }
}
