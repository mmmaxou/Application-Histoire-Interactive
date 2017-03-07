package fr.inkarma.Inkarma;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by maximilien.pluchard on 07/03/17.
 */

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Button bt = (Button) findViewById(R.id.buttonMenu);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/besom.ttf");
        bt.setTypeface(font);

//        HorizontalScrollView sv = (HorizontalScrollView) findViewById(R.id.hScrollView);
//        sv.requestDisallowInterceptTouchEvent(true);
//        sv.onInterceptTouchEvent(MotionEvent.ACTION_DOWN);
    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void onClickMain(View view) {
        LinearLayout mainLayout = ( LinearLayout ) findViewById(R.id.mainLayout);
        LinearLayout buttonLayout = ( LinearLayout ) findViewById(R.id.buttonLayout);

        if( buttonLayout.getVisibility() == View.VISIBLE) {
            buttonLayout.setVisibility(View.GONE);
        } else {
            buttonLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return super.onTouchEvent(event);
    }
}
