package gedoor.kunfei.lunarreminder.async;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;

/**
 * 删除事件
 */

public class DeleteReminderEvents extends CalendarAsyncTask {
    private String calendarId;
    private Event event;

    public DeleteReminderEvents(BaseActivity activity, String calendarId, Event event) {
        super(activity);
        this.event = event;
        this.calendarId = calendarId;
    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = event.getExtendedProperties();
        if (properties != null) {
            String lunarRepeatId = properties.getPrivate().get(LunarRepeatId);
            Events events = client.events().list(calendarId).setFields("items(id)").setPrivateExtendedProperty(Collections.singletonList(LunarRepeatId + "=" + lunarRepeatId)).execute();
            List<Event> items = events.getItems();
            for (Event event : items) {
                client.events().delete(calendarId, event.getId()).execute();
            }
        } else {
            client.events().delete(calendarId, event.getId()).execute();
        }
        new GetReminderEvents(activity, calendarId).execute();
    }
}
