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

    public GetCalendar(BaseActivity activity) {
        super(activity);
    }

    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        activity.getCalendarId();
        CalendarListEntry calendarListEntry;
        if (activity.lunarReminderCalendarId != null) {
            calendarListEntry = client.calendarList().get(activity.lunarReminderCalendarId).execute();
            editor.putInt(activity.getString(R.string.pref_key_reminder_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
            editor.putString(activity.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
            editor.apply();
        }
        if (activity.solarTermsCalendarId != null) {
            calendarListEntry = client.calendarList().get(activity.solarTermsCalendarId).execute();
            editor.putInt(activity.getString(R.string.pref_key_solar_terms_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
            editor.apply();
        }

        new GetEventColors(activity).execute();
    }
}
