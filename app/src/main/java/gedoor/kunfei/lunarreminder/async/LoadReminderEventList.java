package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.data.GEvent;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.listEvent;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;

/**
 * 载入事件列表
 */

public class LoadReminderEventList extends CalendarAsyncTask {
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();

    public LoadReminderEventList(BaseActivity activity) {
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
        for (LinkedHashMap<String, ?> event : listEvent) {
            GEvent gEvent = new GEvent(event);
            HashMap<String, String> listMap = new HashMap<>();
            listMap.put("id", String.valueOf(id));
            listMap.put("summary", gEvent.getSummary());
            listMap.put(LunarRepeatId, gEvent.getLunarRepeatId());
            Calendar c = Calendar.getInstance();
            c.setTime(gEvent.getStart());
            ChineseCalendar eventCC = new ChineseCalendar(c);
            if (!ccYear.equals(eventCC.getChinese(ChineseCalendar.CHINESE_YEAR))) {
                ccYear = eventCC.getChinese(ChineseCalendar.CHINESE_YEAR);
                HashMap<String, String> titleMap = new HashMap<>();
                titleMap.put("summary", ccYear);
                titleMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_ZODIAC_EMOJI));
                titleMap.put("id", "");
                list.add(titleMap);
            }
            listMap.put("start", eventCC.getChinese(ChineseCalendar.CHINESE_MONTH) + "\n" + eventCC.getChinese(ChineseCalendar.CHINESE_DATE));
            listMap.put("bgColor", strBgColor);
            listMap.put("fgColor", "");
            list.add(listMap);
            id++;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        activity.list.clear();
        activity.list.addAll(list);
        activity.eventListFinish();
    }
}
