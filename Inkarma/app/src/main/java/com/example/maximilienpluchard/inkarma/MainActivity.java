package com.example.maximilienpluchard.inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final String PREFS_NAME = "jkhkj";
    private Data data;
    private Frame frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        script = new Script();


        setContentView(R.layout.activity_main);

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        registerForContextMenu(imageButton);

       // if(newGame)
        //////////chargement du i
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int i;

        String save = settings.getString("save", null);
        if ( save != null ) {
            script.evaluate(save);
            i = script.getInt("frame"); // Initialise le i au travers de la frame
        } else {
            i = 1;
        }


        try {
            data = new Data(this);

            ////// obtenir et afficher page 1 --> setFrame(int)
            setFrame(i); // le i


        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void setFrame(int i) {


        // Sauvegarde la frame actuelle i
        script.put("frame", i);

        // Sauvegarde du i
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("save",script.serialize());

        // Commit the edits!
        editor.apply();

        Frame tmp = data.get(i);

        if(tmp != null) {
            frame = tmp;

            // Declaration des elements
            ImageView background =(ImageView) findViewById(R.id.decorImgBox);
            ImageView personnage =(ImageView) findViewById(R.id.personnage);
            ImageView expression =(ImageView) findViewById(R.id.expression);
            TextView textView = (TextView) findViewById(R.id.BoiteDialogue);
            TextView debug = (TextView) findViewById(R.id.debug);
            Button button1 = (Button) findViewById(R.id.choix1);
            Button button2 = (Button) findViewById(R.id.choix2);
            Button buttonNext = (Button) findViewById(R.id.choixNext);



            // Affichage du texte
            textView.setText(script.evaluate("'#'+frame+' ('+lastChoiceID+') + '+frameNumber+\""+frame.text+"\"").toString());


            // Affichage des choix
            if (frame.choix[0] == -1){
                button1.setVisibility(View.GONE);
            } else {
                button1.setVisibility(View.VISIBLE);
            }

            if (frame.choix[1] == -1){
                button2.setVisibility(View.GONE);
            } else {
                button2.setVisibility(View.VISIBLE);
            }

            // Si il n'y a pas de choix, on affiche un bouton pour passer à la frame suivante.
            if (frame.choix[0] == -1 && frame.choix[1] == -1) {
                buttonNext.setVisibility(View.VISIBLE);
            } else {
                buttonNext.setVisibility(View.GONE);
            }

            // Affichage des images
            if (frame.img != -1) {
                background.setImageResource(frame.img);
            }
            if (frame.personnage != -1) {
                personnage.setImageResource(frame.personnage);
            }
            if (frame.expression != -1) {
                expression.setImageResource(frame.expression);
            }

            //////////// AUTO SKIP
            autoSkip(textView, debug);

        }
    }

    private void autoSkip(TextView textView, TextView debug) {
        // Calcul du temps d'auto skip
        int size = textView.length();
        String sizeText = Integer.toString(size);

        // Duree en secondes
        long small = size / 25;
        long moyen = size / 30;
        long rapide = size / 35;

        //Affichage
        debug.setText(sizeText);

        //Dailayage
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (frame.choix[0] == -1 && frame.choix[1] == -1) {
                    setFrame(frame.id + 1);
                }
                else {
                    TextView debug = (TextView) findViewById(R.id.debug);
                    debug.setText("Next");
                }
            }

        }, moyen * 1000); // 5000ms delay
    }

    // Button choix 1
    public void onClick1(View view){
        script.put("lastChoiceID", frame.id);
        script.put("frameNumber", 0);
        setFrame(frame.choix[0]); //Les variables script sont sauvegardées dans le setFrame
    }

    // Button choix 2
    public void onClick2(View view){
        script.put("lastChoiceID", frame.id);
        script.put("frameNumber", 0);
        setFrame(frame.choix[1]); //Les variables script sont sauvegardées dans le setFrame
    };

    // Button Next
    public void onClick3(View view){

        if ( frame.id < 10000 ) {
            int frameNumber = script.getInt("frameNumber");
            script.put("frameNumber",frameNumber + 1 );
            setFrame(frame.id + 1); //Les variables script sont sauvegardées dans le setFrame
        } else {
            TextView textView = (TextView) findViewById(R.id.BoiteDialogue);
            textView.setText("fin");
        }

    }


    // Button Before
    public void onClickBefore(View view) {
        int lastChoiceID = script.getInt("lastChoiceID");
        int frameNumber = script.getInt("frameNumber");
        Log.d("OnClickBefore",lastChoiceID+" "+frameNumber+ "-1");

        if ( frameNumber > 0 ) {
            setFrame(lastChoiceID + frameNumber - 1); // Affiche la frame du dernier choix + le nombre de frames passées avant - 1
        }
    }



    // Button Settings
    public void onClickSettings(View view) {
        openContextMenu(view);
    }

       @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings :
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_menu :
                Intent intent1 = new Intent(this, MenuActivity.class);
                startActivity(intent1);
                return true;

            case R.id.action_save :
                return true;

            case R.id.action_load :
                Intent intent2 = new Intent(this, LoadActivity.class);
                startActivity(intent2);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Script script;









    // Affichage caractère par caracère
/*
    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            tv.setText(mText.subSequence(0, mIndex++));
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        tv.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
*/
}

