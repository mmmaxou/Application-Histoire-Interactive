package com.example.maximilienpluchard.inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        script.put("heroName","toto");


        setContentView(R.layout.activity_main);

       // if(newGame =)
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

        // sauvegarde du i
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        // Sauvegarde la frame actuelle i
        script.put("frame", i);
        editor.putString("save",script.serialize());

//        editor.  putSerializable("HashMap",script.getVarMap());

        // Commit the edits!
        editor.commit();

        Frame tmp = data.get(i);

        if(tmp != null) {
            frame = tmp;

            // Declaration des elements
            ImageView background =(ImageView) findViewById(R.id.decorImgBox);
            ImageView personnage =(ImageView) findViewById(R.id.personnage);
            ImageView expression =(ImageView) findViewById(R.id.expression);
            TextView textView = (TextView) findViewById(R.id.BoiteDialogue);
            Button button1 = (Button) findViewById(R.id.choix1);
            Button button2 = (Button) findViewById(R.id.choix2);
            Button buttonNext = (Button) findViewById(R.id.choixNext);




            // Affichage du texte
            textView.setText(script.evaluate("\""+frame.text+"\"").toString());


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


        }
    }

    // Button choix 1
    public void onClick1(View view){
        setFrame(frame.choix[0]);
    }

    // Button choix 2
    public void onClick2(View view){
        setFrame(frame.choix[1]);

    }

    // Button Next
    public void onClick3(View view){
        setFrame(frame.id + 1);
    }

    // Button MENU


    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }


    // onClick

    // setFRame ( frame.choix[0]

    //

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

