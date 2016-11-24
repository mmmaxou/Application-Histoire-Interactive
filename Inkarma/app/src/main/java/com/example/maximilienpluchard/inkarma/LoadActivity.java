package com.example.maximilienpluchard.inkarma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by maximilien.pluchard on 14/10/16.
 */

public class LoadActivity extends AppCompatActivity {

    private Data data;
    private Frame frame;
    static final String PREFS_NAME = "current";

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

        String save = settings.getString("save1", null);
        ImageView imageView = (ImageView) findViewById(R.id.imageViewSaveID1);
        loadSave(script, save, imageView);

        save = settings.getString("save2", null);
        imageView = (ImageView) findViewById(R.id.imageViewSaveID2);
        loadSave(script, save, imageView);

        save = settings.getString("save3", null);
        imageView = (ImageView) findViewById(R.id.imageViewSaveID3);
        loadSave(script, save, imageView);

        save = settings.getString("save4", null);
        imageView = (ImageView) findViewById(R.id.imageViewSaveID4);
        loadSave(script, save, imageView);
    }

    private void loadSave(Script script, String save, ImageView imageView) {
        if (save != null) {

            script.evaluate(save);
            Frame tmp = data.get(script.getInt("frame"));
            if(tmp != null) {
                frame = tmp;

                script.evaluate(save);
                imageView.setImageResource(frame.img);

            }
        }
    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

}
