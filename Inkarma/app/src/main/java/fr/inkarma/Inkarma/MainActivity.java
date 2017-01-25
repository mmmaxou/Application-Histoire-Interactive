package fr.inkarma.Inkarma;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String PREFS_NAME = "current";
    static float MIN_DISTANCE;
    static float TOUCH_DISTANCE;
    private Data data;
    private Frame frame;
    List<String> historique = new ArrayList<>();
    List<Spanned> historiqueText = new ArrayList<>();
    ListView historiqueView;
    private Boolean gameRunning = false;
    private float x1,x2,y1,y2;
    Script script;
    private Handler handler;
    private MediaPlayer mediaPlayer;
    private String currentImage;


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
        //////////chargement du i qui est la frame sur laquelle on s'est arreté
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


        // On lance la musique dernièrement chargée
        playSound(script.getInt("music"));

        //IMAGE SWITCHER

        final ImageSwitcher myImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher1);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f        );

        ImageView enabledChart = new ImageView(getBaseContext());
        enabledChart.setScaleType(ImageView.ScaleType.CENTER_CROP);
        enabledChart.setImageResource(frame.img);
        myImageSwitcher.addView(enabledChart, param);

        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        myImageSwitcher.setOutAnimation(animationOut);
        myImageSwitcher.setInAnimation(animationIn);

        currentImage = frame.imgTag;
    }

    private void setFrame(int i) {

        if ( handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // Sauvegarde la frame actuelle i
        script.put("frame", i);

        // Sauvegarde du i
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putString("autosave",script.serialize());

        // Commit the edits!
        editor.apply();

        Frame tmp = data.get(i);
        if(tmp != null) {
            frame = tmp;

            // On relance l'autoRun  :
            setGameRunning( true );

            // Declaration des elements
//            ImageView background =(ImageView) findViewById(R.id.decorImgBox);
            ImageSwitcher myImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher1);
            ImageView personnage =(ImageView) findViewById(R.id.personnage);
            ImageView expression =(ImageView) findViewById(R.id.expression);
            TextView textView = (TextView) findViewById(R.id.BoiteDialogue);
            final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.first_layout);
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

//            String text = script.evaluate("'#'+frame+' ('+lastChoiceID+') + '+frameNumber+\""+frame.text+"\"").toString();
            String text = script.evaluate("'#'+frame+' | karma:'+karma+\""+frame.text+"\"").toString();
//            String text = script.evaluate("karma=karma+1;'karma:'+karma+\""+frame.text+"\"").toString();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(text));
            }

            // On regarde si il faux modifier le karma
            if ( frame.karma != 0 && frame.karmaEvaluated == false) {
                int currentKarma = script.getInt("karma");
                script.put("karma", currentKarma + frame.karma);
                frame.karmaEvaluated = true;
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

//            // Si il n'y a pas de choix, on affiche un bouton pour passer à la frame suivante.
//            if (frame.choix[0] == -1 && frame.choix[1] == -1) {
//                buttonNext.setVisibility(View.VISIBLE);
//            } else {
//                buttonNext.setVisibility(View.GONE);
//            }


            // On lance la musique

            if ( frame.music != -1) {
                playSound(frame.music);
            }


            Log.d("debug image :", " ---- Frame : " + frame.imgTag + " ------ memory : " + currentImage);

            // Affichage des images
            if (frame.img != -1) {

                if ( frame.imgTag != currentImage) {

                    // Creation de la nouvelle image

                    ImageView disabledChart = new ImageView(getBaseContext());
                    disabledChart.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    disabledChart.setImageResource(frame.img);

                    // Ajout de la nouvelle.

                    myImageSwitcher.removeView(myImageSwitcher.getNextView());
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f
                    );
                    myImageSwitcher.addView(disabledChart, param);
                    myImageSwitcher.showNext();

                    currentImage = frame.imgTag;

                }

            }



            //Affichage du layout
            mainLayout.setVisibility(View.VISIBLE);


            // Affichage de l'Historique
            historiqueView = (ListView) findViewById(R.id.ListViewHistorique);
            ArrayAdapter<Spanned> arrayAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    historiqueText);

            historiqueView.setAdapter(arrayAdapter);
            historiqueView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    setGameRunning( true );
                    historiqueView.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    String save = historique.get( position );
                    editor.putString("autosave", save);
                    script.evaluate(save);

                    Log.d("DEBUG : ", "position : " + position + "    ; id : " + id);

                    int size = historique.size();
                    for ( int i = position; i < size ; i++) {
                        historique.remove( historique.size() - 1 );
                        historiqueText.remove( historiqueText.size() - 1 );
                    }

                    setFrame(script.getInt("frame"));

                }
            });

            //On enregistre la frame courante dans l'historique
            addToHistorique(settings);

            //////////// AUTO SKIP
            autoSkip(textView);

        }
    }

    private void addToHistorique(SharedPreferences settings) {
        String currentSave = settings.getString("autosave", null);

        if (historique.size() >= 10 ) {
            historique.remove(0);
            historiqueText.remove(0);
        }

        historique.add(currentSave);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            historiqueText.add(Html.fromHtml(frame.text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            historiqueText.add(Html.fromHtml(frame.text));
        }
    }

    private void autoSkip(TextView textView) {

        //Recuperation des informations de vitesse de skip
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final Boolean autoSpeed = settings.getBoolean("pref_autoSkip", false);

        String skipSpeedText = settings.getString("pref_autoSkipSpeed", "30");
        int skipSpeed = Integer.parseInt(skipSpeedText);

        // Calcul du temps d'auto skip
        int size = textView.length();
        // On essaie d'afficher le choix
        displayChoice();

        // Duree en secondes
        int speed = (size / skipSpeed) + 2;

        final int id = frame.id;

        //Dailayage
        handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                //Il n'y a pas de choix; on passe
                if (id == frame.id && getGameRunning() && autoSpeed) {
                    if (frame.choix[0] == -1 && frame.choix[1] == -1) {
                        next();
                    }
                }
            }
        }, speed * 1000); // xx000ms delay

    }

    private void displayChoice() {
        if (frame.choix[0] != -1 && frame.choix[1] != -1 && getGameRunning() ) {
            TextView locuteur = (TextView) findViewById(R.id.textViewLocuteur);
            Button button1 = (Button) findViewById(R.id.choix1);
            Button button2 = (Button) findViewById(R.id.choix2);

            locuteur.setVisibility(View.GONE);
            button1.setText(frame.choixText[0]);
            button2.setText(frame.choixText[1]);
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
        }
    }

    // Button choix 1
    public void onClick1(View view){
//        script.put("lastChoiceID", frame.id);
//        script.put("frameNumber", 0);
        setFrame(frame.choix[0]); //Les variables script sont sauvegardées dans le setFrame
    }

    // Button choix 2
    public void onClick2(View view){
//        script.put("lastChoiceID", frame.id);
//        script.put("frameNumber", 0);
        setFrame(frame.choix[1]); //Les variables script sont sauvegardées dans le setFrame
    }

    // Button Next
    public void onClick3(View view){

        next();

    }

    // Button Auto
    public void onClickAuto(View view) {

        Frame currentFrame = frame;
        if ( currentFrame.choix[0] == -1 || currentFrame.choix[1] == -1) {
            while ( currentFrame.suivant.choix[0] == -1 || currentFrame.suivant.choix[1] == -1 ) {
                currentFrame = currentFrame.suivant;
                if ( currentFrame.karma != 0 && currentFrame.karmaEvaluated == false) {
                    int currentKarma = script.getInt("karma");
                    script.put("karma", currentKarma + currentFrame.karma);
                    data.get(currentFrame.id).karmaEvaluated = true;
                }
            }
            historique.clear();
            historiqueText.clear();
            setFrame(currentFrame.suivant.id);
        }
    }

    private void next() {
        if ( frame.id < 1000000 ) {
//            int frameNumber = script.getInt("frameNumber");
//            script.put("frameNumber",frameNumber + 1 );

            //Si il n'y a qu'un choix, on va a cet endroit
            if (frame.choix[0] != -1 && frame.choix[1] == -1 ){
                data.get(frame.choix[0]).precedent = frame;
                setFrame(frame.choix[0]);
            } else if ( frame.choix[0] == -1 || frame.choix[1] == -1) {
                setFrame(frame.suivant.id); //Les variables script sont sauvegardées dans le setFrame
            }
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

        if ( historique.size() >= 2 ) {
            historique.remove(historique.size() - 1);
            historique.remove(historique.size() - 1);
            historiqueText.remove(historiqueText.size() - 1);
            historiqueText.remove(historiqueText.size() - 1);
        }
        if ( frame.precedent != null ) {
            if ( frame.precedent.choix[0] == -1 || frame.precedent.choix[1] == -1 ) {

                setFrame(frame.precedent.id); // Affiche la frame du dernier choix + le nombre de frames passées avant - 1
            }
        }
    }

    // Button Settings
    public void onClickSettings(View view) {
        setGameRunning( false );
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

        setGameRunning( false );

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
            case R.id.action_historique :
                LinearLayout layout = (LinearLayout) findViewById(R.id.first_layout);
                ListView listView = (ListView) findViewById(R.id.ListViewHistorique);

                listView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);

                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
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

            setGameRunning( true );

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
                else if ( -deltaX > MIN_DISTANCE) {
                    next();
                }
                else if ( Math.abs(deltaX) < TOUCH_DISTANCE && Math.abs(deltaY) < TOUCH_DISTANCE )
                {
//                    Toast.makeText(this, "Touch", Toast.LENGTH_SHORT).show ();
                    next();
                }
                else if ( Math.abs(deltaX) < TOUCH_DISTANCE && deltaY > MIN_DISTANCE * 2 )
                {
//                    Toast.makeText(this, "Background Show", Toast.LENGTH_SHORT).show ();
                    setGameRunning( false );
                    layout.setVisibility(View.GONE);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean getGameRunning () {

        Log.d("Game Running state : " , gameRunning.toString());
        return gameRunning;

    }

    private void setGameRunning( boolean state) {

        Log.d("GameRunning state : " , gameRunning.toString());
        gameRunning = state;

    }

    private void playSound(int music) {

        if ( music == -1) {
            Toast.makeText(this, "Musique non trouvée", Toast.LENGTH_SHORT).show();
        } else {
            // Initialisation
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, music);
                mediaPlayer.start();
                mediaPlayer.setLooping(true);

                script.put("music", music);

            }
            if ( mediaPlayer != null && mediaPlayer.isPlaying() ) {
                int currentMusic = script.getInt("music");
                if ( currentMusic != music) {
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(this, music);
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);

                    script.put("music", music);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    // Affichage caractère par caractère
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

