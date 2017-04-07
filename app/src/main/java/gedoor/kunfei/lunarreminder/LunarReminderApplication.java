package gedoor.kunfei.lunarreminder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.PREF_CALENDAR_TYPE;


/**
 * Created by GKF on 2017/3/10.
 */

public class LunarReminderApplication extends Application {
    public static Context mContext;
    public static String calendarID = null;
    public static String calendarType;
    public static Events googleEvents;
    public static Event googleEvent;
    public static int eventRepeat = 12;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        calendarType = sharedPreferences.getString(PREF_CALENDAR_TYPE, null);
        calendarID = sharedPreferences.getString(getString(R.string.pref_key_calendar_id), null);
    }

}
