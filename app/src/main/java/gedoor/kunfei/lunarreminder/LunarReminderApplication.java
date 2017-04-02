package gedoor.kunfei.lunarreminder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.util.Calendar;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.PREF_CALENDAR_TYPE;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.SetingFile;


/**
 * Created by GKF on 2017/3/10.
 */

public class LunarReminderApplication extends Application {
    public static Context mContext;
    public static String calendarID = null;
    public static String calendarType;
    public static Events googleEvents;
    public static Event googleEvent;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferences sharedPreferences = this.getSharedPreferences(SetingFile, Context.MODE_PRIVATE);
        calendarType = sharedPreferences.getString(PREF_CALENDAR_TYPE, null);
    }

}
