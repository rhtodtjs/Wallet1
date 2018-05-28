package www.seekerslab.com.seekeschat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.reginald.patternlockview.PatternLockView;

//
public class LockActivity extends AppCompatActivity {

    private static final String TAG = "DemoActivity";

    private TextView textView;

    private PatternLockView lockView;

    private String mPassword = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        textView = findViewById(R.id.lockactivity_textview);
        lockView = (PatternLockView) findViewById(R.id.lock_view);


        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        mPassword = pref.getString("lock_password", null);

        if(getIntent().getBooleanExtra("lockinit", false)){
            lockView.setCallBack(new PatternLockView.CallBack() {
                @Override
                public int onFinish(PatternLockView.Password password) {

                    Log.e("lock", password.string + " 설정 창 ");

                    if (password.string.length() > 3){
                        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("lock_password", password.string );

                        editor.commit();
                        setResult(RESULT_OK);
                        finish();
                        return PatternLockView.CODE_PASSWORD_CORRECT;
                    }else{
                        textView.setText("패턴을 다시 정의 하십시오");
                        return PatternLockView.CODE_PASSWORD_ERROR;
                    }

                }
            });
        }else{
            textView.setText("패턴을 입력하세요");

            lockView.setCallBack(new PatternLockView.CallBack() {
                @Override
                public int onFinish(PatternLockView.Password password) {

                    Log.e("lock", mPassword + " 로그인 창 " + password.toString() + " : " + password.string);

                    if (mPassword.equals(password.string)) {

                        setResult(RESULT_OK);
                        finish();
                        return PatternLockView.CODE_PASSWORD_CORRECT;
                    }else{
                        textView.setText("패턴이 틀렸습니다.");

                        return PatternLockView.CODE_PASSWORD_ERROR;
                    }


                }
            });
        }

        lockView.setOnNodeTouchListener(new PatternLockView.OnNodeTouchListener() {
            @Override
            public void onNodeTouched(int NodeId) {

            }
        });




    }
}
