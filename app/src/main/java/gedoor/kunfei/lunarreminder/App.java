package gedoor.kunfei.lunarreminder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.util.CrashHandler;
import gedoor.kunfei.lunarreminder.util.SharedPreferencesUtil;

public class App extends Application {
    /**
     * 是否开启日志输出,在Debug状态下开启,
     * 在Release状态下关闭以提示程序性能
     */
    public final static boolean DEBUG = BuildConfig.DEBUG;
    public final static String qrCodeAliPay = "tsx06677nwdk3javroq4ef0";

    public static ArrayList<LinkedHashMap<String, ?>> listEvent;
    public static Event googleEvent;
    public static String eventRepeatType;
    public static int eventRepeatNum;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        //注册crashHandler
        crashHandler.init(getApplicationContext(), DEBUG);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("version", getVersionName());
        editor.apply();
    }

    public static Boolean getEvents(Context context) {
        if (listEvent != null) {
            return true;
        }
        String strEvents = SharedPreferencesUtil.getString(context, "events", null);
        if (strEvents != null) {
            Gson gson = new Gson();
            listEvent = gson.fromJson(strEvents, new TypeToken<ArrayList<LinkedHashMap<String, ?>>>() {
            }.getType());
            return true;
        } else {
            return false;
        }
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
