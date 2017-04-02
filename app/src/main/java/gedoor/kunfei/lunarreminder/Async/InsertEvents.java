package gedoor.kunfei.lunarreminder.Async;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
    Event event;

    public InsertEvents(MainActivity activity,String calendarid, Event event) {
        super(activity);
        this.calendarid = calendarid;
        this.event  = event;
        DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
        cc = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));

    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = getproperties();
        for (int i = 1; i < 10; i++) {
            Event event = this.event;
            event.setStart(new EventTimeUtil(cc).getEventStartDT());
            event.setEnd(new EventTimeUtil(cc).getEventEndDT());
            event.setExtendedProperties(getproperties());

            client.events().insert(calendarid, event).execute();
            cc.add(ChineseCalendar.CHINESE_YEAR, 1);
        }
        activity.getGoogleEvents();
    }

    private Event.ExtendedProperties getproperties() {
        Event.ExtendedProperties properties = new Event.ExtendedProperties();
        HashMap<String, String> map = new HashMap<>();
        map.put(LunarRepeatId, lunarRepeatId);
        properties.setPrivate(map);

        return properties;
    }
}
