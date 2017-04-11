package gedoor.kunfei.lunarreminder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.util.List;

/**
 * Created by GKF on 2017/3/10.
 */

public class LunarReminderApplication extends Application {
    public static Context mContext;
    public static String calendarID = null;
    public static List<Event> googleEvents;
    public static Event googleEvent;
    public static int eventRepeat = 12;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        calendarID = sharedPreferences.getString(getString(R.string.pref_key_calendar_id), null);
    }

}
