package gedoor.kunfei.lunarreminder.data;

import com.google.api.services.calendar.model.Event;

import java.util.HashMap;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatYear;

/**
 * 标记重复事件字段
 */

public class Properties {
    private String lunarRepeatId;
    private String repeatNum;

    public Properties(String lunarRepeatId, int repeatNum) {
        this.lunarRepeatId = lunarRepeatId;
        this.repeatNum = String.valueOf(repeatNum);
    }

    public Properties(String lunarRepeatId, String repeatNum) {
        this.lunarRepeatId = lunarRepeatId;
        this.repeatNum = repeatNum;
    }

    public Event.ExtendedProperties getProperties() {
        Event.ExtendedProperties properties = new Event.ExtendedProperties();
        HashMap<String, String> map = new HashMap<>();
        map.put(LunarRepeatId, lunarRepeatId);
        map.put(LunarRepeatYear, repeatNum);

        properties.setPrivate(map);

        return properties;
    }
}
