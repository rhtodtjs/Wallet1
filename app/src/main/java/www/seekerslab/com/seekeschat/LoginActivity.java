package www.seekerslab.com.seekeschat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.roger.catloadinglibrary.CatLoadingView;
//

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    CatLoadingView loadingView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingView = new CatLoadingView();

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //앱이 꺼지면 자동 로그아웃
        //mFirebaseAuth.signOut();
        String seekersChatBackground = mFirebaseRemoteConfig.getString("SeekersChat_background");

        getWindow().setStatusBarColor(Color.parseColor(seekersChatBackground));

        id = findViewById(R.id.loginactivity_edittext_id);
        password = findViewById(R.id.loginactivity_edittext_password);

        login = findViewById(R.id.loginactivity_button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingView.show(getSupportFragmentManager(),"");
                login.setEnabled(false);
                loginEvent();
            }
        });

        signup = findViewById(R.id.loginactivity_button_signup);

        login.setBackgroundColor(Color.parseColor(seekersChatBackground));
        signup.setBackgroundColor(Color.parseColor(seekersChatBackground));

        signup.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();

                if(user != null){
                    //로그인
                    //loadingView.dismiss();
                    SharedPreferences pref = LoginActivity.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("id", id.getText().toString());
                    edit.commit();
                    login.setEnabled(false);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //로그아웃

                }

            }
        };
    }

    void loginEvent(){

        SharedPreferences pref = LoginActivity.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("pw", password.getText().toString());
        edit.commit();
        mFirebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    login.setEnabled(true);
                    //login 실패
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    loadingView.dismiss();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(authStateListener);
    }


}
