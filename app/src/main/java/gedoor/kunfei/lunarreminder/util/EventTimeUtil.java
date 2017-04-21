package gedoor.kunfei.lunarreminder.util;

import android.annotation.SuppressLint;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * EventTime转换
 */
@SuppressLint("WrongConstant")
public class EventTimeUtil {
    private Calendar c;

    public EventTimeUtil(Calendar c) {
        this.c = c;
        if (this.c != null) {
            this.c.set(Calendar.HOUR_OF_DAY, 0);
            this.c.set(Calendar.MINUTE, 0);
            this.c.set(Calendar.SECOND, 0);
            this.c.set(Calendar.MILLISECOND, 0);
        }
    }

    public EventDateTime getEventStartDT() {
        DateTime dateTime = new DateTime(getDate());
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        return eventDateTime;
    }

    public EventDateTime getEventEndDT() {
        c.add(Calendar.DATE, 1);
        DateTime dateTime = new DateTime(getDate());
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        c.add(Calendar.DATE, -1);
        return eventDateTime;
    }

    public String getDate(){
        return String.format(Locale.CHINA, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE));
    }

    public Date getDateTime() {
        c.get(Calendar.YEAR);
        return c.getTime();
    }

    public ChineseCalendar getCalendar(DateTime dateTime) {
        ChineseCalendar cc = new ChineseCalendar();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cc.setTime(dateFormat.parse(dateTime.toStringRfc3339()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cc;
    }

}
