package gedoor.kunfei.lunarreminder.CalendarProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.support.v4.app.ActivityCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;


import static gedoor.kunfei.lunarreminder.Data.FinalFields.caledarName;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/10.
 */
@SuppressLint("WrongConstant")
public class initLunar {

    public static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,// 0
            Calendars.ACCOUNT_NAME,// 1
            Calendars.ACCOUNT_TYPE,//2
            Calendars.NAME,//3
            Calendars.CALENDAR_DISPLAY_NAME,// 4
            Calendars.OWNER_ACCOUNT// 5
    };

    private static final int INDEX_ID = 0;
    private static final int INDEX_ACCOUNT_NAME = 1;
    private static final int INDEX_ACCOUNT_TYPE = 2;
    private static final int INDEX_NAME = 3;
    private static final int INDEX_CALENDAR_DISPLAY_NAME = 4;
    private static final int OWNER_ACCOUNT = 5;

    private long calendarID = 0;

    public Cursor getEvents() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        int x = c.get(Calendar.YEAR)*10000+c.get(Calendar.MONTH)*100+c.get(Calendar.DAY_OF_MONTH);
        long st = c.getTimeInMillis();
        ChineseCalendar cc = new ChineseCalendar(c);
        cc.add(ChineseCalendar.CHINESE_YEAR, 1);
        int y = (cc.get(Calendar.YEAR))*10000+cc.get(Calendar.MONTH)*100+cc.get(Calendar.DAY_OF_MONTH);
        long et = cc.getTimeInMillis();
        if (x+10000 < y) {
            c.add(Calendar.YEAR,1);
            c.get(Calendar.YEAR);
            et = c.getTimeInMillis();
            cc = new ChineseCalendar(c);
            cc.add(ChineseCalendar.CHINESE_YEAR, -1);
            cc.get(Calendar.YEAR);
            st = cc.getTimeInMillis();
        }

        Uri uri = CalendarContract.Events.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();
        String selection = new StringBuffer()
                .append(CalendarContract.Events.CALENDAR_ID)
                .append(" = ? and ")
                .append(CalendarContract.Events.DTSTART)
                .append(" < ? and ")
                .append(CalendarContract.Events.DTSTART)
                .append(" >= ? ")
                .toString();
        String[] selectionArgs = new String[]{String.valueOf(calendarID), String.valueOf(et), String.valueOf(st)};
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Cursor cursor = cr.query(uri, null, selection, selectionArgs, CalendarContract.Events.DTSTART);
        return cursor;
    }

    public void addEvent(String title, String dtstart) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        contentValues.put(CalendarContract.Events.DTSTART, dtstart);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.ALL_DAY, true);
        Uri uri = CalendarContract.Events.CONTENT_URI;
    }

    public Uri addCalendar(String accountName, String accountType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Calendars.ACCOUNT_NAME, accountName);
        contentValues.put(Calendars.ACCOUNT_TYPE, accountType);
        contentValues.put(Calendars.NAME, caledarName);
        contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, caledarName);
        contentValues.put(Calendars.CALENDAR_COLOR, R.color.colorLunar);
        contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        contentValues.put(Calendars.OWNER_ACCOUNT, accountName);
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        uri = cr.insert(uri, contentValues);
        return uri;
    }

    public long getCalendarID(String accountName) {
        Cursor cur = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.NAME + " = ?))";
        String[] selectionArgs = new String[] {accountName, caledarName};
        // Submit the query and get a Cursor object back
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cur.moveToNext()) {
            calendarID = cur.getLong(INDEX_ID);
        }
        cur.close();
        return calendarID;
    }

    public Map getCalendarsAccountNames() {
        Map accounts = new HashMap();
        Cursor cur = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        // Submit the query and get a Cursor object back
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        while (cur.moveToNext()) {
            if (!accounts.keySet().contains(cur.getString(INDEX_ACCOUNT_NAME))) {
                accounts.put(cur.getString(INDEX_ACCOUNT_NAME), cur.getString(INDEX_ACCOUNT_TYPE));
            }
        }
        cur.close();
        return accounts;
    }

}
