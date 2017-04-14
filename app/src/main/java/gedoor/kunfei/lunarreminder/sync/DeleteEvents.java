package gedoor.kunfei.lunarreminder.sync;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.ui.MainActivity;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;

/**
 * Created by GKF on 2017/3/31.
 */

public class DeleteEvents extends CalendarAsyncTask {
    String calendarId;
    Event event;
    String lunarRepeatId;

    public DeleteEvents(BaseActivity activity, String calendarId, Event event) {
        super(activity);
        this.event = event;
        this.calendarId = calendarId;
    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = event.getExtendedProperties();
        if (properties != null) {
            lunarRepeatId = properties.getPrivate().get(LunarRepeatId);
            Events events = client.events().list(calendarId).setFields("items(id)").setPrivateExtendedProperty(Arrays.asList(LunarRepeatId + "=" + lunarRepeatId)).execute();
            List<Event> items = events.getItems();
            for (Event event : items) {
                client.events().delete(calendarId, event.getId()).execute();
            }
        } else {
            client.events().delete(calendarId, event.getId()).execute();
        }
        new GetEvents(activity).execute();
    }
}
