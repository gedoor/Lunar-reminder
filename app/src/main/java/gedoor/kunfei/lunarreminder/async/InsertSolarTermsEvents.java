package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ACache;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.data.FinalFields.solarTermsF;
import static gedoor.kunfei.lunarreminder.data.FinalFields.solarTermsJ;

/**
 * Created by GKF on 2017/4/18.
 * 获取节气
 */

public class InsertSolarTermsEvents extends CalendarAsyncTask {
    private String calendarId;
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();

    public InsertSolarTermsEvents(BaseActivity activity, String calendarId) {
        super(activity);
        this.calendarId = calendarId;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        int intBgColor = sharedPreferences.getInt(activity.getString(R.string.pref_key_calendar_color), 0);
        String strBgColor = String.format("#%06X", 0xFFFFFF & intBgColor);
        String urlStr = "http://data.weather.gov.hk/gts/time/calendar/text/T2017c.txt";
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int code = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == code) {
            connection.connect();
            InputStream is = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "Big5"));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                for (int i = 0; i < 24; i++) {
                    if (line.contains(solarTermsF[i])) {
                        String st[] = line.split(" ");
                        String dt[] = st[0].split("年|月|日");
                        int year = Integer.parseInt(dt[0]);
                        int month = Integer.parseInt(dt[1]);
                        int day = Integer.parseInt(dt[2]);
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, day);
                        ChineseCalendar cc = new ChineseCalendar(c);
                        Event event = new Event();
                        event.setSummary(solarTermsJ[i]);
                        event.setStart(new EventTimeUtil(cc).getEventStartDT());
                        event.setEnd(new EventTimeUtil(cc).getEventEndDT());
                        DateTime startDT = new DateTime(new EventTimeUtil(cc).getDateTime());
                        cc.add(Calendar.DATE, 1);
                        DateTime endDT = new DateTime(new EventTimeUtil(cc).getDateTime());
                        Events events = client.events().list(calendarId).setSingleEvents(true).setOrderBy("startTime")
                                .setTimeMin(startDT).setTimeMax(endDT).execute();
                        if (events.getItems().size() == 0) {
                            client.events().insert(calendarId, event).execute();
                        }
                        String start[] = new EventTimeUtil(cc).getDate().split("-");
                        HashMap<String, String> hp = new HashMap<>();
                        hp.put("start", (start[0] + "\n" + start[1] + "-" + start[2]));
                        hp.put("summary", solarTermsJ[i]);
                        hp.put("bgColor", strBgColor);
                        hp.put("id", activity.getString(R.string.solar_terms_calendar_name));
                        list.add(hp);
                    }
                }
            }
            is.close();
            Gson gson = new Gson();
            String str = gson.toJson(list);
            ACache mCache = ACache.get(activity);
            mCache.put("jq", str);
        }
        connection.disconnect();

    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        activity.list.clear();
        activity.list.addAll(list);
        activity.eventListFinish();
    }

}
