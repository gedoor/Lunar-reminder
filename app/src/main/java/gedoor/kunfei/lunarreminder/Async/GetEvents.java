package gedoor.kunfei.lunarreminder.Async;


import android.util.Log;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.UI.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mainEvents;

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
        Events events = client.events().list(calendarID).setSingleEvents(true).setOrderBy("startTime").execute();
        mainEvents = events;
        List<Event> items = events.getItems();
        activity.list.clear();
        for (Event event : items) {
            HashMap<String, String> hp = new HashMap<String, String>();
            hp.put("id", event.getId());
            hp.put("summary", event.getSummary());
            Event.ExtendedProperties properties = event.getExtendedProperties();
            if (properties != null) {
                hp.put(LunarRepeatId, properties.getPrivate().get(LunarRepeatId));
            }
            DateTime start = event.getStart().getDate();
            if (start==null) start = event.getStart().getDateTime();
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
