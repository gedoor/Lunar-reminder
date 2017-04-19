package gedoor.kunfei.lunarreminder;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.util.List;

import gedoor.kunfei.lunarreminder.util.CrashHandler;

public class LunarReminderApplication extends Application {
    /**
     * 是否开启日志输出,在Debug状态下开启,
     * 在Release状态下关闭以提示程序性能
     */
    public final static boolean DEBUG = BuildConfig.DEBUG;

    public static String calendarID;
    public static List<Event> googleEvents;
    public static Event googleEvent;
    public static int eventRepeat;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        calendarID = sharedPreferences.getString(getString(R.string.pref_key_calendar_id), null);

        CrashHandler crashHandler = CrashHandler.getInstance();
        //注册crashHandler
        crashHandler.init(getApplicationContext(), DEBUG);
    }

    public String getVersionName() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0.0.0";
        }
        return packInfo.versionName;
    }



}
