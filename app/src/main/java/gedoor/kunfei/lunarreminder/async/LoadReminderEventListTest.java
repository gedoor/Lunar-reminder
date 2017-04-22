package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.help.ReminderHelp;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ACache;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatYear;

/**
 * Created by GKF on 2017/4/8.
 * 载入事件列表
 */

public class LoadReminderEventListTest extends CalendarAsyncTask {
    private ArrayList<HashMap<String, Object>> list = new ArrayList<>();

    public LoadReminderEventListTest(BaseActivity activity) {
        super(activity);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        int intBgColor = sharedPreferences.getInt(activity.getString(R.string.pref_key_reminder_calendar_color), 0);
        String strBgColor = String.format("#%06X", 0xFFFFFF & intBgColor);
        int id = 0;
        String ccYear = "";
        for (Event event : googleEvents) {
            HashMap<String, Object> listMap = new HashMap<>();
            listMap.put("id", String.valueOf(id));
            listMap.put("summary", event.getSummary());
            Event.ExtendedProperties properties = event.getExtendedProperties();
            if (properties != null) {
                listMap.put(LunarRepeatId, properties.getPrivate().get(LunarRepeatId));
                listMap.put(LunarRepeatYear, properties.getPrivate().get(LunarRepeatYear));
            }
            DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
            ChineseCalendar eventCC = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));
            if (!ccYear.equals(eventCC.getChinese(ChineseCalendar.CHINESE_YEAR))) {
                ccYear = eventCC.getChinese(ChineseCalendar.CHINESE_YEAR);
                HashMap<String, Object> titleMap = new HashMap<>();
                titleMap.put("summary", ccYear);
                titleMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_ZODIAC_EMOJI));
                titleMap.put("id", "");
                list.add(titleMap);
            }
            listMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_MONTH) + "\n" + eventCC.getChinese(ChineseCalendar.CHINESE_DATE));
            listMap.put("bgColor", strBgColor);
            listMap.put("fgColor", "");
            ArrayList<HashMap<String, Object>> reminderL = new ArrayList<>();
            Event.Reminders reminders = event.getReminders();
            List<EventReminder> listReminder = reminders.getOverrides();
            if (listReminder != null) {
                for (EventReminder reminder : listReminder) {
                    HashMap<String, Object> listMap1 = new HashMap<>();
                    listMap1.put("method", reminder.getMethod());
                    listMap1.put("minutes", reminder.getMinutes());
                    reminderL.add(listMap1);
                }
            }
            listMap.put("reminder", reminderL);
            list.add(listMap);
            id++;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        ACache aCache = ACache.get(activity);
        Gson gson = new Gson();
        aCache.put("eventxxx", gson.toJson(list));
    }
}
