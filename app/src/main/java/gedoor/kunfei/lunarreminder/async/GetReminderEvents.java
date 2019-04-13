package gedoor.kunfei.lunarreminder.async;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;
import gedoor.kunfei.lunarreminder.util.SharedPreferencesUtil;

import static gedoor.kunfei.lunarreminder.App.listEvent;

/**
 * 获取提醒事件
 */

public class GetReminderEvents extends CalendarAsyncTask {
    private static final String TAG = "AsyncGetEvents";
    private String calendarId;
    private List<Event> events;

    public GetReminderEvents(BaseActivity activity, String calendarId) {
        super(activity);
        this.calendarId = calendarId;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        getCalendarColor();
        if (activity.showAllEvents) {
            Events events = client.events().list(calendarId).setSingleEvents(true).setOrderBy("startTime")
                    .execute();
            this.events = events.getItems();
        } else {
            ChineseCalendar cc = new ChineseCalendar(Calendar.getInstance());
            cc.add(Calendar.DATE, 1);
            DateTime startDT = new DateTime(new EventTimeUtil(cc).getDateTime());
            cc.add(ChineseCalendar.CHINESE_YEAR, 1);
            cc.add(Calendar.DATE, -1);
            DateTime endDT = new DateTime(new EventTimeUtil(cc).getDateTime());
            Events events = client.events().list(calendarId).setSingleEvents(true).setOrderBy("startTime")
                    .setTimeMin(startDT).setTimeMax(endDT).execute();
            this.events = events.getItems();
        }
        Gson gson = new Gson();
        String strEvents = gson.toJson(events);
        SharedPreferencesUtil.saveData(activity, "events", strEvents);
        listEvent = gson.fromJson(strEvents, new TypeToken<ArrayList<LinkedHashMap<String, ?>>>() {
        }.getType());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            activity.loadReminderCalendar();
        }
    }

    private void getCalendarColor() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        CalendarListEntry calendarListEntry = client.calendarList().get(calendarId).execute();
        editor.putInt(activity.getString(R.string.pref_key_reminder_calendar_color), Color.parseColor(calendarListEntry.getBackgroundColor()));
        editor.putString(activity.getString(R.string.pref_key_timezone),calendarListEntry.getTimeZone());
        editor.apply();
    }
}
