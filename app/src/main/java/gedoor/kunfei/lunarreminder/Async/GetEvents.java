package gedoor.kunfei.lunarreminder.Async;


import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.UI.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

/**
 * Created by GKF on 2017/3/29.
 */

public class GetEvents extends CalendarAsyncTask {
    private static final String TAG = "AsyncGetEvents";

    public GetEvents(MainActivity activity) {
        super(activity);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        if (activity.showAllEvents) {
            googleEvents = client.events().list(calendarID).setSingleEvents(true).setOrderBy("startTime")
                    .execute();
        } else {
            ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
            cc.add(Calendar.DATE, 1);
            DateTime startDT = new DateTime(new EventTimeUtil(cc).getDateTime());
            cc.add(ChineseCalendar.CHINESE_YEAR, 1);
            cc.add(Calendar.DATE, -1);
            DateTime endDT = new DateTime(new EventTimeUtil(cc).getDateTime());
            googleEvents = client.events().list(calendarID).setSingleEvents(true).setOrderBy("startTime")
                    .setTimeMin(startDT).setTimeMax(endDT).execute();
        }
        List<Event> items = googleEvents.getItems();
        activity.list.clear();
        int id = 0;
        String ccYear = "";
        for (Event event : items) {
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
                titleMap.put("start", "");
                titleMap.put("id", "");
                activity.list.add(titleMap);
            }
            listMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_MONTH) + "\n" + eventCC.getChinese(ChineseCalendar.CHINESE_DATE));
            activity.list.add(listMap);
            id++;
        }

    }

}
