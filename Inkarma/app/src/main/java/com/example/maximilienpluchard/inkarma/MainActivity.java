package com.example.maximilienpluchard.inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
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
        setContentView(R.layout.activity_main);

       // if(newGame =)
        //////////chargement du i
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int i = settings.getInt("frame", 1);

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
        editor.putInt("frame", i);

        // Commit the edits!
        editor.commit();



        Frame tmp = data.get(i);

        if(tmp != null) {
            frame = tmp;

            ImageView imgView = (ImageView) findViewById(R.id.decorBoxImg);
            if (frame.img != -1) {
                imgView.setImageResource(frame.img);
            }

            TextView textView = (TextView) findViewById(R.id.BoiteDialogue);
            textView.setText(frame.text);

            if (frame.choix[0] == -1){
                Button button1 = (Button) findViewById(R.id.choix1);
                button1.setEnabled(false);
            } else {
                Button button1 = (Button) findViewById(R.id.choix1);
                button1.setEnabled(true);
            }

            if (frame.choix[1] == -1){
                Button button2 = (Button) findViewById(R.id.choix2);
                button2.setEnabled(false);
            } else {
                Button button2 = (Button) findViewById(R.id.choix2);
                button2.setEnabled(true);
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

}
