package gedoor.kunfei.lunarreminder;

import android.app.Application;
import android.content.Context;

/**
 * Created by GKF on 2017/3/10.
 */

public class LunarReminderApplication extends Application {
    public static Context mContext;
    public static String calendarID = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }

}