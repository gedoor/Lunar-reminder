package gedoor.kunfei.lunarreminder.ui.activity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import androidx.appcompat.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.async.UpdateCalendar;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static SharedPreferences preferences;
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value)-> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            // For all other preferences, set the summary to the value's
            preference.setSummary(stringValue);
        }
        return true;
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);

    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AppPreferenceFragment.class.getName().equals(fragmentName)
                || CalendarPreferenceFragment.class.getName().equals(fragmentName)
                || EventPreferenceFragment.class.getName().equals(fragmentName)
                || SolarTermsPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * 应用设置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_app);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_app_theme)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Calendar设置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_reminder_calendar);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_google_account)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_lunar_reminder_calendar_id)));

        }
        // 添加菜单
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_pref, menu);
            super.onCreateOptionsMenu(menu,inflater);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            } else if (id == R.id.action_apply) {
                applyCalendarSetting();
            }
            return super.onOptionsItemSelected(item);
        }

        private void applyCalendarSetting() {
            String calendarId = preferences.getString(getString(R.string.pref_key_lunar_reminder_calendar_id), null);
            int bgColor = preferences.getInt(getString(R.string.pref_key_reminder_calendar_color), 0);
            ProgressDialog dialog = ProgressDialog.show(this.getActivity(), "提示", "正在更新日历设置");
            new UpdateCalendar(this.getActivity(), dialog, calendarId, bgColor).execute();
        }
    }

    /**
     * Event设置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class EventPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_reminder_event);
            setHasOptionsMenu(true);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            if (sharedPreferences.getString(getString(R.string.pref_key_repeat_year), null) == null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.pref_key_repeat_year), getString(R.string.pref_value_repeat_year));
                editor.apply();
            }

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_repeat_year)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_default_reminder)));
            findPreference(getString(R.string.pref_key_repeat_year)).setOnPreferenceClickListener((Preference preference)->{
                selectRepeatYear(preference);
                return true;
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void selectRepeatYear(Preference preference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle(R.string.select_repeat_num);
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this.getActivity()).inflate(R.layout.dialog_repeat_num, null);
            NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker_repeat_year);
            numberPicker.setMaxValue(36);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Integer.parseInt(preferences.getString(getString(R.string.pref_key_repeat_year), getString(R.string.pref_value_repeat_year))));
            builder.setView(view);
            builder.setPositiveButton(R.string.ok,(DialogInterface dialog, int which)->{
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.pref_key_repeat_year), String.valueOf(numberPicker.getValue()));
                editor.apply();
                preference.setSummary(String.valueOf(numberPicker.getValue()));
            });
            builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which)->{

            });
            builder.create();
            builder.show();
        }

    }

    /**
     * 节气日历设置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SolarTermsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_solar_terms_calendar);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_google_account)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_solar_terms_calendar_id)));
        }
        // 添加菜单
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_pref, menu);
            super.onCreateOptionsMenu(menu,inflater);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            } else if (id == R.id.action_apply) {
                applyCalendarSetting();
            }
            return super.onOptionsItemSelected(item);
        }

        private void applyCalendarSetting() {
            String calendarId = preferences.getString(getString(R.string.pref_key_solar_terms_calendar_id), null);
            int bgColor = preferences.getInt(getString(R.string.pref_key_solar_terms_calendar_color), 0);
            ProgressDialog dialog = ProgressDialog.show(this.getActivity(), "提示", "正在更新日历设置");
            new UpdateCalendar(this.getActivity(), dialog, calendarId, bgColor).execute();
        }

    }

}
