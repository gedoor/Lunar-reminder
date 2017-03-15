package gedoor.kunfei.lunarreminder.CalendarProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gedoor.kunfei.lunarreminder.R;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/10.
 */

public class initlunar {

    public static final String[] EVENT_PROJECTION = new String[] {
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

    public Uri addCalender(String accountName, String accountType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Calendars.ACCOUNT_NAME, accountName);
        contentValues.put(Calendars.ACCOUNT_TYPE, accountType);
        contentValues.put(Calendars.NAME, "Lunar Birthday");
        contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, "Lunar Birthday");
        contentValues.put(Calendars.CALENDAR_COLOR, R.color.colorLunar);
        contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        contentValues.put(Calendars.OWNER_ACCOUNT, accountName);
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        uri = cr.insert(uri, contentValues);
        return uri;
    }

    public long getCalenderID(String accountName) {
        long calenderID = 0;
        Cursor cur = null;
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.NAME + " = ?))";
        String[] selectionArgs = new String[] {accountName, "Lunar Birthday"};
        // Submit the query and get a Cursor object back
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cur.moveToNext()) {
            calenderID = cur.getLong(INDEX_ID);
        }
        cur.close();
        return calenderID;
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
