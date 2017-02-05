package fr.inkarma.Inkarma;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * Created by XullMaster on 05/02/2017.
 */

public class TutoActivity extends AppCompatActivity {

    private TextSwitcher mSwitcher;
    String textToShow[]={
            "Tutoriel sur les commandes du jeu",
            "Swipe vers la gauche ou tape sur l'écran pour passer a la slide suivante !",
            "Swipe vers la droite pour revenir a la slide précédente !",
            "Swipe vers le bas pour regarder plus en détail le background !",
            "Lorsque le texte est en train de défiler, tape une fois pour l'afficher en entier !",
            "Pour changer la vitesse de défilement, la taille de la police et les autres parametres, tape sur les trois petites lignes en haut a droite !",
            "C'est parti. Bon jeu !"
    };
    int messageCount=textToShow.length;
    int currentIndex=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuto);

        mSwitcher = (TextSwitcher) findViewById(R.id.switcher);

        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(TutoActivity.this);
                Typeface font = Typeface.createFromAsset(getAssets(), "fonts/besom.ttf");
                myText.setTypeface(font);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(36);
                myText.setTextColor(Color.parseColor("#BA5DB6"));
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        // set the animation type of textSwitcher
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);

        next();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                next();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void next() {
        currentIndex++;
        if ( currentIndex == messageCount) {
            //Do something
            currentIndex = 0;
            goToMain();
        }
        mSwitcher.setText(textToShow[currentIndex]);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

}
