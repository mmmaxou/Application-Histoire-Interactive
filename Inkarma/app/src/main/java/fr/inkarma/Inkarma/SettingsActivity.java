package fr.inkarma.Inkarma;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SeekBarPreference _seekBarPrefTextSize;
        private SeekBarPreference _seekBarPrefDefilement;
        private Activity activity;

        String valueTextSize = "text_size";
        int minTextSize = 10;

        String valueDefilement = "defilement";
        int minDefilement = 10;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);


            // Get widgets :
            _seekBarPrefTextSize = (SeekBarPreference) this.findPreference(valueTextSize);
            _seekBarPrefDefilement = (SeekBarPreference) this.findPreference(valueDefilement);

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            // Set seekbar summary :
            int radiusTextSize = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt(valueTextSize, minTextSize) + minTextSize;
            _seekBarPrefTextSize.setSummary(this.getString(R.string.settings_summary_size).replace("$1", ""+radiusTextSize));
            int radiusDefilement = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt(valueDefilement, minDefilement) + minDefilement;
            _seekBarPrefDefilement.setSummary(this.getString(R.string.settings_summary_defilement).replace("$1", ""+radiusDefilement));

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            // Set seekbar summary :
            int radiusTextSize = minTextSize, radiusDefilement = minDefilement;
            activity = getActivity();
            if ( activity != null && isAdded() ) {
                radiusTextSize = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt(valueTextSize, minTextSize) + minTextSize;
                _seekBarPrefTextSize.setSummary(this.getString(R.string.settings_summary_size).replace("$1", ""+radiusTextSize));

                radiusDefilement = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt(valueDefilement, minDefilement) + minDefilement;
                _seekBarPrefDefilement.setSummary(this.getString(R.string.settings_summary_defilement).replace("$1", ""+radiusDefilement));
            }
        }

    }

}

