package com.example.maximilienpluchard.inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void onClickPlay(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        boolean newGame = false;
    }

    public void onClickNewGame(View view) {

        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("frame", 1);

        // Commit the edits!
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


    }

    public void onClickCredit(View view) {
        Intent intent = new Intent(this, CreditActivity.class);
        startActivity(intent);
    }


}
