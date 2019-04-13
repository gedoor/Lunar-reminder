package gedoor.kunfei.lunarreminder.async;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;

public class InsertCalendar extends CalendarAsyncTask {
    private static final String TAG = "AsyncInsertCalendar";
    private final String calendarName;
    private String calendarPrefKey;
    private String calendarId;

    InsertCalendar(BaseActivity activity, String calendarName, String calendarPrefKey) {
        super(activity);
        this.calendarName = calendarName;
        this.calendarPrefKey = calendarPrefKey;
    }

    @Override
    protected void doInBackground() throws IOException {
        Calendar mCalendar = new com.google.api.services.calendar.model.Calendar();
        mCalendar.setSummary(calendarName);
        Calendar calendar = client.calendars().insert(mCalendar).execute();
        Log.d(TAG, "calendar timeZone:" + calendar.getTimeZone());
        calendarId = calendar.getId();

    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(calendarPrefKey, calendarId);
            editor.apply();
            if (calendarName.equals(activity.getString(R.string.lunar_reminder_calendar_name))) {
                activity.loadReminderCalendar();
            } else {
                activity.loadSolarTerms();
            }
        }
    }
}
