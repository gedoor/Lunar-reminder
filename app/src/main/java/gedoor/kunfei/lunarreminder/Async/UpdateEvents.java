package gedoor.kunfei.lunarreminder.Async;

import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Arrays;

import gedoor.kunfei.lunarreminder.UI.MainActivity;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;

/**
 * Created by GKF on 2017/3/31.
 */

public class UpdateEvents extends CalendarAsyncTask {
    String calendarId;
    String lunarRepeatId;

    public UpdateEvents(MainActivity activity, String calendarId, String lunarRepeatId) {
        super(activity);
        this.calendarId = calendarId;
        this.lunarRepeatId = lunarRepeatId;
    }

    @Override
    protected void doInBackground() throws IOException {
        Events events = client.events().list(calendarID).setPrivateExtendedProperty(Arrays.asList(LunarRepeatId + "=" + lunarRepeatId)).execute();

    }
}
