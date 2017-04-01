package gedoor.kunfei.lunarreminder;

import android.app.Application;
import android.content.Context;

import com.google.api.services.calendar.model.Events;


/**
 * Created by GKF on 2017/3/10.
 */

public class LunarReminderApplication extends Application {
    public static Context mContext;
    public static String calendarID = null;
    public static Events mainEvents;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }

}
