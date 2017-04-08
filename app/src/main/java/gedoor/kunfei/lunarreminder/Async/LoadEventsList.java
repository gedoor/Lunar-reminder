package gedoor.kunfei.lunarreminder.Async;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.UI.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

/**
 * Created by GKF on 2017/4/8.
 */

public class LoadEventsList extends CalendarAsyncTask {

    public LoadEventsList(MainActivity activity) {
        super(activity);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        activity.list.clear();
        int id = 0;
        String ccYear = "";
        for (Event event : googleEvents) {
            HashMap<String, String> listMap = new HashMap<String, String>();
            listMap.put("id", String.valueOf(id));
            listMap.put("summary", event.getSummary());
            Event.ExtendedProperties properties = event.getExtendedProperties();
            if (properties != null) {
                listMap.put(LunarRepeatId, properties.getPrivate().get(LunarRepeatId));
            }
            DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
            ChineseCalendar eventCC = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));
            if (!ccYear.equals(eventCC.getChinese(ChineseCalendar.CHINESE_YEAR))) {
                ccYear = eventCC.getChinese(ChineseCalendar.CHINESE_YEAR);
                HashMap<String, String> titleMap = new HashMap<String, String>();
                titleMap.put("summary", ccYear);
                titleMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_ZODIAC_EMOJI));
                titleMap.put("id", "");
                activity.list.add(titleMap);
            }
            listMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_MONTH) + "\n" + eventCC.getChinese(ChineseCalendar.CHINESE_DATE));
            activity.list.add(listMap);
            id++;
        }
    }
}
