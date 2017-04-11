package gedoor.kunfei.lunarreminder.Async;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.UI.MainActivity;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/4/11.
 */

public class GetCalendar extends CalendarAsyncTask {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    public GetCalendar(MainActivity activity) {
        super(activity);
    }

    protected void doInBackground() throws IOException {
        CalendarListEntry calendarListEntry = client.calendarList().get(calendarID).execute();
        editor.putInt(mContext.getString(R.string.pref_key_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
        editor.putString(mContext.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
        editor.commit();
    }
}
