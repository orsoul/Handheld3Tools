package com.fanfull.handheldtools.ui.fragmet;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.fanfull.handheldtools.R;

public class MySettingsFragment extends PreferenceFragmentCompat {
  @Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences_settings, rootKey);
  }
}
