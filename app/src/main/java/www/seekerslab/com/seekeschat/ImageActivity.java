package www.seekerslab.com.seekeschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;

//
public class ImageActivity extends AppCompatActivity {

    ZoomageView imageView;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.imageactivity_imageview);
        textView = findViewById(R.id.imageactivity_textview_id);

        textView.setText(getIntent().getStringExtra("name"));

        Glide.with(this).load(getIntent().getStringExtra("imageurl")).centerCrop().into(imageView);


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.scale, R.anim.scaleback);
    }
}
