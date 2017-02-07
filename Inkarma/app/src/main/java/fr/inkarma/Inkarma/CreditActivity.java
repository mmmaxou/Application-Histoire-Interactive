package fr.inkarma.Inkarma;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CreditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        TextView tv = (TextView) findViewById(R.id.textViewCreditTitle);
        Button bt = (Button) findViewById(R.id.buttonMenu);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/besom.ttf");
        tv.setTypeface(font);
        bt.setTypeface(font);

    }



    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
