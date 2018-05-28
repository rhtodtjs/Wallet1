package www.seekerslab.com.seekeschat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

//
public class MainActivity extends AppCompatActivity {

    private final static int GO_TO_LOCK = 35;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);

        String isLock = pref.getString("lock","unset");

        if(isLock.equals("set")){
            Intent intent = new Intent(this, LockActivity.class);
            intent.putExtra("lockinit", false);
            startActivityForResult(intent, GO_TO_LOCK);
        }

        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framlayout, new PeopleFragment()).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.mainactivity_bottomnavigationview);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_people:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framlayout, new PeopleFragment()).commit();
                        return true;

                    case R.id.action_chat:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framlayout, new ChatFragment()).commit();
                        return true;
                    case R.id.action_account:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framlayout, new AccountFragment()).commit();
                        return true;
                }
                return false;
            }
        });
        passPushTokenToServer();

    }

    void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();

        Map<String, Object> map = new HashMap<>();
        map.put("pushToken", token);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GO_TO_LOCK && resultCode == RESULT_OK){

        }else{
            finish();
        }
    }
}
