package gedoor.kunfei.lunarreminder.Async;

import android.util.Log;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.TimeZone;

import gedoor.kunfei.lunarreminder.UI.MainActivity;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.CaledarName;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;

public class LoadCalendars extends CalendarAsyncTask {
    private static final String TAG = "AsyncLoadCalendars";

    public LoadCalendars(MainActivity activity) {
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {
        CalendarList feed = client.calendarList().list().setFields("items(id,summary,timeZone)").execute();
        String timeZone = TimeZone.getDefault().toString();
        for (CalendarListEntry calendar : feed.getItems()) {
            Log.d(TAG, "return calendar summary:" + calendar.getSummary() + " timeZone:" + calendar.getTimeZone());
            if (calendar.getSummary().equals(CaledarName) && calendarID == null) {
                Log.d(TAG, "Lunar Birthday calendar already exist:" + calendar.getId());
                activity.setCalenderID(calendar.getId());
            }
            if (calendar.getSummary().equals(activity.getAccountName())) {
                if (calendar.getTimeZone() != null) timeZone = calendar.getTimeZone();
                activity.setTimeZone(timeZone);
                Log.d(TAG, "get google account timeZone:" + calendar.getTimeZone());
            }
        }
        if (calendarID == null) {
            activity.createGoogleCalender();
        } else {
            activity.getGoogleEvents();
        }

    }

}
