package gedoor.kunfei.lunarreminder.async;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Calendar;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/29.
 */

public class GetEvents extends CalendarAsyncTask {
    private static final String TAG = "AsyncGetEvents";

    public GetEvents(BaseActivity activity) {
        super(activity);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        new GetCalendar(activity).execute();
        if (activity.showAllEvents) {
            Events events = client.events().list(calendarID).setSingleEvents(true).setOrderBy("startTime")
                    .execute();
            googleEvents = events.getItems();
        } else {
            ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
            cc.add(Calendar.DATE, 1);
            DateTime startDT = new DateTime(new EventTimeUtil(cc).getDateTime());
            cc.add(ChineseCalendar.CHINESE_YEAR, 1);
            cc.add(Calendar.DATE, -1);
            DateTime endDT = new DateTime(new EventTimeUtil(cc).getDateTime());
            Events events = client.events().list(calendarID).setSingleEvents(true).setOrderBy("startTime")
                    .setTimeMin(startDT).setTimeMax(endDT).execute();
            googleEvents = events.getItems();

        }
        new LoadEventList(activity).execute();
    }

}
