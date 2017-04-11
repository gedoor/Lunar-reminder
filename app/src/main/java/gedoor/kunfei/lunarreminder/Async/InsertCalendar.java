package gedoor.kunfei.lunarreminder.Async;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.UI.MainActivity;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

public class InsertCalendar extends CalendarAsyncTask {
    private static final String TAG = "AsyncInsertCalendar";
    private final Calendar mCalendar;
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    public InsertCalendar(MainActivity calendarSample, Calendar calendar) {
        super(calendarSample);
        this.mCalendar = calendar;
    }

    @Override
    protected void doInBackground() throws IOException {
        Calendar calendar = client.calendars().insert(mCalendar).execute();
        Log.d(TAG, "calendar timeZone:" + calendar.getTimeZone());
        calendarID = calendar.getId();
        CalendarListEntry calendarListEntry = client.calendarList().get(calendarID).execute();
        editor.putString(mContext.getString(R.string.pref_key_calendar_id), calendarID);
        editor.putString(mContext.getString(R.string.pref_key_lunar_calendar_color), calendarListEntry.getBackgroundColor());
        editor.putString(mContext.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
        editor.commit();

        activity.getGoogleEvents();
    }
}
