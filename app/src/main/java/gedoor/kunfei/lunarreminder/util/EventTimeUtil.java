package gedoor.kunfei.lunarreminder.util;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.text.DateFormat;
import java.text.ParseException;
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
        if (cc != null) {
            cc.set(Calendar.HOUR_OF_DAY, 0);
            cc.set(Calendar.MINUTE, 0);
            cc.set(Calendar.SECOND, 0);
            cc.set(Calendar.MILLISECOND, 0);
        }
    }

    public EventDateTime getEventStartDT() {
        int m = cc.get(Calendar.MONTH)+1;
        DateTime dateTime = new DateTime(getDate());
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        return eventDateTime;
    }

    public EventDateTime getEventEndDT() {
        cc.add(Calendar.DATE, 1);
        DateTime dateTime = new DateTime(getDate());
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        cc.add(Calendar.DATE, -1);
        return eventDateTime;
    }

    public String getDate(){
        int m = cc.get(Calendar.MONTH)+1;
        String month = m<10 ? "0" + m : String.valueOf(m);
        int d = cc.get(Calendar.DATE);
        String date = d<10 ? "0" + d : String.valueOf(d);
        String dt = cc.get(Calendar.YEAR) + "-" + month + "-" + date;
        return dt;
    }

    public Date getDateTime() {
        cc.get(Calendar.YEAR);
        Date date = cc.getTime();
        return date;
    }

    public ChineseCalendar getCalendar(DateTime dateTime) {
        ChineseCalendar cc = new ChineseCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cc.setTime(dateFormat.parse(dateTime.toStringRfc3339()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cc;
    }

}
