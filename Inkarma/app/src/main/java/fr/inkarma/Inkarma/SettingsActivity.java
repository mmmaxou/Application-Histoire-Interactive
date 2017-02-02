package fr.inkarma.Inkarma;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by XullMaster on 08/11/2016.
 */

public class SettingsActivity extends AppCompatActivity{

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

}

