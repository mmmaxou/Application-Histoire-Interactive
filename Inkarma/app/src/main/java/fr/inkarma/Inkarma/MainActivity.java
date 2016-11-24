package fr.inkarma.Inkarma;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final String PREFS_NAME = "current";
    private Data data;
    private Frame frame;
    private Boolean gameRunning = false;
    private float x1,x2,y1,y2;
    static float MIN_DISTANCE;
    static float TOUCH_DISTANCE;
    Script script;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        script = new Script();

        setContentView(R.layout.activity_main);

        MIN_DISTANCE = getResources().getDimension(R.dimen.min_distance_swipe);
        TOUCH_DISTANCE = getResources().getDimension(R.dimen.touch_distance_swipe);

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        registerForContextMenu(imageButton);

       // if(newGame)
        //////////chargement du i
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int i;

        String save = settings.getString("autosave", null);
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

        editor.putString("autosave",script.serialize());

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
            TextView locuteur = (TextView) findViewById(R.id.textViewLocuteur);
            Button button1 = (Button) findViewById(R.id.choix1);
            Button button2 = (Button) findViewById(R.id.choix2);
            Button buttonNext = (Button) findViewById(R.id.choixNext);


            //Reglage de la taille du texte
            SharedPreferences reglages = PreferenceManager.getDefaultSharedPreferences(this);
            String a = reglages.getString("pref_textSize", "14");
            Float textSize = Float.parseFloat(a);

            textView.setTextSize(textSize);

            // Affichage du texte
            String text = script.evaluate("'#'+frame+' ('+lastChoiceID+') + '+frameNumber+\""+frame.text+"\"").toString();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(text));
            }

            // On cache les choix
            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);

            // On affiche le nom du personnage si il y en a un, et on affiche son image;
            if ( frame.locuteur != "") {
                String name = frame.locuteur;
                String upperName = name.substring(0, 1).toUpperCase() + name.substring(1);

                locuteur.setText(upperName);
                locuteur.setVisibility(View.VISIBLE);
                personnage.setImageResource(frame.locuteurImg);

                if (frame.expression != -1) {
                    expression.setImageResource(frame.expression);
                } else {
                    int id = getResources().getIdentifier("neutre", "drawable", getPackageName());
                    expression.setImageResource(id);
                }
                personnage.setVisibility(View.VISIBLE);
                expression.setVisibility(View.VISIBLE);
                locuteur.setVisibility(View.VISIBLE);

            } else {
                personnage.setVisibility(View.INVISIBLE);
                expression.setVisibility(View.INVISIBLE);
                locuteur.setVisibility(View.INVISIBLE);
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

            //////////// AUTO SKIP
            autoSkip(textView, debug);

        }
    }
    private void autoSkip(TextView textView, TextView debug) {

        //Recuperation des informations de vitesse de skip
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final Boolean autoSpeed = settings.getBoolean("pref_autoSkip", false);

        String skipSpeedText = settings.getString("pref_autoSkipSpeed", "30");
        int skipSpeed = Integer.parseInt(skipSpeedText);
//      debug.setText(skipSpeedText);

        // Calcul du temps d'auto skip
        int size = textView.length();

        // Duree en secondes
        int speed = (size / skipSpeed) + 1;

        final int id = frame.id;

        //Dailayage
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                //Il n'y a pas de choix; on passe
                if (id == frame.id && gameRunning && autoSpeed) {
                    if (frame.choix[0] == -1 && frame.choix[1] == -1) {
                        if (frame.id < 10000) {
                            int frameNumber = script.getInt("frameNumber");
                            script.put("frameNumber", frameNumber + 1);
                            setFrame(id + 1); //Les variables script sont sauvegardées dans le setFrame
                        } else {
                            TextView textView = (TextView) findViewById(R.id.BoiteDialogue);
                            textView.setText("fin");
                        }
                    }
                }
                // Il y a un choix, on l'affiche
                displayChoice();
            }
        }, speed * 1000); // xx000ms delay

    }

    private void displayChoice() {
        if (frame.choix[0] != -1 && frame.choix[1] != -1 && gameRunning) {
            TextView debug = (TextView) findViewById(R.id.debug);
            TextView locuteur = (TextView) findViewById(R.id.textViewLocuteur);
            Button button1 = (Button) findViewById(R.id.choix1);
            Button button2 = (Button) findViewById(R.id.choix2);

            debug.setText("Next");
            locuteur.setVisibility(View.GONE);
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
        }
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
    }

    // Button Next
    public void onClick3(View view){

        next();

    }

    private void next() {
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
        before();
    }

    private void before() {
        int lastChoiceID = script.getInt("lastChoiceID");
        int frameNumber = script.getInt("frameNumber");

        if ( frameNumber > 0 ) {

            script.put("frameNumber", frameNumber - 1);
            setFrame(lastChoiceID + frameNumber - 1); // Affiche la frame du dernier choix + le nombre de frames passées avant - 1

        }
    }






    // Button Settings
    public void onClickSettings(View view) {
        gameRunning = false;
        Log.d("State : ", gameRunning.toString());
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

                new AlertDialog.Builder(this)
                        .setTitle("Retour au menu Principal")
                        .setMessage("Etes-vous sur de retourner au Menu principal. Cette action peut entrainer une perte des données non sauvegardées. Sauvegardez votre partie avant en cliquant sur Menu > Sauvegarder.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // yes
                                startMenuActivity();

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;

            case R.id.action_save :
                Intent intent2 = new Intent(this, LoadActivity.class);
                intent2.putExtra("EXTRA_STATE_SAVE", "Oui");
                startActivity(intent2);
                return true;

            case R.id.action_load :
                Intent intent3 = new Intent(this, LoadActivity.class);
                startActivity(intent3);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {

        gameRunning = true;
        super.onContextMenuClosed(menu);
    }

    public void startMenuActivity () {
        Intent intent1 = new Intent(this, MenuActivity.class);
        startActivity(intent1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.first_layout);


        if ( layout.getVisibility() == View.GONE ) {

            gameRunning = true;
            layout.setVisibility(View.VISIBLE);
            return super.onTouchEvent(event);

        }
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;
                Log.d("Debug", "x1 : " + x1 + " ; x2 : " + x2 + " delta : " + deltaX);
                if (deltaX > MIN_DISTANCE)
                {
//                    Toast.makeText(this, "left2right swipe", Toast.LENGTH_SHORT).show ();
                    before();

                }
                else if ( Math.abs(deltaX) < TOUCH_DISTANCE && Math.abs(deltaY) < TOUCH_DISTANCE )
                {
//                    Toast.makeText(this, "Touch", Toast.LENGTH_SHORT).show ();
                    next();
                }
                else if ( Math.abs(deltaX) < TOUCH_DISTANCE && deltaY > MIN_DISTANCE * 2 )
                {
//                    Toast.makeText(this, "Background Show", Toast.LENGTH_SHORT).show ();
                    gameRunning = false;
                    layout.setVisibility(View.GONE);
                }
                break;
        }
        return super.onTouchEvent(event);
    }







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

