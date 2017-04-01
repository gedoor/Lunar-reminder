package gedoor.kunfei.lunarreminder.Async;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import gedoor.kunfei.lunarreminder.UI.MainActivity;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;

/**
 * Created by GKF on 2017/3/31.
 */

public class DeleteEvents extends CalendarAsyncTask {
    String calendarId;
    String lunarRepeatid;

    public DeleteEvents(MainActivity activity, String calendarId, String lunarRepeatid) {
        super(activity);
        this.lunarRepeatid = lunarRepeatid;
        this.calendarId = calendarId;
    }

    @Override
    protected void doInBackground() throws IOException {
        Events events = client.events().list(calendarId).setPrivateExtendedProperty(Arrays.asList(LunarRepeatId + "=" + lunarRepeatid)).execute();
        List<Event> items = events.getItems();
        for (Event event : items) {
            client.events().delete(calendarId, event.getId()).execute();
        }
    }
}
