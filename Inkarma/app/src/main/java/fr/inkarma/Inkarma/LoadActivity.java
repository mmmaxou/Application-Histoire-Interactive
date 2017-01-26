package fr.inkarma.Inkarma;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by maximilien.pluchard on 14/10/16.
 */

public class LoadActivity extends AppCompatActivity {

    private Data data;
    private Frame frame;
    static final String PREFS_NAME = "current";
    Script script;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        Script script = new Script();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        try {
            data = new Data(this);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // AFFICHAGE DES SAUVEGARDES

        String[] saves = {
                settings.getString("save1", null),
                settings.getString("save2", null),
                settings.getString("save3", null),
                settings.getString("save4", null)
        };
        ImageView[] images = {
                (ImageView) findViewById(R.id.imageViewSaveID1),
                (ImageView) findViewById(R.id.imageViewSaveID2),
                (ImageView) findViewById(R.id.imageViewSaveID3),
                (ImageView) findViewById(R.id.imageViewSaveID4)
        };
        TextView[] texts = {
                (TextView) findViewById(R.id.TextViewSaveID1),
                (TextView) findViewById(R.id.TextViewSaveID2),
                (TextView) findViewById(R.id.TextViewSaveID3),
                (TextView) findViewById(R.id.TextViewSaveID4)
        };
        TextView[] dates = {
                (TextView) findViewById(R.id.textViewDateSaveID1),
                (TextView) findViewById(R.id.textViewDateSaveID2),
                (TextView) findViewById(R.id.textViewDateSaveID3),
                (TextView) findViewById(R.id.textViewDateSaveID4)
        };

        displaySave(script, saves, images, texts, dates);

        // REGARDE SI ON TENTE D'ENREGISTRER
        if ( getIntent().getStringExtra("EXTRA_STATE_SAVE") != null ) {
            String savingState = getIntent().getStringExtra("EXTRA_STATE_SAVE");
            if ( savingState.equals("Oui")) {
                // On tente d'enregistrer
                savingState(saves, images);
            }
        }

    }

    private void displaySave(Script script, String[] saves, ImageView[] images, TextView[] texts, TextView[] dates) {
        String save;
        ImageView imageView;
        TextView textView;
        TextView textViewDate;

        for ( int i= 0 ; i < saves.length ; i++) {
            save = saves[i];
            imageView = images[i];
            textView = texts[i];
            textViewDate = dates[i];

            if (save != null) {

                script.evaluate(save);
                Frame tmp = data.get(script.getInt("frame"));

                if (tmp != null) {
                    frame = tmp;

                    SimpleDateFormat simpleDate = new SimpleDateFormat("'le' dd.MM 'à' h:mm a");
                    long date = Long.valueOf(script.getString("date"));

                    script.evaluate(save);
                    imageView.setImageResource(frame.img);
                    textView.setVisibility(View.GONE);
                    textViewDate.setVisibility(View.VISIBLE);

                    textViewDate.setText(simpleDate.format(date));

                    if ( getIntent().getStringExtra("EXTRA_STATE_SAVE") == null ) {
                        final int finalI = i;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                loadState(finalI);

                            }
                        });
                    }
                }
            }
        }
    }

    private void savingState(String[] saves, ImageView[] images) {
        String save;
        ImageView imageView;
        TextView textView;
        for ( int i= 0 ; i < saves.length ; i++) {
            save = saves[i];
            imageView = images[i];

            if ( save == null) {
                final int finalI = i;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        saveState(finalI);

                    }
                });
            } else {
                final int finalI1 = i;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        overwriteSaveState(finalI1);
                    }
                });
            }
        }
    }

    private void loadState(int i) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String saveNumber = "save" + Integer.toString(i + 1);
        String save = settings.getString(saveNumber,null);

        editor.putString("autosave", save);
        editor.apply();

        Log.d("Save Content : ",save);

        Toast.makeText(this, "Chargement effectué", Toast.LENGTH_SHORT).show ();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity



    }

    private void overwriteSaveState (final int i) {
        new AlertDialog.Builder(this)
                .setTitle("Ecraser la sauvegarde ?")
                .setMessage("Etes-vous sur d'écraser la sauvegarde déja existante ? Les données ne pourront plus être récupérées.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // yes
                        saveState( i);

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
    private void saveState(int i) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String save = settings.getString("autosave",null);
        String saveNumber = "save" + Integer.toString(i + 1);
        script = new Script();

        long date = System.currentTimeMillis();
        String dateString = Long.toString(date);

        script.evaluate(save);
        script.put("date", dateString);

        Log.d("Debug : ", script.serialize());

        editor.putString(saveNumber, script.serialize());
        editor.apply();

        Toast.makeText(this, "Sauvegarde effectué", Toast.LENGTH_SHORT).show ();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

}
