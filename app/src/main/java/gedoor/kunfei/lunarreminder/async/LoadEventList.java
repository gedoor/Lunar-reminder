package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

/**
 * Created by GKF on 2017/4/8.
 * 载入事件列表
 */

public class LoadEventList extends CalendarAsyncTask {
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();

    public LoadEventList(BaseActivity activity) {
        super(activity);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        int intBgColor = sharedPreferences.getInt(activity.getString(R.string.pref_key_calendar_color), 0);
        String strBgColor = String.format("#%06X", 0xFFFFFF & intBgColor);
        int id = 0;
        String ccYear = "";
        for (Event event : googleEvents) {
            HashMap<String, String> listMap = new HashMap<>();
            listMap.put("id", String.valueOf(id));
            listMap.put("summary", event.getSummary());
            Event.ExtendedProperties properties = event.getExtendedProperties();
            if (properties != null) {
                listMap.put(LunarRepeatId, properties.getPrivate().get(LunarRepeatId));
            }
            DateTime start = event.getStart().getDate() == null ? event.getStart().getDateTime() : event.getStart().getDate();
            ChineseCalendar eventCC = new ChineseCalendar(new EventTimeUtil(null).getCalendar(start));
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
