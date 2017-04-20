package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;

/**
 * Created by GKF on 2017/3/31.
 */
@SuppressLint("WrongConstant")
public class UpdateEvents extends CalendarAsyncTask {
    private String calendarId;
    private Event event;
    private ChineseCalendar cc;
    private int repeatNum;

    public UpdateEvents(BaseActivity activity, String calendarId, Event event, int repeatNum) {
        super(activity);
        this.calendarId = calendarId;
        this.event = event;
        DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
        cc = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));
        this.repeatNum = repeatNum;
    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = event.getExtendedProperties();
        if (properties != null) {
            String lunarRepeatId = properties.getPrivate().get(LunarRepeatId);
            Event.ExtendedProperties nProperties = new Properties(lunarRepeatId, repeatNum).getProperties();
            Events events = client.events().list(calendarId).setFields("items(id)").setPrivateExtendedProperty(Collections.singletonList(LunarRepeatId + "=" + lunarRepeatId)).execute();
            List<Event> items = events.getItems();
            int i = repeatNum > items.size() ? repeatNum : items.size();
            for (int j=1; j<=i; j++) {
                if (j <= repeatNum && j <= items.size()) {
                    Event event = items.get(j-1);
                    String eventId = event.getId();
                    event = this.event;
                    event.setStart(new EventTimeUtil(cc).getEventStartDT());
                    event.setEnd(new EventTimeUtil(cc).getEventEndDT());
                    event.setId(eventId);
                    event.setExtendedProperties(nProperties);
                    client.events().update(calendarId, eventId, event).execute();

                } else if (j > repeatNum && j <= items.size()) {
                    Event event = items.get(j - 1);
                    client.events().delete(calendarId, event.getId()).execute();
                } else {
                    Event event = new Event();
                    event.setSummary(this.event.getSummary());
                    event.setDescription(this.event.getDescription());
                    event.setExtendedProperties(this.event.getExtendedProperties());
                    event.setStart(new EventTimeUtil(cc).getEventStartDT());
                    event.setEnd(new EventTimeUtil(cc).getEventEndDT());
                    event.setExtendedProperties(nProperties);
                    client.events().insert(calendarId, event).execute();
                }
                cc.add(ChineseCalendar.CHINESE_YEAR, 1);
            }
        } else {
            client.events().update(calendarId, event.getId(), event).execute();
        }
        new GetLunarReminderEvents(activity, calendarId).execute();
    }



}
