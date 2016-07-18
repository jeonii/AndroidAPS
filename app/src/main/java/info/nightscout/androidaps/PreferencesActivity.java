package info.nightscout.androidaps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

import info.nightscout.androidaps.events.EventPreferenceChange;
import info.nightscout.androidaps.events.EventRefreshGui;
import info.nightscout.androidaps.interfaces.PluginBase;
import info.nightscout.androidaps.plugins.DanaR.BluetoothDevicePreference;
import info.nightscout.androidaps.plugins.DanaR.DanaRFragment;
import info.nightscout.utils.LocaleHelper;

public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    MyPreferenceFragment myPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPreferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, myPreferenceFragment).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MainApp.bus().post(new EventPreferenceChange());
        if (key.equals("language")) {
            String lang = sharedPreferences.getString("language", "en");
            LocaleHelper.setLocale(getApplicationContext(), lang);
            recreate();
            MainApp.bus().post(new EventRefreshGui());
        }
        updatePrefSummary(myPreferenceFragment.getPreference(key));
    }

    private static  void updatePrefSummary(Preference pref) {
        if (pref instanceof ListPreference || pref instanceof BluetoothDevicePreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            if (pref.getTitle().toString().toLowerCase().contains("password"))
            {
                pref.setSummary("******");
            } else if (editTextPref.getText() != null && !editTextPref.getText().equals("")){
                pref.setSummary(editTextPref.getText());
            }
        }
        if (pref instanceof MultiSelectListPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            pref.setSummary(editTextPref.getText());
        }
    }

    private static void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_language);
            if (Config.CAREPORTALENABLED)
                addPreferencesFromResource(R.xml.pref_careportal);
            addPreferencesFromResource(R.xml.pref_treatments);
            if (Config.APS)
                addPreferencesFromResource(R.xml.pref_closedmode);
            if (Config.OPENAPSMAENABLED)
                addPreferencesFromResource(R.xml.pref_openapsma);
            if (Config.LOWSUSPEDENABLED)
                addPreferencesFromResource(R.xml.pref_lowsuspend);
            addPreferencesFromResource(R.xml.pref_nightscout);
            if (Config.DANAR) {
                addPreferencesFromResource(R.xml.pref_danar);
                DanaRFragment danaRFragment = (DanaRFragment) MainApp.getSpecificPlugin(DanaRFragment.class);
                if (danaRFragment.isEnabled(PluginBase.PROFILE)) {
                    addPreferencesFromResource(R.xml.pref_danarprofile);
                }
            }
            if (Config.MM640G)
                addPreferencesFromResource(R.xml.pref_mm640g);
            if (Config.SMSCOMMUNICATORENABLED)
                addPreferencesFromResource(R.xml.pref_smscommunicator);
            initSummary(getPreferenceScreen());
        }

        public Preference getPreference (String key) {
            return findPreference(key);
        }
    }
}