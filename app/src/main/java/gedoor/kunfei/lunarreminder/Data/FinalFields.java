package gedoor.kunfei.lunarreminder.Data;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Created by GKF on 2017/3/19.
 */

public final class FinalFields {
    public static final String CalendarName = "Lunar Reminder";

    public static final String LunarRepeatId = "LunarRepeatId";
    public static final String LunarRepeatYear = "LunarRepeatYear";

    public static final String OPERATION = "operation";
    public static final int OPERATION_INSERT = 1;
    public static final int OPERATION_UPDATE = 2;
    public static final int OPERATION_DELETE = 3;

}
