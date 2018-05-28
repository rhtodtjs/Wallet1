package www.seekerslab.com.seekeschat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

//
public class IntroActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id_test));

        linearLayout = findViewById(R.id.loginactivity_linearlayout);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {

                        }
                        displayMessage();
                    }
                });


    }

    void displayMessage(){
        String seekersChatBackground = mFirebaseRemoteConfig.getString("SeekersChat_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("SeekersChat_caps");
        String seekersChatMessage = mFirebaseRemoteConfig.getString("SeekersChat_message");

        linearLayout.setBackgroundColor(Color.parseColor(seekersChatBackground));
        if(caps){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(seekersChatMessage).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.create().show();
        }else{
            new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (InterruptedException e) {
                    }

                }
            }.start();
        }
    }
}
