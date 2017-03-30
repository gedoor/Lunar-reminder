package gedoor.kunfei.lunarreminder.Async;

import android.util.Log;

import com.google.api.services.calendar.model.Calendar;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.MainActivity;

public class InsertCalendar extends CalendarAsyncTask {
    private static final String TAG = "AsyncInsertCalendar";
    private final Calendar entry;

    public InsertCalendar(MainActivity calendarSample, Calendar entry) {
        super(calendarSample);
        this.entry = entry;
    }

    @Override
    protected void doInBackground() throws IOException {
        Calendar calendar = client.calendars().insert(entry).setFields("id,summary,timeZone").execute();
        Log.d(TAG, "calendar timeZone:" + calendar.getTimeZone());
        activity.setCalenderID(calendar.getId());

        activity.getGoogleEvents();
    }
}
