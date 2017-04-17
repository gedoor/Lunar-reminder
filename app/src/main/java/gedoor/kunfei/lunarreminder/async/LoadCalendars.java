package gedoor.kunfei.lunarreminder.async;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.TimeZone;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;

public class LoadCalendars extends CalendarAsyncTask {
    private static final String TAG = "AsyncLoadCalendars";
    private String calendarName;

    public LoadCalendars(BaseActivity activity, String calendarName) {
        super(activity);
        this.calendarName = calendarName;
    }

    @Override
    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        CalendarList feed = client.calendarList().list().setFields("items(id,summary)").execute();
        String timeZone = TimeZone.getDefault().toString();
        for (CalendarListEntry calendar : feed.getItems()) {
            Log.d(TAG, "return calendar summary:" + calendar.getSummary() + " timeZone:" + calendar.getTimeZone());
            if (calendar.getSummary().equals(calendarName)) {
                Log.d(TAG, "Lunar Birthday calendar already exist:" + calendar.getId());
                calendarID = calendar.getId();
                editor.putString(activity.getString(R.string.pref_key_calendar_id), calendarID);
                editor.apply();
            }
        }
        if (calendarID == null) {
            new InsertCalendar(activity, calendarName);
        } else {
            new GetEvents(activity).execute();
        }

    }

}
