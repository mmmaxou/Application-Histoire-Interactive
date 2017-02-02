package fr.inkarma.Inkarma;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(fr.inkarma.Inkarma.R.xml.preferences);

//        Preference button = findPreference("back_button");
//        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                //code for what you want it to do
//
//
//                Log.d("Preference", "retour");
//                getActivity().getFragmentManager().popBackStackImmediate();
//
//                return true;
//            }
//        });
    }

}
