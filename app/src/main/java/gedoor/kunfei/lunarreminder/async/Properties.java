package gedoor.kunfei.lunarreminder.async;

import com.google.api.services.calendar.model.Event;

import java.util.HashMap;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatYear;

/**
 * Created by GKF on 2017/4/7.
 */

class Properties {
    private String lunarRepeatId;
    private String repeatNum;

    Properties(String lunarRepeatId, int repeatNum) {
        this.lunarRepeatId = lunarRepeatId;
        this.repeatNum = String.valueOf(repeatNum);
    }

    Event.ExtendedProperties getProperties() {
        Event.ExtendedProperties properties = new Event.ExtendedProperties();
        HashMap<String, String> map = new HashMap<>();
        map.put(LunarRepeatId, lunarRepeatId);
        map.put(LunarRepeatYear, repeatNum);

        properties.setPrivate(map);

        return properties;
    }
}
