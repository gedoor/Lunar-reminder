package gedoor.kunfei.lunarreminder.sync;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.Calendar;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.ui.MainActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

/**
 * Created by GKF on 2017/3/31.
 */
@SuppressLint("WrongConstant")
public class InsertEvents extends CalendarAsyncTask{
    ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
    String lunarRepeatId = String.valueOf(cc.getTimeInMillis());
    String calendarId;
    Event event;
    int repeatNum;

    public InsertEvents(BaseActivity activity, String calendarid, Event event, int repeatNum) {
        super(activity);
        this.calendarId = calendarid;
        this.event  = event;
        DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
        cc = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));
        this.repeatNum = repeatNum;
    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = new Properties(lunarRepeatId, repeatNum).getProperties();
        for (int i = 1; i <= repeatNum; i++) {
            Event event = this.event;
            event.setStart(new EventTimeUtil(cc).getEventStartDT());
            event.setEnd(new EventTimeUtil(cc).getEventEndDT());
            event.setExtendedProperties(properties);

            client.events().insert(calendarId, event).execute();
            cc.add(ChineseCalendar.CHINESE_YEAR, 1);
        }
        new GetEvents(activity).execute();
    }

}
