package gedoor.kunfei.lunarreminder.Async;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import gedoor.kunfei.lunarreminder.UI.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;

/**
 * Created by GKF on 2017/3/31.
 */
@SuppressLint("WrongConstant")
public class UpdateEvents extends CalendarAsyncTask {
    String calendarId;
    Event event;
    String lunarRepeatId;
    ChineseCalendar cc;

    public UpdateEvents(MainActivity activity, String calendarId, Event event) {
        super(activity);
        this.calendarId = calendarId;
        this.event = event;
        DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
        cc = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));

    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = event.getExtendedProperties();
        if (properties != null) {
            lunarRepeatId = properties.getPrivate().get(LunarRepeatId);
            Events events = client.events().list(calendarID).setFields("items(id)").setPrivateExtendedProperty(Arrays.asList(LunarRepeatId + "=" + lunarRepeatId)).execute();
            List<Event> items = events.getItems();
            for (Event event : items) {
                String eventId = event.getId();
                event = this.event;
                event.setStart(new EventTimeUtil(cc).getEventStartDT());
                event.setEnd(new EventTimeUtil(cc).getEventEndDT());
                event.setId(eventId);

                client.events().update(calendarId, eventId, event).execute();
                cc.add(ChineseCalendar.CHINESE_YEAR, 1);
            }
        } else {
            client.events().update(calendarId, event.getId(), event).execute();
        }

        activity.getGoogleEvents();
    }
}
