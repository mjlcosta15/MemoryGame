package pt.isec.pem.memory;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import pt.isec.pem.memory.R;

public class MyPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }


}
