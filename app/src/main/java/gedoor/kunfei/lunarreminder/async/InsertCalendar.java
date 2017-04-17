package gedoor.kunfei.lunarreminder.async;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.services.calendar.model.Calendar;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;

public class InsertCalendar extends CalendarAsyncTask {
    private static final String TAG = "AsyncInsertCalendar";
    private final String calendarName;

    InsertCalendar(BaseActivity activity, String calendarName) {
        super(activity);
        this.calendarName = calendarName;
    }

    @Override
    protected void doInBackground() throws IOException {
        Calendar mCalendar = new com.google.api.services.calendar.model.Calendar();
        mCalendar.setSummary(calendarName);
        Calendar calendar = client.calendars().insert(mCalendar).execute();
        Log.d(TAG, "calendar timeZone:" + calendar.getTimeZone());
        calendarID = calendar.getId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(activity.getString(R.string.pref_key_calendar_id), calendarID);
        editor.apply();

        new GetEvents(activity).execute();
    }
}
