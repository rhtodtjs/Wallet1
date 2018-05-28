package www.seekerslab.com.seekeschat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.squareup.otto.Subscribe;

public class TestActivity extends AppCompatActivity {

    ToggleButton toggleButton;

    private static final int LOCK_INIT = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        toggleButton = findViewById(R.id.testactivity_toggle);

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);

        Log.e("LockStatus", pref.getString("lock", "unset"));
        if(pref.getString("lock", "unset").equals("set")){
            toggleButton.setChecked(true);
        }else{
            toggleButton.setChecked(false);
        }



        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b){
                    SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("lock", "set");
                    editor.commit();

                    Intent intent = new Intent(TestActivity.this, LockActivity.class);
                    intent.putExtra("lockinit", true);
                    startActivityForResult(intent, LOCK_INIT);


                }else{
                    SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("lock", "unset");
                    editor.commit();
                }

            }
        });

        Button button = findViewById(R.id.testactivity_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(TestActivity.this, Test2Activity.class);

                startActivity(intent);
            }
        });


    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull ActivityResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LOCK_INIT){
            if(resultCode == RESULT_OK){

            }else{
                toggleButton.setChecked(false);
            }
        }
    }
}
