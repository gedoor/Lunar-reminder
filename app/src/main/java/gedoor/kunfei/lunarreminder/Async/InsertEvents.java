package gedoor.kunfei.lunarreminder.Async;

import android.annotation.SuppressLint;

import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.UI.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;

/**
 * Created by GKF on 2017/3/31.
 */
@SuppressLint("WrongConstant")
public class InsertEvents extends CalendarAsyncTask{
    ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
    String lunarRepeatId = String.valueOf(cc.getTimeInMillis());
    String calendarid;
    String title;
    int cMonth;
    int cDate;

    public InsertEvents(MainActivity activity,String calendarid, String title, int cMonth, int cDate) {
        super(activity);
        this.calendarid = calendarid;
        this.title  = title;
        cc.set(ChineseCalendar.CHINESE_MONTH, cMonth);
        cc.set(ChineseCalendar.CHINESE_DATE, cDate);
        cc.set(Calendar.HOUR_OF_DAY, 0);
        cc.set(Calendar.MINUTE, 0);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = getproperties();
        for (int i = 1; i < 10; i++) {
            Event event = new Event();
            event.setSummary(title);
            event.setStart(new EventTimeUtil(cc).getEventStartDT());
            event.setEnd(new EventTimeUtil(cc).getEventEndDT());
            event.setExtendedProperties(properties);
            event.setDescription(title + "(农历)");

            client.events().insert(calendarid, event).execute();
            cc.add(ChineseCalendar.CHINESE_YEAR, 1);
        }

    }

    private Event.ExtendedProperties getproperties() {
        Event.ExtendedProperties properties = new Event.ExtendedProperties();
        HashMap<String, String> map = new HashMap<>();
        map.put(LunarRepeatId, lunarRepeatId);
        properties.setPrivate(map);

        return properties;
    }
}
