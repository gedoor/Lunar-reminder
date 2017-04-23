package gedoor.kunfei.lunarreminder.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by GKF on 2017/4/23.
 */

public class GEvent {
    private String summary;
    private Date start;
    private String id;
    private String lunarRepeatNum;
    private String lunarRepeatId;
    private ArrayList<LinkedHashMap<String, Object>> reminders;


    public GEvent(LinkedHashMap<String, ?> event) {
        summary = (String) event.get("summary");
        LinkedHashMap<String, Object> xx = (LinkedHashMap<String, Object>) event.get("start");
        xx = (LinkedHashMap<String, Object>) xx.get("date");
        Double dv = (Double) xx.get("value");
        long dt = dv.longValue();
        start = new Date(dt);
        id = (String) event.get("id");
        xx = (LinkedHashMap<String, Object>) event.get("extendedProperties");
        xx = (LinkedHashMap<String, Object>) xx.get("private");
        lunarRepeatNum = (String) xx.get("LunarRepeatYear");
        lunarRepeatId = (String) xx.get("LunarRepeatId");
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
