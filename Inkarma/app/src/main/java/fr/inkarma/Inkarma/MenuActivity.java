package fr.inkarma.Inkarma;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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


        SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean musique = params.getBoolean("pref_musique", false);
        if ( musique ) {
            mediaPlayer = MediaPlayer.create(this, R.raw.yggdraop);
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

    public void onClickNewGame(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        createNewSave();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

    private void createNewSave() {

        Script script = new Script();

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


        SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean musique = params.getBoolean("pref_musique", false);

        if ( mediaPlayer == null && musique) {
            mediaPlayer = MediaPlayer.create(this, R.raw.yggdraop);
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }

    // On empeche de quitter en appuyant sur back
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Quitter le jeu")
                .setMessage("Etes-vous sur de vouloir quitter le jeu ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // yes
                        MenuActivity.super.onBackPressed();

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}

