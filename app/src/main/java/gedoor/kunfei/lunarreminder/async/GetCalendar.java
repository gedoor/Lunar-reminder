package gedoor.kunfei.lunarreminder.async;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/4/11.
 */

public class GetCalendar extends CalendarAsyncTask {
    private SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    private SharedPreferences.Editor editor = sharedPreferences.edit();

    public GetCalendar(BaseActivity activity) {
        super(activity);
    }

    protected void doInBackground() throws IOException {
        CalendarListEntry calendarListEntry = client.calendarList().get(calendarID).execute();
        editor.putInt(mContext.getString(R.string.pref_key_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
        editor.putString(mContext.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
        editor.commit();
    }
}
