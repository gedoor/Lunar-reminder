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
    private String calendarId;

    public GetCalendar(BaseActivity activity, String calendarId) {
        super(activity);
        this.calendarId = calendarId;
    }

    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        CalendarListEntry calendarListEntry = client.calendarList().get(calendarId).execute();
        editor.putInt(activity.getString(R.string.pref_key_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
        editor.putString(activity.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
        editor.apply();

        new GetEventColors(activity).execute();
    }
}
