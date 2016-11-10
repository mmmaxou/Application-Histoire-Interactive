package com.example.maximilienpluchard.inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by maximilien.pluchard on 14/10/16.
 */

public class LoadActivity extends AppCompatActivity {

    static final String PREFS_NAME = "current";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        Script script = new Script();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String save1 = settings.getString("autosave", null);

        if (save1 != null) {
            script.evaluate(save1);
            int frame = script.getInt("frame");
            TextView textView = (TextView) findViewById(R.id.textViewSaveID);
            textView.setText(Integer.toString(frame));

        }


    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

}
