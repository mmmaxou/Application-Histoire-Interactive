package fr.inkarma.Inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    static final String PREFS_NAME = "current";
    private MediaPlayer mediaPlayer;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button button1 = (Button) findViewById(R.id.buttonPlay);
        Button button2 = (Button) findViewById(R.id.buttonLoad);

        if (saveExists()){

            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);

        } else {

            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
        }


        mediaPlayer = MediaPlayer.create(this, R.raw.knowingsecret);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
    }

    public boolean saveExists(){

        boolean valid = false;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String save = settings.getString("autosave", null);
        if (save != null) {
            valid = true;
        }

        return valid;
    }

    public void onClickPlay(View view) {
        mediaPlayer.release();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickNewGame(View view) {

        mediaPlayer.release();

        createNewSave();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void createNewSave() {

        Script script = new Script();

        script.put("heroName","toto");
        script.put("frame", 100);
        script.put("frameNumber", 0);
        script.put("karma", 0);
        script.put("lastChoiceID", 100);

        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("autosave",script.serialize());

        // Commit the edits!
        editor.apply();
    }

    public void onClickLoad(View view) {
        Intent intent = new Intent(this, LoadActivity.class);
        startActivity(intent);
    }

    public void onClickParams(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if ( mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if ( mediaPlayer == null ) {
            mediaPlayer = MediaPlayer.create(this, R.raw.knowingsecret);
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }
}
