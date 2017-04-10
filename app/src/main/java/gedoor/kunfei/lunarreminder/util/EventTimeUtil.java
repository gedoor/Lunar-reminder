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
import java.util.Locale;

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
        String dt = String.format(Locale.CHINA, "%04d-%02d-%02d", cc.get(Calendar.YEAR), cc.get(Calendar.MONTH)+1, cc.get(Calendar.DATE));
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
