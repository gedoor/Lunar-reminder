package gedoor.kunfei.lunarreminder.CalendarProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import java.util.TimeZone;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/23.
 */

public class LunarEvents {
    public void addEvent(String title, long dtStart, long dtEnd) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        contentValues.put(CalendarContract.Events.DTSTART, dtStart);
        contentValues.put(CalendarContract.Events.DTEND, dtEnd);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.ALL_DAY, true);
        Uri uri = CalendarContract.Events.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cr.insert(uri, contentValues);
    }

    public void updateEvent(long eventID, String title, long dtStart, long dtEnd) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.DTSTART, dtStart);
        contentValues.put(CalendarContract.Events.DTEND, dtEnd);
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.ALL_DAY, true);
        String selection = new StringBuffer()
                .append(CalendarContract.Events._ID)
                .append(" = ? and ")
                .append(CalendarContract.Events.CALENDAR_ID)
                .append(" = ? ")
                .toString();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cr.update(uri, contentValues, selection, new String[]{String.valueOf(eventID), String.valueOf(calendarID)});
    }

    public void deleteEvent(long eventID) {
        String selection = new StringBuffer()
                .append(CalendarContract.Events._ID)
                .append(" = ? and ")
                .append(CalendarContract.Events.CALENDAR_ID)
                .append(" = ? ")
                .toString();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cr.delete(uri,selection, new String[]{String.valueOf(eventID), String.valueOf(calendarID)});
    }
}
