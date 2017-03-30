package gedoor.kunfei.lunarreminder.Async;


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;

/**
 * Created by GKF on 2017/3/29.
 */

public class GetEvents extends CalendarAsyncTask {
    private static final String TAG = "AsyncGetEvents";

    public GetEvents(MainActivity activity) {
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {
        Events events = client.events().list(calendarID).setFields("items(id,summary,start(date))").setPageToken(null).execute();
        List<Event> items = events.getItems();
        for (Event event : items) {
            HashMap<String, String> hp = new HashMap<String, String>();
            hp.put("id", event.getId());
            hp.put("summary", event.getSummary());
            DateTime start = event.getStart().getDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
            try {
                cc.setTime(dateFormat.parse(start.toStringRfc3339()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            hp.put("start", cc.getChinese(ChineseCalendar.CHINESE_MONTH) + "\n" + cc.getChinese(ChineseCalendar.CHINESE_DATE));
            activity.list.add(hp);
        }

    }

}
