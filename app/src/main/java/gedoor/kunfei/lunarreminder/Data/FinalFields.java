package gedoor.kunfei.lunarreminder.Data;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Created by GKF on 2017/3/19.
 */

public final class FinalFields {
    public static final String CaledarName = "Lunar Reminder";

    public static final String CalendarTypeGoogle = "Google";
    public static final String CalendarTypeLocal = "Local";

    public static final String LunarRepeatId = "LunarRepeatId";

    public static final String PREF_CALENDAR_TYPE = "calendarType";

    public static final String PREF_GOOGLE_CALENDAR_TIMEZONE = "gtimeZone";
    public static final String PREF_GOOGLE_ACCOUNT_NAME = "gAaccountName";

    public static final String PREF_LOCAL_CALENDAR_ID = "lCalendarId";
    public static final String PREF_LOCAL_CALENDAR_TIMEZONE = "ltimeZone";
    public static final String PREF_LOCAL_ACCOUNT_NAME = "lAaccountName";

    public static final String OPERATION = "operation";
    public static final int OPERATION_INSERT = 1;
    public static final int OPERATION_UPDATE = 2;
    public static final int OPERATION_DELETE = 3;

    FinalFields() {

    }
}
