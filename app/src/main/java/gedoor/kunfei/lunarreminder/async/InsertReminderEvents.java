package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.Calendar;

import gedoor.kunfei.lunarreminder.data.Properties;
import gedoor.kunfei.lunarreminder.ui.activity.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

/**
 * 插入事件
 */
@SuppressLint("WrongConstant")
public class InsertReminderEvents extends CalendarAsyncTask{
    private ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
    private String lunarRepeatId = String.valueOf(cc.getTimeInMillis());
    private String calendarId;
    private Event event;
    private String repeatType;
    private int repeatNum;

    public InsertReminderEvents(BaseActivity activity, String calendarId, Event event, String repeatType, int repeatNum) {
        super(activity);
        this.calendarId = calendarId;
        this.event  = event;
        DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
        cc = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));
        this.repeatType = repeatType;
        this.repeatNum = repeatNum;
    }

    @Override
    protected void doInBackground() throws IOException {
        Event.ExtendedProperties properties = new Properties(lunarRepeatId, "year", repeatNum).getProperties();
        for (int i = 1; i <= repeatNum; i++) {
            Event event = this.event;
            event.setStart(new EventTimeUtil(cc).getEventStartDT());
            event.setEnd(new EventTimeUtil(cc).getEventEndDT());
            event.setExtendedProperties(properties);

            client.events().insert(calendarId, event).execute();

            if (repeatType.equals("month")) {
                cc.add(ChineseCalendar.CHINESE_MONTH, 1);
            } else {
                cc.add(ChineseCalendar.CHINESE_YEAR, 1);
            }

        }
        new GetReminderEvents(activity, calendarId).execute();
    }

}
