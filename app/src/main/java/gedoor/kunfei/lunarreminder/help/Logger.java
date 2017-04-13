package gedoor.kunfei.lunarreminder.help;

import android.util.Log;

import gedoor.kunfei.lunarreminder.LunarReminderApplication;

/**
 * Created by PureDark on 2016/9/24.
 */

public class Logger {

    public static void d(String tag, String message) {
        if (LunarReminderApplication.DEBUG)
            Log.d(tag, message);
    }

    public static void e(String tag, String message, Throwable e) {
        if (LunarReminderApplication.DEBUG)
            Log.e(tag, message, e);
    }
}
