package gedoor.kunfei.lunarreminder.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import gedoor.kunfei.lunarreminder.R;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatType;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatNum;

/**
 * 事件
 * Created by GKF on 2017/4/23.
 */

public class GEvent {
    private String summary;
    private Date start;
    private String id;
    private String lunarRepeatType;
    private String lunarRepeatNum;
    private String lunarRepeatId;
    private ArrayList<LinkedHashMap<String, Object>> reminders;

    public GEvent(Context mContext, LinkedHashMap<String, ?> event) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        summary = (String) event.get("summary");
        LinkedHashMap<String, Object> xx = (LinkedHashMap<String, Object>) event.get("start");
        xx = (LinkedHashMap<String, Object>) xx.get("date");
        Double dv = (Double) xx.get("value");
        long dt = dv.longValue();
        start = new Date(dt);
        id = (String) event.get("id");
        xx = (LinkedHashMap<String, Object>) event.get("extendedProperties");
        xx = (LinkedHashMap<String, Object>) xx.get("private");
        lunarRepeatNum = xx.get(LunarRepeatNum) == null
                ? sharedPreferences.getString(mContext.getString(R.string.pref_key_repeat_year), mContext.getString(R.string.pref_value_repeat_year))
                : (String) xx.get(LunarRepeatNum);
        lunarRepeatType = xx.get(LunarRepeatType) == null ? "year" : (String) xx.get(LunarRepeatType);
        lunarRepeatId = (String) xx.get(LunarRepeatId);
        xx = (LinkedHashMap<String, Object>) event.get("reminders");
        reminders = (ArrayList<LinkedHashMap<String, Object>>) xx.get("overrides");
    }

    public String getSummary() {
        return summary;
    }

    public Date getStart() {
        return start;
    }

    public String getId() {
        return id;
    }
    public String getLunarRepeatType() {
        return lunarRepeatType;
    }
    public String getLunarRepeatId() {
        return lunarRepeatId;
    }
    public String getLunarRepeatNum() {
        return lunarRepeatNum;
    }
    public ArrayList<LinkedHashMap<String, Object>> getReminders() {
        return reminders;
    }
}
