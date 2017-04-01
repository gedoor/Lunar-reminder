package gedoor.kunfei.lunarreminder.util;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatId;

/**
 * Created by GKF on 2017/3/31.
 */
@SuppressLint("WrongConstant")
public class EventTimeUtil {
    ChineseCalendar cc;

    public EventTimeUtil(ChineseCalendar cc) {
        this.cc = cc;
    }

    public EventDateTime getEventStartDT() {
        int m = cc.get(Calendar.MONTH)+1;
        DateTime dateTime = new DateTime(getDate(cc));
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        return eventDateTime;
    }

    public EventDateTime getEventEndDT() {
        cc.add(Calendar.DATE, 1);
        DateTime dateTime = new DateTime(getDate(cc));
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        cc.add(Calendar.DATE, -1);
        return eventDateTime;
    }

    public String getDate(Calendar c) {
        int m = c.get(Calendar.MONTH)+1;
        String month = m<10 ? "0" + m : String.valueOf(m);
        int d = c.get(Calendar.DATE);
        String date = d<10 ? "0" + d : String.valueOf(d);
        String dt = c.get(Calendar.YEAR) + "-" + month + "-" + date;
        return dt;
    }

}
