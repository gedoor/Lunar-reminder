package gedoor.kunfei.lunarreminder.async;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;


/**
 * 获取日历信息
 */

public class GetCalendar extends CalendarAsyncTask {
    private String calendarName;
    private String calendarId;

    GetCalendar(BaseActivity activity, String calendarName, String calendarId) {
        super(activity);
        this.calendarName = calendarName;
        this.calendarId = calendarId;
    }
    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        activity.getCalendarId();
        CalendarListEntry calendarListEntry = client.calendarList().get(calendarId).execute();
        if (calendarName.equals(activity.getString(R.string.lunar_reminder_calendar_name))) {
            editor.putInt(activity.getString(R.string.pref_key_reminder_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
            editor.putString(activity.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
            editor.apply();
        }
        if (calendarName.equals(activity.getString(R.string.solar_terms_calendar_name))) {
            editor.putInt(activity.getString(R.string.pref_key_solar_terms_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
            editor.apply();
        }

    }

}
